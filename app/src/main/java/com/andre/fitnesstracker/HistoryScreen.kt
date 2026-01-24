package com.andre.fitnesstracker

import android.app.DatePickerDialog
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard
import com.andre.fitnesstracker.ui.theme.GlassOutlinedButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val density = LocalDensity.current

    // --- Фильтры ---
    var exerciseFilter by remember { mutableStateOf("Все") }
    var dateFilterMs by remember { mutableStateOf<Long?>(null) }

    // --- Меню (лонг-тап) ---
    var menuForId by remember { mutableStateOf<Long?>(null) }
    var menuOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }

    // --- Диалоги ---
    var editEntry by remember { mutableStateOf<ExerciseEntry?>(null) }
    var deleteEntry by remember { mutableStateOf<ExerciseEntry?>(null) }

    val dayFmt = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        selectedLabelColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val filtered = remember(ui.entries, exerciseFilter, dateFilterMs) {
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
                val dayStart = DateUtils.startOfDayMs(e.timestampMs)
                dateFilterMs == null || dayStart == dateFilterMs
            }
            .toList()
            .sortedByDescending { it.timestampMs }
    }

    val dateLabel = remember(dateFilterMs) {
        if (dateFilterMs == null) "Все" else dayFmt.format(Date(dateFilterMs!!))
    }

    // ====== UI ======
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "История",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

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

                val expanded = (menuForId == e.id)

                Box(Modifier.fillMaxWidth()) {
                    GlassCard(
                        Modifier
                            .fillMaxWidth()
                            .pointerInput(e.id) {
                                detectTapGestures(
                                    onTap = {
                                        // просто закрываем меню, если было открыто
                                        menuForId = null
                                    },
                                    onLongPress = { pressOffsetPx ->
                                        // ВАЖНО: корректный px -> dp через LocalDensity
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
                            "${e.exercise} - ${e.amount}",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${timeFmt.format(Date(e.timestampMs))}  •  ${e.session ?: "Без метки"}",
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
                    Text(
                        "Нет записей по выбранным фильтрам.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---- Нижняя панель фильтров ----
        GlassCard(Modifier.fillMaxWidth()) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = exerciseFilter == "Все",
                    onClick = { exerciseFilter = "Все" },
                    label = { Text("Все") },
                    colors = chipColors
                )
                FilterChip(
                    selected = exerciseFilter == "Отжимания",
                    onClick = { exerciseFilter = "Отжимания" },
                    label = { Text("Отжимания") },
                    colors = chipColors
                )
                FilterChip(
                    selected = exerciseFilter == "Приседания",
                    onClick = { exerciseFilter = "Приседания" },
                    label = { Text("Приседания") },
                    colors = chipColors
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassOutlinedButton(
                    text = "Все",
                    modifier = Modifier.weight(1f),
                    onClick = { dateFilterMs = null }
                )

                Button(
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
                ) { Text("Выбрать") }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Дата: $dateLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // ====== Диалог удаления ======
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
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { deleteEntry = null }) { Text("Отмена") }
            }
        )
    }

    // ====== Диалог редактирования ======
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

                    Text("Метка", color = MaterialTheme.colorScheme.onSurfaceVariant)

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
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { editEntry = null }) { Text("Отмена") }
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
            text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
