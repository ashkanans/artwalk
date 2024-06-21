package services.api.places

import io.ashkanans.artwalk.domain.model.Place

data class PlacesResponse(
    val places: List<Place>
)