package io.ashkanans.artwalk.domain.model

data class PlaceType(
    val type: String,
    val color: String,
    var isChecked: Boolean = true // Default checkbox value
)