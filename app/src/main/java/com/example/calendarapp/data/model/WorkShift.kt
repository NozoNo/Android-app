package com.example.calendarapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workshifts",
    foreignKeys = [
        ForeignKey(
            entity = WorkPlace::class,
            parentColumns = ["id"],
            childColumns = ["workPlaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkShift(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workPlaceId: Int,
    val startDateTime: Long,
    val endDateTime: Long,
    val breakMinutes: Int = 0,
    val memo: String = ""
)
