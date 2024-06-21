package io.ashkanans.artwalk.domain.repository.places

import io.ashkanans.artwalk.domain.model.Place
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import services.api.places.PlacesService

class PlacesRepositoryUsage {

    private val placesRepository: PlacesRepository

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://artwalk-1-d74f115da834.herokuapp.com") // Replace with actual base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val placesService = retrofit.create(PlacesService::class.java)
        placesRepository = PlacesRepository(placesService)
    }

    fun fetchPlaces(callback: (List<Place>?) -> Unit) {
        placesRepository.getPlaces { places ->
            if (places != null) {
                println("PlaceTypes fetched successfully:")
                callback(places)
            } else {
                println("Failed to fetch placeTypes.")
                callback(null)
            }
        }
    }
}

