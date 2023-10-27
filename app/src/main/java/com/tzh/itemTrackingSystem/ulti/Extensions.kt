package com.tzh.itemTrackingSystem.ulti

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.app.ActivityCompat
import com.tzh.itemTrackingSystem.MainActivity

object Extensions {
    @OptIn(ExperimentalMaterial3Api::class)
    fun Application.requestPermission(activity: Activity) {
        if (this.checkBluetoothAccessCoarseLocation() && this.checkBluetoothAccessFineLocation() && this.checkBluetoothBackgroundLocation() && this.checkBluetoothConnect() && this.checkBluetoothScan()) {
            return
        }
        val requestArray = mutableListOf<String>()
        if (!this.checkBluetoothAccessCoarseLocation()) {
            requestArray.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!this.checkBluetoothAccessFineLocation()) {
            requestArray.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!this.checkBluetoothBackgroundLocation()) {
            requestArray.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!this.checkBluetoothConnect()) {
                requestArray.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (!this.checkBluetoothScan()) {
                requestArray.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        ActivityCompat.requestPermissions(
            activity, requestArray.toTypedArray(), MainActivity.REQUEST_BT_PERMISSION
        )
    }

    fun Application.checkBluetoothConnect(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun Application.checkBluetoothScan(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun Application.checkBluetoothAccessCoarseLocation(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun Application.checkBluetoothAccessFineLocation(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun Application.checkBluetoothBackgroundLocation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }


    fun getScanStatus(isScanning: Boolean) = if (isScanning) {
        "Scan Status : Scanning"
    } else {
        "Scan Status : Stop"
    }


    fun getStatus(connectionState: ConnectionStatus, deviceName: String?) = when (connectionState) {
        ConnectionStatus.CONNECTED -> {
            "Device Name :\t" + (deviceName ?: "-") + "\nConnection Status :\t" + "Connected"
        }

        ConnectionStatus.IS_CONNECTING -> {
            "Device Name :\t" + (deviceName ?: "-") + "\nConnection Status :\t" + "Connecting Device"
        }

        else -> {
            "Device Name :\t" + (deviceName ?: "-") + "\nConnection Status :\t" + "Disconnected"
        }
    }
}