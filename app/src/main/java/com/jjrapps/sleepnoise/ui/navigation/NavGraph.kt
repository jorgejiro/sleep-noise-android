package com.jjrapps.sleepnoise.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jjrapps.sleepnoise.ui.changelog.ChangelogScreen
import com.jjrapps.sleepnoise.ui.player.PlayerScreen
import com.jjrapps.sleepnoise.ui.settings.SettingsScreen

@Composable
fun SleepNoiseNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Player.route
    ) {
        composable(Screen.Player.route) {
            PlayerScreen(onOpenSettings = { navController.navigate(Screen.Settings.route) })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenChangelog = { navController.navigate(Screen.Changelog.route) }
            )
        }
        composable(Screen.Changelog.route) {
            ChangelogScreen(onBack = { navController.popBackStack() })
        }
    }
}
