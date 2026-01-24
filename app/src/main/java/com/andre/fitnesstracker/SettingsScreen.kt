package com.andre.fitnesstracker

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current

    // Проверка разрешения "Будильники и напоминания" (Exact alarms)
    val alarmManager = remember { ctx.getSystemService(AlarmManager::class.java) }
    var exactAllowed by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        exactAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // ---- Профиль ----
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                text = "Профиль",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = ui.userName,
                onValueChange = { vm.setUserName(it) },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = ui.goalPushups.toString(),
                onValueChange = { v ->
                    val parsed = v.filter(Char::isDigit).toIntOrNull()
                    if (parsed != null) vm.setGoalPushups(parsed)
                },
                label = { Text("Цель Отжимания/день") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = ui.goalSquats.toString(),
                onValueChange = { v ->
                    val parsed = v.filter(Char::isDigit).toIntOrNull()
                    if (parsed != null) vm.setGoalSquats(parsed)
                },
                label = { Text("Цель Приседания/день") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // ---- Напоминания ----
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                text = "Напоминания",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Утро: %02d:%02d".format(ui.morningHour, ui.morningMin),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        TimePickerDialog(
                            ctx,
                            { _, h, m -> vm.setMorningTime(h, m) },
                            ui.morningHour,
                            ui.morningMin,
                            true
                        ).show()
                    }
                ) { Text("Изменить") }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Вечер: %02d:%02d".format(ui.eveningHour, ui.eveningMin),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        TimePickerDialog(
                            ctx,
                            { _, h, m -> vm.setEveningTime(h, m) },
                            ui.eveningHour,
                            ui.eveningMin,
                            true
                        ).show()
                    }
                ) { Text("Изменить") }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "После изменения времени напоминания пересоздаются автоматически.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            // Подсказка, если разрешение Exact alarms выключено
            if (!exactAllowed) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Чтобы напоминания приходили точно по времени, включи разрешение \"Будильники и напоминания\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            try {
                                ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            } catch (_: Exception) {
                                ctx.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:${ctx.packageName}")
                                    }
                                )
                            }
                        } else {
                            ctx.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${ctx.packageName}")
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Включить будильники и напоминания")
                }
            }
        }

        // ---- Версия внизу экрана (вне карточек) ----
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Версия: ${BuildConfig.VERSION_NAME} ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
