package com.tzh.itemTrackingSystem.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tzh.itemTrackingSystem.data.repository.PlanRepository
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.screen.plan.PlanScreen

fun NavGraphBuilder.planNavGraph(
    navController: NavController,
    planRepository: PlanRepository,
    setCurrentRoute: (String) -> Unit
) {
    composable(ROUTE.PlanScreen) {
        setCurrentRoute(ROUTE.PlanScreen)
        PlanScreen(navController, planRepository = planRepository)
    }

}