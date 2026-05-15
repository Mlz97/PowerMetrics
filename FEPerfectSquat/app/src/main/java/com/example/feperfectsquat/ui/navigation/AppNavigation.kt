package com.example.feperfectsquat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feperfectsquat.ui.screens.*
import com.example.feperfectsquat.ui.viewmodels.MainViewModel

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
    val mainViewModel: MainViewModel = viewModel()

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
                viewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() },
                onAnalysisComplete = {
                    navController.navigate(NavDestinations.ANALYSIS_RESULT) {
                        popUpTo(NavDestinations.START_ANALYSIS) { inclusive = true }
                    }
                }
            )
        }
        composable(NavDestinations.ANALYSIS_RESULT) {
            AnalysisResultScreen(
                viewModel = mainViewModel,
                onNavigateBack = {
                    mainViewModel.clearAnalysis()
                    navController.popBackStack(NavDestinations.MAIN_MENU, inclusive = false)
                }
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
