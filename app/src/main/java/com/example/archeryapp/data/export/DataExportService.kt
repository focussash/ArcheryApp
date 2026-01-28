package com.example.archeryapp.data.export

import android.content.Context
import android.net.Uri
import com.example.archeryapp.data.local.dao.ArrowScoreDao
import com.example.archeryapp.data.local.dao.EndDao
import com.example.archeryapp.data.local.dao.SessionDao
import com.example.archeryapp.data.local.entity.ArrowScoreEntity
import com.example.archeryapp.data.local.entity.EndEntity
import com.example.archeryapp.data.local.entity.SessionEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class ExportData(
    val version: Int = 1,
    val exportDate: String,
    val sessions: List<ExportSession>
)

@Serializable
data class ExportSession(
    val id: Long,
    val date: Long,
    val distance: String?,
    val bowType: String?,
    val location: String?,
    val notes: String?,
    val ends: List<ExportEnd>
)

@Serializable
data class ExportEnd(
    val id: Long,
    val endNumber: Int,
    val timestamp: Long,
    val notes: String?,
    val arrows: List<ExportArrow>
)

@Serializable
data class ExportArrow(
    val id: Long,
    val arrowNumber: Int,
    val score: Int,
    val isX: Boolean,
    val xPosition: Float,
    val yPosition: Float
)

class DataExportService(
    private val sessionDao: SessionDao,
    private val endDao: EndDao,
    private val arrowScoreDao: ArrowScoreDao
) {
    private val json = Json { prettyPrint = true }

    suspend fun exportToJson(): String {
        val sessions = sessionDao.getAllSessionsSync()

        val exportSessions = sessions.map { session ->
            val ends = endDao.getEndsBySessionIdSync(session.id)
            val exportEnds = ends.map { end ->
                val arrows = arrowScoreDao.getScoresByEndIdSync(end.id)
                val exportArrows = arrows.map { arrow ->
                    ExportArrow(
                        id = arrow.id,
                        arrowNumber = arrow.arrowNumber,
                        score = arrow.score,
                        isX = arrow.isX,
                        xPosition = arrow.xPosition,
                        yPosition = arrow.yPosition
                    )
                }
                ExportEnd(
                    id = end.id,
                    endNumber = end.endNumber,
                    timestamp = end.timestamp,
                    notes = end.notes,
                    arrows = exportArrows
                )
            }
            ExportSession(
                id = session.id,
                date = session.date,
                distance = session.distance,
                bowType = session.bowType,
                location = session.location,
                notes = session.notes,
                ends = exportEnds
            )
        }

        val exportData = ExportData(
            version = 1,
            exportDate = Instant.now().toString(),
            sessions = exportSessions
        )

        return json.encodeToString(exportData)
    }

    suspend fun exportToCsv(): String {
        val sessions = sessionDao.getAllSessionsSync()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val csvBuilder = StringBuilder()
        csvBuilder.appendLine("Session ID,Session Date,Distance,Bow Type,Location,End Number,Arrow Number,Score,Is X,X Position,Y Position")

        sessions.forEach { session ->
            val sessionDate = Instant.ofEpochMilli(session.date)
                .atZone(ZoneId.systemDefault())
                .format(dateFormatter)

            val ends = endDao.getEndsBySessionIdSync(session.id)
            ends.forEach { end ->
                val arrows = arrowScoreDao.getScoresByEndIdSync(end.id)
                arrows.forEach { arrow ->
                    csvBuilder.appendLine(
                        "${session.id},$sessionDate,${session.distance ?: ""},${session.bowType ?: ""},${session.location ?: ""},${end.endNumber},${arrow.arrowNumber},${arrow.score},${arrow.isX},${arrow.xPosition},${arrow.yPosition}"
                    )
                }
            }
        }

        return csvBuilder.toString()
    }

    fun writeToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray())
        }
    }
}
