package com.tzh.itemTrackingSystem.chf301

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ulti.ConnectionStatus

class GattUpdateReceiver(
    val updateConnectionState: (ConnectionStatus) -> Unit,
    val serviceDiscover: () -> Unit,
    val displayData: (String?) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (BluetoothService.ACTION_GATT_CONNECTED == action) {
            updateConnectionState(ConnectionStatus.CONNECTED)
            println("BroadcastReceiver :" + "device connected")
        } else if (BluetoothService.ACTION_GATT_DISCONNECTED == action) {
            updateConnectionState(ConnectionStatus.DISCONNECTED)
            println("BroadcastReceiver :" + "device disconnected")
        } else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED == action) {
            println("BroadcastReceiver :" + "device SERVICES_DISCOVERED")
            serviceDiscover()
        } else if (BluetoothService.ACTION_DATA_AVAILABLE == action) {
            val temp = intent.extras!!.getString(BluetoothService.EXTRA_DATA)
            displayData(temp)
        }
    }
}


