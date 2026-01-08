package com.example.archeryapp.domain.detection

import android.graphics.Bitmap
import com.example.archeryapp.data.model.ArrowDetection
import com.example.archeryapp.data.model.TargetDetection

interface ArrowDetector {
    fun detect(bitmap: Bitmap, target: TargetDetection): List<ArrowDetection>
}
