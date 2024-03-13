package com.tzh.itemTrackingSystem.service

interface OnDataAvailableListener {

    fun onGetData(rfidList: ArrayList<String>)

}