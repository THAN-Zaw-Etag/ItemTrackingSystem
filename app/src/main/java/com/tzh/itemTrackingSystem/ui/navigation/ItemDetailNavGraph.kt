package com.tzh.itemTrackingSystem.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.screen.detail_Item.DetailItemScreen
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.service.BluetoothService

fun NavGraphBuilder.itemDetailNavGraph(
    bluetoothService: BluetoothService,
    itemRepository: ItemRepository,
    setCurrentRoute: (String) -> Unit
) {

    composable(
        ROUTE.DetailScreen + "/{id}", arguments = listOf(
            navArgument("id") {
                type = NavType.IntType
                defaultValue = 0
            },
        )
    ) { backStackEntry ->
        setCurrentRoute(ROUTE.DetailScreen)
        val itemId = backStackEntry.arguments?.getInt("id") ?: 0
        DetailItemScreen(itemId = itemId, itemRepository = itemRepository, bluetoothService)
    }
}