package com.example.feperfectsquat.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.feperfectsquat.models.ExerciseType
import com.example.feperfectsquat.ui.viewmodels.MainViewModel
import com.example.feperfectsquat.video.MIN_RECOMMENDED_FPS
import com.example.feperfectsquat.video.readVideoFps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartAnalysisScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onAnalysisComplete: () -> Unit,
) {
    val context = LocalContext.current
    var weightInput by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var exerciseType by remember { mutableStateOf(ExerciseType.SQUAT) }
    var bodyWeightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var selectedVideoFps by remember { mutableStateOf<Float?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    // Navegar a resultados cuando el análisis termina
    LaunchedEffect(uiState.processedVideoUri) {
        if (uiState.processedVideoUri != null && !uiState.isAnalyzing) {
            onAnalysisComplete()
        }
    }

    // FPS del vídeo elegido, para avisar antes de subirlo
    LaunchedEffect(selectedVideoUri) {
        selectedVideoFps = selectedVideoUri?.let { uri ->
            withContext(Dispatchers.IO) { readVideoFps(context, uri) }
        }
    }

    val isJump = exerciseType == ExerciseType.VERTICAL_JUMP
    val bodyWeight = bodyWeightInput.toDoubleOrNull()
    val height = heightInput.toDoubleOrNull()
    // El salto necesita peso corporal (potencia) y altura (escala de píxeles a centímetros)
    val canAnalyze = !isJump || (bodyWeight != null && height != null)

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        selectedVideoUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar Análisis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !uiState.isAnalyzing) {
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isAnalyzing) {
                // Estado: analizando
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = uiState.analysisProgress,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (uiState.errorMessage != null) {
                // Estado: error
                Text(
                    text = uiState.errorMessage ?: "Error desconocido",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.clearAnalysis() }) {
                    Text("Reintentar")
                }
            } else {
                // Estado: selección normal
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isJump,
                        onClick = { exerciseType = ExerciseType.SQUAT },
                        label = { Text("Sentadilla") }
                    )
                    FilterChip(
                        selected = isJump,
                        onClick = { exerciseType = ExerciseType.VERTICAL_JUMP },
                        label = { Text("Salto vertical") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isJump) {
                    OutlinedTextField(
                        value = bodyWeightInput,
                        onValueChange = { bodyWeightInput = it },
                        label = { Text("Peso corporal (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Altura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FpsWarningCard(selectedVideoFps)
                } else {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Introduce peso utilizado (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (selectedVideoUri != null) {
                    Text(
                        text = "Video seleccionado ✓",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            selectedVideoUri?.let { uri ->
                                if (isJump) {
                                    viewModel.analyzeVideo(context, uri, exerciseType, null, bodyWeight, height)
                                } else {
                                    viewModel.analyzeVideo(context, uri, exerciseType, weightInput.toDoubleOrNull(), null, null)
                                }
                            }
                        },
                        enabled = canAnalyze,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Analizar")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { selectedVideoUri = null }) {
                        Text("Seleccionar otro video")
                    }
                } else {
                    Button(
                        onClick = { videoPickerLauncher.launch("video/mp4") },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Cargar vídeo")
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FpsWarningCard(videoFps: Float?) {
    val isLowFps = videoFps != null && videoFps < MIN_RECOMMENDED_FPS
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowFps) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Graba el salto a ${MIN_RECOMMENDED_FPS.toInt()} fps o más",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isLowFps) {
                    "Este vídeo está a ${videoFps!!.toInt()} fps: la altura del salto puede tener un error de varios centímetros."
                } else {
                    "La altura se calcula con el tiempo de vuelo: cuantos más fps, más precisa."
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
