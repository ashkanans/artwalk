package io.ashkanans.artwalk.domain.model

import io.ashkanans.artwalk.domain.repository.places.PlacesRepositoryUsage
import io.ashkanans.artwalk.domain.repository.placetypes.PlaceTypesRepositoryUsage
import java.util.Date
import java.util.UUID

object DataModel {
    private var placeModel: Model<Place>? = null
    private var placeTypesModel: Model<PlaceType>? = null
    private var userModel: Model<User>? = null

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
}
