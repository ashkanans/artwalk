package io.ashkanans.artwalk.domain.repository.places

import io.ashkanans.artwalk.domain.model.Place
import io.ashkanans.artwalk.domain.repository.BaseRepository
import services.api.places.PlacesService

class PlacesRepository(private val placesService: PlacesService) : BaseRepository() {

    fun getPlaces(callback: (List<Place>?) -> Unit) {
        val call = placesService.getPlaces()
        handleResponse(call,
            onSuccess = { places -> callback(places) },
            onError = { error -> println(error) }
        )
    }
}
