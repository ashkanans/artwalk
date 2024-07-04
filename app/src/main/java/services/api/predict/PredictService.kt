package services.api.predict

import io.ashkanans.artwalk.domain.model.Place
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PredictService {
    @POST("/api/artwalk/predict")
    fun predictPath(@Body request: PredictRequest): Call<List<Place>>
}