package services.api.types

import io.ashkanans.artwalk.domain.model.PlaceType
import retrofit2.Call
import retrofit2.http.GET

interface TypesService {
    @GET("/api/artwalk/placeTypes")
    fun getPlaceTypes(): Call<List<PlaceType>>
}