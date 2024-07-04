package services.api.predict

import io.ashkanans.artwalk.domain.model.Place

data class PredictResponse(
    val places: List<Place>
)