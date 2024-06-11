package io.ashkanans.artwalk

import CloudVisionManager
import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import io.ashkanans.artwalk.databinding.FragmentSubscriptionBinding
import java.io.IOException

class SubscriptionFragment : Fragment() {
    private lateinit var binding: FragmentSubscriptionBinding
    private val TAG = "CloudVisionExample"
    private val REQUEST_GALLERY_IMAGE = 100
    private val REQUEST_CODE_PICK_ACCOUNT = 101
    private val REQUEST_ACCOUNT_AUTHORIZATION = 102
    private val REQUEST_PERMISSIONS = 13

    private var accessToken: String? = null
    private lateinit var selectedImage: ImageView
    private lateinit var labelResults: TextView
    private lateinit var textResults: TextView
    private lateinit var landmarkResults: TextView
    private var mAccount: Account? = null
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var cloudVisionManager: CloudVisionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        binding = FragmentSubscriptionBinding.inflate(inflater, container, false)

        // Initialize views from the inflated layout
        selectedImage = binding.selectedImage
        labelResults = binding.tvLabelResults
        textResults = binding.tvTextsResults
        landmarkResults = binding.tvLandmarksResults

        // Set click listener for the button using view binding
        binding.selectImageButton.setOnClickListener {
            requestPermissions(
                arrayOf(
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSIONS
            )
        }

        // Return the root view of the binding
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAuthToken()
            } else {
                Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GALLERY_IMAGE -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                performCloudVisionRequest(data.data)
            }

            REQUEST_CODE_PICK_ACCOUNT -> if (resultCode == AppCompatActivity.RESULT_OK) {
                val email = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                val am = AccountManager.get(requireContext())
                val accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
                for (account in accounts) {
                    if (account.name == email) {
                        mAccount = account
                        break
                    }
                }
                getAuthToken()
            } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                Toast.makeText(context, "No Account Selected", Toast.LENGTH_SHORT).show()
            }

            REQUEST_ACCOUNT_AUTHORIZATION -> if (resultCode == AppCompatActivity.RESULT_OK) {
                val extra = data?.extras
                onTokenReceived(extra?.getString("authtoken"))
            } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                Toast.makeText(context, "Authorization Failed", Toast.LENGTH_SHORT).show()
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
                cloudVisionManager.detectImage(
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

    fun onTokenReceived(token: String?) {
        accessToken = token
        cloudVisionManager = CloudVisionManager(token!!)
        launchImagePicker()
    }

    private fun pickUserAccount() {
        val accountTypes = arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
        val intent = AccountPicker.newChooseAccountIntent(
            null,
            null,
            accountTypes,
            false,
            null,
            null,
            null,
            null
        )
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT)
    }

    private fun getAuthToken() {
        val SCOPE = "oauth2:https://www.googleapis.com/auth/cloud-platform"

        // Check if mAccount is available
        if (mAccount == null) {
            // If mAccount is null, prompt user to pick an account
            pickUserAccount()
        } else {
            // If mAccount is not null, execute GetOAuthToken AsyncTask
            GetOAuthToken(
                requireContext(),
                this,
                mAccount!!,
                SCOPE,
                REQUEST_ACCOUNT_AUTHORIZATION
            ).execute()
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
