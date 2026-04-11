package com.example.archeryapp.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.archeryapp.ArcheryApplication
import com.example.archeryapp.data.preferences.UserPreferences
import com.example.archeryapp.data.repository.SessionRepositoryImpl
import com.example.archeryapp.domain.model.Session
import com.example.archeryapp.domain.model.TargetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class HomeUiState(
    val activeSession: Session? = null,
    val hasActiveSession: Boolean = false,
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val sessionNumberToday: Int = 0,
    val totalSessionsToday: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as ArcheryApplication).database
    private val sessionRepository = SessionRepositoryImpl(database.sessionDao())
    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    // When the user explicitly ends a session, we suppress auto-picking the most-recent-today
    // so the home card stays empty until they manually pick or create a new session.
    // Reset by setActiveSession / createSession.
    private val _explicitlyCleared = MutableStateFlow(false)

    init {
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            combine(
                sessionRepository.getAllSessions(),
                _activeSessionId,
                _explicitlyCleared
            ) { all, sel, cleared -> Triple(all, sel, cleared) }
                .collect { (all, sel, cleared) ->
                    recomputeUiState(all, sel, cleared)
                }
        }
    }

    private fun recomputeUiState(
        allSessions: List<Session>,
        selectedId: Long?,
        explicitlyCleared: Boolean
    ) {
        val todayStart = LocalDate.now().atStartOfDay()
        val todayEnd = todayStart.plusDays(1)
        val todaySessions = allSessions.filter { it.date >= todayStart && it.date < todayEnd }

        val effective: Session? = when {
            selectedId != null -> {
                // Explicit selection. If it no longer exists (deleted elsewhere), fall back
                // to most-recent-today so the home card self-heals instead of going stale.
                allSessions.find { it.id == selectedId }
                    ?: todaySessions.maxByOrNull { it.date }
            }
            explicitlyCleared -> null
            else -> todaySessions.maxByOrNull { it.date }
        }

        val totalSessionsToday = todaySessions.size
        val sessionNumberToday = if (effective != null) {
            val sorted = todaySessions.sortedBy { it.date }
            val idx = sorted.indexOfFirst { it.id == effective.id }
            if (idx < 0) 0 else idx + 1
        } else 0

        _uiState.value = _uiState.value.copy(
            activeSession = effective,
            hasActiveSession = effective != null,
            isLoading = false,
            sessionNumberToday = sessionNumberToday,
            totalSessionsToday = totalSessionsToday
        )

        if (selectedId != null && allSessions.none { it.id == selectedId }) {
            _activeSessionId.value = null
        }
    }

    fun setActiveSession(sessionId: Long?) {
        _explicitlyCleared.value = false
        _activeSessionId.value = sessionId
    }

    fun clearActiveSession() {
        _explicitlyCleared.value = true
        _activeSessionId.value = null
    }

    fun endActiveSession() {
        clearActiveSession()
    }

    fun getLastTargetType(): TargetType = userPreferences.getLastTargetType()

    fun createSession(
        targetType: TargetType,
        onCreated: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)
            try {
                val session = Session(
                    date = LocalDateTime.now(),
                    targetType = targetType
                )
                val sessionId = sessionRepository.createSession(session)
                userPreferences.setLastTargetType(targetType)
                _explicitlyCleared.value = false
                _activeSessionId.value = sessionId
                _uiState.value = _uiState.value.copy(isCreating = false)
                onCreated(sessionId)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isCreating = false)
            }
        }
    }
}
