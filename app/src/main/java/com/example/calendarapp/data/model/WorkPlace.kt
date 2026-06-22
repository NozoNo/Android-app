package com.example.calendarapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workplaces")
data class WorkPlace(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val baseWage: Int,
    val nightWage: Int? = null,
    val holidayWage: Int? = null,
    val useHigherWage: Boolean = true
)
