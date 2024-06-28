package services.api.wikipedia

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaService {
    @GET("w/api.php")
    fun getWikipediaPage(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("prop") prop: String = "extracts",
        @Query("titles") titles: String
    ): Call<WikipediaResponse>

    @GET("w/api.php")
    fun getWikipediaExtract(
        @Query("action") action: String,
        @Query("format") format: String,
        @Query("prop") prop: String,
        @Query("titles") titles: String
    ): Call<WikipediaResponse>
}