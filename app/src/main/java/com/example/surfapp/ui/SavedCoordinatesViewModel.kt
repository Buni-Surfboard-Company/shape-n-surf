package com.example.surfapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.surfapp.data.AppDatabase
import com.example.surfapp.data.SavedCoordinatesDataRepository
import com.example.surfapp.data.StoredData
import kotlinx.coroutines.launch

class SavedCoordinatesViewModel (
    application: Application
    ): AndroidViewModel(application)
    {
        private val repository = SavedCoordinatesDataRepository(
            AppDatabase.getInstance(application).savedCoordinatesDao()
        )

        val savedCoordinates = repository.getAllSurfSpots().asLiveData()
        val lastSavedCoordinate = repository.getLastSavedSurfSpot().asLiveData()

        fun addSavedSurfSpot(surfSpot: StoredData) {
            viewModelScope.launch {
                repository.insertSavedCoordinate(surfSpot)
            }
        }

        fun deleteSavedSurfSpot(surfSpot: String) {
            viewModelScope.launch {
                repository.deleteSavedCoordinate(surfSpot)
            }
        }

        fun getSurfSpotByCoordinate(coordinate: String){
            viewModelScope.launch {
                repository.getSpecificSurfSpot(coordinate)
            }
        }
    }