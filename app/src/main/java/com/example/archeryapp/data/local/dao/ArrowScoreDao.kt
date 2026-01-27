package com.example.archeryapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.archeryapp.data.local.entity.ArrowScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArrowScoreDao {
    @Insert
    suspend fun insert(arrowScore: ArrowScoreEntity): Long

    @Insert
    suspend fun insertAll(arrowScores: List<ArrowScoreEntity>)

    @Delete
    suspend fun delete(arrowScore: ArrowScoreEntity)

    @Query("DELETE FROM arrow_scores WHERE endId = :endId")
    suspend fun deleteByEndId(endId: Long)

    @Query("SELECT * FROM arrow_scores WHERE endId = :endId ORDER BY arrowNumber ASC")
    fun getScoresByEndId(endId: Long): Flow<List<ArrowScoreEntity>>

    @Query("SELECT * FROM arrow_scores WHERE endId = :endId ORDER BY arrowNumber ASC")
    suspend fun getScoresByEndIdSync(endId: Long): List<ArrowScoreEntity>

    @Query("SELECT SUM(score) FROM arrow_scores WHERE endId = :endId")
    suspend fun getTotalScoreForEnd(endId: Long): Int?

    @Query("SELECT COUNT(*) FROM arrow_scores WHERE endId = :endId AND isX = 1")
    suspend fun getXCountForEnd(endId: Long): Int

    @Query("SELECT SUM(score) FROM arrow_scores WHERE endId IN (SELECT id FROM ends WHERE sessionId = :sessionId)")
    suspend fun getTotalScoreForSession(sessionId: Long): Int?

    @Query("SELECT COUNT(*) FROM arrow_scores WHERE endId IN (SELECT id FROM ends WHERE sessionId = :sessionId) AND isX = 1")
    suspend fun getXCountForSession(sessionId: Long): Int
}
