package services.api.authentication.signup

fun main() {
    // Base URL for the sign-up API
    val baseUrl = "http://46.100.50.100:63938/"

    // Create an instance of the SignUpService
    val signUpService = SignUp(baseUrl)

    // Define the user credentials for sign-up
    val username = "testuser"
    val password = "testpassword"
    val mobile_number = "1234567890"

    // Call the signUp method of the SignUpService
    signUpService.signUp(username, password, mobile_number) { response ->
        // Check if the response is not null
        if (response != null) {
            // Print sign-up status and token if sign-up is successful
            println("Sign-up successful: ${response.message}")
        } else {
            // Print sign-up failure message if response is null
            println("Sign-up failed.")
        }
    }
}
