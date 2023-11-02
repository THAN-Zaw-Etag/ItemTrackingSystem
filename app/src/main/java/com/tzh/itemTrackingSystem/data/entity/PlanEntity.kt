package com.tzh.itemTrackingSystem.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tzh.itemTrackingSystem.data.TableNameConstant


@Entity(tableName = TableNameConstant.PLAN)
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "PlanName") val planName: String,
    @ColumnInfo(name = "PlanDescription") val planDesc: String? = null
)