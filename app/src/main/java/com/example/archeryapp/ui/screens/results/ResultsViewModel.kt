package com.example.archeryapp.ui.screens.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.archeryapp.ArcheryApplication
import com.example.archeryapp.data.model.ScoringResult
import com.example.archeryapp.data.repository.ScoreRepositoryImpl
import com.example.archeryapp.data.repository.SessionRepositoryImpl
import com.example.archeryapp.domain.usecase.SaveScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SavedEndSummary(
    val endNumber: Int,
    val totalScore: Int,
    val arrowCount: Int,
    val xCount: Int
)

data class ResultsUiState(
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,
    val currentSessionId: Long? = null,
    val endCount: Int = 0,
    val savedEnds: List<SavedEndSummary> = emptyList(),
    val endSaved: Boolean = false,
    val savedEndId: Long? = null,
    val navigateToEdit: Boolean = false
)

class ResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as ArcheryApplication).database

    private val sessionRepository = SessionRepositoryImpl(database.sessionDao())
    private val scoreRepository = ScoreRepositoryImpl(database.endDao(), database.arrowScoreDao())
    private val saveScoreUseCase = SaveScoreUseCase(sessionRepository, scoreRepository)

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    fun setInitialSessionId(sessionId: Long?) {
        if (sessionId != null && _uiState.value.currentSessionId == null) {
            viewModelScope.launch {
                // Load existing ends for this session
                val ends = scoreRepository.getEndsForSessionSync(sessionId)
                val savedEnds = ends.map { end ->
                    SavedEndSummary(
                        endNumber = end.endNumber,
                        totalScore = end.totalScore,
                        arrowCount = end.arrows.size,
                        xCount = end.xCount
                    )
                }
                _uiState.value = _uiState.value.copy(
                    currentSessionId = sessionId,
                    endCount = ends.size,
                    savedEnds = savedEnds
                )
            }
        }
    }

    fun saveScore(
        scoringResult: ScoringResult,
        continueSession: Boolean = true
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)

            val sessionId = if (continueSession) _uiState.value.currentSessionId else null

            val result = saveScoreUseCase.execute(
                scoringResult = scoringResult,
                sessionId = sessionId
            )

            result.fold(
                onSuccess = { (sessionId, endId) ->
                    val ends = scoreRepository.getEndsForSessionSync(sessionId)
                    val savedEnds = ends.map { end ->
                        SavedEndSummary(
                            endNumber = end.endNumber,
                            totalScore = end.totalScore,
                            arrowCount = end.arrows.size,
                            xCount = end.xCount
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = true,
                        currentSessionId = sessionId,
                        endCount = ends.size,
                        savedEnds = savedEnds,
                        endSaved = true,
                        savedEndId = endId
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveError = error.message ?: "Failed to save"
                    )
                }
            )
        }
    }

    fun deleteEndForEdit() {
        viewModelScope.launch {
            val endId = _uiState.value.savedEndId ?: return@launch
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)

            try {
                val existingEnd = scoreRepository.getEndById(endId)
                if (existingEnd != null) {
                    scoreRepository.deleteEnd(existingEnd)
                }
                // Reset state and trigger navigation
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    endSaved = false,
                    savedEndId = null,
                    navigateToEdit = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveError = e.message ?: "Failed to delete end for edit"
                )
            }
        }
    }

    fun clearNavigateToEdit() {
        _uiState.value = _uiState.value.copy(navigateToEdit = false)
    }

    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(saveSuccess = false, saveError = null)
    }

    fun startNewSession() {
        _uiState.value = _uiState.value.copy(currentSessionId = null)
    }
}
