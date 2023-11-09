package com.tzh.itemTrackingSystem

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.tzh.itemTrackingSystem.screen.main.MainScreen
import com.tzh.itemTrackingSystem.screen.main.MainViewModel
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ui.theme.ItemTrackingSystemTheme
import com.tzh.itemTrackingSystem.ulti.Extensions.requestPermission
import com.tzh.itemTrackingSystem.ulti.Extensions.showToast

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_BT_PERMISSION = 100
    }

    private val application: ItemTrackingSystemApplication by lazy {
        applicationContext as ItemTrackingSystemApplication
    }
    private val bluetoothService by lazy {
        application.bluetoothService
    }
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Companion.FACTORY(application) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startMyService()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        application.requestPermission(this)
        bluetoothService.openBluetooth(this)
        setContent {
            ItemTrackingSystemApp()
        }
    }


    @Composable
    private fun ItemTrackingSystemApp() {
        ItemTrackingSystemTheme {
            MainScreen(
                bluetoothService = bluetoothService,
                application = application,
                mainViewModel
            ) {
                showToast(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothService.disconnect()
        bluetoothService.close()
        unregisterReceiver(mainViewModel.mGattUpdateReceiver)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun startMyService() {
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothService.ACTION_GATT_CONNECTED)
            addAction(BluetoothService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothService.ACTION_DATA_AVAILABLE)
        }

        registerReceiver(mainViewModel.mGattUpdateReceiver, intentFilter)


//        val intent = Intent(this, MyService::class.java)
//        this.startService(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQUEST_BT_PERMISSION == requestCode) {
            bluetoothService.openBluetooth(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BluetoothService.REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    showToast("Bluetooth open")
                }
                if (resultCode == RESULT_CANCELED) {
                    showToast("Bluetooth need to open for search and chat")
                    bluetoothService.openBluetooth(this)
                }
            }
        }
    }
}
