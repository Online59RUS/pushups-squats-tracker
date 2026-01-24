package com.andre.fitnesstracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                recomputeStreakFromEntries(list)
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

        // времена напоминаний
        viewModelScope.launch { prefs.morningHourFlow.collect { h -> _ui.update { it.copy(morningHour = h) } } }
        viewModelScope.launch { prefs.morningMinFlow.collect { m -> _ui.update { it.copy(morningMin = m) } } }
        viewModelScope.launch { prefs.eveningHourFlow.collect { h -> _ui.update { it.copy(eveningHour = h) } } }
        viewModelScope.launch { prefs.eveningMinFlow.collect { m -> _ui.update { it.copy(eveningMin = m) } } }
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
    fun setToday() = _ui.update { it.copy(selectedDayMs = DateUtils.startOfDayMs(System.currentTimeMillis())) }

    fun setYesterday() = _ui.update {
        val todayStart = DateUtils.startOfDayMs(System.currentTimeMillis())
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

    fun setMorningTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefs.setMorningTime(hour, minute)
            rescheduleReminders()
        }
    }

    fun setEveningTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefs.setEveningTime(hour, minute)
            rescheduleReminders()
        }
    }

    fun rescheduleReminders() {
        val s = _ui.value
        ReminderScheduler.scheduleAll(
            context = getApplication(),
            morningH = s.morningHour,
            morningM = s.morningMin,
            eveningH = s.eveningHour,
            eveningM = s.eveningMin
        )
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
                timestampMs = dateMs
            )
            _ui.update { it.copy(amountText = "") }
            refreshTotals()
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repo.delete(id)
            refreshTotals()
        }
    }

    fun updateEntry(entry: ExerciseEntry) {
        viewModelScope.launch {
            repo.update(entry)
            refreshTotals()
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

    // streak: день засчитан, если есть И отжимания И приседания в этот день (любые сессии)
    private fun recomputeStreakFromEntries(entries: List<ExerciseEntry>) {
        val todayStart = DateUtils.startOfDayMs(System.currentTimeMillis())

        val byDay: Map<Long, Set<String>> = entries
            .groupBy { DateUtils.startOfDayMs(it.timestampMs) }
            .mapValues { (_, list) -> list.map { it.exercise }.toSet() }

        fun isDayDone(dayStart: Long): Boolean {
            val set = byDay[dayStart] ?: emptySet()
            return set.contains("Отжимания") && set.contains("Приседания")
        }

        var streak = 0
        var cur = todayStart
        while (isDayDone(cur)) {
            streak++
            cur -= 24L * 60 * 60 * 1000
        }

        _ui.update {
            it.copy(
                todayDone = isDayDone(todayStart),
                streakDays = streak
            )
        }
    }
}
