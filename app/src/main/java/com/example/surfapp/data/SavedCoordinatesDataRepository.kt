package com.example.surfapp.data

class SavedCoordinatesDataRepository (
    private val dao: SavedCoordinatesDao
    ) {
    suspend fun insertSavedCoordinate (coordinateToSave : StoredData) = dao.insert(coordinateToSave)
    suspend fun deleteSavedCoordinate (savedCoordinate : String) = dao.deleteMarkByCoordinate(savedCoordinate)
    fun getAllSurfSpots() = dao.getAllMarks()
    fun getLastSavedSurfSpot() = dao.getLastSavedCoordinate()
    fun getSpecificSurfSpot(savedCoordinate : String) = dao.getMarkByCoordinate(savedCoordinate)
}