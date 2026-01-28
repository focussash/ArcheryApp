package com.example.archeryapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.archeryapp.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    suspend fun getAllSessionsSync(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getSessionsByDateRangeSync(startDate: Long, endDate: Long): List<SessionEntity>

    @Query("SELECT * FROM sessions ORDER BY date DESC LIMIT 1")
    suspend fun getMostRecentSession(): SessionEntity?

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Statistics queries
    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getTotalSessionCount(): Int

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}
