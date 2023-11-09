package com.tzh.itemTrackingSystem.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.screen.addItem.AddItemViewModel
import com.tzh.itemTrackingSystem.screen.item.ItemScreen
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.service.BluetoothService

fun NavGraphBuilder.homeNavGraph(
    currentRoute: String,
    navController: NavController,
    bluetoothService: BluetoothService,
    itemRepository: ItemRepository,
    addItemViewModel: AddItemViewModel,
    setCurrentRoute: (String) -> Unit,
) {
    composable(ROUTE.Home) {
        setCurrentRoute(ROUTE.Home)
        ItemScreen(
            bluetoothService = bluetoothService,
            modifier = Modifier.fillMaxSize(),
            itemRepository = itemRepository,
            editItem = { item ->
                if (currentRoute == ROUTE.Home) {
                    addItemViewModel.setEditItem(item)
                    navController.navigate(ROUTE.AddItem)
                }
            },
            onClick = { item ->
                if (currentRoute == ROUTE.Home) {
                    navController.navigate(ROUTE.DetailScreen + "/${item.id}")
                }
            },
        )
    }
}