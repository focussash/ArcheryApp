package com.example.archeryapp.data.repository

import com.example.archeryapp.data.local.dao.ArrowScoreDao
import com.example.archeryapp.data.local.dao.EndDao
import com.example.archeryapp.data.local.entity.ArrowScoreEntity
import com.example.archeryapp.data.local.entity.EndEntity
import com.example.archeryapp.domain.model.Arrow
import com.example.archeryapp.domain.model.End
import com.example.archeryapp.domain.model.Position
import com.example.archeryapp.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ScoreRepositoryImpl(
    private val endDao: EndDao,
    private val arrowScoreDao: ArrowScoreDao
) : ScoreRepository {

    override suspend fun saveEnd(sessionId: Long, end: End): Long {
        // Insert end first to get the ID
        val endEntity = EndEntity(
            id = 0, // Auto-generate
            sessionId = sessionId,
            endNumber = end.endNumber,
            timestamp = end.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            notes = end.notes
        )
        val endId = endDao.insert(endEntity)

        // Insert all arrows for this end
        val arrowEntities = end.arrows.map { arrow ->
            ArrowScoreEntity(
                id = 0, // Auto-generate
                endId = endId,
                arrowNumber = arrow.arrowNumber,
                score = arrow.score,
                isX = arrow.isX,
                xPosition = arrow.position.x,
                yPosition = arrow.position.y
            )
        }
        arrowScoreDao.insertAll(arrowEntities)

        return endId
    }

    override suspend fun updateEnd(end: End) {
        val existingEnd = endDao.getById(end.id) ?: return

        endDao.update(
            EndEntity(
                id = end.id,
                sessionId = existingEnd.sessionId,
                endNumber = end.endNumber,
                timestamp = end.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                notes = end.notes
            )
        )

        // Replace all arrows: delete existing and insert new
        arrowScoreDao.deleteByEndId(end.id)
        val arrowEntities = end.arrows.map { arrow ->
            ArrowScoreEntity(
                id = 0,
                endId = end.id,
                arrowNumber = arrow.arrowNumber,
                score = arrow.score,
                isX = arrow.isX,
                xPosition = arrow.position.x,
                yPosition = arrow.position.y
            )
        }
        arrowScoreDao.insertAll(arrowEntities)
    }

    override suspend fun deleteEnd(end: End) {
        val entity = endDao.getById(end.id) ?: return
        endDao.delete(entity)
        // Arrow scores are deleted via CASCADE
    }

    override suspend fun getEndById(id: Long): End? {
        val endEntity = endDao.getById(id) ?: return null
        val arrows = arrowScoreDao.getScoresByEndIdSync(id).map { it.toDomain() }
        return endEntity.toDomain(arrows)
    }

    override fun getEndsForSession(sessionId: Long): Flow<List<End>> {
        return endDao.getEndsBySessionId(sessionId).map { ends ->
            ends.map { endEntity ->
                val arrows = arrowScoreDao.getScoresByEndIdSync(endEntity.id).map { it.toDomain() }
                endEntity.toDomain(arrows)
            }
        }
    }

    override suspend fun getEndsForSessionSync(sessionId: Long): List<End> {
        return endDao.getEndsBySessionIdSync(sessionId).map { endEntity ->
            val arrows = arrowScoreDao.getScoresByEndIdSync(endEntity.id).map { it.toDomain() }
            endEntity.toDomain(arrows)
        }
    }

    override fun getArrowsForEnd(endId: Long): Flow<List<Arrow>> {
        return arrowScoreDao.getScoresByEndId(endId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTotalScoreForSession(sessionId: Long): Int {
        return arrowScoreDao.getTotalScoreForSession(sessionId) ?: 0
    }

    override suspend fun getXCountForSession(sessionId: Long): Int {
        return arrowScoreDao.getXCountForSession(sessionId)
    }

    override suspend fun getEndCountForSession(sessionId: Long): Int {
        return endDao.getEndCountForSession(sessionId)
    }

    private fun EndEntity.toDomain(arrows: List<Arrow>): End {
        return End(
            id = id,
            endNumber = endNumber,
            timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()),
            arrows = arrows,
            notes = notes
        )
    }

    private fun ArrowScoreEntity.toDomain(): Arrow {
        return Arrow(
            id = id,
            arrowNumber = arrowNumber,
            score = score,
            isX = isX,
            position = Position(xPosition, yPosition)
        )
    }
}
