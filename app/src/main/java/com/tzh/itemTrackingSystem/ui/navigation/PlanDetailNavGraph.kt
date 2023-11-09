package com.tzh.itemTrackingSystem.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tzh.itemTrackingSystem.ItemTrackingSystemApplication
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.screen.plan.planDetail.PlanDetailScreen
import com.tzh.itemTrackingSystem.service.BluetoothService


fun NavGraphBuilder.planDetailNavGraph(
    bluetoothService: BluetoothService,
    application: ItemTrackingSystemApplication,
    setCurrentRoute: (String) -> Unit
) {

    composable(
        ROUTE.PlanDetailScreen + "/{planId}/{planName}",
        arguments = listOf(
            navArgument("planId") {
                type = NavType.IntType
                defaultValue = 0
            },
            navArgument("planName") {
                type = NavType.StringType
                defaultValue = ""
            },
        )
    ) { backStackEntry ->
        setCurrentRoute(ROUTE.PlanDetailScreen)
        val planId = backStackEntry.arguments?.getInt("planId") ?: 0
        val planName = backStackEntry.arguments?.getString("planName") ?: ""
        PlanDetailScreen(
            bluetoothService,
            planId,
            planName,
            planRepository = application.planRepository,
            itemRepository = application.itemRepository
        )
    }

}