package io.ashkanans.artwalk.domain.repository.predict

import io.ashkanans.artwalk.domain.model.Place
import io.ashkanans.artwalk.domain.repository.BaseRepository
import services.api.predict.PredictService

class PredictRepository(private val predictService: PredictService) : BaseRepository() {

    fun predictPath(
        startId: String,
        endId: String,
        timeSpan: Int,
        callback: (List<Place>?) -> Unit
    ) {
        val request = services.api.predict.PredictRequest(startId, endId, timeSpan)
        val call = predictService.predictPath(request)
        handleResponse(call,
            onSuccess = { places -> callback(places) },
            onError = { error -> println(error) }
        )
    }
}