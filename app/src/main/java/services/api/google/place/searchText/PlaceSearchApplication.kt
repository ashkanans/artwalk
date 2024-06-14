package services.api.google.place.searchText

class PlaceSearchApplication(private val placeSearchService: PlaceSearchService) {

    suspend fun run(textQuery: String): String? {
        val result = placeSearchService.search(textQuery)
        return when (result) {
            is PlaceSearchResult.Success -> result.placeId
            is PlaceSearchResult.Failure -> {
                println("Error occurred: ${result.exception.message}")
                null
            }
        }
    }
}