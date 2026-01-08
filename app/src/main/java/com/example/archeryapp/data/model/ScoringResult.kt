package com.example.archeryapp.data.model

import android.graphics.Bitmap

data class ScoringResult(
    val originalImage: Bitmap,
    val processedImage: Bitmap,
    val target: TargetDetection,
    val arrows: List<ArrowDetection>,
    val scores: List<ArrowScore>,
    val totalScore: Int = scores.sumOf { it.score },
    val xCount: Int = scores.count { it.isX }
)
