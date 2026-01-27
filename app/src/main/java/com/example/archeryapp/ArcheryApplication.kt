package com.example.archeryapp

import android.app.Application
import android.util.Log
import com.example.archeryapp.data.local.database.AppDatabase
import org.opencv.android.OpenCVLoader

class ArcheryApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

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
