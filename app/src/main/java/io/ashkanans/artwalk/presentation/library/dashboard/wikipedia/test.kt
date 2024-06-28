package io.ashkanans.artwalk.presentation.library.dashboard.wikipedia

import io.ashkanans.artwalk.domain.repository.wikipedia.WikipediaRepositoryUsage

fun main() {
    val wikipediaRepositoryUsage = WikipediaRepositoryUsage()

    // Test with a valid Wikipedia page title, e.g., "Park"
    val pageTitle = "Park"

    wikipediaRepositoryUsage.fetchWikipediaPage(pageTitle) { wikipediaPage ->
        if (wikipediaPage != null) {
            println("Wikipedia Page Title: ${wikipediaPage.title}")
            println("Sections:")
            for ((sectionTitle, sectionContent) in wikipediaPage.sections) {
                println("Section Title: $sectionTitle")
                println("Section Content: $sectionContent")
                println("---------------")
            }
        } else {
            println("Failed to fetch Wikipedia page for title: $pageTitle")
        }
    }
}