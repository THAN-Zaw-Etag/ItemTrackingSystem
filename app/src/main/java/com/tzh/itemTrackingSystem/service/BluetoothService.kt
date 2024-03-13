package com.tzh.itemTrackingSystem.service

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.util.Log
import com.tzh.itemTrackingSystem.ItemTrackingSystemApplication
import com.tzh.itemTrackingSystem.chf301.BTClient
import com.tzh.itemTrackingSystem.ulti.ConnectionStatus
import com.tzh.itemTrackingSystem.ulti.Extensions.checkBluetoothScan
import com.tzh.itemTrackingSystem.ulti.Extensions.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothService(private val application: ItemTrackingSystemApplication) {
    companion object {
        const val TAG = "BluetoothService : "
        const val REQUEST_ENABLE_BT = 101
        var connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED
        private var mBluetoothGatt: BluetoothGatt? = null
        var mBluetoothDeviceAddress: String? = null
        var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>> = ArrayList()
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
        const val HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb"
        var mBleArray: ArrayList<BluetoothDevice> = ArrayList()
        const val DefaultPower = 26.toByte()
    }

    var scanResult: HashMap<String, Int> = HashMap()
    var epcBytes: HashMap<String, ByteArray> = HashMap()
    var RecvString: String? = ""
    var target_chara: BluetoothGattCharacteristic? = null
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothDeviceList = mutableListOf<BluetoothDevice>()

    var mScanStateListener: MutableList<ScanStateListener> = mutableListOf()
    fun setScanStateListener(onScanStateListener: ScanStateListener) {
        mScanStateListener.add(onScanStateListener)
    }

    fun removeScanStateListener(onScanStateListener: ScanStateListener) {
        mScanStateListener.remove(onScanStateListener)
    }


    var mOnDiscoverListener: OnDiscoverListener? = null

    fun setOnDiscoverListener(onDataAvailableListener: OnDiscoverListener?) {
        mOnDiscoverListener = onDataAvailableListener
    }

    fun removeOnDiscoverListener() {
        mOnDiscoverListener = null
    }

    var mOnDataAvailableListener: OnDataAvailableListener? = null
    fun setOnDataAvailableListener(dataAvailableListener: OnDataAvailableListener?) {
        mOnDataAvailableListener = dataAvailableListener
    }

    fun removeOnDataAvailableListener() {
        mOnDataAvailableListener = null
    }

    var bluetoothConnectionState: ConnectionStateListener? = null

    fun getConnectState(): Boolean = connectionStatus == ConnectionStatus.CONNECTED

    fun setPower(power: Int): Boolean {
        return try {
            BTClient.SetPower(power.toByte())
            Log.e("SET POWER IS :", "SET power $power")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setDefaultRegion() {
        try {
            val min = (8 and 3 shl 6) or (49 and 0x3F)
//            val min = (840.125 + 19 * 0.25).toInt().toByte()
//            val max = (840.125 + 19 * 0.25).toInt().toByte()

            val max = (8 and 0x0c shl 4) or (49 and 0x3F)
            BTClient.SetRegion(max.toByte(), min.toByte())
            Log.e("SET DEFAULT REGION IS :", "MIN $min , MAX $max")
        } catch (e: Exception) {

        }
    }

    fun scanBtDevice(enable: Boolean) {
        if (enable) {
            Log.i(TAG, "Scan begin.....................")
            if (!application.checkBluetoothScan()) {
                return
            }
            Log.i(TAG, "Scan begin.....................2")
            bluetoothAdapter.startLeScan(mLeScanCallback)
        } else {
            Log.i(TAG, "stoping................")
            bluetoothAdapter.stopLeScan(mLeScanCallback)
        }
    }

    fun updateDeviceStatus(address: String?, connectionStatus: ConnectionStatus) {
        Companion.connectionStatus = connectionStatus
        bluetoothConnectionState?.onUpdate(connectionStatus)
        bluetoothConnectionState?.onConnectedDeviceName(address)
        if (connectionStatus == ConnectionStatus.DISCONNECTED) {
            disconnect()
            close()
        }
        if (connectionStatus == ConnectionStatus.CONNECTED) {
            application.sharedPreferences.saveDeviceAddress(mBluetoothDeviceAddress)
        }
    }

    suspend fun connectBT(address: String?, connectionStateListener: ConnectionStateListener): Boolean {
        if (!bluetoothAdapter.isEnabled) {
            return false
        }
        return withContext(Dispatchers.IO) {
            bluetoothConnectionState = connectionStateListener
            Log.i("BluetoothLeService:", "connect")
            if (bluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
                return@withContext false
            }
            updateDeviceStatus(address, ConnectionStatus.IS_CONNECTING)
            ////Previously connected device.  Try to reconnect.
            if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress && mBluetoothGatt != null) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
                return@withContext mBluetoothGatt!!.connect()
            }
            val device = bluetoothAdapter.getRemoteDevice(address)
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.")
                return@withContext false
            }
            mBluetoothGatt = device.connectGatt(application, false, mGattCallback)
            Log.d(TAG, "Trying to create a new connection.")
            mBluetoothDeviceAddress = address
            println("device.getBondState==" + device.bondState)
            return@withContext true
        }
    }

    fun disconnect() {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt?.disconnect()
    }

    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    var timer: Timer? = null


    suspend fun startScan() {
        mScanStateListener.forEach {
            it.onScanUpdate(true)
        }
        withContext(Dispatchers.IO) {
            if (timer != null) {
                return@withContext
            }
            Log.e("Start Scan", "TRUE")
            scanResult.clear()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    if (connectionStatus == ConnectionStatus.CONNECTED) {
                        readuid()
//                        val data = scanResult
//                        val scanlist = ArrayList<String>(data.keys)
//                        getData(scanlist)
                    } else {
                        stopScan()
                    }
                }
            }, 0, 20L)
        }
    }

    fun stopScan() {
        mScanStateListener.forEach {
            it.onScanUpdate(false)
        }
        timer?.cancel()
        timer = null
    }

    private fun readuid() {
        var scaned_num: Int
        val lable = ScanUID()
        if (lable == null) {
            scaned_num = 0
            return
        }
        //   Log.e("RFID LIST", Arrays.toString(lable))
        scanResult.clear()
        scaned_num = lable.size
        for (i in 0 until scaned_num) {
            val key = lable[i]
            if (key == null || key == "") return
            val num = if (scanResult[key] == null) 0 else scanResult[key]
            if (num != null) {
                scanResult[key] = (num + 1)
            }
        }
        val list = ArrayList(scanResult.keys)
        mOnDataAvailableListener?.onGetData(list)
    }

    fun ScanUID(): Array<String?>? //
    {
        try {
            val EPCList = ByteArray(5000)
            val CardNum = IntArray(2)
            val EPCLength = IntArray(2)
            CardNum[0] = 0
            EPCLength[0] = 0

            val result: Int = BTClient.Inventory_G2(
                4.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), CardNum, EPCList, EPCLength
            )
            if (CardNum[0] and 255 > 0 && result != 0x30) {
                val Scan6CNum = CardNum[0] and 255
                val lable = arrayOfNulls<String>(Scan6CNum)
                var bf: StringBuffer
                var j = 0
                var k: Int
                var str: String
                var epc: ByteArray
                //Log.i("zdy", "num = " + Scan6CNum + ">>>>>>" + "len = " + EPCLength[0])
                for (i in 0 until Scan6CNum) {
                    bf = StringBuffer("")
                    //   Log.i("yl", "length = " + EPCList[j])
                    epc = ByteArray(EPCList[j].toInt() and 0xff)
                    k = 0
                    while (k < EPCList[j].toInt() and 0xff) {
                        str = Integer.toHexString(EPCList[j + k + 1].toInt() and 0xff)
                        if (str.length == 1) {
                            bf.append("0")
                        }
                        bf.append(str)
                        epc[k] = EPCList[j + k + 1]
                        k++
                    }
                    lable[i] = bf.toString().uppercase(Locale.getDefault())
                    lable[i]?.let { epcBytes.put(it, epc) }
                    j = j + k + 2
                }
                return lable
            }
        } catch (e: Exception) {

        }
        return null
    }


    fun openBluetooth(activity: Activity) {
        if (bluetoothAdapter == null) {
            application.showToast("Bluetooth not support")
        } else {
            if (!bluetoothAdapter.isEnabled) {
                enabledBT(activity)
            } else {
                application.showToast("Bluetooth open")
            }
        }
    }

    private fun enabledBT(activity: Activity) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    fun displayData(rev_string: String?) {
        RecvString += rev_string
        //   Log.e("RECVI STRING", RecvString.toString())
    }

    fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        Log.i(TAG, "displayGattServices - target_chara test")
        if (gattServices == null) {
            Log.e(TAG, "null")
            return
        }
        var uuid: String? = null
        val unknownServiceString = "unknown_service"
        val unknownCharaString = "unknown_characteristic"
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mGattCharacteristics = ArrayList()
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            gattServiceData.add(currentServiceData)
            println("Service uuid:$uuid")
            val gattCharacteristicGroupData = ArrayList<HashMap<String, String>>()

            val gattCharacteristics = gattService.characteristics
            val charas = ArrayList<BluetoothGattCharacteristic>()

            for (gattCharacteristic in gattCharacteristics) {
                charas.add(gattCharacteristic)
                val currentCharaData = HashMap<String, String>()
                uuid = gattCharacteristic.uuid.toString()
                if (gattCharacteristic.uuid.toString() == HEART_RATE_MEASUREMENT) {
                    setCharacteristicNotification(gattCharacteristic, true)
                    target_chara = gattCharacteristic
                }
                val descriptors = gattCharacteristic.descriptors
                for (descriptor in descriptors) {
                    println("---descriptor UUID:" + descriptor.uuid)
                    getCharacteristicDescriptor(descriptor)
                }
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mGattCharacteristics.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }
    }


    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.writeCharacteristic(characteristic)
    }

    fun readRssi() {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readRemoteRssi()
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic, enabled: Boolean
    ) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)
        val clientConfig = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        if (enabled) {
            clientConfig.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        } else {
            clientConfig.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
        mBluetoothGatt!!.writeDescriptor(clientConfig)
    }

    fun getCharacteristicDescriptor(descriptor: BluetoothGattDescriptor?) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readDescriptor(descriptor)
    }

    val supportedGattServices: List<BluetoothGattService>?
        /**
         * Retrieves a list of supported GATT services on the connected device. This should be
         * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
         *
         * @return A `List` of supported services.
         */
        get() = if (mBluetoothGatt == null) null else mBluetoothGatt!!.services


    private var mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->

        mOnDiscoverListener?.onDiscover(device)
