package com.example.archeryapp.domain.model

data class Arrow(
    val id: Long = 0,
    val arrowNumber: Int,
    val score: Int,
    val isX: Boolean,
    val position: Position  // relative position on target (0-1, 0-1)
)

data class Position(
    val x: Float,
    val y: Float
)
