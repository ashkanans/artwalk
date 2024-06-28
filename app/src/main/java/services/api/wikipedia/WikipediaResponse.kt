package services.api.wikipedia

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


data class WikipediaResponse(
    val query: Query
) {
    val sections: Map<String, Any?> by lazy {
        extractSections(query.pages.values.firstOrNull()?.extract.orEmpty())
    }

    private fun extractSections(extract: String): Map<String, Any?> {
        val sectionsMap = mutableMapOf<String, Any?>()

        // Parse HTML using Jsoup
        val doc: Document = Jsoup.parse(extract)

        // Extract top-level headers and content
        val topLevelHeaders = doc.select("h2").mapNotNull { element ->
            val headerText = element.select("span").firstOrNull()?.text() ?: ""
            val contentElement = element.nextElementSibling()
            val content = if (contentElement != null) contentElement.text() else ""
            Pair(headerText, content)
        }.associateBy({ it.first }, { it.second })

        // Recursively extract nested headers
        topLevelHeaders.forEach { (header, _) ->
            val nestedHeaders = extractNestedHeaders(doc, header)
            if (nestedHeaders.isNotEmpty()) {
                sectionsMap[header] = nestedHeaders
            } else {
                sectionsMap[header] = topLevelHeaders[header]
            }
        }

        return sectionsMap
    }

    private fun extractNestedHeaders(doc: Document, header: String): Map<String, String> {
        val nestedHeadersMap = mutableMapOf<String, String>()

        // Select the header element
        val headerElement = doc.select("h2:has(span:containsOwn($header))").firstOrNull()

        // Traverse siblings to collect nested headers until next h2 is encountered
        var currentElement: Element? = headerElement?.nextElementSibling()
        while (currentElement != null && !currentElement.tagName()
                .equals("h2", ignoreCase = true)
        ) {
            if (currentElement.tagName().equals("h3", ignoreCase = true)) {
                val nestedHeader = currentElement.select("span").firstOrNull()?.text() ?: ""
                val nestedContent = currentElement.nextElementSibling()?.text() ?: ""
                nestedHeadersMap[nestedHeader] = nestedContent
            }
            currentElement = currentElement.nextElementSibling()
        }

        return nestedHeadersMap
    }
}

data class Query(
    val pages: Map<String, Page>
)

data class Page(
    val pageid: Int,
    val ns: Int,
    val title: String,
    val extract: String
)
