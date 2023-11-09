package com.tzh.itemTrackingSystem.screen.addPlan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tzh.itemTrackingSystem.data.entity.PlanEntity
import com.tzh.itemTrackingSystem.data.repository.PlanRepository
import com.tzh.itemTrackingSystem.screen.common.TextFieldWithTitle
import com.tzh.itemTrackingSystem.screen.common.TitleText
import com.tzh.itemTrackingSystem.ulti.Extensions.showToast
import java.util.Locale

@Composable
fun CreatePlanScreen(
    navController: NavController,
    planRepository: PlanRepository,
    viewModel: CreatePlanViewModel = viewModel(factory = CreatePlanViewModel.Companion.FACTORY(planRepository))
) {
    val context = LocalContext.current

    var planName by remember {
        mutableStateOf("")
    }
    var planDesc by remember {
        mutableStateOf("")
    }
    var planEntity: PlanEntity? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(key1 = planEntity) {
        planEntity?.let {
            viewModel.addPlan(it) {
                navController.navigateUp()
                planEntity = null
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        TitleText("ADD Plan")
        TextFieldWithTitle(textChange = {
            planName = it
        }, title = "Name", value = planName)
        TextFieldWithTitle(textChange = {
            planDesc = it
        }, title = "Description", value = planDesc)
        ElevatedButton(onClick = {
            if (planName.isEmpty()) {
                context.showToast("Please enter Plan Name")
                return@ElevatedButton
            }
            planEntity = PlanEntity(
                planName = planName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() },
                planDesc = planDesc
            )
        }, modifier = Modifier.align(Alignment.End)) {
            Text(text = "Save")
        }
    }
}
