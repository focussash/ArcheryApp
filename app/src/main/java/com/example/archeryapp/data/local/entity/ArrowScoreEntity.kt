package com.example.archeryapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "arrow_scores",
    foreignKeys = [ForeignKey(
        entity = EndEntity::class,
        parentColumns = ["id"],
        childColumns = ["endId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["endId"])]
)
data class ArrowScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val endId: Long,
    val arrowNumber: Int,
    val score: Int,  // 0-10 (0 = miss)
    val isX: Boolean,  // true if X (inner 10)
    val xPosition: Float,  // relative position on target (0-1)
    val yPosition: Float   // relative position on target (0-1)
)
