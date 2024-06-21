package io.ashkanans.artwalk.presentation.location.configurations

data class ConfigModel(
    val title: String,
    val description: String,
    val date: String,
    val image: Int,
    val viewType: Int // Add this line
)