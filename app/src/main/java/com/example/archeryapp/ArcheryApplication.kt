package com.example.archeryapp

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class ArcheryApplication : Application() {

    companion object {
        private const val TAG = "ArcheryApplication"
    }

    override fun onCreate() {
        super.onCreate()
        initOpenCV()
    }

    private fun initOpenCV() {
        if (OpenCVLoader.initLocal()) {
            Log.d(TAG, "OpenCV loaded successfully")
        } else {
            Log.e(TAG, "OpenCV initialization failed")
        }
    }
}
