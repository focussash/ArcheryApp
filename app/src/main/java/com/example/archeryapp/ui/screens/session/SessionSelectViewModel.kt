package com.example.archeryapp.ui.screens.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.archeryapp.ArcheryApplication
import com.example.archeryapp.data.preferences.UserPreferences
import com.example.archeryapp.data.repository.ScoreRepositoryImpl
import com.example.archeryapp.data.repository.SessionRepositoryImpl
import com.example.archeryapp.domain.model.Session
import com.example.archeryapp.domain.model.TargetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class SessionSummary(
    val id: Long,
    val date: LocalDateTime,
    val distance: String?,
    val bowType: String?,
    val endCount: Int,
    val totalScore: Int,
    val totalArrows: Int
)

data class SessionSelectUiState(
    val sessions: List<SessionSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val error: String? = null
)

class SessionSelectViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as ArcheryApplication).database
    private val sessionRepository = SessionRepositoryImpl(database.sessionDao())
    private val scoreRepository = ScoreRepositoryImpl(database.endDao(), database.arrowScoreDao())
    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(SessionSelectUiState())
    val uiState: StateFlow<SessionSelectUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val sessions = sessionRepository.getAllSessionsSync()
                val summaries = sessions.map { session ->
                    val endCount = scoreRepository.getEndCountForSession(session.id)
                    val totalScore = scoreRepository.getTotalScoreForSession(session.id)
                    val ends = scoreRepository.getEndsForSessionSync(session.id)
                    val totalArrows = ends.sumOf { it.arrows.size }

                    SessionSummary(
                        id = session.id,
                        date = session.date,
                        distance = session.distance,
                        bowType = session.bowType,
                        endCount = endCount,
                        totalScore = totalScore,
                        totalArrows = totalArrows
                    )
                }.sortedByDescending { it.date }

                _uiState.value = _uiState.value.copy(
                    sessions = summaries,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun getLastTargetType(): TargetType = userPreferences.getLastTargetType()

    fun createNewSession(
        targetType: TargetType,
        distance: String? = null,
        bowType: String? = null,
        location: String? = null,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)

            try {
                val session = Session(
                    date = LocalDateTime.now(),
                    distance = distance,
                    bowType = bowType,
                    location = location,
                    targetType = targetType
                )
                val sessionId = sessionRepository.createSession(session)
                userPreferences.setLastTargetType(targetType)
                _uiState.value = _uiState.value.copy(isCreating = false)
                onCreated(sessionId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = e.message
                )
            }
        }
    }

    fun refresh() {
        loadSessions()
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                sessionRepository.deleteSessionById(sessionId)
                loadSessions() // Refresh list after deletion
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
