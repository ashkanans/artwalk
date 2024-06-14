package services.api.google.place.searchText


class Main() {

    suspend fun run() {
        val apiKey = "AIzaSyCGygp0SRJldfPq7nWt7kPNtaJ168VZH7E" // Replace with your actual API key
        val placeSearchService = PlaceSearchServiceImpl(apiKey)
        val app = PlaceSearchApplication(placeSearchService)

        val textQuery = "piazza del popolo"
        val placeId = app.run(textQuery)

        if (placeId != null) {
            println("Place ID found: $placeId")
        } else {
            println("Place ID not found.")
        }
    }
}

suspend fun main() {
    val main = Main()
    main.run()
}