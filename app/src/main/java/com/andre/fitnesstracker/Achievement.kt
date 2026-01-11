package com.andre.fitnesstracker

data class Achievement(
    val exercise: String,
    val threshold: Int,
    val title: String,
    val achieved: Boolean,
    val progress: Int
)

object AchievementRules {
    val thresholds = listOf(100, 500, 1000, 5000, 10000)

    fun build(exercise: String, total: Int): List<Achievement> =
        thresholds.map { t ->
            Achievement(
                exercise = exercise,
                threshold = t,
                title = when (t) {
                    100 -> "Первые 100"
                    500 -> "Первые 500"
                    1000 -> "Первые 1000"
                    5000 -> "Первые 5000"
                    10000 -> "Первые 10000"
                    else -> "Цель $t"
                },
                achieved = total >= t,
                progress = total.coerceAtMost(t)
            )
        }
}
