package com.andre.fitnesstracker

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard
import java.util.Calendar

private enum class Range(val days: Int) { D7(7), D31(31), D365(365) }
private enum class Part { ALL, MORNING, EVENING }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()
    val scrollState = rememberScrollState()

    var range by remember { mutableStateOf(Range.D31) }
    var exercise by remember { mutableStateOf(ui.selectedExercise) }
    var part by remember { mutableStateOf(Part.ALL) }

    val availableExercises = remember(ui.seriesMode) {
        when (ui.seriesMode) {
            "pushups" -> listOf("Отжимания")
            "squats" -> listOf("Приседания")
            else -> listOf("Отжимания", "Приседания")
        }
    }

    LaunchedEffect(ui.selectedExercise, ui.seriesMode) {
        exercise = when {
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

    val xKeys: List<String> = remember(range) {
        if (range == Range.D365) buildLastMonths(12) else buildLastDays(range.days)
    }

    val totals: List<Int> = remember(ui.entries, exercise, range, part, xKeys) {
        val map = mutableMapOf<String, Int>()
        xKeys.forEach { map[it] = 0 }

        ui.entries.forEach { e ->
            if (e.exercise != exercise) return@forEach
            if (part == Part.MORNING && e.session != "Утро") return@forEach
            if (part == Part.EVENING && e.session != "Вечер") return@forEach

            val key = if (range == Range.D365) monthKey(e.timestampMs) else dayKey(e.timestampMs)
            if (map.containsKey(key)) {
                map[key] = (map[key] ?: 0) + e.amount
            }
        }

        xKeys.map { k -> map[k] ?: 0 }
    }

    val sum = totals.sum()
    val maxV = totals.maxOrNull() ?: 0
    val avg = if (totals.isEmpty()) 0.0 else sum.toDouble() / totals.size.toDouble()

    val periodLabel = when (range) {
        Range.D7 -> "7 дней"
        Range.D31 -> "31 день"
        Range.D365 -> "12 месяцев"
    }

    val partLabel = when (part) {
        Part.ALL -> "Все подходы"
        Part.MORNING -> "Утро"
        Part.EVENING -> "Вечер"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Аналитика",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Смотри динамику по упражнениям и времени суток",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = exercise,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$periodLabel - $partLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnalyticsStat("Сумма", "$sum")
                    AnalyticsStat(
                        if (range == Range.D365) "Макс/мес" else "Макс/день",
                        "$maxV"
                    )
                    AnalyticsStat(
                        if (range == Range.D365) "Сред/мес" else "Сред/день",
                        "%.1f".format(avg)
                    )
                }
            }
        }

        Text(
            text = "Период",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = range == Range.D7,
                onClick = { range = Range.D7 },
                label = { Text("7 дней") },
                colors = chipColors
            )
            FilterChip(
                selected = range == Range.D31,
                onClick = { range = Range.D31 },
                label = { Text("31 день") },
                colors = chipColors
            )
            FilterChip(
                selected = range == Range.D365,
                onClick = { range = Range.D365 },
                label = { Text("Год") },
                colors = chipColors
            )
        }

        Text(
            text = "Срез",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = part == Part.ALL,
                onClick = { part = Part.ALL },
                label = { Text("Все") },
                colors = chipColors
            )
            FilterChip(
                selected = part == Part.MORNING,
                onClick = { part = Part.MORNING },
                label = { Text("Утро") },
                colors = chipColors
            )
            FilterChip(
                selected = part == Part.EVENING,
                onClick = { part = Part.EVENING },
                label = { Text("Вечер") },
                colors = chipColors
            )
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
                        selected = exercise == ex,
                        onClick = { exercise = ex },
                        label = { Text(ex) },
                        colors = chipColors
                    )
                }
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Динамика",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (range == Range.D365) {
                        "По месяцам за последний год"
                    } else {
                        "По дням за выбранный период"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                BarChart(
                    values = totals,
                    xKeys = xKeys,
                    range = range,
                    barColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }
        }

        Text(
            text = "Позже сюда можно добавить детализацию по нажатию на столбик.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AnalyticsStat(
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
private fun BarChart(
    values: List<Int>,
    xKeys: List<String>,
    range: Range,
    barColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    val maxV = (values.maxOrNull() ?: 0).coerceAtLeast(1)

    Canvas(modifier = modifier) {
        val n = values.size.coerceAtLeast(1)

        val leftAxisArea = 42f
        val gap = if (range == Range.D365) 10f else 6f
        val labelArea = 28f
        val chartH = (size.height - labelArea).coerceAtLeast(1f)
        val chartW = (size.width - leftAxisArea).coerceAtLeast(1f)
        val barW = ((chartW - gap * (n - 1)) / n).coerceAtLeast(1f)

        val gridLines = 4

        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = labelColor.copy(alpha = 0.75f).toArgb()
            textSize = 20f
            textAlign = android.graphics.Paint.Align.LEFT
        }

        for (i in 0..gridLines) {
            val value = maxV * i / gridLines
            val y = chartH - (chartH * i.toFloat() / gridLines.toFloat())

            drawLine(
                color = labelColor.copy(alpha = 0.18f),
                start = Offset(leftAxisArea, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    value.toString(),
                    4f,
                    y - 4f,
                    textPaint
                )
            }
        }

        values.forEachIndexed { i, v ->
            val h = (v.toFloat() / maxV.toFloat()) * chartH
            val x = leftAxisArea + i * (barW + gap)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, chartH - h),
                size = Size(barW, h),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }

        val labels = buildXAxisLabels(xKeys, range)

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = labelColor.toArgb()
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val y = chartH + 22f

            labels.forEachIndexed { i, text ->
                if (text.isNullOrBlank()) return@forEachIndexed

                val xCenter = leftAxisArea + i * (barW + gap) + barW / 2f

                canvas.nativeCanvas.drawText(
                    text,
                    xCenter,
                    y,
                    paint
                )
            }
        }
    }
}

