package io.ashkanans.artwalk.domain.model

data class WikipediaPage(
    val title: String,
    val sections: Map<String, String>,
    val nestedHeaders: List<Map<String, String>>
)