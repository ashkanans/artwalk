package services.api.places

object ApiTestMain {
    private const val BASE_URL = "https://artwalk-1-d74f115da834.herokuapp.com"

    @JvmStatic
    fun main(args: Array<String>) {

        val places = Places(BASE_URL)

        places.getPlaces { places ->
            if (places != null) {
                println("API Test Successful!")

                // Print summary or details of the first place in the list
                if (places.isNotEmpty()) {
                    val firstPlace = places[0]
                    println("First Place ID: ${firstPlace.id}")
                    println("Display Name: ${firstPlace.displayName}")
                    println("Location: ${firstPlace.location}")
                    println("Rating: ${firstPlace.rating}")
                    // Add more details as needed
                }
            } else {
                println("API Test Failed!")
            }
        }
    }
}
