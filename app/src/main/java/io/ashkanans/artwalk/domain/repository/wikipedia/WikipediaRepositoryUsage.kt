package io.ashkanans.artwalk.domain.repository.wikipedia

import io.ashkanans.artwalk.domain.model.WikipediaPage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import services.api.wikipedia.WikipediaService

class WikipediaRepositoryUsage {

    private val wikipediaRepository: WikipediaRepository

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://en.wikipedia.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val wikipediaService = retrofit.create(WikipediaService::class.java)
        wikipediaRepository = WikipediaRepository(wikipediaService)
    }

    fun fetchWikipediaPage(
        title: String,
        callback: (WikipediaPage?) -> Unit
    ) {
        wikipediaRepository.fetchWikipediaPage(title,
            onSuccess = { page -> callback(page) },
            onError = { error -> println("Error: $error") }
        )
    }
}