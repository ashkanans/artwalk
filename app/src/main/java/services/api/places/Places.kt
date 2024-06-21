package services.api.places

import io.ashkanans.artwalk.domain.model.Place
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Places(private val baseUrl: String) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
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

    private val service: PlacesService = retrofit.create(PlacesService::class.java)

    fun getPlaces(callback: (List<Place>?) -> Unit) {
        val call = service.getPlaces()

        call.enqueue(object : Callback<List<Place>> {
            override fun onResponse(
                call: Call<List<Place>>,
                response: Response<List<Place>>
            ) {
                if (response.isSuccessful) {
                    val places = response.body()
                    callback(places)
                } else {
                    println("Error response code: ${response.code()}")
                    println("Error response message: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<Place>>, t: Throwable) {
                println("Request failed: ${t.message}")
                callback(null)
            }
        })
    }
}
