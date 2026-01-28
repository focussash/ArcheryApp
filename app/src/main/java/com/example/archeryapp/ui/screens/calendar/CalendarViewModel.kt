package com.example.archeryapp.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.archeryapp.ArcheryApplication
import com.example.archeryapp.data.repository.SessionRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class DaySessionData(
    val sessionCount: Int,
    val totalScore: Int,
    val totalArrows: Int,
    val xCount: Int,
    val averageScore: Float,
    val sessionIds: List<Long>
)

data class CalendarUiState(
    val sessionsByDate: Map<LocalDate, DaySessionData> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as ArcheryApplication).database
    private val sessionRepository = SessionRepositoryImpl(database.sessionDao())
    private val sessionDao = database.sessionDao()
    private val arrowScoreDao = database.arrowScoreDao()

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val aggregatedData = withContext(Dispatchers.IO) {
                    val sessions = sessionRepository.getAllSessionsSync()
                    val sessionsByDate = mutableMapOf<LocalDate, MutableList<SessionData>>()

                    // Group sessions by date - use batch queries to avoid N+1 problem
                    sessions.forEach { session ->
                        val date = session.date.toLocalDate()
                        // Use optimized single queries instead of loading all ends
                        val totalScore = arrowScoreDao.getTotalScoreForSession(session.id) ?: 0
                        val xCount = arrowScoreDao.getXCountForSession(session.id)
                        val arrowCount = arrowScoreDao.getArrowCountForSession(session.id)

                        val data = SessionData(
                            id = session.id,
                            totalScore = totalScore,
                            totalArrows = arrowCount,
                            xCount = xCount
                        )

                        sessionsByDate.getOrPut(date) { mutableListOf() }.add(data)
                    }

                    // Aggregate data per day
                    sessionsByDate.mapValues { (_, sessionList) ->
                        val totalScore = sessionList.sumOf { it.totalScore }
                        val totalArrows = sessionList.sumOf { it.totalArrows }
                        val xCount = sessionList.sumOf { it.xCount }
                        val averageScore = if (totalArrows > 0) totalScore.toFloat() / totalArrows else 0f

                        DaySessionData(
                            sessionCount = sessionList.size,
                            totalScore = totalScore,
                            totalArrows = totalArrows,
                            xCount = xCount,
                            averageScore = averageScore,
                            sessionIds = sessionList.map { it.id }
                        )
                    }
                }

                _uiState.value = _uiState.value.copy(
                    sessionsByDate = aggregatedData,
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

    fun refresh() {
        loadSessions()
    }

    private data class SessionData(
        val id: Long,
        val totalScore: Int,
        val totalArrows: Int,
        val xCount: Int
    )
}
