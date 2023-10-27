package com.tzh.itemTrackingSystem.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tzh.itemTrackingSystem.data.TableNameConstant
import com.tzh.itemTrackingSystem.data.entity.Item
import com.tzh.itemTrackingSystem.data.entity.ItemWithCategory
import kotlinx.coroutines.flow.Flow


@Dao
abstract class ItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addItem(item: Item): Long

    @Query("SELECT * FROM ${TableNameConstant.ITEM} order by `ItemName`")
    abstract fun getAllItem(): Flow<List<Item>>

    @Update()
    abstract suspend fun updateItem(item: Item)

    @Query("SELECT * FROM ${TableNameConstant.ITEM} where CategoryId = :id order by `ItemName`")
    abstract suspend fun getItemByCategory(id: Int): List<Item>

    @Query(
        "SELECT * FROM ${TableNameConstant.ITEM} where Rfid = :rfid Limit 1"
    )
    abstract suspend fun checkRfid(rfid: String): Item?

    @Query("Delete FROM ${TableNameConstant.ITEM} where id = :id")
    abstract suspend fun deleteItem(id: Int)

    @Query(
        """SELECT *, tblCategory.CategoryName
                FROM tblItem
                LEFT JOIN tblCategory ON tblItem.CategoryId = tblCategory.id
                WHERE tblItem.CategoryId IS NOT NULL"""
    )
    abstract fun getItemsCategory(): Flow<List<ItemWithCategory>>

}