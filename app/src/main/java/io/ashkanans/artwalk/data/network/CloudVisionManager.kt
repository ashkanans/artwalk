package io.ashkanans.artwalk.data.network

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import com.google.api.services.vision.v1.model.Feature
import com.google.api.services.vision.v1.model.LatLng
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale

class CloudVisionManager(private val accessToken: String) {
    private val TAG = "io.ashkanans.artwalk.data.network.CloudVisionManager"
    private var landmark: String = ""
    private var location: LatLng? = null

    fun detectImage(
        bitmap: Bitmap,
        onLabelsDetected: (String) -> Unit,
        onTextsDetected: (String) -> Unit,
        onLandmarksDetected: (String) -> Unit,
        onError: (String) -> Unit
    ) {
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
                response?.let {
                    onLabelsDetected(getDetectedLabels(it))
                    onTextsDetected(getDetectedTexts(it))
                    onLandmarksDetected(getDetectedLandmarks(it))
                } ?: onError("Error in detecting image features")
            }
        }.execute()
    }

    private fun getBase64EncodedJpeg(bitmap: Bitmap): com.google.api.services.vision.v1.model.Image {
        val image = com.google.api.services.vision.v1.model.Image()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        image.encodeContent(imageBytes)
        return image
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
                    setLandmark(landmark.description)
                    setLocation(locationInfo.latLng)
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

    fun getLandmark(): String {
        return landmark
    }

    fun setLandmark(landmark: String) {
        this.landmark = landmark
    }

    fun getLocation(): LatLng? {
        return this.location
    }

    fun setLocation(location: LatLng) {
        this.location = location
    }
}
