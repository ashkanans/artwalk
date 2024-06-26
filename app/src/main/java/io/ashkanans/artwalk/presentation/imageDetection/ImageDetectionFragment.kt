package io.ashkanans.artwalk.presentation.imageDetection

import android.accounts.Account
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.ashkanans.artwalk.databinding.FragmentSubscriptionBinding
import io.ashkanans.artwalk.presentation.viewmodel.SharedViewModel
import services.api.google.cloudvision.CloudVisionManager
import java.io.IOException

class ImageDetectionFragment : Fragment() {
    private lateinit var binding: FragmentSubscriptionBinding
    private val TAG = "CloudVisionExample"
    private val REQUEST_GALLERY_IMAGE = 100

    private var accessToken: String? = null
    private lateinit var selectedImage: ImageView
    private lateinit var labelResults: TextView
    private lateinit var textResults: TextView
    private lateinit var landmarkResults: TextView
    private var mAccount: Account? = null
    private var cloudVisionManager: CloudVisionManager? = null
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        cloudVisionManager =
            sharedViewModel.getToken(this.requireContext())?.let { CloudVisionManager(it) }

        // Initialize views from the inflated layout
        selectedImage = binding.selectedImage
        labelResults = binding.tvLabelResults
        textResults = binding.tvTextsResults
        landmarkResults = binding.tvLandmarksResults

        // Set click listener for the button using view binding
        binding.selectImageButton.setOnClickListener {
            launchImagePicker()
        }

        progressDialog = ProgressDialog(context).apply {
            setMessage("Processing Image...")
            setCancelable(false)
        }

        return binding.root
    }

    private fun launchImagePicker() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select an image"),
            REQUEST_GALLERY_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GALLERY_IMAGE -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                performCloudVisionRequest(data.data)
            }
        }
    }

    private fun performCloudVisionRequest(uri: Uri?) {
        uri?.let {
            try {
                val bitmap = resizeBitmap(
                    MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        uri
                    )
                )
                selectedImage.setImageBitmap(bitmap)
                cloudVisionManager?.detectImage(
                    bitmap,
                    onLabelsDetected = { labels -> labelResults.text = labels },
                    onTextsDetected = { texts -> textResults.text = texts },
                    onLandmarksDetected = { landmarks -> landmarkResults.text = landmarks },
                    onError = { error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show() }
                )
            } catch (e: IOException) {
                Log.e(TAG, e.message ?: "IOException occurred")
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val maxDimension = 1024
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension
            resizedWidth = (resizedHeight * (originalWidth.toFloat() / originalHeight)).toInt()
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension
            resizedHeight = (resizedWidth * (originalHeight.toFloat() / originalWidth)).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    }
}
