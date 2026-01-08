package com.example.archeryapp.data.model

data class ArrowScore(
    val arrow: ArrowDetection,
    val score: Int,
    val isX: Boolean = false
)
