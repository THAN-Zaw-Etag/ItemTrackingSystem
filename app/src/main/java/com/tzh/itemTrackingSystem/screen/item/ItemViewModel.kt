package com.tzh.itemTrackingSystem.screen.item

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.data.mapper.toItemMapper
import com.tzh.itemTrackingSystem.data.model.Item
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.service.OnDataAvailableListener
import com.tzh.itemTrackingSystem.service.ScanStateListener
import com.tzh.itemTrackingSystem.ulti.Constant.DefaultCategory
import com.tzh.itemTrackingSystem.ulti.SuccessListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemViewModel(private val repository: ItemRepository) : ViewModel(), OnDataAvailableListener,
    ScanStateListener {

    var categoryList = mutableStateListOf<Category>()

    var itemList = mutableStateListOf<Item>()
    private val rfidScanList = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(ItemScreenUiState())

    val uiState = combine(
        _uiState,
        repository.getItemList(),
        repository.getItemsCategory()
    ) { itemUiState, list, cList ->
        val filterList = list.filter { item ->
            if (itemUiState.searchText.isNotEmpty()) {
                item.itemName.contains(itemUiState.searchText, true)
            } else {
                true
            }
        }.filter { item ->
            if (itemUiState.filterCategory.id == 0) {
                true
            } else {
                item.categoryId == itemUiState.filterCategory.id
            }
        }.filter { item ->
            if (itemUiState.isForShop == null) {
                true
            } else {
                item.isForShop == itemUiState.isForShop
            }
        }.map { it.toItemMapper() }

        val mList = cList.mapNotNull { it.category }.toMutableList().apply {
            add(0, DefaultCategory)
        }.toSet().toList()
        categoryList = mList.toMutableStateList()
        itemList = filterList.toMutableStateList()
        itemUiState.copy(isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(3000), ItemScreenUiState())

    fun updateSearchText(text: String) {
        _uiState.update {
            it.copy(searchText = text)
        }
    }

    fun updateFilterItem(category: Category, isForShop: Boolean?) {
        _uiState.update {
            it.copy(filterCategory = category, isForShop = isForShop)
        }
    }

    fun deleteItem(id: Int, successListener: SuccessListener, showToast: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteItem(id)
                itemList.forEach { if (it.id == id) itemList.remove(it) }
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
                    itemList.find { it.rfid == rfid }?.let {
                        val mItem = it
                        mItem.isScan = true
//                        itemList[iIndex] = mItem
                        Log.e("RFID is found", "TRUE")
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
            itemList.forEach { item ->
//                val iIndex = itemList.indexOf(item)
//                val mItem = itemList[iIndex].copy()
                item.isScan = false
//                itemList[iIndex] = mItem
            }
            _uiState.update { currentState ->
                currentState.copy()
            }
        }
    }


    companion object {
        class FACTORY(private val repository: ItemRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ItemViewModel(repository) as T
            }
        }
    }

}

data class ItemScreenUiState(
    val isLoading: Boolean = true,
    val searchText: String = "",
    val filterCategory: Category = DefaultCategory,
    val isForShop: Boolean? = null,
)