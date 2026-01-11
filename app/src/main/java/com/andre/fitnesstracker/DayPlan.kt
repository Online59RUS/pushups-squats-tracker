package com.andre.fitnesstracker

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "day_plan",
    primaryKeys = ["dayStartMs", "exercise"],
    indices = [Index("dayStartMs"), Index("exercise")]
)
data class DayPlan(
    val dayStartMs: Long,      // начало дня (00:00)
    val exercise: String,      // "Отжимания"
    val goal: Int,             // цель на день
    val closed: Boolean        // день закрыт?
)
