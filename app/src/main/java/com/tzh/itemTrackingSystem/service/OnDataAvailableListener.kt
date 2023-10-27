package com.tzh.itemTrackingSystem.service

interface OnDataAvailableListener {
//    fun onCharacteristicRead(
//        gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int
//    )
//
//    fun onCharacteristicWrite(
//        gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?
//    )
//
//    fun onCharacteristicChanged(
//        gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?
//    )

    fun onGetData(rfidList: ArrayList<String>)

}