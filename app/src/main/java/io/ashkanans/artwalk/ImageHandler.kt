package io.ashkanans.artwalk

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.domain.model.DataModel.doesBitmapExistForKey
import io.ashkanans.artwalk.presentation.viewmodel.SharedViewModel
import services.api.google.cloudvision.CloudVisionManager
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageHandler(
    private val activity: Activity,
    private val sharedViewModel: SharedViewModel
) {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_CODE_GALLERY = 123
    private var photoUri: Uri? = null
    private lateinit var currentPhotoPath: String
    private var cloudVisionManager: CloudVisionManager? = null

    init {
        cloudVisionManager = CloudVisionManager(DataModel.getToken())
    }
    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    photoUri = FileProvider.getUriForFile(
                        activity,
                        "io.ashkanans.artwalk.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun openGalleryAndSelectImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        activity.startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            val selectedImages = mutableListOf<Uri>()
            if (data?.clipData != null) {
                val clipData = data.clipData
                for (i in 0 until clipData!!.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    selectedImages.add(uri)
                }
            } else if (data?.data != null) {
                selectedImages.add(data.data!!)
            }
            DataModel.appendImages(activity, selectedImages)
            doDetection(selectedImages)
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photoUri?.let {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = it
                activity.sendBroadcast(mediaScanIntent)
                Toast.makeText(activity, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                DataModel.appendImages(activity, listOf(it))
                doDetection(listOf(it))
            }
        }
    }

    private fun doDetection(uris: List<Uri>) {

        uris.forEach { uri ->
            try {
                val bitmap = DataModel.getBitmapFromUri(uri)
                if (bitmap != null && !doesBitmapExistForKey(bitmap)) {
                    cloudVisionManager?.detectImage(
                        bitmap,
                        { labels ->
                            // Handle label detection if needed
                        },
                        { texts ->
                            // Handle text detection if needed
                        },
                        { landmark ->
                            if (landmark != "nothing\n") {
                                val landmarkNames = landmark.lines()
                                    .map { line -> line.substringBefore(':') }
                                    .toList()
                                landmarkNames.filterNot { it.isEmpty() }.forEach { name ->
                                    DataModel.addBitmapToKey(name, bitmap)
                                    DataModel.mapStringToImageUris.value?.let { map ->
                                        DataModel.appendMapStringToImageUris(map)
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    activity,
                                    "No landmark detected by Google Vision",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        { error ->
                            Log.e(ContentValues.TAG, "Error detecting image: $error")
                        }
                    )
                } else {
                    Log.e(ContentValues.TAG, "Unable to open input stream for URI: $uri")
                }
            } catch (e: SecurityException) {
                Log.e(
                    ContentValues.TAG,
                    "SecurityException: No persistable permission grants found for URI: $uri",
                    e
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error processing image URI: $uri", e)
            }
        }
    }
}
