package services.api.google.place.searchText

import com.google.gson.annotations.SerializedName

data class PlaceResponse(
    @SerializedName("places") val places: List<Place>
)

data class Place(
    @SerializedName("id") val id: String
)