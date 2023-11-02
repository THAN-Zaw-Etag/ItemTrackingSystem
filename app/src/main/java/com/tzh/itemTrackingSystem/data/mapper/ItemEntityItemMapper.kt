package com.tzh.itemTrackingSystem.data.mapper

import com.tzh.itemTrackingSystem.data.entity.ItemEntity
import com.tzh.itemTrackingSystem.data.model.Item

fun ItemEntity.toItemMapper(): Item = Item(
    id = this.id,
    itemName = this.itemName,
    isForShop = this.isForShop,
    desc = this.desc,
    rfid = this.rfid,
    categoryId = this.categoryId,
    isScan = false
)

fun Item.toItemEntityMapper(): ItemEntity = ItemEntity(
    id = this.id,
    itemName = this.itemName,
    isForShop = this.isForShop,
    desc = this.desc,
    rfid = this.rfid,
    categoryId = this.categoryId
)

