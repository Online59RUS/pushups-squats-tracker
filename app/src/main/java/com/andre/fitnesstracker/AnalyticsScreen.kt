package com.andre.fitnesstracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard
import java.util.Calendar

private enum class Range(val days: Int) { D7(7), D31(31), D365(365) }
private enum class Part { ALL, MORNING, EVENING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(vm: MainViewModel) {
    val ui = vm.ui.collectAsState().value

    var range by remember { mutableStateOf(Range.D31) }
    var exercise by remember { mutableStateOf(ui.selectedExercise) }
    var part by remember { mutableStateOf(Part.ALL) }

    LaunchedEffect(ui.selectedExercise) { exercise = ui.selectedExercise }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        selectedLabelColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    // X-ось:
    // - 7/31: список дней
    // - год: список месяцев
    val xKeys: List<String> = remember(range) {
        if (range == Range.D365) buildLastMonths(12) else buildLastDays(range.days)
    }

    val totals: List<Int> = remember(ui.entries, exercise, range, part) {
        val map = mutableMapOf<String, Int>()
        xKeys.forEach { map[it] = 0 }

        ui.entries.forEach { e ->
            if (e.exercise != exercise) return@forEach
            if (part == Part.MORNING && e.session != "Утро") return@forEach
            if (part == Part.EVENING && e.session != "Вечер") return@forEach

            val key = if (range == Range.D365) monthKey(e.timestampMs) else dayKey(e.timestampMs)
            if (map.containsKey(key)) map[key] = (map[key] ?: 0) + e.amount
        }

        xKeys.map { k -> map[k] ?: 0 }
    }

    val sum = totals.sum()
    val maxV = totals.maxOrNull() ?: 0
    val avg = if (totals.isEmpty()) 0.0 else sum.toDouble() / totals.size.toDouble()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Аналитика",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = range == Range.D7, onClick = { range = Range.D7 }, label = { Text("7 дней") }, colors = chipColors)
            FilterChip(selected = range == Range.D31, onClick = { range = Range.D31 }, label = { Text("31 день") }, colors = chipColors)
            FilterChip(selected = range == Range.D365, onClick = { range = Range.D365 }, label = { Text("Год") }, colors = chipColors)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = part == Part.ALL, onClick = { part = Part.ALL }, label = { Text("Все") }, colors = chipColors)
            FilterChip(selected = part == Part.MORNING, onClick = { part = Part.MORNING }, label = { Text("Утро") }, colors = chipColors)
            FilterChip(selected = part == Part.EVENING, onClick = { part = Part.EVENING }, label = { Text("Вечер") }, colors = chipColors)
        }

        Text("Упражнение", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ui.exercises.forEach { ex ->
                FilterChip(selected = exercise == ex, onClick = { exercise = ex }, label = { Text(ex) }, colors = chipColors)
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            val partTitle = when (part) {
                Part.ALL -> "Все"
                Part.MORNING -> "Утро"
                Part.EVENING -> "Вечер"
            }
            Text("Сумма за период ($partTitle): $sum", color = MaterialTheme.colorScheme.onBackground)
            Text("Максимум (${if (range == Range.D365) "за месяц" else "за день"}): $maxV", color = MaterialTheme.colorScheme.onBackground)
            Text("Среднее (${if (range == Range.D365) "в месяц" else "в день"}): ${"%.1f".format(avg)}", color = MaterialTheme.colorScheme.onBackground)
        }

        BarChart(
            values = totals,
            xKeys = xKeys,
            range = range,
            barColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().height(240.dp)
        )

        Text(
            "Тап по столбику добавим позже.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // Для "Год" делаем gap меньше, чтобы было плотнее и красивее
        val gap = if (range == Range.D365) 10f else 6f
        val labelArea = 28f
        val chartH = (size.height - labelArea).coerceAtLeast(1f)
        val barW = ((size.width - gap * (n - 1)) / n).coerceAtLeast(1f)

        values.forEachIndexed { i, v ->
            val h = (v.toFloat() / maxV.toFloat()) * chartH
            val x = i * (barW + gap)
            drawRect(color = barColor, topLeft = Offset(x, chartH - h), size = Size(barW, h))
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
                val xCenter = i * (barW + gap) + barW / 2f
                canvas.nativeCanvas.drawText(text, xCenter, y, paint)
            }
        }
    }
}

// ---------- keys ----------
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

// ---------- labels ----------
private fun buildXAxisLabels(xKeys: List<String>, range: Range): List<String?> =
    when (range) {
        Range.D7 -> xKeys.map { weekdayShort(it) }
        Range.D31 -> xKeys.mapIndexed { i, key ->
            val day = key.takeLast(2)
            if (i == 0 || i == xKeys.lastIndex || i % 5 == 0) day else null
        }
        Range.D365 -> xKeys.mapIndexed { i, key ->
            // key = YYYY-MM
            val mm = key.takeLast(2)
            // показываем подписи разреженно, чтобы не было каши
            if (i == 0 || i == xKeys.lastIndex || i % 2 == 0) monthShort(mm) else null
        }
    }

private fun weekdayShort(key: String): String {
    // key = YYYY-MM-DD
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
