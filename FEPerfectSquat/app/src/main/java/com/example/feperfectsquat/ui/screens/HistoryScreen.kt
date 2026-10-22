package com.example.feperfectsquat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.feperfectsquat.models.AnalysisSession
import com.example.feperfectsquat.models.ExerciseType
import com.example.feperfectsquat.ui.viewmodels.SessionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: SessionsViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.errorMessage != null -> Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                uiState.sessions.isEmpty() -> Text(
                    text = "Todavía no hay análisis",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.sessions, key = { it.id }) { session ->
                        SessionCard(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: AnalysisSession) {
    val isJump = session.exerciseType == ExerciseType.VERTICAL_JUMP
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isJump) "Salto vertical" else "Sentadilla",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${session.createdAt?.take(16)?.replace('T', ' ') ?: "-"} · ${session.state}",
                style = MaterialTheme.typography.bodySmall
            )
            if (session.state == "FINISHED") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isJump) jumpSummary(session) else squatSummary(session),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun squatSummary(session: AnalysisSession): String {
    val reps = "${session.validReps ?: 0}/${session.totalReps ?: 0} repeticiones válidas"
    val weight = session.weightKg?.let { String.format(" · %.1f kg", it) } ?: ""
    return reps + weight
}

private fun jumpSummary(session: AnalysisSession): String {
    val jumps = "${session.totalJumps ?: 0} saltos"
    val best = session.bestJumpHeightCm?.let { String.format(" · mejor %.1f cm", it) } ?: ""
    return jumps + best
}
