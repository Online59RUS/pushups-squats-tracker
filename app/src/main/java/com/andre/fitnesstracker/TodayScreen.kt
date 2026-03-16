package com.andre.fitnesstracker

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard
import com.andre.fitnesstracker.ui.theme.PrimaryActionButton
import com.andre.fitnesstracker.ui.theme.SecondaryActionButton
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
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

    val availableExercises = remember(ui.seriesMode) {
        when (ui.seriesMode) {
            "pushups" -> listOf("Отжимания")
            "squats" -> listOf("Приседания")
            else -> listOf("Отжимания", "Приседания")
        }
    }

    val currentExercise = remember(ui.selectedExercise, ui.seriesMode) {
        when {
            ui.selectedExercise in availableExercises -> ui.selectedExercise
            else -> availableExercises.first()
        }
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        selectedLabelColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val dayStart = ui.selectedDayMs
    val dayEnd = dayStart + 24L * 60 * 60 * 1000

    val (morningSum, eveningSum) = remember(ui.entries, ui.selectedDayMs, currentExercise) {
        var m = 0
        var e = 0
        ui.entries.forEach { entry ->
            val inDay = entry.timestampMs in dayStart until dayEnd
            val sameExercise = entry.exercise == currentExercise
            if (!inDay || !sameExercise) return@forEach

            when (entry.session) {
                "Утро" -> m += entry.amount
                "Вечер" -> e += entry.amount
            }
        }
        m to e
    }

    val factDay = remember(ui.entries, ui.selectedDayMs, currentExercise) {
        ui.entries
            .asSequence()
            .filter { it.exercise == currentExercise }
            .filter { it.timestampMs in dayStart until dayEnd }
            .sumOf { it.amount }
    }

    val planDay = vm.goalFor(currentExercise)
    val leftDay = (planDay - factDay).coerceAtLeast(0)
    val progressDay = if (planDay <= 0) 0f else (factDay.toFloat() / planDay.toFloat()).coerceIn(0f, 1f)

    val level = vm.currentStrengthLevel()
    val strengthPair = vm.currentStrengthProgress()
    val currentStrengthDays = strengthPair.first
    val targetStrengthDays = strengthPair.second
    val strengthProgress =
        if (targetStrengthDays <= 0) 0f
        else (currentStrengthDays.toFloat() / targetStrengthDays.toFloat()).coerceIn(0f, 1f)
    val daysToNext = vm.daysToNextLevel()

    val totalAllTime = ui.totals[currentExercise] ?: 0
    val nextThreshold = remember(totalAllTime, currentExercise) {
        vm.achievementsFor(currentExercise)
            .map { it.threshold }
            .sorted()
            .firstOrNull { it > totalAllTime }
    }
    val leftToNext = if (nextThreshold == null) 0 else (nextThreshold - totalAllTime)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Сегодня",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        GlassCard(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "СИЛА",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(level.iconRes),
                        contentDescription = level.name,
                        modifier = Modifier.matchParentSize(),
                        alpha = 0.18f
                    )

                    Image(
                        painter = painterResource(level.iconRes),
                        contentDescription = level.name,
                        modifier = Modifier
                            .matchParentSize()
                            .drawWithContent {
                                val clipTop = size.height * (1f - strengthProgress)
                                clipRect(
                                    left = 0f,
                                    top = clipTop,
                                    right = size.width,
                                    bottom = size.height
                                ) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    )
                }

                Text(
                    text = "Уровень: ${level.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "🔥 Серия: ${ui.streakDays} дней",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                LinearProgressIndicator(
                    progress = { strengthProgress },
                    modifier = Modifier.fillMaxWidth()
                )

                if (level.toDays == Int.MAX_VALUE) {
                    Text(
                        text = "Максимальный уровень",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "$currentStrengthDays / $targetStrengthDays дней",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (daysToNext > 0) {
                    Text(
                        text = "До следующего уровня: $daysToNext дней",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = currentExercise,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = when (ui.seriesMode) {
                            "pushups" -> "Режим: отжимания"
                            "squats" -> "Режим: приседания"
                            else -> "Режим: оба"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { progressDay },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMini("План", "$planDay")
                    StatMini("Факт", "$factDay")
                    StatMini("Осталось", "$leftDay")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMini("Утро", "$morningSum")
                    StatMini("Вечер", "$eveningSum")
                    StatMini("Всего", "$factDay")
                }
            }
        }

        if (ui.seriesMode == "both") {
            Text(
                text = "Упражнение",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableExercises.forEach { ex ->
                    FilterChip(
                        selected = currentExercise == ex,
                        onClick = { vm.setExercise(ex) },
                        label = { Text(ex) },
                        colors = chipColors
                    )
                }
            }
        }

        Text(
            text = "Сессия",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )

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

        Text(
            text = "Дата",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryActionButton(
                text = "Сегодня",
                modifier = Modifier.weight(1f),
                onClick = { vm.setToday() }
            )

            SecondaryActionButton(
                text = "Вчера",
                modifier = Modifier.weight(1f),
                onClick = { vm.setYesterday() }
            )

            PrimaryActionButton(
                text = "Выбрать",
                modifier = Modifier.weight(1f),
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
                }
            )
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Прогресс по ${currentExercise.lowercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Итого: $totalAllTime",
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (nextThreshold != null) {
                    Text(
                        text = "До следующей цели: $leftToNext",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Все цели выполнены 🎉",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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
                Text("Сброс")
            }
        }

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

        PrimaryActionButton(
            text = "Сохранить",
            modifier = Modifier.fillMaxWidth(),
            onClick = { vm.addExerciseIfValid(ui.selectedDayMs) }
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatMini(
    title: String,
    value: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun QuickAddButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SecondaryActionButton(
        text = label,
        modifier = modifier,
        onClick = onClick
    )
}