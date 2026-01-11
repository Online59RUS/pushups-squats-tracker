package com.andre.fitnesstracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard

@Composable
fun BadgesScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Медали",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(ui.exercises) { ex ->
                val total = ui.totals[ex] ?: 0
                val badges = vm.achievementsFor(ex)

                GlassCard(Modifier.fillMaxWidth()) {
                    Text(ex, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Text("Всего: $total", color = MaterialTheme.colorScheme.onBackground)

                    badges.forEach { a ->
                        val label =
                            if (a.achieved) "🎖 ${a.title} (${a.threshold})"
                            else "🔒 ${a.title} (${a.threshold})"

                        Text(label, color = MaterialTheme.colorScheme.onBackground)

                        LinearProgressIndicator(
                            progress = {
                                (a.progress.toFloat() / a.threshold.toFloat()).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
