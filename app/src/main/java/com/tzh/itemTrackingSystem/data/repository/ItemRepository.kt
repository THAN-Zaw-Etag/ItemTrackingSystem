package com.tzh.itemTrackingSystem.data.repository

import com.tzh.itemTrackingSystem.data.dao.ItemDao
import com.tzh.itemTrackingSystem.data.entity.Item

class ItemRepository(private val itemDao: ItemDao) {

    fun getItemList() = itemDao.getAllItem()


    fun getItemsCategory() = itemDao.getItemsCategory()

    suspend fun checkRfid(rfid: String): Boolean {
        return itemDao.checkRfid(rfid = rfid) != null
    }

    suspend fun addItem(item: Item): Long {
        return itemDao.addItem(item)
    }

    suspend fun updateItem(item: Item) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItem(id: Int) {
        itemDao.deleteItem(id)
    }

}