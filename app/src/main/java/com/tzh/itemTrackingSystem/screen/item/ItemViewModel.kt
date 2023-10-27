package com.tzh.itemTrackingSystem.screen.item

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.data.entity.Item
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.service.OnDataAvailableListener
import com.tzh.itemTrackingSystem.ulti.SuccessListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemViewModel(private val repository: ItemRepository) : ViewModel(), OnDataAvailableListener {

    val categoryList = repository.getItemsCategory()

    private val rfidScanList = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(ItemScreenUiState())

    val uiState = combine(_uiState, repository.getItemList()) { itemUiState, list ->
        val filterList = list.filter { item ->
            if (itemUiState.searchText.isNotEmpty()) item.itemName.contains(itemUiState.searchText, true) else true
        }.filter { item ->
            if (itemUiState.categoryId == 0) true else item.categoryId == itemUiState.categoryId
        }.filter { item ->
            if (itemUiState.isForShop == null) true else item.isForShop == itemUiState.isForShop
        }
        itemUiState.copy(list = filterList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(3000), ItemScreenUiState())

    fun updateSearchText(text: String) {
        _uiState.update {
            it.copy(searchText = text)
        }
    }

    fun deleteItem(id: Int, successListener: SuccessListener, showToast: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteItem(id)
                successListener()
            } catch (e: Exception) {
                showToast(e.message.toString())
            }
        }
    }

    fun clearRfidList() {

    }

    override fun onGetData(rfidList: ArrayList<String>) {
        viewModelScope.launch {
            if (rfidList.isEmpty()) {
                return@launch
            }
            rfidList.forEach { rfid ->
                if (!rfidScanList.contains(rfid)) {
                    rfidScanList.add(rfid)
                }
            }
        }
    }

    companion object {
        class FACTORY(val repository: ItemRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ItemViewModel(repository) as T
            }
        }
    }
}

data class ItemScreenUiState(
    val searchText: String = "",
    val list: List<Item> = emptyList(),
    val categoryId: Int = 0,
    val isForShop: Boolean? = null,
)