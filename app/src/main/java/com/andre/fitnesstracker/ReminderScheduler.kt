package com.andre.fitnesstracker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ReminderScheduler {

    // Основное - планирование на конкретное время (8:00, 21:00 и т.д.)
    fun scheduleDaily(
        context: Context,
        uniqueName: String,
        hour: Int,
        minute: Int,
        session: String
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delayMs = max(0L, target.timeInMillis - now.timeInMillis)

        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "session" to session,
                    "hour" to hour,
                    "minute" to minute,
                    "uniqueName" to uniqueName
                )
            )
            .build()

        // ВАЖНО - уникальная очередь, без дублей
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    // ТЕСТ - планирование "через N секунд" (для проверки в эмуляторе)
    fun scheduleInSeconds(
        context: Context,
        uniqueName: String,
        seconds: Long,
        session: String
    ) {
        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(seconds, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                    "session" to session,
                    "hour" to -1,
                    "minute" to -1,
                    "uniqueName" to uniqueName
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.REPLACE,
            req
        )
    }
}
