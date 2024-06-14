package services.api.google.place.searchText

interface PlaceSearchService {
    suspend fun search(textQuery: String): PlaceSearchResult
}