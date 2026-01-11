package com.andre.fitnesstracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        val session = inputData.getString("session") ?: "Тренировка"
        val hour = inputData.getInt("hour", -1)
        val minute = inputData.getInt("minute", -1)
        val uniqueName = inputData.getString("uniqueName") ?: "reminder_generic"

        val name = UserPrefs(applicationContext).getNameOnceOrDefault("Андрей")

        val text = when (session) {
            "Утро" -> "$name, пора сделать утренний сет"
            "Вечер" -> "$name, пора сделать вечерний сет"
            else -> "$name, пора сделать сет"
        }

        showNotification(text)

        // Если это "боевое" (hour/minute задано) - планируем следующий день
        if (hour in 0..23 && minute in 0..59) {
            ReminderScheduler.scheduleDaily(
                context = applicationContext,
                uniqueName = uniqueName,
                hour = hour,
                minute = minute,
                session = session
            )
        }

        Result.success()
    }

    private fun showNotification(text: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "daily_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId,
                "Напоминания",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(ch)
        }

        val n = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_stat_notify) // твой файл в res/drawable
            .setContentTitle("FitnessTracker")
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        nm.notify((System.currentTimeMillis() % 100000).toInt(), n)
    }
}
