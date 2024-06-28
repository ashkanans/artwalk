package services.api.wikipedia

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Wikipedia(private val baseUrl: String) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: WikipediaService = retrofit.create(WikipediaService::class.java)

    fun getWikipediaExtract(title: String, callback: (Page?) -> Unit) {
        val call = service.getWikipediaExtract("query", "json", "extracts", title)
        call.enqueue(object : Callback<WikipediaResponse> {
            override fun onResponse(
                call: Call<WikipediaResponse>,
                response: Response<WikipediaResponse>
            ) {
                if (response.isSuccessful) {
                    val pages = response.body()?.query?.pages
                    if (pages != null) {
                        val page = pages.values.firstOrNull()
                        callback(page)
                    } else {
                        callback(null)
                    }
                } else {
                    println("Error response code: ${response.code()}")
                    println("Error response message: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<WikipediaResponse>, t: Throwable) {
                println("Request failed: ${t.message}")
                callback(null)
            }
        })
    }
}
