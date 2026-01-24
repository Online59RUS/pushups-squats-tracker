package com.andre.fitnesstracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {

    fun scheduleAll(
        context: Context,
        morningH: Int,
        morningM: Int,
        eveningH: Int,
        eveningM: Int
    ) {
        scheduleNext(context, "reminder_morning", "Утро", morningH, morningM)
        scheduleNext(context, "reminder_evening", "Вечер", eveningH, eveningM)
    }

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun scheduleNext(
        context: Context,
        uniqueName: String,
        label: String,
        hour: Int,
        minute: Int
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // На Android 12+ если запрещены точные будильники – НЕ пытаемся ставить exact (иначе SecurityException)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // Ничего не планируем (а UI должен показать пользователю кнопку "разрешить")
            return
        }

        val triggerAt = nextTriggerTimeMillis(System.currentTimeMillis(), hour, minute)

        val pi = pendingIntent(context, uniqueName, label, hour, minute)

        // Сначала отменим старый (чтобы не было дублей)
        am.cancel(pi)

        // Точное срабатывание
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pi
        )
    }

    private fun pendingIntent(
        context: Context,
        uniqueName: String,
        label: String,
        hour: Int,
        minute: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("uniqueName", uniqueName)
            putExtra("label", label)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val requestCode = uniqueName.hashCode()

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun nextTriggerTimeMillis(nowMs: Long, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance().apply { timeInMillis = nowMs }

        val target = Calendar.getInstance().apply {
            timeInMillis = nowMs
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        // Если время уже прошло – на завтра
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis
    }
}
