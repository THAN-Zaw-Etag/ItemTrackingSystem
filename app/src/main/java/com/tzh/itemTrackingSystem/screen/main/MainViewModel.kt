package com.tzh.itemTrackingSystem.screen.main

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.service.ConnectionStateListener
import com.tzh.itemTrackingSystem.service.OnDiscoverListener
import com.tzh.itemTrackingSystem.service.ScanStateListener
import com.tzh.itemTrackingSystem.ulti.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(), ConnectionStateListener,
    OnDiscoverListener, ScanStateListener {

    private val bluetoothDeviceList = mutableListOf<BluetoothDevice>()
    val btDeviceList = MutableStateFlow<List<BluetoothDevice>>(listOf())
    val connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectedDeviceName = MutableStateFlow<String?>(null)
    val isScanning = MutableStateFlow(false)


    fun toggleScan(bluetoothService: BluetoothService, showToast: (String) -> Unit) {
        if (connectionStatus.value != ConnectionStatus.CONNECTED) {
            showToast("Reader not connected")
            return
        }
        if (!isScanning.value) {
            startScan(bluetoothService)
        } else {
            stopScan(bluetoothService)
        }
    }

    fun startScan(bluetoothService: BluetoothService) {
        viewModelScope.launch {
            bluetoothService.startScan()
//            updateScanStatus()
        }
    }

    fun stopScan(bluetoothService: BluetoothService) {
        Log.e("Stop Scan", "TRUE")
        if (!isScanning.value) return
        bluetoothService.stopScan()
//        updateScanStatus()
    }


    fun updateScanStatus() {
        isScanning.update {
            !it
        }
    }


    override fun onUpdate(state: ConnectionStatus) {
        connectionStatus.update {
            state
        }
        if (state != ConnectionStatus.CONNECTED) {
            isScanning.update {
                false
            }
        }
    }

    override fun onConnectedDeviceName(name: String?) {
        connectedDeviceName.update {
            name
        }
    }

    override fun onScanUpdate(isScan: Boolean) {
        isScanning.update {
            isScan
        }
    }

    companion object {
        class FACTORY : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel() as T
            }
        }
    }

    override fun onDiscover(device: BluetoothDevice) {
        if (!bluetoothDeviceList.contains(device)) {
            println("Address:" + device.address)
            bluetoothDeviceList.add(device)
            btDeviceList.update {
                bluetoothDeviceList.toList()
            }
            //rssis.add(rssi);
        }
    }
}