package com.example.archeryapp.data.export

import android.content.Context
import android.net.Uri
import com.example.archeryapp.data.local.dao.ArrowScoreDao
import com.example.archeryapp.data.local.dao.EndDao
import com.example.archeryapp.data.local.dao.SessionDao
import com.example.archeryapp.data.local.entity.ArrowScoreEntity
import com.example.archeryapp.data.local.entity.EndEntity
import com.example.archeryapp.data.local.entity.SessionEntity
import kotlinx.serialization.json.Json

data class ImportResult(
    val success: Boolean,
    val sessionsImported: Int = 0,
    val endsImported: Int = 0,
    val arrowsImported: Int = 0,
    val errorMessage: String? = null
)

class DataImportService(
    private val sessionDao: SessionDao,
    private val endDao: EndDao,
    private val arrowScoreDao: ArrowScoreDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importFromJson(jsonString: String): ImportResult {
        return try {
            val exportData = json.decodeFromString<ExportData>(jsonString)

            var sessionsImported = 0
            var endsImported = 0
            var arrowsImported = 0

            exportData.sessions.forEach { exportSession ->
                // Create session with new ID
                val sessionEntity = SessionEntity(
                    id = 0, // Auto-generate new ID
                    date = exportSession.date,
                    distance = exportSession.distance,
                    bowType = exportSession.bowType,
                    location = exportSession.location,
                    notes = exportSession.notes
                )
                val newSessionId = sessionDao.insert(sessionEntity)
                sessionsImported++

                exportSession.ends.forEach { exportEnd ->
                    // Create end with new ID linked to new session
                    val endEntity = EndEntity(
                        id = 0, // Auto-generate new ID
                        sessionId = newSessionId,
                        endNumber = exportEnd.endNumber,
                        timestamp = exportEnd.timestamp,
                        notes = exportEnd.notes
                    )
                    val newEndId = endDao.insert(endEntity)
                    endsImported++

                    // Create arrows linked to new end
                    val arrowEntities = exportEnd.arrows.map { exportArrow ->
                        ArrowScoreEntity(
                            id = 0, // Auto-generate new ID
                            endId = newEndId,
                            arrowNumber = exportArrow.arrowNumber,
                            score = exportArrow.score,
                            isX = exportArrow.isX,
                            xPosition = exportArrow.xPosition,
                            yPosition = exportArrow.yPosition
                        )
                    }
                    arrowScoreDao.insertAll(arrowEntities)
                    arrowsImported += arrowEntities.size
                }
            }

            ImportResult(
                success = true,
                sessionsImported = sessionsImported,
                endsImported = endsImported,
                arrowsImported = arrowsImported
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errorMessage = e.message ?: "Unknown error during import"
            )
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            }
        } catch (e: Exception) {
            null
        }
    }
}
