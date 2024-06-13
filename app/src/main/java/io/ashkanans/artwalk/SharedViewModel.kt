// SharedViewModel.kt
package io.ashkanans.artwalk

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException

class SharedViewModel : ViewModel() {
    private val _navigateToFragment = MutableLiveData<Class<out Fragment>>()
    val navigateToFragment: LiveData<Class<out Fragment>> = _navigateToFragment

    // Use MutableLiveData to hold the list of image URIs
    private val _imageUris = MutableLiveData<List<Uri>>() // Change type to List<Uri>
    val imageUris: LiveData<List<Uri>> = _imageUris

    private val _mapStringToImageUris = MutableLiveData<Map<String, List<Bitmap>>>()
    val mapStringToImageUris: LiveData<Map<String, List<Bitmap>>> = _mapStringToImageUris

    // Define the map to store URI to Bitmap mapping
    val uriToBitmapMap: MutableMap<Uri, Bitmap> = mutableMapOf()

    // Function to add a mapping from URI to Bitmap
    fun addUriToBitmapMapping(uri: Uri, bitmap: Bitmap) {
        uriToBitmapMap[uri] = bitmap
    }

    // Function to get a Bitmap from a URI
    fun getBitmapFromUri(uri: Uri): Bitmap? {
        return uriToBitmapMap[uri]
    }

    init {
        _mapStringToImageUris.value = emptyMap()
    }

    fun navigateTo(fragmentClass: Class<out Fragment>) {
        _navigateToFragment.value = fragmentClass
    }

    // Function to set the list of image URIs
    fun setImageUris(uris: List<Uri>) { // Change parameter type to List<Uri>
        _imageUris.value = uris
    }

    // Function to append new images to the existing list
    fun appendImages(context: Context, newImages: List<Uri>) {
        val currentList = _imageUris.value ?: emptyList()
        val updatedList = currentList.toMutableList()
        updatedList.addAll(newImages)

        // Loop through the new URIs and add the URI to Bitmap mapping
        newImages.forEach { uri ->
            val bitmap = getBitmapFromUri(context, uri)
            if (bitmap != null) {
                addUriToBitmapMapping(uri, bitmap)
            }
        }
        _imageUris.value = updatedList
    }

    // Function to remove a specific image URI from the list
    fun removeImageUri(uri: Uri) {
        val currentList = _imageUris.value ?: emptyList()
        val updatedList = currentList.toMutableList()
        updatedList.remove(uri)
        _imageUris.value = updatedList
    }

    fun saveImageUris(context: Context) {
        val sharedPreferences = context.getSharedPreferences("image_uris", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val uriStrings = _imageUris.value?.map { it.toString() }?.toSet() ?: emptySet()
        editor.putStringSet("uris", uriStrings)
        editor.apply()
    }

    fun loadImageUris(context: Context) {
        val sharedPreferences = context.getSharedPreferences("image_uris", Context.MODE_PRIVATE)
        val uriStrings = sharedPreferences.getStringSet("uris", emptySet()) ?: emptySet()
        val uris = uriStrings.map { Uri.parse(it) }
        _imageUris.value = uris
    }

    fun saveAccessToken(context: Context, token: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val editor = sharedPreferences.edit()
        editor.putString("access_token", token)
        editor.apply()
    }

    fun getAccessToken(context: Context): String? {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString("access_token", null)
    }

    fun saveToken(context: Context, token: String) {
        saveAccessToken(context, token)
    }

    fun getToken(context: Context): String? {
        return getAccessToken(context)
    }

    fun addBitmapToKey(key: String, bitmap: Bitmap) {
        val currentMap = _mapStringToImageUris.value?.toMutableMap() ?: mutableMapOf()
        val currentList = currentMap[key]?.toMutableList() ?: mutableListOf()

        // Check if bitmap already exists in the list
        if (!currentList.contains(bitmap)) {
            currentList.add(bitmap)
            currentMap[key] = currentList
            _mapStringToImageUris.value = currentMap
        }
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                uri
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

