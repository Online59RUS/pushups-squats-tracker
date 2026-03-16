package com.andre.fitnesstracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard

@Composable
fun StrengthScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()

    val level = vm.currentStrengthLevel()
    val progressPair = vm.currentStrengthProgress()
    val currentDays = progressPair.first
    val targetDays = progressPair.second
    val progress =
        if (targetDays <= 0) 0f
        else (currentDays.toFloat() / targetDays.toFloat()).coerceIn(0f, 1f)
    val daysToNext = vm.daysToNextLevel()

    val showDialog = remember { mutableStateOf(false) }
    val dialogTitle = remember { mutableStateOf("") }
    val dialogText = remember { mutableStateOf("") }

    LaunchedEffect(level.name) {
        val levelUp = vm.checkLevelUp()
        if (levelUp != null) {
            val msg = levelMessage(levelUp.name)
            if (msg.first.isNotBlank()) {
                dialogTitle.value = msg.first
                dialogText.value = msg.second
                showDialog.value = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Сила",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        GlassCard(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(level.iconRes),
                        contentDescription = level.name,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 0.18f
                    )

                    Image(
                        painter = painterResource(level.iconRes),
                        contentDescription = level.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                val clipTop = size.height * (1f - progress)
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
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )

                if (level.toDays == Int.MAX_VALUE) {
                    Text(
                        text = "Максимальный уровень",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "$currentDays / $targetDays дней",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Текущая серия: ${ui.streakDays} дней",
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (daysToNext > 0) {
                    Text(
                        text = "До следующего уровня: $daysToNext дней",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = "Путь силы",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        strengthLevels.forEach { lvl ->
            val unlocked = ui.streakDays >= lvl.fromDays
            val isCurrent = lvl.name == level.name

            GlassCard(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(lvl.iconRes),
                        contentDescription = lvl.name,
                        modifier = Modifier.size(56.dp),
                        alpha = if (unlocked) 1f else 0.28f
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = lvl.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (lvl.toDays == Int.MAX_VALUE) {
                                "${lvl.fromDays}+ дней серии"
                            } else {
                                "${lvl.fromDays}-${lvl.toDays} дней серии"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = when {
                            isCurrent -> "Текущий"
                            unlocked -> "Открыт"
                            else -> "Закрыт"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(dialogTitle.value) },
            text = { Text(dialogText.value) },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Круто!")
                }
            }
        )
    }
}