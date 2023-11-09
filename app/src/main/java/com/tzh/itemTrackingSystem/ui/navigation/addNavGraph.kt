package com.tzh.itemTrackingSystem.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tzh.itemTrackingSystem.ItemTrackingSystemApplication
import com.tzh.itemTrackingSystem.screen.addItem.AddItemScreen
import com.tzh.itemTrackingSystem.screen.addItem.AddItemViewModel
import com.tzh.itemTrackingSystem.screen.addPlan.CreatePlanScreen
import com.tzh.itemTrackingSystem.screen.createCategory.CreateCategoryScreen
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.service.BluetoothService

fun NavGraphBuilder.addScreenNavGraph(
    bluetoothService: BluetoothService,
    itemViewModel: AddItemViewModel,
    navController: NavHostController,
    application: ItemTrackingSystemApplication,
    setCurrentRoute: (String) -> Unit
) {
    composable(ROUTE.AddItem) {
        setCurrentRoute(ROUTE.AddItem)
        AddItemScreen(bluetoothService, itemViewModel, navController = navController)
    }

    composable(ROUTE.AddCategory) {
        setCurrentRoute(ROUTE.AddCategory)
        CreateCategoryScreen { category ->
            itemViewModel.addCategory(category) {
                navController.navigateUp()
            }
        }
    }

    composable(ROUTE.AddPlan) {
        setCurrentRoute(ROUTE.AddPlan)
        CreatePlanScreen(navController = navController, planRepository = application.planRepository)
    }
}
