package com.andre.fitnesstracker

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class UserPrefs(context: Context) {

    private val sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _nameFlow = MutableStateFlow(sp.getString(KEY_NAME, "") ?: "")
    val nameFlow: StateFlow<String> = _nameFlow

    private val _goalPushupsFlow = MutableStateFlow(sp.getInt(KEY_GOAL_PUSHUPS, 50))
    val goalPushupsFlow: StateFlow<Int> = _goalPushupsFlow

    private val _goalSquatsFlow = MutableStateFlow(sp.getInt(KEY_GOAL_SQUATS, 50))
    val goalSquatsFlow: StateFlow<Int> = _goalSquatsFlow

    fun setName(v: String) {
        sp.edit().putString(KEY_NAME, v).apply()
        _nameFlow.value = v
    }

    fun setGoalPushups(v: Int) {
        sp.edit().putInt(KEY_GOAL_PUSHUPS, v).apply()
        _goalPushupsFlow.value = v
    }

    fun setGoalSquats(v: Int) {
        sp.edit().putInt(KEY_GOAL_SQUATS, v).apply()
        _goalSquatsFlow.value = v
    }

    suspend fun getNameOnceOrDefault(default: String): String {
        return try {
            nameFlow.first()
        } catch (_: Throwable) {
            default
        }
    }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_GOAL_PUSHUPS = "goal_pushups"
        const val KEY_GOAL_SQUATS = "goal_squats"
    }
}
