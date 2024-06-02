package services.api.authentication.signin

data class SignInResponse(
    val id: Int,
    val message: String,
    val token: String,
    val username: String
)