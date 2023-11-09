package com.tzh.itemTrackingSystem

import android.app.Application
import com.tzh.itemTrackingSystem.chf301.BTClient
import com.tzh.itemTrackingSystem.data.AppDatabase
import com.tzh.itemTrackingSystem.data.repository.CategoryRepository
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.data.repository.PlanRepository
import com.tzh.itemTrackingSystem.localStorage.SharePreferenceHelper
import com.tzh.itemTrackingSystem.service.BluetoothService

class ItemTrackingSystemApplication : Application() {

    lateinit var appDatabase: AppDatabase
    lateinit var sharedPreferences: SharePreferenceHelper
    lateinit var itemRepository: ItemRepository
    lateinit var categoryRepository: CategoryRepository
    lateinit var planRepository: PlanRepository

    //    private val myS: MyService = MyService()
    lateinit var bluetoothService: BluetoothService
    override fun onCreate() {
        super.onCreate()
        bluetoothService = BluetoothService(this)

        BTClient.mBluetoothLeService = bluetoothService
        sharedPreferences = SharePreferenceHelper(this)
        appDatabase = AppDatabase.getInstance(this)
        itemRepository = ItemRepository(appDatabase.getItemDao())
        categoryRepository = CategoryRepository(appDatabase.getCategoryDao())
        planRepository = PlanRepository(appDatabase.getPlanDao())
    }
}