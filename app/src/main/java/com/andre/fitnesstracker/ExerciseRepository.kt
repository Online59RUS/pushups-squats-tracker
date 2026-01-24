package com.andre.fitnesstracker

import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val dayPlanDao: DayPlanDao
) {
    fun observeAll(): Flow<List<ExerciseEntry>> = exerciseDao.observeAll()

    suspend fun add(exercise: String, amount: Int, session: String?, timestampMs: Long) {
        exerciseDao.insert(
            ExerciseEntry(
                exercise = exercise,
                amount = amount,
                session = session,
                timestampMs = timestampMs
            )
        )
    }

    suspend fun update(entry: ExerciseEntry) {
        exerciseDao.update(entry)
    }

    suspend fun delete(id: Long) {
        exerciseDao.deleteById(id)
    }

    suspend fun total(exercise: String): Int = exerciseDao.totalForExercise(exercise)
}
