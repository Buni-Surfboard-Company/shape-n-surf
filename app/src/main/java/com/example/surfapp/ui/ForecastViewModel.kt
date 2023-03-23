package com.example.surfapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surfapp.api.ApiService
import com.example.surfapp.data.WaveForecastResponse
import com.example.surfapp.data.ForecastRepository
import kotlinx.coroutines.launch

class ForecastViewModel : ViewModel() {
    private val repository = ForecastRepository(ApiService.create())

    /*
     * The most recent response from the forecast API is stored in this
     * private property.  These results are exposed to the outside world in immutable form via the
     * public `forecast` property below.
     */
    private val _forecast = MutableLiveData<List<WaveForecastResponse>?>(null)

    /**
     * This value provides the most recent response from the API.
     * It is null if there are no current results (e.g. in the case of an error).
     */
    val forecast: LiveData<List<WaveForecastResponse>?> = _forecast

    /*
     * The current error for the most recent API query is stored in this private property.  This
     * error is exposed to the outside world in immutable form via the public `error` property
     * below.
     */
    private val _error = MutableLiveData<Throwable?>(null)

    /**
     * This property provides the error associated with the most recent API query, if there is
     * one.  If there was no error associated with the most recent API query, it will be null.
     */
    val error: LiveData<Throwable?> = _error

    /*
     * The current loading state is stored in this private property.  This loading state is exposed
     * to the outside world in immutable form via the public `loading` property below.
     */
    private val _loading = MutableLiveData<Boolean>(false)

    val loading: LiveData<Boolean> = _loading

    fun loadForecast(
        latitude: Float,
        longitude: Float,
        startDate: String,
        endDate: String,
        hourly: Array<String>
    ) {
        /*
         * Launch a new coroutine in which to execute the API call.  The coroutine is tied to the
         * lifecycle of this ViewModel by using `viewModelScope`.
         */
        viewModelScope.launch {
            _loading.value = true
            val result = repository.loadSurfForecast(latitude, longitude, startDate, endDate, hourly)
            _loading.value = false
            _error.value = result.exceptionOrNull()
            _forecast.value = result.getOrNull()
        }
    }
}