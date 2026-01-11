package com.andre.fitnesstracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andre.fitnesstracker.ui.theme.GlassCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(vm: MainViewModel) {
    val ui by vm.ui.collectAsState()
    val fmt = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "История",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ui.entries) { e ->
                val dt = fmt.format(Date(e.timestampMs))
                GlassCard(Modifier.fillMaxWidth()) {
                    Text(
                        "${e.exercise} - ${e.amount}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "$dt  •  ${e.session ?: "Без метки"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
