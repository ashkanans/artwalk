package io.ashkanans.artwalk.domain.model

data class Model<T>(
    val id: Int,
    val name: String,
    val dataModel: List<T>,
    val lastUpdated: Long
)