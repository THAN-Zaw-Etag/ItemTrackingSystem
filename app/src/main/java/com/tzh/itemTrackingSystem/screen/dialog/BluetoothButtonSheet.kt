package com.tzh.itemTrackingSystem.screen.dialog

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tzh.itemTrackingSystem.screen.common.TitleText
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ulti.Extensions.showToast

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BluetoothBottomSheet(
    isShow: Boolean,
    sheetState: SheetState,
    bluetoothService: BluetoothService,
    deviceList: List<BluetoothDevice>,
    dismiss: () -> Unit,
    onConnect: (String) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val bottomPadding = WindowInsets.navigationBars.getBottom(density).dp
    var isScanning by remember {
        mutableStateOf(false)
    }
    var selectedBtDevice: BluetoothDevice? by remember {
        mutableStateOf(null)
    }

    if (isShow) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedBtDevice = null

                if (isScanning) {
                    isScanning = false
                    bluetoothService.scanBtDevice(false)
                }

                dismiss()
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.8f)
                .padding(bottom = bottomPadding)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TitleText("Bluetooth Devices", modifier = Modifier.padding(8.dp))

                IconButton(
                    onClick = {
                        if (isScanning) {
                            isScanning = false
                            bluetoothService.scanBtDevice(false)
                        }
                        selectedBtDevice = null
                        dismiss()
                    }, modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "")
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(deviceList.sortedByDescending {
                    it.name ?: "-"
                }, key = { it.address }) { device: BluetoothDevice ->

                    val cardBgColor by animateColorAsState(
                        targetValue = if (device.address == selectedBtDevice?.address) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.background.copy(alpha = 0.7f), label = ""
                    )

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        onClick = {
                            selectedBtDevice = device
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = cardBgColor,
                        ),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = 0.dp

                        ),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = device.name ?: "-" + " :",
                                modifier = Modifier.padding(8.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = device.address,
                                modifier = Modifier.padding(8.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(8.dp), Arrangement.spacedBy(8.dp)
            ) {
                ElevatedButton(onClick = {
                    isScanning = !isScanning
                    bluetoothService.scanBtDevice(isScanning)
                }) {
                    Text(text = if (isScanning) "Stop BT Scan" else "Start BT Scan")
                }
                ElevatedButton(onClick = {
                    if (selectedBtDevice == null) {
                        context.showToast("Please selected the bt address")
                        return@ElevatedButton
                    }
                    if (isScanning) {
                        isScanning = false
                        bluetoothService.scanBtDevice(false)
                    }
                    onConnect(selectedBtDevice!!.address)
                    selectedBtDevice = null
                }) {
                    Text(text = "Connect")
                }
            }
        }
    }
}
