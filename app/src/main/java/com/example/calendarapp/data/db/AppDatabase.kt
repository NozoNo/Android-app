package com.example.calendarapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.calendarapp.data.model.Event
import com.example.calendarapp.data.model.WorkPlace
import com.example.calendarapp.data.model.WorkShift

@Database(
    entities = [Event::class, WorkPlace::class, WorkShift::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun workPlaceDao(): WorkPlaceDao
    abstract fun workShiftDao(): WorkShiftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calendar_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
