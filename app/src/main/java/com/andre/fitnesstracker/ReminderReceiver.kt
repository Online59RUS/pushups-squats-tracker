package com.andre.fitnesstracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uniqueName = intent.getStringExtra("uniqueName") ?: return
        val label = intent.getStringExtra("label") ?: "Напоминание"
        val hour = intent.getIntExtra("hour", 8)
        val minute = intent.getIntExtra("minute", 0)

        // Показ уведомления
        NotificationUtil.show(context, label)

        // Планируем следующее на завтра (важно!)
        ReminderScheduler.scheduleNext(context, uniqueName, label, hour, minute)
    }
}
