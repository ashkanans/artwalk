package services.api.authentication.signup

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SignUp(private val baseUrl: String) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)  // Increase the connect timeout
        .writeTimeout(30, TimeUnit.SECONDS)    // Increase the write timeout
        .readTimeout(30, TimeUnit.SECONDS)     // Increase the read timeout
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: SignupService = retrofit.create(SignupService::class.java)

    fun signUp(
        username: String,
        password: String,
        mobile_number: String,
        callback: (SignUpResponse?) -> Unit
    ) {
        val request = SignUpRequest(username, password, mobile_number)
        val call = service.signUp(request)

        call.enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(
                call: Call<SignUpResponse>,
                response: Response<SignUpResponse>
            ) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    println("Error response code: ${response.code()}")
                    println("Error response message: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                println("Request failed: ${t.message}")
                callback(null)
            }
        })
    }
}
