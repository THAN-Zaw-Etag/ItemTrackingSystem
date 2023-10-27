package com.tzh.itemTrackingSystem.ulti

import androidx.compose.ui.unit.dp
import com.tzh.itemTrackingSystem.data.entity.Category


typealias SuccessListener = () -> Unit

object Constant {

    val DropdownMenuVerticalPadding = 4.dp


    val DefaultCategory = Category(0, "")


    const val ADD_ITEM = "Add Item"
    const val ADD_CATEGORY = "Add Category"

    val addDropDownMenu = listOf<String>(ADD_ITEM, ADD_CATEGORY)

}