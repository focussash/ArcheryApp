package com.example.archeryapp.ui.screens.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.archeryapp.ArcheryApplication
import com.example.archeryapp.data.export.DataExportService
import com.example.archeryapp.data.export.DataImportService
import com.example.archeryapp.data.export.ImportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ExportFormat {
    JSON, CSV
}

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isWiping: Boolean = false,
    val exportSuccess: Boolean? = null,
    val importResult: ImportResult? = null,
    val wipeSuccess: Boolean? = null,
    val errorMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as ArcheryApplication).database
    private val sessionDao = database.sessionDao()
    private val endDao = database.endDao()
    private val arrowScoreDao = database.arrowScoreDao()
    private val exportService = DataExportService(
        sessionDao = sessionDao,
        endDao = endDao,
        arrowScoreDao = arrowScoreDao
    )
    private val importService = DataImportService(
        sessionDao = sessionDao,
        endDao = endDao,
        arrowScoreDao = arrowScoreDao
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportData(context: Context, uri: Uri, format: ExportFormat) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportSuccess = null,
                errorMessage = null
            )

            try {
                val content = when (format) {
                    ExportFormat.JSON -> exportService.exportToJson()
                    ExportFormat.CSV -> exportService.exportToCsv()
                }
                exportService.writeToUri(context, uri, content)

                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                importResult = null,
                errorMessage = null
            )

            try {
                val jsonContent = importService.readFromUri(context, uri)
                if (jsonContent == null) {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importResult = ImportResult(success = false, errorMessage = "Could not read file")
                    )
                    return@launch
                }

                val result = importService.importFromJson(jsonContent)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = ImportResult(success = false, errorMessage = e.message)
                )
            }
        }
    }

    fun wipeAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isWiping = true,
                wipeSuccess = null,
                errorMessage = null
            )

            try {
                // Delete in correct order to respect foreign key constraints
                arrowScoreDao.deleteAll()
                endDao.deleteAll()
                sessionDao.deleteAll()

                _uiState.value = _uiState.value.copy(
                    isWiping = false,
                    wipeSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isWiping = false,
                    wipeSuccess = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            exportSuccess = null,
            importResult = null,
            wipeSuccess = null,
            errorMessage = null
        )
    }
}
