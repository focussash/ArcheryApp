package com.example.archeryapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,  // timestamp in milliseconds
    val distance: String?,  // e.g., "18m", "70m"
    val bowType: String?,  // e.g., "recurve", "compound", "barebow"
    val location: String?,
    val notes: String?
)
