package services.api.wikipedia

object ApiTestWikipedia {
    @JvmStatic
    fun main(args: Array<String>) {
        val wikipedia = Wikipedia("https://en.wikipedia.org/")

        wikipedia.getWikipediaExtract("Colosseum") { page ->
            if (page != null) {
                println("Title: ${page.title}")
                println("Extract: ${page.extract}")
            } else {
                println("Failed to fetch data.")
            }
        }
    }
}
