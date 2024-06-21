package io.ashkanans.artwalk.domain.model

data class DataModel(
    val placeModel: Model<Place>,
    val placeTypesModel: Model<PlaceType>
)