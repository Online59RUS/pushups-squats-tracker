package com.andre.fitnesstracker

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard
import com.andre.fitnesstracker.ui.theme.GlassOutlinedButton
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val scroll = rememberScrollState()

    val dateText = remember(ui.selectedDayMs) {
        val c = Calendar.getInstance().apply { timeInMillis = ui.selectedDayMs }
        "%02d.%02d.%04d".format(
            c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.YEAR)
        )
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        selectedLabelColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    // Диапазон выбранного дня
    val dayStart = ui.selectedDayMs
    val dayEnd = dayStart + 24L * 60 * 60 * 1000

    // Утро/Вечер по выбранной дате и упражнению
    val (morningSum, eveningSum) = remember(ui.entries, ui.selectedDayMs, ui.selectedExercise) {
        var m = 0
        var e = 0
        ui.entries.forEach { entry ->
            val inDay = entry.timestampMs in dayStart until dayEnd
            val sameExercise = entry.exercise == ui.selectedExercise
            if (!inDay || !sameExercise) return@forEach

            when (entry.session) {
                "Утро" -> m += entry.amount
                "Вечер" -> e += entry.amount
            }
        }
        m to e
    }

    // Факт за день (включая записи "без метки")
    val factDay = remember(ui.entries, ui.selectedDayMs, ui.selectedExercise) {
        ui.entries
            .asSequence()
            .filter { it.exercise == ui.selectedExercise }
            .filter { it.timestampMs in dayStart until dayEnd }
            .sumOf { it.amount }
    }

    // План (цель на день) для выбранного упражнения
    val planDay = remember(ui.goalPushups, ui.goalSquats, ui.selectedExercise) {
        vm.goalFor(ui.selectedExercise)
    }

    val leftDay = (planDay - factDay).coerceAtLeast(0)
    val progress = if (planDay <= 0) 0f else (factDay.toFloat() / planDay.toFloat()).coerceIn(0f, 1f)

    // Итого за всё время + до следующей медали (без unlocked)
    val totalAllTime = ui.totals[ui.selectedExercise] ?: 0
    val nextThreshold = remember(totalAllTime, ui.selectedExercise, ui.totals) {
        vm.achievementsFor(ui.selectedExercise)
            .map { it.threshold }
            .sorted()
            .firstOrNull { it > totalAllTime }
    }
    val leftToNext = if (nextThreshold == null) 0 else (nextThreshold - totalAllTime)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Сегодня",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Серия / статус (пока просто выводим как есть из UiState)
        GlassCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Серия: ${ui.streakDays} дн.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text("• Сегодня", color = MaterialTheme.colorScheme.onBackground)
            }
        }

        // Сессия
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = ui.session == "Утро",
                onClick = { vm.setSession("Утро") },
                label = { Text("Утро") },
                colors = chipColors
            )
            FilterChip(
                selected = ui.session == "Вечер",
                onClick = { vm.setSession("Вечер") },
                label = { Text("Вечер") },
                colors = chipColors
            )
            FilterChip(
                selected = ui.session == null,
                onClick = { vm.setSession(null) },
                label = { Text("Без метки") },
                colors = chipColors
            )
        }

        // Упражнение
        Text(
            "Упражнение",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ui.exercises.forEach { ex ->
                FilterChip(
                    selected = ui.selectedExercise == ex,
                    onClick = { vm.setExercise(ex) },
                    label = { Text(ex) },
                    colors = chipColors
                )
            }
        }

        // Дата + кнопки
        Text(
            "Дата: $dateText",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            GlassOutlinedButton(
                modifier = Modifier.weight(1f),
                text = "Сегодня",
                onClick = { vm.setToday() }
            )
            GlassOutlinedButton(
                modifier = Modifier.weight(1f),
                text = "Вчера",
                onClick = { vm.setYesterday() }
            )
            Button(
                onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = ui.selectedDayMs }
                    DatePickerDialog(
                        ctx,
                        { _, y, m, d ->
                            val c = Calendar.getInstance().apply {
                                set(Calendar.YEAR, y)
                                set(Calendar.MONTH, m)
                                set(Calendar.DAY_OF_MONTH, d)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            vm.setSelectedDay(c.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) { Text("Выбрать") }
        }

        // Цель на день (вариант A)
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                "Цель на день",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(6.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("План", color = MaterialTheme.colorScheme.onBackground)
                Text("$planDay", color = MaterialTheme.colorScheme.onBackground)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Факт", color = MaterialTheme.colorScheme.onBackground)
                Text("$factDay", color = MaterialTheme.colorScheme.onBackground)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Осталось", color = MaterialTheme.colorScheme.onBackground)
                Text("$leftDay", color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
            )


            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("• Утро", color = MaterialTheme.colorScheme.onBackground)
                Text("$morningSum", color = MaterialTheme.colorScheme.onBackground)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("• Вечер", color = MaterialTheme.colorScheme.onBackground)
                Text("$eveningSum", color = MaterialTheme.colorScheme.onBackground)
            }

            Text(
                "Считаем по выбранному упражнению и дате.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Итого по упражнению + до следующей медали
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                "Итого по \"${ui.selectedExercise}\": $totalAllTime",
                color = MaterialTheme.colorScheme.onBackground
            )
            if (nextThreshold != null) {
                Text(
                    "До следующей медали осталось: $leftToNext",
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Text("Все медали получены 🎉", color = MaterialTheme.colorScheme.onBackground)
            }
        }

        // Ввод + сброс
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = ui.amountText,
                onValueChange = vm::setAmountText,
                label = { Text("Повторы") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            TextButton(onClick = { vm.setAmountText("") }) {
                Text("Сброс", color = MaterialTheme.colorScheme.onBackground)
            }
        }

        // Быстрые кнопки
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickAddButton("+5", Modifier.weight(1f)) {
                val cur = ui.amountText.toIntOrNull() ?: 0
                vm.setAmountText((cur + 5).toString())
            }
            QuickAddButton("+10", Modifier.weight(1f)) {
                val cur = ui.amountText.toIntOrNull() ?: 0
                vm.setAmountText((cur + 10).toString())
            }
            QuickAddButton("+20", Modifier.weight(1f)) {
                val cur = ui.amountText.toIntOrNull() ?: 0
                vm.setAmountText((cur + 20).toString())
            }
        }

        // Сохранить (важно: передаем выбранную дату)
        Button(
            onClick = { vm.addExerciseIfValid(ui.selectedDayMs) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Сохранить") }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuickAddButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Text(label)
    }
}
