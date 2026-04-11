package com.example.archeryapp.data.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object BlePermissionHandler {

    fun requiredRuntimePermissions(): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Manifest declares BLUETOOTH_SCAN with neverForLocation, so FINE_LOCATION is not needed on API 31+.
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    fun isBleSupported(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    fun isBluetoothEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager ?: return false
        val adapter: BluetoothAdapter? = manager.adapter
        return adapter?.isEnabled == true
    }

    fun hasAllPermissions(context: Context): Boolean =
        missingPermissions(context).isEmpty()

    fun missingPermissions(context: Context): List<String> =
        requiredRuntimePermissions().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

    fun readiness(context: Context): BleReadiness = when {
        !isBleSupported(context) -> BleReadiness.UNSUPPORTED
        !hasAllPermissions(context) -> BleReadiness.PERMISSIONS_MISSING
        !isBluetoothEnabled(context) -> BleReadiness.BLUETOOTH_DISABLED
        else -> BleReadiness.READY
    }
}

enum class BleReadiness {
    UNSUPPORTED,
    PERMISSIONS_MISSING,
    BLUETOOTH_DISABLED,
    READY,
}
