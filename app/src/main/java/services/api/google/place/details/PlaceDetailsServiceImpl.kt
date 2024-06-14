package services.api.google.place.details

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class PlaceDetailsServiceImpl(private val apiKey: String) : PlaceDetailsService {

    private val client = HttpClient()
    private val fields =
        "id,name,types,formattedAddress,plusCode,location,rating,googleMapsUri,websiteUri,businessStatus,userRatingCount,displayName,shortFormattedAddress,editorialSummary"

    override suspend fun getDetails(placeId: String): PlaceDetailsResult {
        val url = "https://places.googleapis.com/v1/places/$placeId"

        return try {
            val response: HttpResponse = client.get(url) {
                headers {
                    append("X-Goog-Api-Key", apiKey)
                    append("X-Goog-FieldMask", fields)
                }
            }

            val responseBody = response.bodyAsText()
            if (response.status.isSuccess()) {
                val placeDetails = Json.decodeFromString(PlaceDetails.serializer(), responseBody)
                PlaceDetailsResult.Success(placeDetails)
            } else {
                PlaceDetailsResult.Failure(Exception("Failed to retrieve place details. Status code: ${response.status}"))
            }
        } catch (e: Exception) {
            PlaceDetailsResult.Failure(e)
        }
    }
}
