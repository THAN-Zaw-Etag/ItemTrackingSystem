package com.tzh.itemTrackingSystem.screen.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.data.entity.PlanEntity
import com.tzh.itemTrackingSystem.data.repository.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


class PlanViewModel(private val planRepository: PlanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState = combine(_uiState, planRepository.getAllPlan()) { state, plans ->
        state.copy(planList = plans)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PlanUiState())

    companion object {
        class FACTORY(val planRepository: PlanRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlanViewModel(planRepository) as T
            }
        }
    }
}

data class PlanUiState(
    val planList: List<PlanEntity> = emptyList()
)