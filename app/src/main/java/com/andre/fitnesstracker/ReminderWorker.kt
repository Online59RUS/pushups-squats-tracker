package com.andre.fitnesstracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uniqueName = inputData.getString("uniqueName") ?: return Result.success()
        val label = inputData.getString("label") ?: "Напоминание"
        val hour = inputData.getInt("hour", 8)
        val minute = inputData.getInt("minute", 0)

        // показать уведомление
        NotificationUtil.show(applicationContext, label)

        // запланировать следующее на завтра
        // (для test_reminder можно не планировать, но пусть будет ок)
        if (uniqueName != "test_reminder") {
            ReminderScheduler.scheduleNext(applicationContext, uniqueName, label, hour, minute)
        }

        return Result.success()
    }
}
