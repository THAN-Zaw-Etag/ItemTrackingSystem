package com.tzh.itemTrackingSystem.screen.addPlan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.data.entity.PlanEntity
import com.tzh.itemTrackingSystem.data.repository.PlanRepository
import kotlinx.coroutines.launch

class CreatePlanViewModel(private val planRepository: PlanRepository) : ViewModel() {


    fun addPlan(planEntity: PlanEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            planRepository.addPlan(planEntity)
            onDone()
        }
    }

    companion object {
        class FACTORY(private val planRepository: PlanRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreatePlanViewModel(planRepository) as T
            }
        }
    }
}