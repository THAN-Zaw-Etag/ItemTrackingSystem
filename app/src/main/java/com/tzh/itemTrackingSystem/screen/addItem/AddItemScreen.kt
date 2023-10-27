package com.tzh.itemTrackingSystem.screen.addItem

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tzh.itemTrackingSystem.ItemTrackingSystemApplication
import com.tzh.itemTrackingSystem.screen.common.ControlBluetoothLifecycle
import com.tzh.itemTrackingSystem.screen.common.MyAppDropDown
import com.tzh.itemTrackingSystem.screen.common.RFIDTextFieldWithTitle
import com.tzh.itemTrackingSystem.screen.common.TextFieldWithTitle
import com.tzh.itemTrackingSystem.screen.common.TitleText
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ui.theme.RFIDTextColor

@Composable
fun AddItemScreen(
    application: ItemTrackingSystemApplication, bluetoothService: BluetoothService,
    itemViewModel: AddItemViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    ControlBluetoothLifecycle(
        LocalLifecycleOwner.current,
        onCreate = {
            bluetoothService.setOnDataAvailableListener(itemViewModel)
        },
        onPause = {
            bluetoothService.removeOnDataAvailableListener()
        },
    )

    val uiState by itemViewModel.uiState.collectAsState()
    Log.e("DATA", uiState.toString())

    LaunchedEffect(
        key1 = uiState.rfidText,
        block = {
            if (uiState.rfidText.isNotEmpty()) {
                bluetoothService.stopScan()
            }
        },
    )

//    if (uiState.isShowCategoryDialog) {
//        CrateCategoryDialog(save = {
//
//            itemViewModel.addCategory(it) {
//                navController.navigateUp()
//            }
//
//        }) {
//            itemViewModel.dismissCategoryDialog()
//        }
//    }


    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        TitleText(if (uiState.editItem == null) "ADD ITEM" else "EDIT ITEM", modifier = Modifier.fillMaxWidth())
        TextFieldWithTitle(title = "Item Name", value = uiState.itemName) { text ->
            itemViewModel.updateItemName(text)
        }
        TextFieldWithTitle(title = "Description", value = uiState.itemDescription) { text ->
            itemViewModel.updateItemDescription(text)
        }

        RFIDTextFieldWithTitle(text = uiState.rfidText)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Justify, fontFamily = FontFamily.Serif, color = RFIDTextColor, fontWeight = FontWeight.Bold
            )
            Text(
                text = "Category", modifier = Modifier.weight(.3f), style = style
            )
            MyAppDropDown(
                Modifier.weight(.8f),
                list = uiState.categoryList.map { it.categoryName },
                value = uiState.selectedCategory.categoryName
            ) { categoryName ->
                uiState.categoryList.find { it.categoryName == categoryName }?.let { itemViewModel.selectedCategory(it) }

            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Is this Item for shop", style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Justify, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold
                )
            )
            Checkbox(checked = uiState.isForShop, onCheckedChange = { itemViewModel.updateIsForShop() })
            Spacer(modifier = Modifier.weight(1f))
            ElevatedButton(
                onClick = {
                    itemViewModel.addItem(showToast = {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }, success = {
                        if (uiState.editItem != null) {
                            Toast.makeText(context, "Successfully edited", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Successfully Added", Toast.LENGTH_LONG).show()
                        }
                        navController.navigateUp()
                    })
                }, modifier = Modifier, enabled = uiState.isSaveEnabled
            ) {
                Text(text = "Save")
            }
        }
    }
}

