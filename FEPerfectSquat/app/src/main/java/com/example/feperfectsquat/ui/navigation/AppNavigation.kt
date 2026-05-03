package com.example.feperfectsquat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feperfectsquat.ui.screens.*

object NavDestinations {
    const val MAIN_MENU = "main_menu"
    const val START_ANALYSIS = "start_analysis"
    const val ANALYSIS_RESULT = "analysis_result"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavDestinations.MAIN_MENU) {
        composable(NavDestinations.MAIN_MENU) {
            MenuScreen(
                onNavigateToAnalysis = { navController.navigate(NavDestinations.START_ANALYSIS) },
                onNavigateToHistory = { navController.navigate(NavDestinations.HISTORY) },
                onNavigateToStatistics = { navController.navigate(NavDestinations.STATISTICS) },
                onExit = { /* TODO: Finish Activity */ }
            )
        }
        composable(NavDestinations.START_ANALYSIS) {
            StartAnalysisScreen(
                onNavigateBack = { navController.popBackStack() },
                onAnalysisStarted = { 
                    // Simularemos que va al resultado despues de cargar
                    navController.navigate(NavDestinations.ANALYSIS_RESULT) 
                }
            )
        }
        composable(NavDestinations.ANALYSIS_RESULT) {
            AnalysisResultScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavDestinations.HISTORY) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavDestinations.STATISTICS) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
