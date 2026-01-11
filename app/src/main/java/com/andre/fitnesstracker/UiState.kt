package com.andre.fitnesstracker

data class UiState(
    // данные
    val entries: List<ExerciseEntry> = emptyList(),
    val totals: Map<String, Int> = emptyMap(),

    // выборы на экране
    val exercises: List<String> = listOf("Отжимания", "Приседания"),
    val selectedExercise: String = "Отжимания",
    val session: String? = "Утро",
    val amountText: String = "",

    // выбранная дата (начало дня, 00:00)
    val selectedDayMs: Long = DateUtils.startOfDayMs(System.currentTimeMillis()),

    // серия
    val streakDays: Int = 0,
    val todayDone: Boolean = false,

    // персонализация
    val userName: String = "",
    val goalPushups: Int = 50,
    val goalSquats: Int = 50
)
