package services.api.authentication.signin

data class SignInRequest(
    val username: String,
    val password: String
)