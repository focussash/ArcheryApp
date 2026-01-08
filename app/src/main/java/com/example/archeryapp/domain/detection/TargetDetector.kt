package com.example.archeryapp.domain.detection

import android.graphics.Bitmap
import com.example.archeryapp.data.model.TargetDetection

interface TargetDetector {
    fun detect(bitmap: Bitmap): TargetDetection?
}
