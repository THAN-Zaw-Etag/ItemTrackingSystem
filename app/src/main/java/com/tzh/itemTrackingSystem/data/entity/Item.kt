package com.tzh.itemTrackingSystem.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tzh.itemTrackingSystem.data.TableNameConstant

@Entity(tableName = TableNameConstant.ITEM)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "ItemName") val itemName: String,
    @ColumnInfo(name = "Description") val desc: String? = null,
    @ColumnInfo(name = "Rfid") val rfid: String? = null,
    @ColumnInfo(name = "CategoryId") val categoryId: Int? = null,
    @ColumnInfo(name = "IsForShop") val isForShop: Boolean = false
)
