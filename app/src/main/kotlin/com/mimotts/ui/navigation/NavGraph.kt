package com.mimotts.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mimotts.ui.screen.settings.SettingsScreen
import com.mimotts.ui.screen.apikey.ApiKeyScreen

sealed class Screen(val route: String) {
    data object Settings : Screen("settings")
    data object ApiKey : Screen("api_key")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Settings.route) {
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        composable(Screen.ApiKey.route) {
            ApiKeyScreen(navController)
        }
    }
}
