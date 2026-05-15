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
import com.example.feperfectsquat.ui.viewmodels.MainViewModel

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
    val uiState by viewModel.uiState.collectAsState()

    // Navegar a resultados cuando el análisis termina
    LaunchedEffect(uiState.processedVideoUri) {
        if (uiState.processedVideoUri != null && !uiState.isAnalyzing) {
            onAnalysisComplete()
        }
    }

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
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Introduce peso utilizado (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
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
                            val weight = weightInput.toDoubleOrNull()
                            selectedVideoUri?.let { uri ->
                                viewModel.analyzeVideo(context, uri, weight)
                            }
                        },
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
