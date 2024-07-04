package services.api.predict

object ApiTestMain {
    private const val BASE_URL = "https://artwalk-1-d74f115da834.herokuapp.com"

    @JvmStatic
    fun main(args: Array<String>) {
        val predict = Predict(BASE_URL)

        val startId = "ChIJ1UCDJ1NgLxMRtrsCzOHxdvY"
        val endId = "ChIJKcGbg2NgLxMRthZkUqDs4M8"
        val timeSpan = 120 * 60 // 2 hours in seconds

        predict.predictPath(startId, endId, timeSpan) { places ->
            if (places != null) {
                println("Predict API Test Successful!")

//                // Print summary of the recommended path
//                if (places.isNotEmpty()) {
//                    val firstPlace = places[0]
//                    println("First Place ID: ${firstPlace.id}")
//                    println("Display Name: ${firstPlace.displayName}")
//                    println("Location: ${firstPlace.location}")
//                    println("Rating: ${firstPlace.rating}")
//                    // Add more details as needed
//                }

                // Print all display names
                val displayNames = places.map { it.displayName }
                println("Display Names: ${displayNames.joinToString()}")
            } else {
                println("Predict API Test Failed!")
            }
        }
    }
}