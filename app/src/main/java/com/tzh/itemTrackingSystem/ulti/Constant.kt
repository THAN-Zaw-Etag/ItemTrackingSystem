package com.tzh.itemTrackingSystem.ulti

import androidx.compose.ui.unit.dp
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.screen.dialog.FilterCardItem


typealias SuccessListener = () -> Unit

object Constant {

    val DropdownMenuVerticalPadding = 4.dp


    val DefaultCategory = Category(0, "All")


    const val ADD_ITEM = "Add Item"
    const val ADD_CATEGORY = "Add Category"
    const val ADD_PLAN = "Add Plan"

    val addDropDownMenu = listOf<String>(ADD_ITEM, ADD_CATEGORY, ADD_PLAN)


    val filterCardItemList = listOf(
        FilterCardItem(id = 0, desc = "All"),
        FilterCardItem(id = 1, desc = "For Shop"),
        FilterCardItem(id = 2, desc = "Others")
    )

}