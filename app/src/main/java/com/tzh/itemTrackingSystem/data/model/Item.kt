package com.tzh.itemTrackingSystem.data.model

data class Item(
    val id: Int = 0,
    val itemName: String,
    val desc: String? = null,
    val rfid: String? = null,
    val categoryId: Int? = null,
    val isForShop: Boolean = false,
    var isScan: Boolean = false,
    val categoryName: String = "",
    var isCheck: Boolean = false,
)