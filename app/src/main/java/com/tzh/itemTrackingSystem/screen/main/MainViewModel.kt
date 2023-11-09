package com.tzh.itemTrackingSystem.screen.main

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.ItemTrackingSystemApplication
import com.tzh.itemTrackingSystem.chf301.GattUpdateReceiver
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.service.ConnectionStateListener
import com.tzh.itemTrackingSystem.service.OnDiscoverListener
import com.tzh.itemTrackingSystem.service.ScanStateListener
import com.tzh.itemTrackingSystem.ulti.ConnectionStatus
import com.tzh.itemTrackingSystem.ulti.Extensions.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val application: ItemTrackingSystemApplication
) : ViewModel(), ConnectionStateListener, OnDiscoverListener, ScanStateListener {

    val bluetoothService = application.bluetoothService

    private val bluetoothDeviceList = mutableListOf<BluetoothDevice>()
    val btDeviceList = MutableStateFlow<List<BluetoothDevice>>(listOf())
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        setSelectedDevice(application.sharedPreferences.getPreviousDeviceAddress())
    }


    var mGattUpdateReceiver: GattUpdateReceiver = GattUpdateReceiver(
        updateConnectionState = {
            bluetoothService.updateDeviceStatus(BluetoothService.mBluetoothDeviceAddress, it)
        },
        serviceDiscover = {
            if (bluetoothService.supportedGattServices != null) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        bluetoothService.displayGattServices(bluetoothService.supportedGattServices)
                        bluetoothService.setPower(BluetoothService.DefaultPower.toInt())
                        bluetoothService.setDefaultRegion()
                    }
                }
            }
        },
        displayData = bluetoothService::displayData,
    )

    fun setSelectedDevice(address: String?) {
        _uiState.update {
            it.copy(selectedBtDevice = address, showBtSheet = false)
        }
        viewModelScope.launch {
            bluetoothService.scanBtDevice(false)
            if (uiState.value.connectionStatus == ConnectionStatus.CONNECTED) {
                bluetoothService.disconnect()
                bluetoothService.close()
            }
            var count = 0
            while (true) {
                bluetoothService.connectBT(
                    address, connectionStateListener = this@MainViewModel
                )
                delay(2000)
                count++
                if (bluetoothService.getConnectState()) {
                    _uiState.update {
                        it.copy(selectedBtDevice = null, showBtSheet = false)
                    }
                    break
                }
                if (count == 30) {
                    bluetoothService.updateDeviceStatus(null, ConnectionStatus.DISCONNECTED)
                    _uiState.update {
                        it.copy(selectedBtDevice = null, showBtSheet = false)
                    }
                    break
                }
            }
            if (!bluetoothService.getConnectState()) {
                application.showToast("Bluetooth Device is not connected")
            } else {
                application.showToast("Bluetooth Device is connected")
            }
        }
    }

    fun showBtSheet() {
        _uiState.update {
            it.copy(showBtSheet = true)
        }
    }

    fun dismissBtSheet() {
        _uiState.update {
            it.copy(showBtSheet = false)
        }
    }

    fun showPowerSheet() {
        _uiState.update {
            it.copy(showPowerSheet = true)
        }
    }

    fun dismissPowerSheet() {
        _uiState.update {
            it.copy(showPowerSheet = false)
        }
    }


    fun setCurrentRoute(route: String) {
        _uiState.update {
            it.copy(currentRoute = route)
        }
    }

    fun toggleScan(bluetoothService: BluetoothService, showToast: (String) -> Unit) {
        if (uiState.value.connectionStatus != ConnectionStatus.CONNECTED) {
            showToast("Reader not connected")
            return
        }
        if (!uiState.value.isScanning) {
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
        if (!uiState.value.isScanning) return
        bluetoothService.stopScan()
//        updateScanStatus()
    }


    fun updateScanStatus() {
        _uiState.update {
            it.copy(
                isScanning = !it.isScanning
            )
        }
    }


    override fun onUpdate(state: ConnectionStatus) {

        _uiState.update {
            it.copy(
                connectionStatus = state,
            )
        }
        if (state != ConnectionStatus.CONNECTED) {
            _uiState.update {
                it.copy(
                    connectionStatus = state, isScanning = false
                )
            }
        }
    }

    override fun onConnectedDeviceName(name: String?) {
        _uiState.update {
            it.copy(
                connectedDeviceName = name,
            )
        }

    }

    override fun onScanUpdate(isScan: Boolean) {
        _uiState.update {
            it.copy(
                isScanning = isScan,
            )
        }

    }

    companion object {
        class FACTORY(val application: ItemTrackingSystemApplication) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(application) as T
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

data class MainUiState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedDeviceName: String? = null,
    val isScanning: Boolean = false,
    val selectedBtDevice: String? = null,
    val showBtSheet: Boolean = false,
    val showPowerSheet: Boolean = false,
    val currentRoute: String = ROUTE.Home
)