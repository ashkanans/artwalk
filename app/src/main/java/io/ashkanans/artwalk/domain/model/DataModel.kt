package io.ashkanans.artwalk.domain.model

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ashkanans.artwalk.domain.repository.places.PlacesRepositoryUsage
import io.ashkanans.artwalk.domain.repository.placetypes.PlaceTypesRepositoryUsage
import java.io.IOException
import java.util.Date
import java.util.UUID

object DataModel {
    private var placeModel: Model<Place>? = null
    private var placeTypesModel: Model<PlaceType>? = null
    private var userModel: Model<User>? = null

    private val _imageUris = MutableLiveData<List<Uri>>()
    val imageUris: LiveData<List<Uri>> = _imageUris

    val uriToBitmapMap: MutableMap<Uri, Bitmap> = mutableMapOf()

    private val _mapStringToImageUris = MutableLiveData<Map<String, List<Bitmap>>>()
    val mapStringToImageUris: LiveData<Map<String, List<Bitmap>>>
        get() = _mapStringToImageUris
    fun getPlaceModel(callback: (Model<Place>?) -> Unit) {
        if (placeModel == null) {
            val placesRepository = PlacesRepositoryUsage()
            placesRepository.fetchPlaces { places ->
                if (places != null) {
                    placeModel = Model(
                        dataModel = places,
                        name = "places",
                        lastUpdated = Date(),
                        uniqueId = UUID.randomUUID().toString()
                    )
                    callback(placeModel)
                } else {
                    println("Failed to fetch places.")
                    callback(null)
                }
            }
        } else {
            callback(placeModel)
        }
    }

    fun getPlaceTypesModel(callback: (Model<PlaceType>?) -> Unit) {
        if (placeTypesModel == null) {
            val placeTypesRepository = PlaceTypesRepositoryUsage()
            placeTypesRepository.fetchPlaceTypes { types ->
                if (types != null) {
                    placeTypesModel = Model(
                        dataModel = types,
                        name = "placeTypes",
                        lastUpdated = Date(),
                        uniqueId = UUID.randomUUID().toString()
                    )
                    callback(placeTypesModel)
                } else {
                    println("Failed to fetch place types.")
                    callback(null)
                }
            }
        } else {
            callback(placeTypesModel)
        }
    }

    fun getUserModel(): Model<User>? = userModel

    fun setPlaceModel(model: Model<Place>) {
        placeModel = model
    }

    fun setPlaceTypesModel(model: Model<PlaceType>) {
        placeTypesModel = model
    }

    fun setUserModel(user: User) {
        val model = Model(
            dataModel = listOf(user),
            name = "user",
            lastUpdated = Date(),
            uniqueId = UUID.randomUUID().toString()
        )
        userModel = model
    }

    fun appendImages(context: Activity, newImages: List<Uri>) {
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

    fun getBitmapFromUri(uri: Uri): Bitmap? {
        return uriToBitmapMap[uri]
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

    private fun addUriToBitmapMapping(uri: Uri, bitmap: Bitmap) {
        uriToBitmapMap[uri] = bitmap
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
}
