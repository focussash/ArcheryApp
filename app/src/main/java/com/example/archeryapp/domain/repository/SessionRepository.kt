package com.example.archeryapp.domain.repository

import com.example.archeryapp.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun createSession(session: Session): Long
    suspend fun updateSession(session: Session)
    suspend fun deleteSession(session: Session)
    suspend fun deleteSessionById(id: Long)
    suspend fun getSessionById(id: Long): Session?
    fun getAllSessions(): Flow<List<Session>>
    suspend fun getAllSessionsSync(): List<Session>
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<Session>>
    suspend fun getSessionsByDateRange(startDate: java.time.LocalDateTime, endDate: java.time.LocalDateTime): List<Session>
    suspend fun getMostRecentSession(): Session?
}
