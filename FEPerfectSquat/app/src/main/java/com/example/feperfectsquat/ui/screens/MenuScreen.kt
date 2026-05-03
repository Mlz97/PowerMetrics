package com.example.feperfectsquat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onNavigateToAnalysis: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onExit: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PerfectSquat",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onNavigateToAnalysis,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Iniciar análisis")
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Histórico sentadillas")
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateToStatistics,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Estadísticas")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Salir")
            }
        }
    }
}
