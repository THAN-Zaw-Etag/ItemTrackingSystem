package com.tzh.itemTrackingSystem.data.repository

import android.util.Log
import com.tzh.itemTrackingSystem.data.dao.ItemDao
import com.tzh.itemTrackingSystem.data.entity.ItemEntity
import com.tzh.itemTrackingSystem.data.entity.ItemWithCategory

class ItemRepository(private val itemDao: ItemDao) {

    fun getItemList() = itemDao.getAllItem()
    fun getCategoryById(id: Int) = itemDao.getCategoryById(id)

    fun getItemsCategory() = itemDao.getItemsCategory()
    fun getAllItemsCategory() = itemDao.getAllItemsCategory()

    suspend fun checkRfid(rfid: String): Boolean {
        return itemDao.checkRfid(rfid = rfid) != null
    }

    suspend fun addItem(itemEntity: ItemEntity): Long {
        return itemDao.addItem(itemEntity)
    }

    suspend fun updateItem(itemEntity: ItemEntity) {
        itemDao.updateItem(itemEntity)
    }

    suspend fun deleteItem(id: Int) {
        itemDao.deleteItem(id)
    }

    suspend fun findItemById(id: Int): ItemEntity? {
        return itemDao.findItemById(id)
    }

    suspend fun findItemCategoryById(id: Int): ItemWithCategory? {
        return try {
            itemDao.getItemCategoryById(id)
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
            null
        }
    }
}