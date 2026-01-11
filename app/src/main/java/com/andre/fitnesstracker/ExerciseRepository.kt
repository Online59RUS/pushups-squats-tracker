package com.andre.fitnesstracker

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val planDao: DayPlanDao
) {
    fun observeAll(): Flow<List<ExerciseEntry>> = exerciseDao.observeAll()

    suspend fun add(
        exercise: String,
        amount: Int,
        session: String?,
        timestampMs: Long
    ) {
        // 1) вставляем запись упражнения
        exerciseDao.insert(
            ExerciseEntry(
                exercise = exercise,
                amount = amount,
                timestampMs = timestampMs,
                session = session
            )
        )

        // 2) ensure DayPlan на день существует (для целей/галочки "день закрыт")
        val dayStart = startOfDayMs(timestampMs)

        // ВАЖНО:
        // твой DayPlan судя по ошибке требует: dayStartMs + exercise + goal + closed
        // поэтому создаём план с дефолтами (цель=0, закрыт=false)
        planDao.upsert(
            DayPlan(
                dayStartMs = dayStart,
                exercise = exercise,
                goal = 0,
                closed = false
            )
        )
    }

    suspend fun total(exercise: String): Int = exerciseDao.totalForExercise(exercise)

    private fun startOfDayMs(ts: Long): Long {
        val c = Calendar.getInstance().apply { timeInMillis = ts }
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
