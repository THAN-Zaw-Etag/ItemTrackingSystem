package com.tzh.itemTrackingSystem.screen.plan.planDetail

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.data.entity.ItemEntity
import com.tzh.itemTrackingSystem.data.mapper.toItemMapper
import com.tzh.itemTrackingSystem.data.model.Item
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.data.repository.PlanRepository
import com.tzh.itemTrackingSystem.service.OnDataAvailableListener
import com.tzh.itemTrackingSystem.service.ScanStateListener
import com.tzh.itemTrackingSystem.ulti.SaveResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlanDetailViewModel(
    val planId: Int, private val planRepository: PlanRepository, private val itemRepository: ItemRepository
) : ViewModel(), OnDataAvailableListener, ScanStateListener {

    var itemList = mutableStateListOf<Item>()
    var planItems = mutableStateListOf<Item>()
    private val rfidScanList = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(PlanDetailUiState())
    val uiState: StateFlow<PlanDetailUiState> get() = _uiState.asStateFlow()

    init {
        planRepository.getAllItemByPlanId(planId).onEach {
            val list = it.map { item -> item.toItemMapper() }.toMutableStateList()
            planItems.clear()
            planItems.addAll(list)
        }.launchIn(viewModelScope)
        viewModelScope.launch {
            itemRepository.getItemList().collectLatest {
                itemList = it.map { item ->
                    if (item.categoryId == null) {
                        item.toItemMapper()
                    } else {
                        val category = itemRepository.getCategoryById(item.categoryId)
                        item.toItemMapper().copy(
                            categoryName = category.categoryName
                        )
                    }
                }.toMutableStateList()
            }
        }
    }

    fun showAddItemScreen(isShow: Boolean) {
        _uiState.update {
            it.copy(
                isShowAddItemScreen = isShow
            )
        }
    }

    fun dismissErrorMessage() {
        _uiState.update {
            it.copy(
                errorMessage = ""
            )
        }
    }

    fun reset() {
        _uiState.update {
            PlanDetailUiState()
        }
    }

    fun removePlanItem(itemId: Int) {
        viewModelScope.launch { planRepository.planItemRemove(planId, itemId) }
    }

    fun addItemToPlan(items: List<ItemEntity>) {
        if (items.isEmpty()) {
            return
        }

        planRepository.addPlanItem(planId, items).onEach { result ->
            when (result) {
                is SaveResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, loadingMessage = "", errorMessage = result.errorMessage.toString())
                    }
                }

                SaveResult.Loading -> {
                    _uiState.update {
                        it.copy(isLoading = true, loadingMessage = "Adding Item please wait....")
                    }
                }

                SaveResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, loadingMessage = "", isSuccess = true)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private lateinit var mediaPlayer: MediaPlayer
    fun setMediaPlayer(mediaPlayer: MediaPlayer) {
        this.mediaPlayer = mediaPlayer
    }

    override fun onGetData(rfidList: ArrayList<String>) {
        viewModelScope.launch {
            if (rfidList.isEmpty()) {
                return@launch
            }
            rfidList.forEach { rfid ->
                if (!rfidScanList.contains(rfid)) {
                    rfidScanList.add(rfid)
                    planItems.forEach { item ->
                        if (item.rfid == rfid) {
                            val iIndex = planItems.indexOf(item)
                            val mItem = planItems[iIndex].copy()
                            mItem.isScan = true
                            planItems[iIndex] = mItem
                            if (!mediaPlayer.isPlaying) {
                                mediaPlayer.start()
                            }
                        }
                    }
                    _uiState.update { currentState ->
                        currentState.copy()
                    }
                }
            }
            Log.e("RFID DATA", rfidList.toString())
        }
    }

    override fun onScanUpdate(isScan: Boolean) {
        if (isScan) {
            rfidScanList.clear()
            planItems.forEach { item ->
                val iIndex = planItems.indexOf(item)
                val mItem = planItems[iIndex].copy()
                mItem.isScan = false
                planItems[iIndex] = mItem
            }
            _uiState.update { currentState ->
                currentState.copy()
            }
        }
    }


    companion object {

        class FACTORY(
            private val planId: Int, private val planRepository: PlanRepository, private val itemRepository: ItemRepository
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlanDetailViewModel(planId, planRepository, itemRepository) as T
            }
        }
    }
}

data class PlanDetailUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val errorMessage: String = "",
    val isSuccess: Boolean = false,
    val isShowAddItemScreen: Boolean = false,
)