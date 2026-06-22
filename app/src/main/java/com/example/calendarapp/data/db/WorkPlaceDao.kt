package com.example.calendarapp.data.db

import androidx.room.*
import com.example.calendarapp.data.model.WorkPlace
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkPlaceDao {
    @Query("SELECT * FROM workplaces ORDER BY name ASC")
    fun getAllWorkPlaces(): Flow<List<WorkPlace>>

    @Query("SELECT * FROM workplaces WHERE id = :id")
    suspend fun getWorkPlaceById(id: Int): WorkPlace?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkPlace(workPlace: WorkPlace)

    @Update
    suspend fun updateWorkPlace(workPlace: WorkPlace)

    @Delete
    suspend fun deleteWorkPlace(workPlace: WorkPlace)
}
