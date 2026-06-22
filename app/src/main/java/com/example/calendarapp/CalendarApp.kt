package com.example.calendarapp

import android.app.Application
import com.example.calendarapp.data.db.AppDatabase

class CalendarApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
