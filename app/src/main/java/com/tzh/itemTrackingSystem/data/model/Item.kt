package com.tzh.itemTrackingSystem.data.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Item(
    val id: Int = 0,
    val itemName: String,
    val desc: String? = null,
    val rfid: String? = null,
    val categoryId: Int? = null,
    val isForShop: Boolean = false,
    var initialIsScan: Boolean = false,
    var categoryName: String = "",
    var initialIsCheck: Boolean = false,
) {
    var isScan by mutableStateOf(initialIsScan)
    var isCheck by mutableStateOf(initialIsCheck)
}