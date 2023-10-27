package com.tzh.itemTrackingSystem

import android.app.Application
import com.tzh.itemTrackingSystem.data.AppDatabase
import com.tzh.itemTrackingSystem.data.repository.CategoryRepository
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.service.BluetoothService

class ItemTrackingSystemApplication : Application() {

    lateinit var appDatabase: AppDatabase

    lateinit var itemRepository: ItemRepository
    lateinit var categoryRepository: CategoryRepository

    //    private val myS: MyService = MyService()
    lateinit var bluetoothService: BluetoothService
    override fun onCreate() {
        super.onCreate()
        bluetoothService = BluetoothService(this)
        appDatabase = AppDatabase.getInstance(this)
        itemRepository = ItemRepository(appDatabase.getItemDao())
        categoryRepository = CategoryRepository(appDatabase.getCategoryDao())
    }
}