package com.andre.fitnesstracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.andre.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.andre.fitnesstracker.ui.theme.GradientBackground
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationsPermission()

        // Напоминания (до setContent)
        ReminderScheduler.scheduleDaily(
            context = this,
            uniqueName = "reminder_morning",
            hour = 8,
            minute = 0,
            session = "Утро"
        )

        ReminderScheduler.scheduleDaily(
            context = this,
            uniqueName = "reminder_evening",
            hour = 21,
            minute = 0,
            session = "Вечер"
        )

        setContent {
            FitnessTrackerTheme {

                var showSplash by rememberSaveable { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(1000) // 1 сек
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    GradientBackground {
                        val vm: MainViewModel = viewModel()
                        val nav = rememberNavController()

                        val items = listOf(
                            "today" to "Сегодня",
                            "history" to "История",
                            "badges" to "Медали",
                            "analytics" to "Аналитика",
                            "settings" to "Настройки"
                        )

                        Scaffold(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                ) {
                                    val currentBackStack by nav.currentBackStackEntryAsState()
                                    val route = currentBackStack?.destination?.route

                                    items.forEach { (r, title) ->
                                        NavigationBarItem(
                                            selected = route == r,
                                            onClick = {
                                                nav.navigate(r) {
                                                    popUpTo("today") { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            label = { Text(title) },
                                            icon = {}
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = nav,
                                startDestination = "today",
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable("today") { TodayScreen(vm) }
                                composable("history") { HistoryScreen(vm) }
                                composable("badges") { BadgesScreen(vm) }
                                composable("analytics") { AnalyticsScreen(vm) }
                                composable("settings") { SettingsScreen(vm) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
