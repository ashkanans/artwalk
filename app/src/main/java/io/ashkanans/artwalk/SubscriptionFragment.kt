package io.ashkanans.artwalk

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
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
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.model.*
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import io.ashkanans.artwalk.databinding.FragmentSubscriptionBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale

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
                arrayOf(Manifest.permission.GET_ACCOUNTS),
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
                callCloudVision(bitmap)
                selectedImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                Log.e(TAG, e.message ?: "IOException occurred")
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun callCloudVision(bitmap: Bitmap) {
        mProgressDialog =
            ProgressDialog.show(context, null, "Scanning image with Vision API...", true)

        object : AsyncTask<Any, Void, BatchAnnotateImagesResponse?>() {
            override fun doInBackground(vararg params: Any): BatchAnnotateImagesResponse? {
                return try {
                    val credential = GoogleCredential().setAccessToken(accessToken)
                    val httpTransport: HttpTransport = AndroidHttp.newCompatibleTransport()
                    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

                    val vision = Vision.Builder(httpTransport, jsonFactory, credential).build()

                    val featureList = listOf(
                        Feature().apply {
                            type = "LABEL_DETECTION"
                            maxResults = 10
                        },
                        Feature().apply {
                            type = "TEXT_DETECTION"
                            maxResults = 10
                        },
                        Feature().apply {
                            type = "LANDMARK_DETECTION"
                            maxResults = 10
                        }
                    )

                    val imageList = listOf(
                        AnnotateImageRequest().apply {
                            image = getBase64EncodedJpeg(bitmap)
                            features = featureList
                        }
                    )

                    val batchAnnotateImagesRequest = BatchAnnotateImagesRequest().apply {
                        requests = imageList
                    }

                    val annotateRequest =
                        vision.images().annotate(batchAnnotateImagesRequest).apply {
                            disableGZipContent = true
                        }
                    Log.d(TAG, "Sending request to Google Cloud")
                    annotateRequest.execute()
                } catch (e: GoogleJsonResponseException) {
                    Log.e(TAG, "Request error: " + e.content)
                    null
                } catch (e: IOException) {
                    Log.d(TAG, "Request error: " + e.message)
                    null
                }
            }

            override fun onPostExecute(response: BatchAnnotateImagesResponse?) {
                mProgressDialog.dismiss()
                textResults.text = getDetectedTexts(response)
                labelResults.text = getDetectedLabels(response)
                landmarkResults.text = getDetectedLandmarks(response)
            }
        }.execute()
    }

    private fun getDetectedLabels(response: BatchAnnotateImagesResponse?): String {
        val message = StringBuilder()
        val labels = response?.responses?.get(0)?.labelAnnotations
        if (labels != null) {
            for (label in labels) {
                message.append(
                    String.format(
                        Locale.getDefault(), "%.3f: %s",
                        label.score, label.description
                    )
                ).append("\n")
            }
        } else {
            message.append("nothing\n")
        }
        return message.toString()
    }

    private fun getDetectedLandmarks(response: BatchAnnotateImagesResponse?): String {
        val message = StringBuilder()
        val landmarks = response?.responses?.get(0)?.landmarkAnnotations
        if (landmarks != null) {
            for (landmark in landmarks) {
                val locationInfo = landmark.locations.firstOrNull()
                if (locationInfo != null) {
                    message.append(
                        String.format(
                            Locale.getDefault(),
                            "%s: %s",
                            landmark.description,
                            locationInfo.latLng
                        )
                    ).append("\n")
                } else {
                    message.append(String.format(Locale.getDefault(), "%s", landmark.description))
                        .append("\n")
                }
            }
        } else {
            message.append("nothing\n")
        }
        return message.toString()
    }

    private fun getDetectedTexts(response: BatchAnnotateImagesResponse?): String {
        val message = StringBuilder()
        val texts = response?.responses?.get(0)?.textAnnotations
        if (texts != null) {
            for (text in texts) {
                message.append(
                    String.format(
                        Locale.getDefault(), "%s: %s",
                        text.locale, text.description
                    )
                ).append("\n")
            }
        } else {
            message.append("nothing\n")
        }
        return message.toString()
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

    private fun getBase64EncodedJpeg(bitmap: Bitmap): com.google.api.services.vision.v1.model.Image {
        val image = Image()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        image.encodeContent(imageBytes)
        return image
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

    fun onTokenReceived(token: String?) {
        accessToken = token
        launchImagePicker()
    }

    private fun detectLandmarks(bitmap: Bitmap) {
        val requests = mutableListOf<AnnotateImageRequest>()
        val img = Image.newBuilder().setContent(ByteString.copyFrom(encodeImageToByteArray(bitmap)))
            .build()
        val feat = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
        requests.add(request)

        ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(requests)
            val responses = response.responsesList

            for (res in responses) {
                if (res.hasError()) {
                    Log.e(TAG, "Error: ${res.error.message}")
                    return
                }

                for (annotation in res.landmarkAnnotationsList) {
                    val info = annotation.locationsList.listIterator().next()
                    Log.d(TAG, "Landmark: ${annotation.description} ${info.latLng}")
                }
            }
        }
    }

    private fun encodeImageToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}
