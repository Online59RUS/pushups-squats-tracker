package com.andre.fitnesstracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.app.Application


@Database(
    entities = [
        ExerciseEntry::class,
        DayPlan::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun dayPlanDao(): DayPlanDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun get(app: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    app,
                    AppDatabase::class.java,
                    "fitness.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
