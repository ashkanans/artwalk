package services.api.types

import io.ashkanans.artwalk.domain.model.PlaceType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Types(private val baseUrl: String) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: TypesService = retrofit.create(TypesService::class.java)

    fun getPlaceTypes(callback: (List<PlaceType>?) -> Unit) {
        val call = service.getPlaceTypes()

        call.enqueue(object : Callback<List<PlaceType>> {
            override fun onResponse(
                call: Call<List<PlaceType>>,
                response: Response<List<PlaceType>>
            ) {
                if (response.isSuccessful) {
                    val types = response.body()
                    callback(types)
                } else {
                    println("Error response code: ${response.code()}")
                    println("Error response message: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<PlaceType>>, t: Throwable) {
                println("Request failed: ${t.message}")
                callback(null)
            }
        })
    }
}
