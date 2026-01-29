package com.example.archeryapp.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.archeryapp.ArcheryApplication
import com.example.archeryapp.data.repository.SessionRepositoryImpl
import com.example.archeryapp.domain.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val activeSession: Session? = null,
    val hasActiveSession: Boolean = false,
    val isLoading: Boolean = true,
    val sessionNumberToday: Int = 0,
    val totalSessionsToday: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as ArcheryApplication).database
    private val sessionRepository = SessionRepositoryImpl(database.sessionDao())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    init {
        loadTodaysSession()
    }

    private fun loadTodaysSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Check for session created today
            val todayStart = LocalDate.now().atStartOfDay()
            val todayEnd = todayStart.plusDays(1)

            val todaySessions = sessionRepository.getSessionsByDateRange(todayStart, todayEnd)
            val mostRecent = todaySessions.maxByOrNull { it.date }

            val totalSessionsToday = todaySessions.size
            val sessionNumberToday = if (mostRecent != null) {
                val sorted = todaySessions.sortedBy { it.date }
                sorted.indexOfFirst { it.id == mostRecent.id } + 1
            } else 0

            _uiState.value = _uiState.value.copy(
                activeSession = mostRecent,
                hasActiveSession = mostRecent != null,
                isLoading = false,
                sessionNumberToday = sessionNumberToday,
                totalSessionsToday = totalSessionsToday
            )
            _activeSessionId.value = mostRecent?.id
        }
    }

    fun setActiveSession(sessionId: Long?) {
        viewModelScope.launch {
            if (sessionId != null) {
                val session = sessionRepository.getSessionById(sessionId)

                val todayStart = LocalDate.now().atStartOfDay()
                val todayEnd = todayStart.plusDays(1)
                val todaySessions = sessionRepository.getSessionsByDateRange(todayStart, todayEnd)
                val totalSessionsToday = todaySessions.size
                val sessionNumberToday = if (session != null) {
                    val sorted = todaySessions.sortedBy { it.date }
                    sorted.indexOfFirst { it.id == session.id } + 1
                } else 0

                _uiState.value = _uiState.value.copy(
                    activeSession = session,
                    hasActiveSession = session != null,
                    sessionNumberToday = sessionNumberToday,
                    totalSessionsToday = totalSessionsToday
                )
                _activeSessionId.value = sessionId
            } else {
                _uiState.value = _uiState.value.copy(
                    activeSession = null,
                    hasActiveSession = false,
                    sessionNumberToday = 0,
                    totalSessionsToday = 0
                )
                _activeSessionId.value = null
            }
        }
    }

    fun clearActiveSession() {
        _uiState.value = _uiState.value.copy(
            activeSession = null,
            hasActiveSession = false
        )
        _activeSessionId.value = null
    }

    fun refresh() {
        loadTodaysSession()
    }
}
