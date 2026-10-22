package com.example.feperfectsquat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feperfectsquat.ui.screens.*
import com.example.feperfectsquat.ui.screens.onboarding.ProfileScreen
import com.example.feperfectsquat.ui.screens.onboarding.WelcomeScreen
import com.example.feperfectsquat.ui.viewmodels.MainViewModel
import com.example.feperfectsquat.ui.viewmodels.ProfileViewModel
import com.example.feperfectsquat.ui.viewmodels.SessionsViewModel

object NavDestinations {
    const val WELCOME = "welcome"
    const val PROFILE = "profile"
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
    val sessionsViewModel: SessionsViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    // Onboarding only runs until a profile is saved on the device
    val startDestination = remember {
        if (profileViewModel.profile.value == null) NavDestinations.WELCOME else NavDestinations.MAIN_MENU
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavDestinations.WELCOME) {
            WelcomeScreen(onFinish = { navController.navigate(NavDestinations.PROFILE) })
        }
        composable(NavDestinations.PROFILE) {
            val draft by profileViewModel.draft.collectAsState()
            ProfileScreen(
                draft = draft,
                onDraftChange = profileViewModel::updateDraft,
                onBack = { navController.popBackStack() },
                onContinue = {
                    if (profileViewModel.saveDraft()) {
                        navController.navigate(NavDestinations.MAIN_MENU) {
                            popUpTo(NavDestinations.WELCOME) { inclusive = true }
                        }
                    }
                }
            )
        }
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
                viewModel = sessionsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavDestinations.STATISTICS) {
            StatisticsScreen(
                viewModel = sessionsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
