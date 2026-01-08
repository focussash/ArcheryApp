package com.example.archeryapp.data.model

import android.graphics.PointF

data class TargetDetection(
    val center: PointF,
    val radius: Float,
    val confidence: Float = 1.0f
)
