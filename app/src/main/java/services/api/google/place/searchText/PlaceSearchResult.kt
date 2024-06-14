package services.api.google.place.searchText

sealed class PlaceSearchResult {
    data class Success(val placeId: String) : PlaceSearchResult()
    data class Failure(val exception: Exception) : PlaceSearchResult()
}