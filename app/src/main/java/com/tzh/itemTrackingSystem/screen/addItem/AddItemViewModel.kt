package com.tzh.itemTrackingSystem.screen.addItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.data.entity.Item
import com.tzh.itemTrackingSystem.data.repository.CategoryRepository
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.service.OnDataAvailableListener
import com.tzh.itemTrackingSystem.ulti.Constant.DefaultCategory
import com.tzh.itemTrackingSystem.ulti.SuccessListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class AddItemViewModel(private val itemRepository: ItemRepository, private val categoryRepository: CategoryRepository) :
    ViewModel(), OnDataAvailableListener {

    private val categoryList = categoryRepository.getCategoryList()

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState = combine(categoryList, _uiState) { categories, addItemUiState ->
        val currentList = listOf(DefaultCategory) + categories
        val newState = addItemUiState.copy(
            categoryList = currentList,
            isSaveEnabled = addItemUiState.itemName.isNotEmpty()
        )
        newState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(3000), AddItemUiState())

    fun updateItemName(name: String) {
        _uiState.update {
            it.copy(
                itemName = name
            )
        }
    }

    fun updateItemDescription(desc: String) {
        _uiState.update {
            it.copy(
                itemDescription = desc
            )
        }
    }

    fun updateIsForShop() {
        _uiState.update {
            it.copy(
                isForShop = !it.isForShop
            )
        }
    }

    fun selectedCategory(category: Category) {
        _uiState.update {
            it.copy(
                selectedCategory = category
            )
        }
    }

    fun showCreateCategoryDialog() {
        _uiState.update {
            it.copy(
                isShowCategoryDialog = true
            )
        }
    }

    fun dismissCategoryDialog() {
        _uiState.update {
            it.copy(
                isShowCategoryDialog = false
            )
        }
    }

    fun addCategory(category: Category, success: SuccessListener) {
        viewModelScope.launch {
            val latestCategory = categoryRepository.addCategory(category = category)
            _uiState.update {
                it.copy(selectedCategory = latestCategory)
            }
            success()
        }
    }

    fun addItem(success: SuccessListener, showToast: (String) -> Unit) {
        viewModelScope.launch {
            val data = uiState.value

            val item = Item(
                itemName = data.itemName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() },
                desc = data.itemDescription,
                rfid = data.rfidText.ifEmpty { null },
                categoryId = if (data.selectedCategory.id == 0) null else data.selectedCategory.id,
                isForShop = data.isForShop
            )
            if (data.editItem != null) {
                if (data.rfidText == data.editItem.rfid) {
                    itemRepository.updateItem(item.copy(id = data.editItem.id))
                } else {
                    if (itemRepository.checkRfid(data.rfidText)) {
                        showToast("Rfid already exist .Please change to another one")
                        return@launch
                    }
                    itemRepository.updateItem(item.copy(id = data.editItem.id))
                }
            } else {
                if (itemRepository.checkRfid(data.rfidText)) {
                    showToast("Rfid already exist .Please change to another one")
                    return@launch
                }
                itemRepository.addItem(item)
            }
            success()
        }
    }

    fun setEditItem(item: Item) {
        updateData(item)
    }

    fun resetData() {
        _uiState.update {
            AddItemUiState()
        }
    }

    private fun updateData(editItem: Item) {
        viewModelScope.launch {
            val currentCategory =
                if (editItem.categoryId == null) DefaultCategory else categoryRepository.getCategory(editItem.categoryId)
            _uiState.update {
                it.copy(
                    editItem = editItem,
                    rfidText = editItem.rfid ?: "",
                    itemName = editItem.itemName,
                    itemDescription = editItem.desc ?: "",
                    isForShop = editItem.isForShop,
                    selectedCategory = currentCategory
                )
            }
        }
    }

    override fun onGetData(rfidList: ArrayList<String>) {
        viewModelScope.launch {
            if (rfidList.isEmpty()) {
                return@launch
            }
            _uiState.update {
                it.copy(
                    rfidText = rfidList.first()
                )
            }
        }
    }


    companion object {
        class FACTORY(val repository: ItemRepository, val categoryRepository: CategoryRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddItemViewModel(repository, categoryRepository) as T
            }
        }
    }
}

data class AddItemUiState(
    val rfidText: String = "",
    val itemName: String = "",
    val itemDescription: String = "",
    val isForShop: Boolean = false,
    val isShowCategoryDialog: Boolean = false,
    val categoryList: List<Category> = emptyList(),
    val selectedCategory: Category = DefaultCategory,
    val isSaveEnabled: Boolean = false,
    val editItem: Item? = null
)