private fun dayKey(timestampMs: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = timestampMs }
    val y = c.get(Calendar.YEAR)
    val m = c.get(Calendar.MONTH) + 1
    val d = c.get(Calendar.DAY_OF_MONTH)
    return "%04d-%02d-%02d".format(y, m, d)
}

private fun monthKey(timestampMs: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = timestampMs }
    val y = c.get(Calendar.YEAR)
    val m = c.get(Calendar.MONTH) + 1
    return "%04d-%02d".format(y, m)
}

private fun buildLastDays(days: Int): List<String> {
    val list = ArrayList<String>(days)
    val c = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    c.add(Calendar.DAY_OF_YEAR, -(days - 1))
    repeat(days) {
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val d = c.get(Calendar.DAY_OF_MONTH)
        list.add("%04d-%02d-%02d".format(y, m, d))
        c.add(Calendar.DAY_OF_YEAR, 1)
    }
    return list
}

private fun buildLastMonths(months: Int): List<String> {
    val list = ArrayList<String>(months)
    val c = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    c.add(Calendar.MONTH, -(months - 1))
    repeat(months) {
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        list.add("%04d-%02d".format(y, m))
        c.add(Calendar.MONTH, 1)
    }
    return list
}

private fun buildXAxisLabels(xKeys: List<String>, range: Range): List<String?> =
    when (range) {
        Range.D7 -> xKeys.map { weekdayShort(it) }
        Range.D31 -> xKeys.mapIndexed { i, key ->
            val day = key.takeLast(2)
            if (i == 0 || i == xKeys.lastIndex || i % 5 == 0) day else null
        }
        Range.D365 -> xKeys.mapIndexed { i, key ->
            val mm = key.takeLast(2)
            if (i == 0 || i == xKeys.lastIndex || i % 2 == 0) monthShort(mm) else null
        }
    }

private fun weekdayShort(key: String): String {
    val y = key.substring(0, 4).toInt()
    val m = key.substring(5, 7).toInt()
    val d = key.substring(8, 10).toInt()
    val c = Calendar.getInstance().apply {
        set(Calendar.YEAR, y)
        set(Calendar.MONTH, m - 1)
        set(Calendar.DAY_OF_MONTH, d)
    }
    return when (c.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Пн"
        Calendar.TUESDAY -> "Вт"
        Calendar.WEDNESDAY -> "Ср"
        Calendar.THURSDAY -> "Чт"
        Calendar.FRIDAY -> "Пт"
        Calendar.SATURDAY -> "Сб"
        Calendar.SUNDAY -> "Вс"
        else -> ""
    }
}

private fun monthShort(mm: String): String = when (mm) {
    "01" -> "янв"
    "02" -> "фев"
    "03" -> "мар"
    "04" -> "апр"
    "05" -> "май"
    "06" -> "июн"
    "07" -> "июл"
    "08" -> "авг"
    "09" -> "сен"
    "10" -> "окт"
    "11" -> "ноя"
    "12" -> "дек"
    else -> ""
}