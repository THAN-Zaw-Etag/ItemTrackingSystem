package com.tzh.itemTrackingSystem.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

data class ItemWithCategory(
    @Embedded val itemEntity: ItemEntity,
    @Relation(
        parentColumn = "CategoryId", entityColumn = "id"
    ) val category: Category?,
    @ColumnInfo("CategoryName") val categoryName: String?,
)