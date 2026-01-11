package com.andre.fitnesstracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPlanDao {
    @Query("SELECT * FROM day_plan WHERE dayStartMs = :dayStartMs AND exercise = :exercise LIMIT 1")
    fun observe(dayStartMs: Long, exercise: String): Flow<DayPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: DayPlan)
}
