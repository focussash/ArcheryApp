package com.example.archeryapp.data.model

import android.graphics.PointF
import android.graphics.RectF

data class ArrowDetection(
    val position: PointF,
    val confidence: Float = 1.0f,
    val boundingBox: RectF? = null
)
