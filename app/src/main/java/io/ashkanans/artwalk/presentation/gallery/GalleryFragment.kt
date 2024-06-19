package io.ashkanans.artwalk.presentation.gallery

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.data.network.CloudVisionManager
import io.ashkanans.artwalk.presentation.viewmodel.SharedViewModel

class GalleryFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var imageAdapter: ImageAdapter
    private val REQUEST_GALLERY_IMAGE = 100
    private val REQUEST_PERMISSIONS = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.staggeredImages)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        imageAdapter = ImageAdapter(requireActivity(), sharedViewModel)
        recyclerView.adapter = imageAdapter

        sharedViewModel.imageUris.observe(viewLifecycleOwner, Observer { images ->
            images?.let {
                imageAdapter.updateImages(it.map { uri -> uri.toString() })
                doDetection(it)
            }
        })

        // Set click listener to open image picker
        view.findViewById<View>(R.id.staggeredImages).setOnClickListener {
            if (hasReadExternalStoragePermission()) {
                launchImagePicker()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSIONS
                )
            }
        }
    }

    private fun hasReadExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun launchImagePicker() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_OPEN_DOCUMENT
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select images"),
            REQUEST_GALLERY_IMAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_PERMISSIONS -> {
                    // Permission granted for opening image picker
                    launchImagePicker()
                }
            }
        } else {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doDetection(uris: List<Uri>) {
        val cloudVisionManager =
            CloudVisionManager(sharedViewModel.getToken(this.requireContext()) ?: "")

        uris.forEach { uri ->
            try {
                val bitmap = sharedViewModel.getBitmapFromUri(uri)
                if (bitmap != null) {
                    cloudVisionManager.detectImage(
                        bitmap,
                        { labels ->
                            // Handle label detection if needed
                        },
                        { texts ->
                            // Handle text detection if needed
                        },
                        { landmark ->
                            if (landmark != "nothing") {
                                val landmarkNames = landmark.lines()
                                    .map { line -> line.substringBefore(':') }
                                    .toList()
                                landmarkNames.filterNot { it.isEmpty() }.forEach { name ->
                                    sharedViewModel.addBitmapToKey(name, bitmap)
                                }
                            }
                        },
                        { error ->
                            Log.e(TAG, "Error detecting image: $error")
                        }
                    )
                } else {
                    Log.e(TAG, "Unable to open input stream for URI: $uri")
                }
            } catch (e: SecurityException) {
                Log.e(
                    TAG,
                    "SecurityException: No persistable permission grants found for URI: $uri",
                    e
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image URI: $uri", e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GALLERY_IMAGE -> if (resultCode == Activity.RESULT_OK && data != null) {
                val clipData = data.clipData
                val selectedUris = mutableListOf<Uri>()

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        clipData.getItemAt(i).uri?.let { uri ->
                            selectedUris.add(uri)
                            try {
                                requireContext().contentResolver.takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e: SecurityException) {
                                Log.e(TAG, "Failed to persist permission for URI: $uri", e)
                            }
                        }
                    }
                } else {
                    data.data?.let { uri ->
                        selectedUris.add(uri)
                        try {
                            requireContext().contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: SecurityException) {
                            Log.e(TAG, "Failed to persist permission for URI: $uri", e)
                        }
                    }
                }

                if (selectedUris.isNotEmpty()) {
                    sharedViewModel.appendImages(this.requireContext(), selectedUris)
                }
            }
        }
    }

    // Getter for imageAdapter
    fun getImageAdapter(): ImageAdapter {
        return imageAdapter
    }
}