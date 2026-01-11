package com.andre.fitnesstracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard
import com.andre.fitnesstracker.BuildConfig

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()

    // Имя
    var name by remember(ui.userName) {
        mutableStateOf(ui.userName)
    }

    // Цели — независимые поля
    var pushupsGoalText by remember(ui.goalPushups) {
        mutableStateOf(ui.goalPushups.toString())
    }

    var squatsGoalText by remember(ui.goalSquats) {
        mutableStateOf(ui.goalSquats.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // -------- ИМЯ --------
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                text = "Имя",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Введите имя") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { vm.setUserName(name.trim()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить имя")
            }
        }

        // -------- ЦЕЛИ --------
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                text = "Цели на день",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(12.dp))

            // Отжимания
            Text(
                text = "Отжимания",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = pushupsGoalText,
                onValueChange = { pushupsGoalText = it.filter(Char::isDigit).take(5) },
                singleLine = true,
                label = { Text("Например: 50") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    vm.setGoalPushups(pushupsGoalText.toIntOrNull() ?: 0)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить цель для отжиманий")
            }

            Spacer(Modifier.height(16.dp))

            // Приседания
            Text(
                text = "Приседания",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = squatsGoalText,
                onValueChange = { squatsGoalText = it.filter(Char::isDigit).take(5) },
                singleLine = true,
                label = { Text("Например: 50") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    vm.setGoalSquats(squatsGoalText.toIntOrNull() ?: 0)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить цель для приседаний")
            }
        }

        Spacer(Modifier.height(12.dp))

        // -------- ВЕРСИЯ --------
        Text(
            text = "Версия: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
