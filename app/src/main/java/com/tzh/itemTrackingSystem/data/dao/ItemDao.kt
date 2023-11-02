package com.tzh.itemTrackingSystem.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tzh.itemTrackingSystem.data.TableNameConstant
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.data.entity.ItemEntity
import com.tzh.itemTrackingSystem.data.entity.ItemWithCategory
import kotlinx.coroutines.flow.Flow


@Dao
abstract class ItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addItem(itemEntity: ItemEntity): Long

    @Query("SELECT * FROM ${TableNameConstant.ITEM} order by `ItemName`")
    abstract fun getAllItem(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM ${TableNameConstant.ITEM} where id=:id Limit 1")
    abstract fun findItemById(id: Int): ItemEntity?

    @Update()
    abstract suspend fun updateItem(itemEntity: ItemEntity)

    @Query("SELECT * FROM ${TableNameConstant.ITEM} where CategoryId = :id order by `ItemName`")
    abstract suspend fun getItemByCategory(id: Int): List<ItemEntity>

    @Query(
        "SELECT * FROM ${TableNameConstant.ITEM} where Rfid = :rfid Limit 1"
    )
    abstract suspend fun checkRfid(rfid: String): ItemEntity?

    @Query("Delete FROM ${TableNameConstant.ITEM} where id = :id")
    abstract suspend fun deleteItem(id: Int)

    @Query(
        """SELECT  *, tblCategory.CategoryName
                FROM tblItem
                LEFT JOIN tblCategory ON tblItem.CategoryId = tblCategory.id
                WHERE tblItem.CategoryId IS NOT NULL"""
    )
    abstract fun getItemsCategory(): Flow<List<ItemWithCategory>>

    @Query(
        """SELECT  *, tblCategory.CategoryName
                FROM tblItem
                INNER JOIN tblCategory ON tblItem.CategoryId = tblCategory.id
                """
    )
    abstract fun getAllItemsCategory(): Flow<List<ItemWithCategory>>

    @Query("SELECT * FROM ${TableNameConstant.CATEGORY} where id =:id")
    abstract fun getCategoryById(id: Int): Category

    @Query(
        """SELECT  *, tblCategory.CategoryName
                FROM tblItem
                LEFT JOIN tblCategory ON tblItem.CategoryId = tblCategory.id
                WHERE tblItem.id =:id"""
    )
    abstract suspend fun getItemCategoryById(id: Int): ItemWithCategory?

}