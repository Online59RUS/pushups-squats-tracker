package com.andre.fitnesstracker

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPrefs(context: Context) {

    // ВАЖНО: возвращаем старое имя, чтобы не потерять настройки пользователей
    private val sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _nameFlow = MutableStateFlow(sp.getString(KEY_NAME, "") ?: "")
    val nameFlow: StateFlow<String> = _nameFlow.asStateFlow()

    private val _goalPushupsFlow = MutableStateFlow(sp.getInt(KEY_GOAL_PUSHUPS, 50))
    val goalPushupsFlow: StateFlow<Int> = _goalPushupsFlow.asStateFlow()

    private val _goalSquatsFlow = MutableStateFlow(sp.getInt(KEY_GOAL_SQUATS, 50))
    val goalSquatsFlow: StateFlow<Int> = _goalSquatsFlow.asStateFlow()

    private val _morningHourFlow = MutableStateFlow(sp.getInt(KEY_MORNING_H, 8))
    val morningHourFlow: StateFlow<Int> = _morningHourFlow.asStateFlow()

    private val _morningMinFlow = MutableStateFlow(sp.getInt(KEY_MORNING_M, 0))
    val morningMinFlow: StateFlow<Int> = _morningMinFlow.asStateFlow()

    private val _eveningHourFlow = MutableStateFlow(sp.getInt(KEY_EVENING_H, 21))
    val eveningHourFlow: StateFlow<Int> = _eveningHourFlow.asStateFlow()

    private val _eveningMinFlow = MutableStateFlow(sp.getInt(KEY_EVENING_M, 0))
    val eveningMinFlow: StateFlow<Int> = _eveningMinFlow.asStateFlow()

    suspend fun setName(v: String) {
        sp.edit().putString(KEY_NAME, v).apply()
        _nameFlow.value = v
    }

    suspend fun setGoalPushups(v: Int) {
        sp.edit().putInt(KEY_GOAL_PUSHUPS, v).apply()
        _goalPushupsFlow.value = v
    }

    suspend fun setGoalSquats(v: Int) {
        sp.edit().putInt(KEY_GOAL_SQUATS, v).apply()
        _goalSquatsFlow.value = v
    }

    suspend fun setMorningTime(hour: Int, minute: Int) {
        sp.edit().putInt(KEY_MORNING_H, hour).putInt(KEY_MORNING_M, minute).apply()
        _morningHourFlow.value = hour
        _morningMinFlow.value = minute
    }

    suspend fun setEveningTime(hour: Int, minute: Int) {
        sp.edit().putInt(KEY_EVENING_H, hour).putInt(KEY_EVENING_M, minute).apply()
        _eveningHourFlow.value = hour
        _eveningMinFlow.value = minute
    }

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_GOAL_PUSHUPS = "goal_pushups"
        private const val KEY_GOAL_SQUATS = "goal_squats"
        private const val KEY_MORNING_H = "morning_h"
        private const val KEY_MORNING_M = "morning_m"
        private const val KEY_EVENING_H = "evening_h"
        private const val KEY_EVENING_M = "evening_m"
    }
}
