package com.andre.fitnesstracker

import java.util.Calendar

object StreakRules {
    val thresholds = listOf(3, 7, 14, 30, 60, 100)

    fun build(streakDays: Int): List<Achievement> {
        return thresholds.map { t ->
            Achievement(
                exercise = "Серия",                 // <-- важно!
                title = "Без пропусков $t дней",
                threshold = t,
                progress = streakDays.coerceAtMost(t),
                achieved = streakDays >= t
            )
        }
    }

    private fun dayKey(ms: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = ms }
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val d = c.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }

    private fun startOfDayMs(ms: Long): Long {
        val c = Calendar.getInstance().apply { timeInMillis = ms }
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun calcStreak(
        entries: List<ExerciseEntry>,
        nowMs: Long = System.currentTimeMillis()
    ): Pair<Int, Boolean> {
        val todayStart = startOfDayMs(nowMs)
        val yesterdayStart = todayStart - 24L * 60 * 60 * 1000

        val doneDays = entries.asSequence()
            .map { dayKey(it.timestampMs) }
            .toSet()

        val todayKey = dayKey(todayStart)
        val yesterdayKey = dayKey(yesterdayStart)

        val todayDone = doneDays.contains(todayKey)

        var streak = 0
        var cursorStart = if (todayDone) todayStart else yesterdayStart

        while (true) {
            val key = dayKey(cursorStart)
            if (!doneDays.contains(key)) break
            streak += 1
            cursorStart -= 24L * 60 * 60 * 1000
        }

        return streak to todayDone
    }
}
