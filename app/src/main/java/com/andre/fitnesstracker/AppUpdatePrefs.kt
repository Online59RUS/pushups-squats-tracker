package com.andre.fitnesstracker

import android.content.Context

object AppUpdatePrefs {
    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_LAST_SEEN_VERSION = "last_seen_version"

    fun shouldShowWhatsNew(context: Context, currentVersion: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSeenVersion = prefs.getString(KEY_LAST_SEEN_VERSION, null)
        return lastSeenVersion != currentVersion
    }

    fun markWhatsNewSeen(context: Context, currentVersion: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_SEEN_VERSION, currentVersion).apply()
    }
}