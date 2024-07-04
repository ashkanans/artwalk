package io.ashkanans.artwalk.domain.repository.predict

import io.ashkanans.artwalk.domain.model.Place
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import services.api.predict.PredictService

class PredictRepositoryUsage {

    private val predictRepository: PredictRepository

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://artwalk-1-d74f115da834.herokuapp.com") // Replace with actual base URL
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