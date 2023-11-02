package com.tzh.itemTrackingSystem.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tzh.itemTrackingSystem.data.entity.PlanEntity
import com.tzh.itemTrackingSystem.data.repository.PlanRepository
import com.tzh.itemTrackingSystem.screen.common.TitleText
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.ui.theme.gradientList

@Composable
fun PlanScreen(
    navController: NavController,
    planRepository: PlanRepository,
    viewModel: PlanViewModel = viewModel(factory = PlanViewModel.Companion.FACTORY(planRepository)),
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize()) {
        TitleText("Plans", Modifier.fillMaxWidth())
        LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
            items(uiState.planList, key = { it.id }) { plan: PlanEntity ->
                PlanCardItem(planEntity = plan) {
                    navController.navigate(ROUTE.PlanDetailScreen + "/${plan.id}/${plan.planName}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCardItem(planEntity: PlanEntity, onClick: () -> Unit) {
    val backGroundColor by remember {
        mutableStateOf(
            Brush.horizontalGradient(
                colors = listOf(
                    gradientList.random(), gradientList.random()
                )
            )
        )
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(backGroundColor)
                .padding(8.dp)
        ) {
            Text(
                text = planEntity.planName, style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                ), modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = (planEntity.planDesc ?: "-").ifEmpty { "-" }, style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Justify,
                ), modifier = Modifier.fillMaxWidth()
            )
        }
    }
}