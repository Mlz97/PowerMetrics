package com.example.feperfectsquat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feperfectsquat.models.AnalysisSession
import com.example.feperfectsquat.models.SessionStats
import com.example.feperfectsquat.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Session list for the history and statistics screens (GET /session). */
class SessionsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val apiService = RetrofitClient.apiService

    data class UiState(
        val isLoading: Boolean = false,
        val sessions: List<AnalysisSession> = emptyList(),
        val stats: SessionStats = SessionStats.from(emptyList()),
        val errorMessage: String? = null
    )

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val sessions = apiService.listSessions()
                _uiState.update {
                    it.copy(isLoading = false, sessions = sessions, stats = SessionStats.from(sessions))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${e.message}") }
            }
        }
    }
}
