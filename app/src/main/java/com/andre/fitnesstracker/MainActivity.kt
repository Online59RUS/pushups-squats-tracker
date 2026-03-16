package com.andre.fitnesstracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

        setContent {
            FitnessTrackerTheme {

                var showSplash by rememberSaveable { mutableStateOf(true) }
                var showWhatsNew by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(700)
                    showSplash = false
                    showWhatsNew = AppUpdatePrefs.shouldShowWhatsNew(
                        context = this@MainActivity,
                        currentVersion = BuildConfig.VERSION_NAME
                    )
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    GradientBackground {
                        val vm: MainViewModel = viewModel()
                        val nav = rememberNavController()

                        LaunchedEffect(Unit) {
                            vm.rescheduleReminders()
                        }

                        val items = listOf(
                            "today" to "Сегодня",
                            "history" to "История",
                            "badges" to "Сила",
                            "analytics" to "Аналитика",
                            "settings" to "Настройки"
                        )

                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface
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
                                composable("badges") { StrengthScreen(vm) }
                                composable("analytics") { AnalyticsScreen(vm) }
                                composable("settings") { SettingsScreen(vm) }
                            }
                        }

                        if (showWhatsNew) {
                            WhatsNewDialog(
                                versionName = BuildConfig.VERSION_NAME,
                                onDismiss = {
                                    AppUpdatePrefs.markWhatsNewSeen(
                                        context = this@MainActivity,
                                        currentVersion = BuildConfig.VERSION_NAME
                                    )
                                    showWhatsNew = false
                                }
                            )
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

@androidx.compose.runtime.Composable
private fun WhatsNewDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Что нового в $versionName")
        },
        text = {
            Text(
                "🔥 Система силы\n" +
                        "Теперь приложение повышает уровень за серию тренировок.\n\n" +
                        "📈 Новый экран силы\n" +
                        "Появились уровни, прогресс и путь развития.\n\n" +
                        "📅 Обновлён главный экран\n" +
                        "Статистика дня стала чище и понятнее.\n\n" +
                        "📊 Улучшена аналитика\n" +
                        "Добавлены сетка и шкала для удобного чтения графиков."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Начать")
            }
        }
    )
}