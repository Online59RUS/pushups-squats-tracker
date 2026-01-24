package com.andre.fitnesstracker

data class UiState(
    val entries: List<ExerciseEntry> = emptyList(),

    val exercises: List<String> = listOf("Отжимания", "Приседания"),
    val selectedExercise: String = "Отжимания",
    val session: String? = "Утро",
    val amountText: String = "",

    val totals: Map<String, Int> = emptyMap(),

    val userName: String = "",
    val goalPushups: Int = 50,
    val goalSquats: Int = 50,

    // Today: выбранная дата (00:00)
    val selectedDayMs: Long = DateUtils.startOfDayMs(System.currentTimeMillis()),

    // streak
    val streakDays: Int = 0,
    val todayDone: Boolean = false,

    // напоминания
    val morningHour: Int = 8,
    val morningMin: Int = 0,
    val eveningHour: Int = 21,
    val eveningMin: Int = 0
)
