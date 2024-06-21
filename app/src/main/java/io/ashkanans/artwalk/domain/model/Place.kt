package io.ashkanans.artwalk.domain.model

data class Place(
    val displayName: String,
    val id: String,
    val types: String,
    val location: Location,
    val viewport: Viewport,
    val rating: String,
    val regularOpeningHours: RegularOpeningHours,
    val utcOffsetMinutes: String,
    val userRatingCount: Int,
    val currentOpeningHours: CurrentOpeningHours,
    val primaryType: String,
    val accessibilityOptions: AccessibilityOptions
) {
    data class Location(
        val latitude: Double,
        val longitude: Double
    )

    data class Viewport(
        val low: Location,
        val high: Location
    )

    data class RegularOpeningHours(
        val openNow: Boolean,
        val periods: List<Period>,
        val weekdayDescriptions: List<String>
    )

    data class Period(
        val open: OpenCloseTime,
        val close: OpenCloseTime
    )

    data class OpenCloseTime(
        val day: Int,
        val hour: Int,
        val minute: Int,
        val date: Date? = null
    )

    data class Date(
        val year: Int,
        val month: Int,
        val day: Int
    )

    data class CurrentOpeningHours(
        val openNow: Boolean,
        val periods: List<Period>,
        val weekdayDescriptions: List<String>
    )

    data class AccessibilityOptions(
        val wheelchairAccessibleEntrance: Boolean,
        val wheelchairAccessibleParking: Boolean,
        val wheelchairAccessibleSeating: Boolean
    )
}
