package services.api.google.place.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDetails(
    val id: String = "",
    val name: String = "",
    val types: List<String> = emptyList(),
    val formattedAddress: String = "",
    val plusCode: PlusCode = PlusCode("", ""),
    val location: Location = Location(0.0, 0.0),
    val rating: Double = 0.0,
    @SerialName("googleMapsUri") val googleMapsUri: String = "",
    @SerialName("websiteUri") val websiteUri: String? = null,
    val businessStatus: String = "",
    val userRatingCount: Int = 0,
    val displayName: DisplayName = DisplayName("", ""),
    val shortFormattedAddress: String = "",
    val editorialSummary: EditorialSummary = EditorialSummary("", "")
)

@Serializable
data class PlusCode(
    val globalCode: String,
    val compoundCode: String
)

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class DisplayName(
    val text: String,
    val languageCode: String
)

@Serializable
data class EditorialSummary(
    val text: String,
    val languageCode: String
)
