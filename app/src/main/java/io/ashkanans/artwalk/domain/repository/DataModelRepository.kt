package io.ashkanans.artwalk.domain.repository

import io.ashkanans.artwalk.domain.model.DataModel

interface DataModelRepository {
    suspend fun fetchDataModel(): DataModel
}