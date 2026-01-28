package com.example.archeryapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.archeryapp.data.local.entity.EndEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EndDao {
    @Insert
    suspend fun insert(end: EndEntity): Long

    @Update
    suspend fun update(end: EndEntity)

    @Delete
    suspend fun delete(end: EndEntity)

    @Query("SELECT * FROM ends WHERE id = :id")
    suspend fun getById(id: Long): EndEntity?

    @Query("SELECT * FROM ends WHERE sessionId = :sessionId ORDER BY endNumber ASC")
    fun getEndsBySessionId(sessionId: Long): Flow<List<EndEntity>>

    @Query("SELECT * FROM ends WHERE sessionId = :sessionId ORDER BY endNumber ASC")
    suspend fun getEndsBySessionIdSync(sessionId: Long): List<EndEntity>

    @Query("SELECT COUNT(*) FROM ends WHERE sessionId = :sessionId")
    suspend fun getEndCountForSession(sessionId: Long): Int

    @Query("SELECT MAX(endNumber) FROM ends WHERE sessionId = :sessionId")
    suspend fun getMaxEndNumber(sessionId: Long): Int?

    // Statistics queries
    @Query("SELECT COUNT(*) FROM ends")
    suspend fun getTotalEndCount(): Int

    @Query("DELETE FROM ends")
    suspend fun deleteAll()
}
