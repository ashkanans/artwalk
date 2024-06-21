package io.ashkanans.artwalk.domain.repository.placetypes

import io.ashkanans.artwalk.domain.model.PlaceType
import io.ashkanans.artwalk.domain.repository.BaseRepository
import services.api.types.TypesService

class PlaceTypesRepository(private val typesService: TypesService) : BaseRepository() {

    fun getPlaceTypes(callback: (List<PlaceType>?) -> Unit) {
        val call = typesService.getPlaceTypes()
        handleResponse(call,
            onSuccess = { types -> callback(types) },
            onError = { error -> println(error) }
        )
    }
}
