package services.api.places

import io.ashkanans.artwalk.domain.model.Place
import retrofit2.Call
import retrofit2.http.GET

interface PlacesService {
    @GET("/api/artwalk/places")
    fun getPlaces(): Call<List<Place>>
}