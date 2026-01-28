package com.example.archeryapp.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.archeryapp.ArcheryApplication
import com.example.archeryapp.data.repository.ScoreRepositoryImpl
import com.example.archeryapp.data.repository.SessionRepositoryImpl
import com.example.archeryapp.ui.screens.history.SessionWithEnds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayDetailUiState(
    val sessions: List<SessionWithEnds> = emptyList(),
    val expandedSessionIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DayDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as ArcheryApplication).database
    private val sessionRepository = SessionRepositoryImpl(database.sessionDao())
    private val scoreRepository = ScoreRepositoryImpl(database.endDao(), database.arrowScoreDao())

    private val _uiState = MutableStateFlow(DayDetailUiState())
    val uiState: StateFlow<DayDetailUiState> = _uiState.asStateFlow()

    fun loadSessionsForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val startOfDay = date.atStartOfDay()
                val endOfDay = date.plusDays(1).atStartOfDay()

                val sessions = sessionRepository.getSessionsByDateRange(startOfDay, endOfDay)
                val sessionsWithEnds = sessions.map { session ->
                    val ends = scoreRepository.getEndsForSessionSync(session.id)
                    val totalScore = ends.sumOf { it.totalScore }
                    val totalArrows = ends.sumOf { it.arrows.size }
                    val xCount = ends.sumOf { it.xCount }

                    SessionWithEnds(
                        id = session.id,
                        date = session.date,
                        distance = session.distance,
                        bowType = session.bowType,
                        location = session.location,
                        ends = ends,
                        totalScore = totalScore,
                        totalArrows = totalArrows,
                        xCount = xCount
                    )
                }.sortedBy { it.date }

                _uiState.value = _uiState.value.copy(
                    sessions = sessionsWithEnds,
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

    fun toggleSessionExpanded(sessionId: Long) {
        val currentExpanded = _uiState.value.expandedSessionIds
        _uiState.value = _uiState.value.copy(
            expandedSessionIds = if (sessionId in currentExpanded) {
                currentExpanded - sessionId
            } else {
                currentExpanded + sessionId
            }
        )
    }
}
