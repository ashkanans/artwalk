package services.api.authentication.signin

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationService {
    @POST("/api/artwalk/authenticate")
    fun signIn(@Body request: SignInRequest): Call<SignInResponse>
}