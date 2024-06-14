package services.api.google.place.details

import services.api.google.place.searchText.PlaceSearchResult
import services.api.google.place.searchText.PlaceSearchService

class PlaceDetailsApplication(
    private val placeSearchService: PlaceSearchService,
    private val placeDetailsService: PlaceDetailsService
) {

    suspend fun searchPlaceId(textQuery: String): String? {
        val result = placeSearchService.search(textQuery)
        return when (result) {
            is PlaceSearchResult.Success -> result.placeId
            is PlaceSearchResult.Failure -> {
                println("Error occurred: ${result.exception.message}")
                null
            }
        }
    }

    suspend fun getPlaceDetails(placeId: String): PlaceDetails? {
        val result = placeDetailsService.getDetails(placeId)
        return when (result) {
            is PlaceDetailsResult.Success -> result.placeDetails
            is PlaceDetailsResult.Failure -> {
                println("Error occurred: ${result.exception.message}")
                null
            }
        }
    }
}