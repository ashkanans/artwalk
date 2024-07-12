package io.ashkanans.artwalk.domain.repository.predict

import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.domain.model.Place
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import services.api.predict.PredictService
import java.util.concurrent.TimeUnit

class PredictRepositoryUsage {

    private val predictRepository: PredictRepository

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

        val predictService = retrofit.create(PredictService::class.java)
        predictRepository = PredictRepository(predictService)
    }

    fun fetchPredictedPath(
        startId: String,
        endId: String,
        timeSpan: Int,
        callback: (List<Place>?) -> Unit
    ) {
        predictRepository.predictPath(startId, endId, timeSpan) { places ->
            if (places != null) {
                println("Predicted path fetched successfully:")
                callback(places)
            } else {
                println("Failed to fetch predicted path.")
                callback(null)
            }
        }
    }
}