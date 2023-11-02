package com.tzh.itemTrackingSystem.screen.detail_Item

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tzh.itemTrackingSystem.data.mapper.toItemMapper
import com.tzh.itemTrackingSystem.data.model.Item
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.service.OnDataAvailableListener
import com.tzh.itemTrackingSystem.service.ScanStateListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailItemViewModel(private val itemId: Int, private val itemRepository: ItemRepository) : ViewModel(),
    OnDataAvailableListener, ScanStateListener {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val itemEntity = itemRepository.findItemCategoryById(itemId)
            itemEntity?.let { item ->
                _uiState.update {
                    it.copy(
                        item = item.itemEntity.toItemMapper().copy(
                            categoryName = item.categoryName ?: ""
                        )
                    )
                }
            }
        }
    }

    private lateinit var mediaPlayer: MediaPlayer
    fun setMediaPlayer(mediaPlayer: MediaPlayer) {
        this.mediaPlayer = mediaPlayer
    }

    private lateinit var bluetoothService: BluetoothService

    fun setBluetoothService(bluetoothService: BluetoothService) {
        this.bluetoothService = bluetoothService
    }


    fun clearScanResult() {
        bluetoothService.scanResult.clear()
    }

    companion object {
        class FACTORY(val itemId: Int, val itemRepository: ItemRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailItemViewModel(itemId, itemRepository = itemRepository) as T
            }
        }
    }

    var runningJob: Job? = null
    override fun onGetData(rfidList: ArrayList<String>) {
        if (rfidList.isEmpty()) {
            return
        }

        if (runningJob?.isActive == true) {
            return
        }
        runningJob = viewModelScope.launch {
            rfidList.take(2).forEach { rfid ->
                if (rfid == uiState.value.item?.rfid) {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                    }
                    if (uiState.value.scanLevel < 14) {
                        _uiState.update {
                            it.copy(
                                scanLevel = it.scanLevel + 2
                            )
                        }
                    }

                } else {
//                    if (uiState.value.scanLevel > 1) {
//                        _uiState.update {
//                            it.copy(
//                                scanLevel = it.scanLevel - 1
//                            )
//                        }
//                    }
                }
            }
            clearScanResult()
        }
        Log.e("RFID List", rfidList.toList().toString())
    }

    override fun onScanUpdate(isScan: Boolean) {
        _uiState.update {
            it.copy(
                scanLevel = if (isScan) 0 else it.scanLevel,
                isScan = isScan,
            )
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                while (uiState.value.isScan) {
                    if (uiState.value.scanLevel > 1) {
                        delay(500)
                        _uiState.update {
                            it.copy(
                                scanLevel = it.scanLevel - 1
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DetailUiState(
    val item: Item? = null, val scanLevel: Int = 0, val isScan: Boolean = false
)