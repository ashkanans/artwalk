import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import com.google.api.services.vision.v1.model.Feature
import com.google.api.services.vision.v1.model.Image
import java.io.ByteArrayOutputStream
import java.io.IOException

class CloudVisionManager(private val context: Context, private val accessToken: String) {
    private var textResults: String = ""
    private var labelResults: String = ""
    private var landmarkResults: String = ""

    fun detectImage(imageUri: android.net.Uri) {
        object : AsyncTask<Void, Void, Bitmap?>() {
            override fun doInBackground(vararg params: Void): Bitmap? {
                return getURIAsBitmap(imageUri)
            }

            override fun onPostExecute(bitmap: Bitmap?) {
                bitmap?.let { resizedBitmap ->
                    processImage(resizedBitmap)
                } ?: run {
                    Log.e(TAG, "Error: Failed to load image")
                }
            }
        }.execute()
    }

    private fun processImage(bitmap: Bitmap) {
        object : AsyncTask<Bitmap, Void, BatchAnnotateImagesResponse?>() {
            override fun doInBackground(vararg bitmaps: Bitmap): BatchAnnotateImagesResponse? {
                val resizedBitmap = bitmaps[0]
                return try {
                    val credential = GoogleCredential().setAccessToken(accessToken)
                    val httpTransport = AndroidHttp.newCompatibleTransport()
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
                            image = getBase64EncodedJpeg(resizedBitmap)
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
                } catch (e: IOException) {
                    Log.e(TAG, "Error executing Cloud Vision request: ${e.message}")
                    null
                }
            }

            override fun onPostExecute(response: BatchAnnotateImagesResponse?) {
                if (response != null) {
                    textResults = getDetectedTexts(response)
                    labelResults = getDetectedLabels(response)
                    landmarkResults = getDetectedLandmarks(response)
                } else {
                    Log.e(TAG, "Error: Response is null")
                }
            }
        }.execute(bitmap)
    }

    private fun getURIAsBitmap(uri: android.net.Uri): Bitmap? {
        return try {
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image from URI: ${e.message}")
            null
        }
    }


    private fun getBase64EncodedJpeg(bitmap: Bitmap): com.google.api.services.vision.v1.model.Image {
        val image = Image()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        image.encodeContent(imageBytes)
        return image
    }

    private fun getDetectedLabels(response: BatchAnnotateImagesResponse): String {
        return labelResults
    }

    private fun getDetectedLandmarks(response: BatchAnnotateImagesResponse): String {
        return landmarkResults
    }

    private fun getDetectedTexts(response: BatchAnnotateImagesResponse): String {
        return textResults
    }

    companion object {
        private const val TAG = "CloudVisionManager"
    }
}
