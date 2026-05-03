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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartAnalysisScreen(
    onNavigateBack: () -> Unit,
    onAnalysisStarted: () -> Unit,
) {
    var weightInput by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                    text = "Video seleccionado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAnalysisStarted,
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
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
