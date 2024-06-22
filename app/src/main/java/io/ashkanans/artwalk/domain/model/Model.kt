package io.ashkanans.artwalk.domain.model

import java.util.Date

data class Model<T>(
    val uniqueId: String,
    val name: String,
    var dataModel: List<T>,
    val lastUpdated: Date
)