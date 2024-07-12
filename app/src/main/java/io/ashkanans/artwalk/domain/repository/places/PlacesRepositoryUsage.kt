package io.ashkanans.artwalk.domain.repository.places

import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.domain.model.Place
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import services.api.places.PlacesService
import java.util.concurrent.TimeUnit

class PlacesRepositoryUsage {

    private val placesRepository: PlacesRepository

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer ${DataModel.getUserToken()}")
            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://artwalk-1-d74f115da834.herokuapp.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val placesService = retrofit.create(PlacesService::class.java)
        placesRepository = PlacesRepository(placesService)
    }

    fun fetchPlaces(callback: (List<Place>?) -> Unit) {
        placesRepository.getPlaces { places ->
            if (places != null) {
                println("PlaceTypes fetched successfully:")
                callback(places)
            } else {
                println("Failed to fetch placeTypes.")
                callback(null)
            }
        }
    }
}
