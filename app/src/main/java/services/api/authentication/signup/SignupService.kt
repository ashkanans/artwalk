package services.api.authentication.signup

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SignupService {
    @POST("/api/artwalk/signup")
    fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>
}