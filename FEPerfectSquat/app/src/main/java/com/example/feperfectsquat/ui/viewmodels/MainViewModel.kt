package com.example.feperfectsquat.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feperfectsquat.models.AnalysisSession
import com.example.feperfectsquat.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val apiService = RetrofitClient.apiService

    data class UiState(
        val isAnalyzing: Boolean = false,
        val analysisProgress: String = "",
        val analysisResult: AnalysisSession? = null,
        val processedVideoUri: Uri? = null,
        val errorMessage: String? = null,
        val sessionId: Long? = null
    )

    fun analyzeVideo(context: Context, videoUri: Uri, weightKg: Double?) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isAnalyzing = true, errorMessage = null, analysisProgress = "Creando sesión...") }

                // 1. Crear sesión
                val session = apiService.createSession()
                val sessionId = session.id
                _uiState.update { it.copy(sessionId = sessionId, analysisProgress = "Subiendo video...") }

                // 2. Preparar archivo para subida
                val filePart = prepareVideoPart(context, videoUri)
                val weightPart = weightKg?.toString()
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Subir video y esperar análisis
                _uiState.update { it.copy(analysisProgress = "Analizando video (esto puede tardar)...") }
                val analysisResult = apiService.uploadVideo(sessionId, filePart, weightPart)

                // 4. Descargar video procesado
                _uiState.update { it.copy(analysisProgress = "Descargando video procesado...") }
                val processedVideoUri = downloadProcessedVideo(context, sessionId)

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analysisResult = analysisResult,
                        processedVideoUri = processedVideoUri,
                        analysisProgress = "Completado"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Error: ${e.message}",
                        analysisProgress = ""
                    )
                }
            }
        }
    }

    private suspend fun prepareVideoPart(context: Context, videoUri: Uri): MultipartBody.Part {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(videoUri)
                ?: throw Exception("No se pudo abrir el video")
            val bytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = bytes.toRequestBody("video/mp4".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", "squat_video.mp4", requestBody)
        }
    }

    private suspend fun downloadProcessedVideo(context: Context, sessionId: Long): Uri {
        return withContext(Dispatchers.IO) {
            val responseBody = apiService.downloadProcessedVideo(sessionId)
            val file = File(context.cacheDir, "processed_video_${sessionId}.mp4")
            file.outputStream().use { output ->
                responseBody.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        }
    }

    fun clearAnalysis() {
        _uiState.update {
            UiState()
        }
    }
}
