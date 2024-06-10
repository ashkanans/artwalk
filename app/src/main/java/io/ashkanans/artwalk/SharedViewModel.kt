// SharedViewModel.kt
package io.ashkanans.artwalk

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _navigateToFragment = MutableLiveData<Class<out Fragment>>()
    val navigateToFragment: LiveData<Class<out Fragment>> = _navigateToFragment

    // Use MutableLiveData to hold the list of image URIs
    private val _imageUris = MutableLiveData<List<Uri>>() // Change type to List<Uri>
    val imageUris: LiveData<List<Uri>> = _imageUris

    fun navigateTo(fragmentClass: Class<out Fragment>) {
        _navigateToFragment.value = fragmentClass
    }

    // Function to set the list of image URIs
    fun setImageUris(uris: List<Uri>) { // Change parameter type to List<Uri>
        _imageUris.value = uris
    }

    // Function to append new images to the existing list
    fun appendImages(newImages: List<Uri>) {
        val currentList = _imageUris.value ?: emptyList()
        val updatedList = currentList.toMutableList()
        updatedList.addAll(newImages)
        _imageUris.value = updatedList
    }

    // Function to remove a specific image URI from the list
    fun removeImageUri(uri: Uri) {
        val currentList = _imageUris.value ?: emptyList()
        val updatedList = currentList.toMutableList()
        updatedList.remove(uri)
        _imageUris.value = updatedList
    }
}
