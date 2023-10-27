package com.tzh.itemTrackingSystem.service

import android.bluetooth.BluetoothDevice

interface OnDiscoverListener {
    fun onDiscover(device: BluetoothDevice)
}