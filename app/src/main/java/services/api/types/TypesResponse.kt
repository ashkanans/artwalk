package services.api.types

import io.ashkanans.artwalk.domain.model.PlaceType

data class TypesResponse(
    val places: List<PlaceType>
)