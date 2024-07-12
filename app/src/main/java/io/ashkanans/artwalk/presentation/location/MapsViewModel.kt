package io.ashkanans.artwalk.presentation.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.domain.model.Place
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MapsViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _predictions = MutableLiveData<List<Place>>()
    val predictions: LiveData<List<Place>> get() = _predictions

    private var predictionJob: Job? = null

    fun getPredictionModel(origin: String, destination: String, minutes: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        predictionJob = viewModelScope.launch {
            try {
                val predictions = getPredictionModelAsync(origin, destination, minutes)
                _predictions.value = predictions
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getPredictionModelAsync(
        origin: String,
        destination: String,
        minutes: Int
    ): List<Place> =
        suspendCancellableCoroutine { continuation ->
            DataModel.getPredictionModel(origin, destination, minutes) { predictions ->
                if (predictions != null) {
                    continuation.resume(predictions.dataModel)
                } else {
                    continuation.resumeWithException(Exception("Failed to get predictions!"))
                }
            }
        }

    fun cancelPrediction() {
        predictionJob?.cancel()
        _isLoading.value = false
        _errorMessage.value = "Task cancelled"
    }
}
