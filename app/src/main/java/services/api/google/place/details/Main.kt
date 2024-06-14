package services.api.google.place.details

import services.api.google.place.searchText.PlaceSearchServiceImpl


class Main() {

    suspend fun run() {
        val apiKey = "AIzaSyCGygp0SRJldfPq7nWt7kPNtaJ168VZH7E" // Replace with your actual API key
        val placeSearchService = PlaceSearchServiceImpl(apiKey)
        val placeDetailsService = PlaceDetailsServiceImpl(apiKey)
        val app = PlaceDetailsApplication(placeSearchService, placeDetailsService)

        val textQuery = "Terraza Del pincio"
        val placeId = app.searchPlaceId(textQuery)

        if (placeId != null) {
            println("Place ID found: $placeId")
            val placeDetails = app.getPlaceDetails(placeId)
            if (placeDetails != null) {
                println("Place Details: $placeDetails")
            } else {
                println("Place details not found.")
            }
        } else {
            println("Place ID not found.")
        }
    }
}

suspend fun main() {
    val main = Main()
    main.run()
}