package com.andre.fitnesstracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(entry: ExerciseEntry)

    @Query("SELECT * FROM exercise_entries ORDER BY timestampMs DESC")
    fun observeAll(): Flow<List<ExerciseEntry>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM exercise_entries WHERE exercise = :exercise")
    suspend fun totalForExercise(exercise: String): Int
}
