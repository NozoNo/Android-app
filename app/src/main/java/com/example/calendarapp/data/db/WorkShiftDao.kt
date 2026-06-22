package com.example.calendarapp.data.db

import androidx.room.*
import com.example.calendarapp.data.model.WorkShift
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkShiftDao {
    @Query("SELECT * FROM workshifts ORDER BY startDateTime ASC")
    fun getAllShifts(): Flow<List<WorkShift>>

    @Query("SELECT * FROM workshifts WHERE startDateTime >= :startMs AND startDateTime < :endMs ORDER BY startDateTime ASC")
    fun getShiftsBetween(startMs: Long, endMs: Long): Flow<List<WorkShift>>

    @Query("SELECT * FROM workshifts WHERE id = :id")
    suspend fun getShiftById(id: Int): WorkShift?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: WorkShift)

    @Update
    suspend fun updateShift(shift: WorkShift)

    @Delete
    suspend fun deleteShift(shift: WorkShift)
}
