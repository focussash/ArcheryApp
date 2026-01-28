package com.example.archeryapp.ui.screens.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.archeryapp.ArcheryApplication
import com.example.archeryapp.data.repository.OverallStatistics
import com.example.archeryapp.data.repository.SessionTrendData
import com.example.archeryapp.data.repository.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val overallStats: OverallStatistics? = null,
    val trendData: List<SessionTrendData> = emptyList(),
    val scoreDistribution: Map<Int, Int> = emptyMap(),
    val recentBest: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as ArcheryApplication).database
    private val statisticsRepository = StatisticsRepository(
        sessionDao = database.sessionDao(),
        endDao = database.endDao(),
        arrowScoreDao = database.arrowScoreDao()
    )

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val overallStats = statisticsRepository.getOverallStatistics()
                val trendData = statisticsRepository.getSessionTrends(30)
                val scoreDistribution = statisticsRepository.getScoreDistribution()
                val recentBest = statisticsRepository.getRecentBest(30)

                _uiState.value = _uiState.value.copy(
                    overallStats = overallStats,
                    trendData = trendData,
                    scoreDistribution = scoreDistribution,
                    recentBest = recentBest,
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
        loadStatistics()
    }
}
