package com.tzh.itemTrackingSystem

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.tzh.itemTrackingSystem.chf301.BTClient
import com.tzh.itemTrackingSystem.localStorage.SharePreferenceHelper
import com.tzh.itemTrackingSystem.screen.main.MainScreen
import com.tzh.itemTrackingSystem.screen.main.MainViewModel
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ui.theme.TestChatTheme
import com.tzh.itemTrackingSystem.ulti.Extensions.requestPermission

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {


    companion object {
        val REQUEST_BT_PERMISSION = 100
    }

    lateinit var sharedPreferences: SharePreferenceHelper
    val application: ItemTrackingSystemApplication by lazy { applicationContext as ItemTrackingSystemApplication }

    val bluetoothService by lazy { application.bluetoothService }

    lateinit var mainToast: Toast
    fun showToast(msg: String) {
        mainToast.setText(msg)
        mainToast.show()
    }

    val viewModel: MainViewModel by viewModels {
        MainViewModel.Companion.FACTORY()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        BTClient.mBluetoothLeService = bluetoothService
        (applicationContext as Application).requestPermission(this)
        startMyService()
        bluetoothService.openBluetooth(this)
        sharedPreferences = application.sharedPreferences
        mainToast = Toast.makeText(this, "", Toast.LENGTH_LONG)
        setContent {
            TestChatApp()
        }
    }


    @Composable
    private fun TestChatApp() {
        TestChatTheme {
            MainScreen(
                mainViewModel = viewModel, bluetoothService = bluetoothService, application,
                previousBTAddress = sharedPreferences.getPreviousDeviceAddress(),
                showToast = {
                    showToast(it)
                },
            )
//            var name by remember {
//                mutableStateOf(sharedPreferences.getName())
//            }
//            if (name.isEmpty()) {
//                EnterNameDialog(
//                    showToast = ::showToast, name = name,
//                    onSave = {
//                        name = it
//                        sharedPreferences.saveName(it)
//                    },
//                )
//            } else {
//
//            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothService.disconnect()
        bluetoothService.close()
        unregisterReceiver(bluetoothService.mGattUpdateReceiver)
    }

    private fun startMyService() {
        registerReceiver(bluetoothService.mGattUpdateReceiver, IntentFilter().apply {
            addAction(BluetoothService.ACTION_GATT_CONNECTED)
            addAction(BluetoothService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothService.ACTION_DATA_AVAILABLE)
        })


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
