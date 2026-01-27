package com.example.archeryapp.data.repository

import com.example.archeryapp.data.local.dao.SessionDao
import com.example.archeryapp.data.local.entity.SessionEntity
import com.example.archeryapp.domain.model.Session
import com.example.archeryapp.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SessionRepositoryImpl(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun createSession(session: Session): Long {
        return sessionDao.insert(session.toEntity())
    }

    override suspend fun updateSession(session: Session) {
        sessionDao.update(session.toEntity())
    }

    override suspend fun deleteSession(session: Session) {
        sessionDao.delete(session.toEntity())
    }

    override suspend fun deleteSessionById(id: Long) {
        sessionDao.deleteById(id)
    }

    override suspend fun getSessionById(id: Long): Session? {
        return sessionDao.getById(id)?.toDomain()
    }

    override fun getAllSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllSessionsSync(): List<Session> {
        return sessionDao.getAllSessionsSync().map { it.toDomain() }
    }

    override fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<Session>> {
        return sessionDao.getSessionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSessionsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Session> {
        val startMillis = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return sessionDao.getSessionsByDateRangeSync(startMillis, endMillis).map { it.toDomain() }
    }

    override suspend fun getMostRecentSession(): Session? {
        return sessionDao.getMostRecentSession()?.toDomain()
    }

    private fun Session.toEntity(): SessionEntity {
        return SessionEntity(
            id = id,
            date = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            distance = distance,
            bowType = bowType,
            location = location,
            notes = notes
        )
    }

    private fun SessionEntity.toDomain(): Session {
        return Session(
            id = id,
            date = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()),
            distance = distance,
            bowType = bowType,
            location = location,
            notes = notes,
            ends = emptyList() // Ends loaded separately via ScoreRepository
        )
    }
}
