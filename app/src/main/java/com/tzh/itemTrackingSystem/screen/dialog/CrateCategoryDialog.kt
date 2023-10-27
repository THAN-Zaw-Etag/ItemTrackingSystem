package com.tzh.itemTrackingSystem.screen.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.screen.createCategory.CreateCategoryScreen


@Composable
fun CrateCategoryDialog(save: (Category) -> Unit, dismiss: () -> Unit) {
    Dialog(onDismissRequest = { dismiss() }, DialogProperties(usePlatformDefaultWidth = false)) {
        CreateCategoryScreen(save)
    }
}
