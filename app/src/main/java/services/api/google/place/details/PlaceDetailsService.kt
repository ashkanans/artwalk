package services.api.google.place.details

interface PlaceDetailsService {
    suspend fun getDetails(placeId: String): PlaceDetailsResult
}
