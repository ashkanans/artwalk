package services.api.authentication.signup

data class SignUpRequest(
    val username: String,
    val password: String,
    val mobile_number: String
)