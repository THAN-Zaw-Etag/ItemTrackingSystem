package com.tzh.itemTrackingSystem.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tzh.itemTrackingSystem.data.TableNameConstant


@Entity(tableName = TableNameConstant.ITEM_PLAN_ENTITY)
data class ItemPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("PlanId") val planId: Int,
    @ColumnInfo("ItemId") val itemId: Int,
)
