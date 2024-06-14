package services.api.google.place.searchText

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PlaceSearchServiceImpl(private val apiKey: String) : PlaceSearchService {

    private val client = HttpClient()

    override suspend fun search(textQuery: String): PlaceSearchResult {
        val url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json"
        val parameters = mapOf(
            "input" to textQuery,
            "inputtype" to "textquery",
            "key" to apiKey
        )

        return try {
            val response: HttpResponse = client.get(url) {
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }

            val responseBody = response.bodyAsText()
            if (response.status.isSuccess()) {
                val placeId = parsePlaceId(responseBody)
                if (placeId != null) {
                    PlaceSearchResult.Success(placeId)
                } else {
                    PlaceSearchResult.Failure(Exception("Place ID not found in response"))
                }
            } else {
                PlaceSearchResult.Failure(Exception("Failed to retrieve place ID. Status code: ${response.status}"))
            }
        } catch (e: Exception) {
            PlaceSearchResult.Failure(e)
        }
    }

    private fun parsePlaceId(responseBody: String): String? {
        return try {
            val json = Json.parseToJsonElement(responseBody)
            val candidates = json.jsonObject["candidates"]?.jsonArray
            candidates?.getOrNull(0)?.jsonObject?.get("place_id")?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }
}
