package com.tzh.itemTrackingSystem.service

import com.tzh.itemTrackingSystem.ulti.ConnectionStatus


interface ConnectionStateListener {
    fun onUpdate(state: ConnectionStatus)
    fun onConnectedDeviceName(name: String?)
    fun onScanUpdate(isScan: Boolean)
}