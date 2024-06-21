package io.ashkanans.artwalk.domain.repository


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseRepository {

    protected fun <T> handleResponse(
        call: Call<T>,
        onSuccess: (T?) -> Unit,
        onError: (String) -> Unit
    ) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    onSuccess(response.body())
                } else {
                    onError("Error response code: ${response.code()}. Error response message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                onError("Request failed: ${t.message}")
            }
        })
    }
}
