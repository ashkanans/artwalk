package services.api.google.place.details

sealed class PlaceDetailsResult {
    data class Success(val placeDetails: PlaceDetails) : PlaceDetailsResult()
    data class Failure(val exception: Exception) : PlaceDetailsResult()
}
