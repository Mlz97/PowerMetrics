package com.example.feperfectsquat.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feperfectsquat.data.CommunityRepository
import com.example.feperfectsquat.data.SampleCommunityRepository
import com.example.feperfectsquat.models.CommunityMetric
import com.example.feperfectsquat.models.ExerciseType
import com.example.feperfectsquat.models.HomeStats
import com.example.feperfectsquat.models.UserProfile
import com.example.feperfectsquat.models.lastFinishedSession
import com.example.feperfectsquat.ui.screens.AppShell
import com.example.feperfectsquat.ui.screens.AppTab
import com.example.feperfectsquat.ui.screens.home.CommunityTeaser
import com.example.feperfectsquat.ui.screens.home.HomeScreen
import com.example.feperfectsquat.ui.screens.onboarding.ProfileScreen
import com.example.feperfectsquat.ui.screens.onboarding.WelcomeScreen
import com.example.feperfectsquat.ui.theme.PmTextMuted
import com.example.feperfectsquat.ui.viewmodels.ProfileViewModel
import com.example.feperfectsquat.ui.viewmodels.SessionsViewModel
import java.util.Date

object NavDestinations {
    const val WELCOME = "welcome"
    const val PROFILE = "profile"
    const val APP = "app"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()
    // Onboarding only runs until a profile is saved on the device
    val startDestination = remember {
        if (profileViewModel.profile.value == null) NavDestinations.WELCOME else NavDestinations.APP
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
                        navController.navigate(NavDestinations.APP) {
                            popUpTo(NavDestinations.WELCOME) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(NavDestinations.APP) {
            val profile by profileViewModel.profile.collectAsState()
            profile?.let { AppTabs(it) }
        }
    }
}

@Composable
private fun AppTabs(
    profile: UserProfile,
    community: CommunityRepository = SampleCommunityRepository,
) {
    val sessionsViewModel: SessionsViewModel = viewModel()
    val sessionsState by sessionsViewModel.uiState.collectAsState()
    var tab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var statsLift by rememberSaveable { mutableStateOf(ExerciseType.SQUAT) }

    LaunchedEffect(tab) {
        if (tab == AppTab.HOME || tab == AppTab.HISTORY) sessionsViewModel.loadSessions()
    }

    AppShell(tab = tab, onTabChange = { tab = it }) { current ->
        when (current) {
            AppTab.HOME -> HomeScreen(
                profile = profile,
                today = Date(),
                stats = HomeStats.from(sessionsState.sessions, Date()),
                lastSession = lastFinishedSession(sessionsState.sessions),
                community = CommunityTeaser(
                    rank = community.yourRank(CommunityMetric.LOAD),
                    members = community.memberCount(ExerciseType.SQUAT),
                    context = "83 kg class",
                    isSample = community.isSample
                ),
                statsLift = statsLift,
                onStatsLiftChange = { statsLift = it },
                onAnalyze = { tab = AppTab.ANALYZE },
                onHistory = { tab = AppTab.HISTORY },
                onCommunity = { tab = AppTab.COMMUNITY },
            )
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(current.label, color = PmTextMuted)
            }
        }
    }
}
