package com.andre.fitnesstracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_entries")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exercise: String,
    val amount: Int,
    val timestampMs: Long,
    val session: String? = null
)
