package io.ashkanans.artwalk.domain.repository.placetypes

import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.domain.model.PlaceType
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import services.api.types.TypesService
import java.util.concurrent.TimeUnit

class PlaceTypesRepositoryUsage {

    private val placeTypesRepository: PlaceTypesRepository

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

        val placeTypesService = retrofit.create(TypesService::class.java)
        placeTypesRepository = PlaceTypesRepository(placeTypesService)
    }

    fun fetchPlaceTypes(callback: (List<PlaceType>?) -> Unit) {
        placeTypesRepository.getPlaceTypes { types ->
            if (types != null) {
                println("Places fetched successfully:")
                callback(types)
            } else {
                println("Failed to fetch places.")
                callback(null)
            }
        }
    }
}

