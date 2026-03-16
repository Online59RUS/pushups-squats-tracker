package com.andre.fitnesstracker

import android.app.DatePickerDialog
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard
import com.andre.fitnesstracker.ui.theme.PrimaryActionButton
import com.andre.fitnesstracker.ui.theme.SecondaryActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val density = LocalDensity.current

    var exerciseFilter by remember { mutableStateOf("Все") }
    var dateFilterMs by remember { mutableStateOf<Long?>(null) }

    var menuForId by remember { mutableStateOf<Long?>(null) }
    var menuOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }

    var editEntry by remember { mutableStateOf<ExerciseEntry?>(null) }
    var deleteEntry by remember { mutableStateOf<ExerciseEntry?>(null) }

    val dayFmt = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    val availableExercises = remember(ui.seriesMode) {
        when (ui.seriesMode) {
            "pushups" -> listOf("Отжимания")
            "squats" -> listOf("Приседания")
            else -> listOf("Отжимания", "Приседания")
        }
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        selectedLabelColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val filtered = remember(ui.entries, exerciseFilter, dateFilterMs, ui.seriesMode) {
        ui.entries
            .asSequence()
            .filter { e ->
                when (exerciseFilter) {
                    "Отжимания" -> e.exercise == "Отжимания"
                    "Приседания" -> e.exercise == "Приседания"
                    else -> true
                }
            }
            .filter { e ->
                if (ui.seriesMode == "pushups" && e.exercise != "Отжимания") return@filter false
                if (ui.seriesMode == "squats" && e.exercise != "Приседания") return@filter false
                true
            }
            .filter { e ->
                val dayStart = DateUtils.startOfDayMs(e.timestampMs)
                dateFilterMs == null || dayStart == dateFilterMs
            }
            .toList()
            .sortedByDescending { it.timestampMs }
    }

    val dateLabel = remember(dateFilterMs) {
        if (dateFilterMs == null) "Все" else dayFmt.format(Date(dateFilterMs!!))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "История",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Все сохранённые записи по упражнениям",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Фильтры",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = exerciseFilter == "Все",
                        onClick = { exerciseFilter = "Все" },
                        label = { Text("Все") },
                        colors = chipColors
                    )

                    if ("Отжимания" in availableExercises) {
                        FilterChip(
                            selected = exerciseFilter == "Отжимания",
                            onClick = { exerciseFilter = "Отжимания" },
                            label = { Text("Отжимания") },
                            colors = chipColors
                        )
                    }

                    if ("Приседания" in availableExercises) {
                        FilterChip(
                            selected = exerciseFilter == "Приседания",
                            onClick = { exerciseFilter = "Приседания" },
                            label = { Text("Приседания") },
                            colors = chipColors
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryActionButton(
                        text = "Все даты",
                        modifier = Modifier.weight(1f),
                        onClick = { dateFilterMs = null }
                    )

                    PrimaryActionButton(
                        text = "Выбрать",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = dateFilterMs ?: System.currentTimeMillis()
                            }
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
                                    dateFilterMs = c.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    )
                }

                Text(
                    text = "Дата: $dateLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            var lastDayKey: Long? = null

            itemsIndexed(filtered, key = { _, e -> e.id }) { _, e ->
                val dayKey = DateUtils.startOfDayMs(e.timestampMs)

                if (lastDayKey != dayKey) {
                    lastDayKey = dayKey
                    DayHeader(text = dayFmt.format(Date(dayKey)))
                }

                val expanded = menuForId == e.id

                Box(Modifier.fillMaxWidth()) {
                    GlassCard(
                        Modifier
                            .fillMaxWidth()
                            .pointerInput(e.id) {
                                detectTapGestures(
                                    onTap = { menuForId = null },
                                    onLongPress = { pressOffsetPx ->
                                        menuOffset = with(density) {
                                            DpOffset(
                                                x = pressOffsetPx.x.toDp(),
                                                y = pressOffsetPx.y.toDp()
                                            )
                                        }
                                        menuForId = e.id
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = "${e.exercise} - ${e.amount}",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = e.session ?: "Без метки",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { menuForId = null },
                        offset = menuOffset
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                menuForId = null
                                editEntry = e
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            onClick = {
                                menuForId = null
                                deleteEntry = e
                            }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = "Нет записей по выбранным фильтрам.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (deleteEntry != null) {
        val e = deleteEntry!!
        AlertDialog(
            onDismissRequest = { deleteEntry = null },
            title = { Text("Удалить запись?") },
            text = { Text("${e.exercise} - ${e.amount}") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteEntry(e.id)
                        deleteEntry = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteEntry = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (editEntry != null) {
        val e = editEntry!!

        var amountText by remember(e.id) { mutableStateOf(e.amount.toString()) }
        var session by remember(e.id) { mutableStateOf(e.session) }

        AlertDialog(
            onDismissRequest = { editEntry = null },
            title = { Text("Редактировать") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter(Char::isDigit).take(5) },
                        label = { Text("Повторы") },
                        singleLine = true
                    )

                    Text(
                        text = "Метка",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = session == "Утро",
                            onClick = { session = "Утро" },
                            label = { Text("Утро") },
                            colors = chipColors
                        )
                        FilterChip(
                            selected = session == "Вечер",
                            onClick = { session = "Вечер" },
                            label = { Text("Вечер") },
                            colors = chipColors
                        )
                        FilterChip(
                            selected = session == null,
                            onClick = { session = null },
                            label = { Text("Без метки") },
                            colors = chipColors
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newAmount = amountText.toIntOrNull() ?: e.amount
                        vm.updateEntry(
                            e.copy(
                                amount = newAmount,
                                session = session
                            )
                        )
                        editEntry = null
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { editEntry = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun DayHeader(text: String) {
    Row(
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}