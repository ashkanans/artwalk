// SharedViewModel.kt
package io.ashkanans.artwalk.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.ByteArrayOutputStream
import java.io.IOException

class SharedViewModel : ViewModel() {
    private val _navigateToFragment = MutableLiveData<Class<out Fragment>>()
    val navigateToFragment: LiveData<Class<out Fragment>> = _navigateToFragment

    private val _imageUris = MutableLiveData<List<Uri>>()
    val imageUris: LiveData<List<Uri>> = _imageUris

    private val _mapStringToImageUris = MutableLiveData<Map<String, List<Bitmap>>>()
    val mapStringToImageUris: LiveData<Map<String, List<Bitmap>>>
        get() = _mapStringToImageUris

    val uriToBitmapMap: MutableMap<Uri, Bitmap> = mutableMapOf()

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

    fun deleteMapStringToImageUris(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("map_string_to_image_uris", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun saveMapStringToImageUris(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("map_string_to_image_uris", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val map = _mapStringToImageUris.value ?: emptyMap()

        // Convert map to a storable format
        val serializedMap = map.mapValues { entry ->
            entry.value.map { bitmap ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            }.toSet()
        }

        serializedMap.forEach { (key, value) ->
            editor.putStringSet(key, value)
        }

        editor.apply()
    }

    fun loadMapStringToImageUris(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("map_string_to_image_uris", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        val deserializedMap = allEntries.mapValues { entry ->
            (entry.value as Set<String>).map { base64String ->
                val byteArray = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            }
        }

        _mapStringToImageUris.value = deserializedMap
    }

    fun saveUriToBitmapMap(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("uri_to_bitmap_map", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert map to a storable format
        val serializedMap = uriToBitmapMap.mapValues { (_, bitmap) ->
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        serializedMap.forEach { (key, value) ->
            editor.putString(key.toString(), value)
        }

        editor.apply()
    }

    fun loadUriToBitmapMap(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("uri_to_bitmap_map", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        val deserializedMap = allEntries.mapValues { (_, value) ->
            val byteArray = Base64.decode(value as String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }.mapKeys { entry ->
            Uri.parse(entry.key)
        }

        uriToBitmapMap.clear()
        uriToBitmapMap.putAll(deserializedMap)
    }

    fun clearUriToBitmapMap(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("uri_to_bitmap_map", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Clear all entries
        editor.apply()

        // After clearing SharedPreferences, also clear the local map
        clearLocalUriToBitmapMap()
    }

    fun clearLocalUriToBitmapMap() {
        uriToBitmapMap.clear()
    }

    fun removeBitmapFromAllValues(bitmap: Bitmap) {
        val currentMap = _mapStringToImageUris.value?.toMutableMap() ?: mutableMapOf()
        val newMap = mutableMapOf<String, List<Bitmap>>() // Use a regular mutable map

        val iterator = currentMap.iterator()
        while (iterator.hasNext()) {
            val (key, bitmaps) = iterator.next()
            val filteredList = bitmaps.filter { bmap -> bmap != bitmap }
                .toList() // Convert filtered list to immutable List
            if (filteredList.isNotEmpty()) {
                newMap[key] = filteredList
            }
        }

        _mapStringToImageUris.value = emptyMap()
        _mapStringToImageUris.value =
            newMap // Assign the updated map directly to _mapStringToImageUris.value
    }

    fun removeAll() {

        _imageUris.value = emptyList()
        _mapStringToImageUris.value = emptyMap()
        uriToBitmapMap.clear()
    }
}

