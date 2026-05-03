package com.example.feperfectsquat.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen() {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PerfectSquat Analysis",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (selectedVideoUri != null) {
                Text(
                    text = "Video seleccionado correctamente",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* TODO: Iniciar subida con Retrofit */ }) {
                    Text("Subir y Analizar")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { selectedVideoUri = null }) {
                    Text("Seleccionar otro")
                }
            } else {
                Button(onClick = { videoPickerLauncher.launch("video/mp4") }) {
                    Text("Seleccionar Video")
                }
            }
        }
    }
}
