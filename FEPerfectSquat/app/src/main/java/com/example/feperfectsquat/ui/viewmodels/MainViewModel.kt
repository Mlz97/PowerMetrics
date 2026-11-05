package com.example.feperfectsquat.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feperfectsquat.models.AnalysisSession
import com.example.feperfectsquat.models.CreateSessionRequest
import com.example.feperfectsquat.models.ExerciseType
import com.example.feperfectsquat.network.ContentUriRequestBody
import com.example.feperfectsquat.network.RetrofitClient
import com.example.feperfectsquat.video.readVideoFps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/** Steps of one video analysis, in order. */
enum class AnalysisStage { IDLE, UPLOADING, ANALYZING, DOWNLOADING, DONE, ERROR }

/** Runs a video analysis against the backend: create session → upload → poll → download the tracked video. */
class MainViewModel : ViewModel() {
    private companion object {
        const val POLL_INTERVAL_MS = 2_000L
        const val ANALYSIS_TIMEOUT_MS = 10 * 60 * 1_000L
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val apiService = RetrofitClient.apiService
    private var job: Job? = null

    data class UiState(
        val stage: AnalysisStage = AnalysisStage.IDLE,
        val exerciseType: ExerciseType = ExerciseType.SQUAT,
        val fileName: String = "",
        /** Upload fraction (0..1); null while the stage has no measurable progress. */
        val uploadProgress: Float? = null,
        /** Frame rate of the chosen jump video, read before uploading so a low value can be flagged early. */
        val videoFps: Float? = null,
        val analysisResult: AnalysisSession? = null,
        val processedVideoUri: Uri? = null,
        val errorMessage: String? = null,
    ) {
        val isBusy: Boolean
            get() = stage == AnalysisStage.UPLOADING || stage == AnalysisStage.ANALYZING || stage == AnalysisStage.DOWNLOADING
    }

    fun analyzeVideo(
        context: Context,
        videoUri: Uri,
        exerciseType: ExerciseType,
        weightKg: Double?,
        bodyWeightKg: Double?,
        heightCm: Double?
    ) {
        if (_uiState.value.isBusy) return
        job = viewModelScope.launch {
            _uiState.value = UiState(
                stage = AnalysisStage.UPLOADING,
                exerciseType = exerciseType,
                fileName = displayName(context, videoUri),
                uploadProgress = 0f
            )
            try {
                if (exerciseType == ExerciseType.VERTICAL_JUMP) {
                    val fps = withContext(Dispatchers.IO) { readVideoFps(context, videoUri) }
                    _uiState.update { it.copy(videoFps = fps) }
                }
                val session = apiService.createSession(CreateSessionRequest(exerciseType))
                val filePart = prepareVideoPart(context, videoUri)
                // The backend answers 202 and analyzes in the background
                apiService.uploadVideo(
                    session.id, filePart, weightKg?.toTextPart(), bodyWeightKg?.toTextPart(), heightCm?.toTextPart()
                )

                _uiState.update { it.copy(stage = AnalysisStage.ANALYZING, uploadProgress = null) }
                val result = waitForAnalysis(session.id)

                _uiState.update { it.copy(stage = AnalysisStage.DOWNLOADING, analysisResult = result) }
                // The tracked video is a bonus: a failed download must not hide the metrics
                val processed = runCatching { downloadProcessedVideo(context, session.id) }.getOrNull()

                _uiState.update { it.copy(stage = AnalysisStage.DONE, processedVideoUri = processed) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(stage = AnalysisStage.ERROR, uploadProgress = null, errorMessage = e.message ?: "Something went wrong")
                }
            }
        }
    }

    private fun Double.toTextPart() = toString().toRequestBody("text/plain".toMediaTypeOrNull())

    private suspend fun waitForAnalysis(sessionId: Long): AnalysisSession {
        val deadline = System.currentTimeMillis() + ANALYSIS_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val session = apiService.getSession(sessionId)
            when (session.state) {
                "FINISHED" -> return session
                "ERROR" -> throw Exception("The video analysis failed. Check the filming tips and try again.")
            }
            delay(POLL_INTERVAL_MS)
        }
        throw Exception("The analysis is taking too long. Try again later.")
    }

    private fun prepareVideoPart(context: Context, videoUri: Uri): MultipartBody.Part {
        // The video is read while it is sent, never loaded whole in memory
        val requestBody = ContentUriRequestBody(context.contentResolver, videoUri, "video/mp4".toMediaTypeOrNull()) { sent ->
            _uiState.update { if (it.stage == AnalysisStage.UPLOADING) it.copy(uploadProgress = sent) else it }
        }
        return MultipartBody.Part.createFormData("file", "squat_video.mp4", requestBody)
    }

    private fun displayName(context: Context, uri: Uri): String =
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull() ?: uri.lastPathSegment ?: "video"

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
        job?.cancel()
        _uiState.value = UiState()
    }
}
