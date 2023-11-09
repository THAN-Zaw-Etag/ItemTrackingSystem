package com.tzh.itemTrackingSystem.screen.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.service.ConnectionStateListener
import com.tzh.itemTrackingSystem.ulti.ConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun StartConnecting(
    selectedBtDevice: String?,
    bluetoothService: BluetoothService,
    connectionStateListener: ConnectionStateListener,
    connectionStatus: ConnectionStatus,
    showToast: (String) -> Unit,
    onDone: () -> Unit
) {
    LaunchedEffect(key1 = selectedBtDevice) {
        if (selectedBtDevice != null) {
            bluetoothService.scanBtDevice(false)
            withContext(Dispatchers.IO) {
                if (connectionStatus == ConnectionStatus.CONNECTED) {
                    bluetoothService.disconnect()
                    bluetoothService.close()
                }
                var count = 0
                while (true) {
                    bluetoothService.connectBT(
                        selectedBtDevice, connectionStateListener = connectionStateListener
                    )
                    delay(2000)
                    count++
                    if (bluetoothService.getConnectState()) {
                        onDone()
                        break
                    }
                    if (count == 30) {
                        bluetoothService.updateDeviceStatus(null, ConnectionStatus.DISCONNECTED)
                        onDone()
                        break
                    }
                }
                if (!bluetoothService.getConnectState()) {
                    showToast("Bluetooth Device is not connected")
                } else {
                    showToast("Bluetooth Device is connected")
                }
            }
        }
    }
}