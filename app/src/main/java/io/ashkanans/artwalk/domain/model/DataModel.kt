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
import io.ashkanans.artwalk.domain.repository.predict.PredictRepositoryUsage
import io.ashkanans.artwalk.domain.repository.wikipedia.WikipediaRepositoryUsage
import java.io.IOException
import java.util.Date
import java.util.UUID

object DataModel {
    private var token: String = ""

    private var WikipediaModel: Model<WikipediaPage>? = null
    private var placeModel: Model<Place>? = null
    private var predictionModel: Model<Place>? = null
    private var placeTypesModel: Model<PlaceType>? = null
    private var userModel: Model<User>? = null

    private val _imageUris = MutableLiveData<List<Uri>>()
    val imageUris: LiveData<List<Uri>> = _imageUris

    val uriToBitmapMap: MutableMap<Uri, Bitmap> = mutableMapOf()

    private val _mapStringToImageUris = MutableLiveData<Map<String, List<Bitmap>>>()
    val mapStringToImageUris: LiveData<Map<String, List<Bitmap>>>
        get() = _mapStringToImageUris


    fun getPredictionModel(
        startId: String,
        endId: String,
        timeSpan: Int,
        callback: (Model<Place>?) -> Unit
    ) {
        if (true) {
            val predictRepositoryUsage = PredictRepositoryUsage()
            predictRepositoryUsage.fetchPredictedPath(startId, endId, timeSpan) { places ->
                if (places != null) {
                    predictionModel = Model(
                        dataModel = places,
                        name = "Predicted Places",
                        lastUpdated = Date(),
                        uniqueId = UUID.randomUUID().toString()
                    )
                    callback(predictionModel)
                } else {
                    println("Failed to fetch predicted places.")
                    callback(null)
                }
            }
        } else {
            callback(placeModel)
        }
    }

    fun getWikipediaModel(title: String, callback: (Model<WikipediaPage>?) -> Unit) {
        if (WikipediaModel == null) {
            val wikipediaRepository = WikipediaRepositoryUsage()
            wikipediaRepository.fetchWikipediaPage(title) { wikipediaPage ->
                if (wikipediaPage != null) {
                    WikipediaModel = Model(
                        dataModel = listOf(wikipediaPage),
                        name = wikipediaPage.title,
                        lastUpdated = Date(),
                        uniqueId = UUID.randomUUID().toString()
                    )
                    callback(WikipediaModel)
                } else {
                    println("Failed to fetch Wikipedia page for title: $title")
                    callback(null)
                }
            }
        } else {
            callback(WikipediaModel)
        }
    }
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


    // Method to append another map to _mapStringToImageUris
    fun appendMapStringToImageUris(otherMap: Map<String, List<Bitmap>>) {
        val currentMap = _mapStringToImageUris.value ?: emptyMap()
        val combinedMap = mutableMapOf<String, List<Bitmap>>()

        // Add all entries from currentMap
        combinedMap.putAll(currentMap)

        // Add or replace entries from otherMap
        otherMap.forEach { (key, value) ->
            combinedMap[key] = value
        }

        _mapStringToImageUris.value = combinedMap
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

    fun doesBitmapExistForKey(bitmap: Bitmap): Boolean {
        val currentMap = _mapStringToImageUris.value ?: return false
        for (entry in currentMap) {
            val currentList = entry.value
            if (currentList.contains(bitmap)) {
                return true
            }
        }
        return false
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

    fun removeImageUri(uri: Uri) {
        val currentList = _imageUris.value ?: emptyList()
        val updatedList = currentList.toMutableList()
        updatedList.remove(uri)
        _imageUris.value = updatedList
    }

    fun setToken(givenToken: String) {
        token = givenToken
    }

    fun getToken(): String {
        return token
    }
}
