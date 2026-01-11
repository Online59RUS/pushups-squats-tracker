package com.andre.fitnesstracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val repo = ExerciseRepository(db.exerciseDao(), db.dayPlanDao())
    private val prefs = UserPrefs(app)

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        // записи
        viewModelScope.launch {
            repo.observeAll().collect { list ->
                _ui.update { it.copy(entries = list) }
                refreshTotals()
                recomputeStreakFromEntries(list) // ✅ ВАЖНО: серия по фактическим записям
            }
        }

        // имя + цели
        viewModelScope.launch {
            prefs.nameFlow.collect { name ->
                _ui.update { it.copy(userName = name) }
            }
        }
        viewModelScope.launch {
            prefs.goalPushupsFlow.collect { g ->
                _ui.update { it.copy(goalPushups = g) }
            }
        }
        viewModelScope.launch {
            prefs.goalSquatsFlow.collect { g ->
                _ui.update { it.copy(goalSquats = g) }
            }
        }
    }

    private fun refreshTotals() {
        viewModelScope.launch {
            val exercises = _ui.value.exercises
            val totals = exercises.associateWith { ex -> repo.total(ex) }
            _ui.update { it.copy(totals = totals) }
        }
    }

    fun setExercise(v: String) = _ui.update { it.copy(selectedExercise = v) }
    fun setSession(v: String?) = _ui.update { it.copy(session = v) }
    fun setAmountText(v: String) = _ui.update { it.copy(amountText = v) }

    fun setSelectedDay(ms: Long) = _ui.update { it.copy(selectedDayMs = ms) }
    fun setToday() = _ui.update { it.copy(selectedDayMs = startOfDayMs(System.currentTimeMillis())) }

    fun setYesterday() = _ui.update {
        val todayStart = startOfDayMs(System.currentTimeMillis())
        it.copy(selectedDayMs = todayStart - 24L * 60 * 60 * 1000)
    }

    fun setUserName(v: String) {
        viewModelScope.launch { prefs.setName(v) }
    }

    fun setGoalPushups(v: Int) {
        viewModelScope.launch { prefs.setGoalPushups(v) }
    }

    fun setGoalSquats(v: Int) {
        viewModelScope.launch { prefs.setGoalSquats(v) }
    }

    fun addExerciseIfValid(dateMs: Long) {
        val ex = _ui.value.selectedExercise
        val session = _ui.value.session
        val amount = _ui.value.amountText.toIntOrNull() ?: return

        viewModelScope.launch {
            repo.add(
                exercise = ex,
                amount = amount,
                session = session,
                timestampMs = dateMs // ✅ сохраняем выбранную дату
            )
            _ui.update { it.copy(amountText = "") }
            refreshTotals()
            // серию пересчитает observeAll() (после вставки запись прилетит из Room)
        }
    }

    fun goalFor(exercise: String): Int {
        return when (exercise) {
            "Отжимания" -> _ui.value.goalPushups
            "Приседания" -> _ui.value.goalSquats
            else -> 0
        }
    }

    fun achievementsFor(exercise: String): List<Achievement> {
        val total = _ui.value.totals[exercise] ?: 0
        return AchievementRules.build(exercise, total)
    }

    // -------------------- СЕРИЯ --------------------
    // День "засчитан", если в этот день есть И отжимания, И приседания (любая сессия).
    private fun recomputeStreakFromEntries(entries: List<ExerciseEntry>) {
        val todayStart = startOfDayMs(System.currentTimeMillis())

        // dayStart -> набор упражнений, которые были в этот день
        val dayToExercises: Map<Long, Set<String>> = entries
            .groupBy { startOfDayMs(it.timestampMs) }
            .mapValues { (_, list) -> list.map { it.exercise }.toSet() }

        fun dayIsComplete(dayStartMs: Long): Boolean {
            val set = dayToExercises[dayStartMs] ?: emptySet()
            return set.contains("Отжимания") && set.contains("Приседания")
        }

        var streak = 0
        var cur = todayStart
        while (dayIsComplete(cur)) {
            streak++
            cur -= 24L * 60 * 60 * 1000
        }

        _ui.update {
            it.copy(
                todayDone = dayIsComplete(todayStart),
                streakDays = streak
            )
        }
    }

    // -------------------- utils --------------------
    private fun startOfDayMs(ts: Long): Long {
        val c = Calendar.getInstance().apply { timeInMillis = ts }
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
