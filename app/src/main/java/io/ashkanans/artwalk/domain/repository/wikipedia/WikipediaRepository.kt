package io.ashkanans.artwalk.domain.repository.wikipedia

import io.ashkanans.artwalk.domain.model.WikipediaPage
import io.ashkanans.artwalk.domain.repository.BaseRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import services.api.wikipedia.WikipediaResponse
import services.api.wikipedia.WikipediaService

class WikipediaRepository(private val wikipediaService: WikipediaService) : BaseRepository() {

    fun fetchWikipediaPage(
        title: String,
        onSuccess: (WikipediaPage?) -> Unit,
        onError: (String) -> Unit
    ) {
        val call = wikipediaService.getWikipediaPage(titles = title)
        handleResponse(call,
            onSuccess = { response ->
                val wikipediaPage = response?.let { mapToWikipediaPage(it) }
                onSuccess(wikipediaPage)
            },
            onError = { error -> onError(error) }
        )
    }

    private fun mapToWikipediaPage(response: WikipediaResponse): WikipediaPage {
        val page = response.query.pages.values.firstOrNull()
        val title = page?.title.orEmpty()
        val sections = extractSections(page?.extract.orEmpty())
        // You can also implement extractNestedHeaders if needed

        return WikipediaPage(title, sections, emptyList())
    }

    private fun extractSections(extract: String): Map<String, String> {
        val doc: Document = Jsoup.parse(extract)
        val sections = mutableMapOf<String, String>()
        var currentSection = ""

        doc.select("h2, h3, p").forEach { element ->
            when {
                element.tagName() == "h2" -> {
                    currentSection = element.select("span").attr("id")
                }

                element.tagName() == "h3" -> {
                    val header = element.select("span").attr("id")
                    val content = extractContent(element)
                    sections["$currentSection:$header"] = content
                }

                else -> {
                    if (currentSection.isNotBlank()) {
                        sections.compute(currentSection) { _, existing ->
                            existing?.plus("\n${element.text()}") ?: element.text()
                        }
                    }
                }
            }
        }

        return sections
    }

    private fun extractContent(element: Element): String {
        val content = StringBuilder()
        var nextElement = element.nextElementSibling()
        while (nextElement != null && nextElement.tagName() != "h2" && nextElement.tagName() != "h3") {
            content.append(nextElement.text()).append("\n")
            nextElement = nextElement.nextElementSibling()
        }
        return content.toString().trim()
    }
}