//        if (!bluetoothDeviceList.contains(device)) {
//            println("Address:" + device.address)
//            bluetoothDeviceList.add(device)
//            btDeviceList.update {
//                bluetoothDeviceList.toList()
//            }
//            //rssis.add(rssi);
//        }

//        //System.out.println("Name:"+device.getName());
//        //System.out.println("rssi:"+rssi);
    }

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "--onCharacteristicRead called--")
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            } else {
                Log.i(TAG, "--onCharacteristicRead FAIL--")
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                broadcastUpdate(intentAction)
                updateDeviceStatus(mBluetoothDeviceAddress, ConnectionStatus.CONNECTED)
                Log.i(TAG, "Connected to GATT server.")
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt!!.discoverServices())
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                updateDeviceStatus(mBluetoothDeviceAddress, ConnectionStatus.DISCONNECTED)
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                Log.i(TAG, "--onServicesDiscovered called--")
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
                println("onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "--onCharacteristicRead called--")
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            } else {
                Log.i(TAG, "--onCharacteristicRead FAIL--")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {
            // println("++++++++++++++++")
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            Log.w(TAG, "--onCharacteristicWrite--: $status")

        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            Log.w(TAG, "----onDescriptorRead status: $status")
            val desc = descriptor.value
            if (desc != null) {
                Log.w(TAG, "----onDescriptorRead value: " + desc.toString())
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            Log.w(TAG, "--onDescriptorWrite--: $status")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            Log.w(TAG, "--onReadRemoteRssi--: $status")
            broadcastUpdate(ACTION_DATA_AVAILABLE, rssi)
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            Log.w(TAG, "--onReliableWriteCompleted--: $status")
        }
    }

    private fun broadcastUpdate(action: String, rssi: Int) {
        val intent = Intent(action)
        intent.putExtra(EXTRA_DATA, rssi.toString())
        application.sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        application.sendBroadcast(intent)
    }

    fun broadcastUpdate(
        action: String?, characteristic: BluetoothGattCharacteristic
    ) {
        val intent = Intent(action)
        val data = characteristic.value
        if (data != null && data.size > 0) {
            val recv = BTClient.bytesToHexString(data, 0, data.size)
            intent.putExtra(EXTRA_DATA, recv)
        }
        application.sendBroadcast(intent)
    }
}
