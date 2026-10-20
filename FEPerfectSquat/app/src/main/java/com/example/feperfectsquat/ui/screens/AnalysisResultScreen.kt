package com.example.feperfectsquat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.feperfectsquat.models.AnalysisSession
import com.example.feperfectsquat.models.ExerciseType
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.feperfectsquat.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Inicializar ExoPlayer con el video procesado
    val exoPlayer = remember(uiState.processedVideoUri) {
        ExoPlayer.Builder(context).build().apply {
            uiState.processedVideoUri?.let {
                val mediaItem = MediaItem.fromUri(it)
                setMediaItem(mediaItem)
                prepare()
            }
        }
    }

    // Gestionar el ciclo de vida del player
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados del Análisis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reproductor de video procesado (con esqueleto)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.processedVideoUri != null) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "No se ha podido cargar el video procesado",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Métricas del análisis
            uiState.analysisResult?.let { result ->
                val isJump = result.exerciseType == ExerciseType.VERTICAL_JUMP

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resumen del Análisis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isJump) {
                            JumpSummaryRows(result)
                        } else {
                            MetricRow("Repeticiones válidas", "${result.validReps ?: 0} / ${result.totalReps ?: 0}")

                            result.avgConcentricVelocity?.let {
                                MetricRow("Velocidad concéntrica media", String.format("%.2f m/s", it))
                            }

                            result.estimatedRM?.let {
                                MetricRow("RM estimado (Epley)", String.format("%.1f kg", it))
                            }

                            result.weightKg?.let {
                                MetricRow("Peso utilizado", String.format("%.1f kg", it))
                            }
                        }

                        result.fatigueIndex?.let {
                            MetricRow("Índice de fatiga", String.format("%.1f%%", it * 100))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (result.lowFpsWarning == true) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "⚠️ Vídeo a ${result.videoFps?.toInt() ?: "?"} fps: graba a 60 fps o más para que la altura sea precisa.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isJump && !result.jumps.isNullOrEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Saltos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            result.jumps.forEach { jump ->
                                val depth = jump.countermovementDepthCm?.let { String.format(" · bajada %.0f cm", it) } ?: ""
                                MetricRow(
                                    "Salto ${jump.jumpNumber}",
                                    String.format("%.1f cm · %.2f s", jump.heightFlightCm ?: 0.0, jump.flightTime ?: 0.0) + depth
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!isJump && !result.reps.isNullOrEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Repeticiones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            result.reps.forEach { rep ->
                                val validity = if (rep.valid) "válida" else "no válida"
                                val knee = rep.minKneeAngle?.let { String.format(" · rodilla %.0f°", it) } ?: ""
                                MetricRow("Repetición ${rep.repNumber}", validity + knee)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Estado de la sesión
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (result.state) {
                            "FINISHED" -> MaterialTheme.colorScheme.secondaryContainer
                            "ERROR" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (result.state) {
                                "FINISHED" -> "✅ Completado"
                                "ERROR" -> "❌ Error"
                                "PROCESSING" -> "⏳ Procesando"
                                else -> result.state
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones inferiores
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al menú")
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun JumpSummaryRows(result: AnalysisSession) {
    MetricRow("Saltos detectados", "${result.totalJumps ?: 0}")
    result.bestJumpHeightCm?.let {
        MetricRow("Mejor salto", String.format("%.1f cm", it))
    }
    result.avgJumpHeightCm?.let {
        MetricRow("Altura media", String.format("%.1f cm", it))
    }
    result.peakPowerW?.let {
        MetricRow("Potencia pico (Sayers)", String.format("%.0f W", it))
    }
    result.bodyWeightKg?.let {
        MetricRow("Peso corporal", String.format("%.1f kg", it))
    }
    result.heightCm?.let {
        MetricRow("Altura", String.format("%.0f cm", it))
    }
}
