package io.ashkanans.artwalk.domain.repository.placetypes

import io.ashkanans.artwalk.domain.model.PlaceType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import services.api.types.TypesService

class PlaceTypesRepositoryUsage {

    private val placeTypesRepository: PlaceTypesRepository

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://artwalk-1-d74f115da834.herokuapp.com") // Replace with actual base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val placeTypesService = retrofit.create(TypesService::class.java)
        placeTypesRepository = PlaceTypesRepository(placeTypesService)
    }

    fun fetchPlaceTypes(callback: (List<PlaceType>?) -> Unit) {
        placeTypesRepository.getPlaceTypes { types ->
            if (types != null) {
                println("Places fetched successfully:")
                callback(types)
            } else {
                println("Failed to fetch places.")
                callback(null)
            }
        }
    }
}

