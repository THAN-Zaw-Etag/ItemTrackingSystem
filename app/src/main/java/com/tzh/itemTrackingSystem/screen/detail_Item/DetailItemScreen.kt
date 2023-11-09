package com.tzh.itemTrackingSystem.screen.detail_Item

import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tzh.itemTrackingSystem.R
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.screen.common.ControlBluetoothLifecycle
import com.tzh.itemTrackingSystem.screen.common.TitleText
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ui.theme.RFIDTextColor

@Composable
fun DetailItemScreen(
    itemId: Int,
    itemRepository: ItemRepository,
    bluetoothService: BluetoothService,
    viewModel: DetailItemViewModel = viewModel(factory = DetailItemViewModel.Companion.FACTORY(itemId, itemRepository)),
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = uiState.item
    val lifecycleOwner = LocalLifecycleOwner.current
    val mContext = LocalContext.current
    val mMediaPlayer by remember { mutableStateOf(MediaPlayer.create(mContext, R.raw.beep)) }
    ControlBluetoothLifecycle(
        LocalLifecycleOwner.current,
        onCreate = {
            viewModel.setBluetoothService(bluetoothService)
        },
        onResume = {
            viewModel.setMediaPlayer(mMediaPlayer)
            bluetoothService.setOnDataAvailableListener(viewModel)
            bluetoothService.setScanStateListener(viewModel)
        },
        onPause = {
            bluetoothService.stopScan()
            bluetoothService.removeOnDataAvailableListener()
            bluetoothService.removeScanStateListener(viewModel)
            mMediaPlayer.release()
        },
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        TitleText("Detail", modifier = Modifier.fillMaxWidth())
        ElevatedCard(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                RowTextItem("Item Name : ", item?.itemName ?: "-")
                RowTextItem("Description : ", item?.desc ?: "-")
                RowTextItem("RFID : ", item?.rfid ?: "-")
                RowTextItem("Category :", item?.categoryName ?: "-")

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        for (i in 7 downTo 1) {
                            ScanLevelDivider(
                                isLevelHeight = i + 1 < uiState.scanLevel, modifier = Modifier.fillMaxWidth(i / 10f)
                            )
                        }
                        Text(text = "Scan Level")
                    }
                }
            }
        }
    }
}

@Composable
fun ScanLevelDivider(isLevelHeight: Boolean, modifier: Modifier) {
    val scanColor by animateColorAsState(
        targetValue = if (isLevelHeight) {
            Color.Green.copy(alpha = 0.8f)
        } else {
            Color.DarkGray.copy(alpha = 0.5f)
        },
        label = "",
    )
    HorizontalDivider(
        thickness = 8.dp, modifier = modifier.clip(RoundedCornerShape(30.dp)), color = scanColor
    )
}

@Composable
fun RowTextItem(title: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val style = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Justify, fontFamily = FontFamily.Serif, color = RFIDTextColor, fontWeight = FontWeight.Bold
        )
        Text(
            text = title, style = style
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label, style = style.copy(color = Color.Black, fontWeight = FontWeight.Thin)
        )
    }
}