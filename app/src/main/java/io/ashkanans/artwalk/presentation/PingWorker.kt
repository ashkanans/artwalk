package io.ashkanans.artwalk.presentation

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.ashkanans.artwalk.domain.model.DataModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class PingWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://artwalk-1-d74f115da834.herokuapp.com/")
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                DataModel.setPingSuccessful(true)
                Result.success()
            } else {
                DataModel.setPingSuccessful(false)
                Result.retry()
            }
        } catch (e: Exception) {
            DataModel.setPingSuccessful(false)
            Result.retry()
        }
    }
}
