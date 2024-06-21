package services.api.types

object ApiTestPlaceTypes {
    private const val BASE_URL = "https://artwalk-1-d74f115da834.herokuapp.com"

    @JvmStatic
    fun main(args: Array<String>) {
        val types = Types(BASE_URL)

        types.getPlaceTypes { types ->
            if (types != null) {
                println("API Test Successful!")

                // Print all place types
                types.forEach { type ->
                    println("Type: $type")
                }
            } else {
                println("API Test Failed!")
            }
        }
    }
}
