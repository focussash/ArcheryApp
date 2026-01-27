package com.example.archeryapp.domain.repository

import com.example.archeryapp.domain.model.Arrow
import com.example.archeryapp.domain.model.End
import kotlinx.coroutines.flow.Flow

interface ScoreRepository {
    suspend fun saveEnd(sessionId: Long, end: End): Long
    suspend fun updateEnd(end: End)
    suspend fun deleteEnd(end: End)
    suspend fun getEndById(id: Long): End?
    fun getEndsForSession(sessionId: Long): Flow<List<End>>
    suspend fun getEndsForSessionSync(sessionId: Long): List<End>
    fun getArrowsForEnd(endId: Long): Flow<List<Arrow>>
    suspend fun getTotalScoreForSession(sessionId: Long): Int
    suspend fun getXCountForSession(sessionId: Long): Int
    suspend fun getEndCountForSession(sessionId: Long): Int
}
