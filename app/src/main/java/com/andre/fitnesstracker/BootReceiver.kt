package com.andre.fitnesstracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != "android.intent.action.BOOT_COMPLETED") return

        // Достаём время из prefs и пересоздаём напоминания
        val prefs = UserPrefs(context)

        CoroutineScope(Dispatchers.Default).launch {
            val mh = prefs.morningHourFlow.first()
            val mm = prefs.morningMinFlow.first()
            val eh = prefs.eveningHourFlow.first()
            val em = prefs.eveningMinFlow.first()

            ReminderScheduler.scheduleAll(context, mh, mm, eh, em)
        }
    }
}
