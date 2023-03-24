package com.example.surfapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCoordinatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coordinate: StoredData)

    @Query("DELETE FROM StoredData WHERE coordinate = :coordinate")
    suspend fun deleteMarkByCoordinate(coordinate: String)

    @Query("SELECT * FROM StoredData ORDER BY TimeStamp DESC")
    fun getAllMarks(): Flow<List<StoredData>>

    @Query("SELECT * FROM StoredData WHERE coordinate = :coordinate LIMIT 1")
    fun getMarkByCoordinate(coordinate: String): Flow<StoredData?>

    @Query("SELECT * FROM StoredData ORDER BY timeStamp ASC LIMIT 1")
    fun getLastSavedCoordinate(): Flow<StoredData?>
}