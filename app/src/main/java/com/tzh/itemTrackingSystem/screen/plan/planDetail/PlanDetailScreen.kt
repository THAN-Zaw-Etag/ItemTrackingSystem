package com.tzh.itemTrackingSystem.screen.plan.planDetail

import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tzh.itemTrackingSystem.R
import com.tzh.itemTrackingSystem.data.entity.ItemEntity
import com.tzh.itemTrackingSystem.data.mapper.toItemEntityMapper
import com.tzh.itemTrackingSystem.data.model.Item
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.data.repository.PlanRepository
import com.tzh.itemTrackingSystem.screen.common.ControlBluetoothLifecycle
import com.tzh.itemTrackingSystem.screen.common.TitleText
import com.tzh.itemTrackingSystem.screen.dialog.LoadingDialog
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ui.theme.RFIDTextColor
import com.tzh.itemTrackingSystem.ulti.Extensions.showToast

@Composable
fun PlanDetailScreen(
    bluetoothService: BluetoothService,
    planId: Int,
    planName: String,
    planRepository: PlanRepository,
    itemRepository: ItemRepository,
    viewModel: PlanDetailViewModel = viewModel(
        factory = PlanDetailViewModel.Companion.FACTORY(
            planId, planRepository, itemRepository
        )
    )
) {
    val itemList = viewModel.planItems
    val uiState by viewModel.uiState.collectAsState()
    val mContext = LocalContext.current
    val mMediaPlayer by remember { mutableStateOf(MediaPlayer.create(mContext, R.raw.beep)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            mContext.showToast(uiState.errorMessage)
            viewModel.dismissErrorMessage()
        }
    }
    ControlBluetoothLifecycle(
        lifecycleOwner,
        onCreate = {
            viewModel.setMediaPlayer(mMediaPlayer)
        },
        onResume = {
            bluetoothService.setScanStateListener(viewModel)
            bluetoothService.setOnDataAvailableListener(viewModel)
        },
        onPause = {
            bluetoothService.stopScan()
            bluetoothService.removeOnDataAvailableListener()
            bluetoothService.removeScanStateListener(viewModel)
            mMediaPlayer.release()
        },
    )
    LoadingDialog(uiState.isLoading, uiState.loadingMessage)

    if (uiState.isShowAddItemScreen) {
        AddItemScreen(
            uiState.itemList, uiState.planItemList,
            dismiss = {
                viewModel.showAddItemScreen(false)
            },
            addItem = {
                viewModel.showAddItemScreen(false)
                viewModel.addItemToPlan(it)
            },
        )
    }

    Column(Modifier.fillMaxSize()) {
        TitleText("Plan : $planName")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                viewModel.showAddItemScreen(true)
                bluetoothService.stopScan()
            },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Add Item")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(itemList, key = { item -> item.id }) { item: Item ->
                CardItem(
                    itemEntity = item,
                    remove = {
                        viewModel.removePlanItem(item.id)
                    },
                )
            }
        }
    }
}


@Composable
private fun AddItemScreen(
    allItemList: List<Item>, planItems: List<Item>, dismiss: () -> Unit, addItem: (List<ItemEntity>) -> Unit
) {
    Dialog(
        onDismissRequest = dismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val itemList = remember {
            allItemList.toMutableStateList().apply {
                planItems.forEach { planItem ->
                    val item = this.find { planItem.id == it.id }
                    this.remove(item)
                }
            }
        }
        var selectedAll by remember {
            mutableStateOf(false)
        }
        Column(
            modifier = Modifier
                .padding(8.dp)
                .background(Color.White)
                .padding(8.dp)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            TitleText(text = "Items")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .border(1.dp, color = Color.Black)
                    .height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Justify,
                    fontFamily = FontFamily.Serif,
                    color = RFIDTextColor,
                )
                Checkbox(
                    checked = selectedAll, onCheckedChange = {
                        selectedAll = !selectedAll
                        if (selectedAll) {
                            itemList.forEach { item ->
                                item.isCheck = true
//
//                                val index = itemList.indexOf(item)
//                                itemList[index] = itemList[index].copy(isCheck = true)
                            }
                        } else {
                            itemList.forEach { item ->
                                item.isCheck = false
//                                val index = itemList.indexOf(item)
//                                itemList[index] = itemList[index].copy(isCheck = false)
                            }
                        }
                    }, modifier = Modifier
                        .width(36.dp)
                        .fillMaxHeight()
                )
                VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp)
                Text(text = "Product", style = style, modifier = Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp)
                Text(text = "Category", style = style, modifier = Modifier.weight(1f))
            }
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(itemList, key = { it.id }) { item ->
                    val style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Justify, fontFamily = FontFamily.Serif, fontSize = 16.sp
                    )
//                    var isSelected by remember {
//                        mutableStateOf(false)
//                    }
//                    Log.e("asdf", selectedItems.toList().toString())
                    Row(
                        modifier = Modifier
                            .border(1.dp, color = Color.Black)
                            .height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isCheck, onCheckedChange = {
//                                val index = itemList.indexOf(item)
//                                itemList[index] = itemList[index].copy(isCheck = !item.isCheck)
                                item.isCheck = !item.isCheck
                                selectedAll = itemList.size == itemList.filter { it.isCheck }.size
                            }, modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight()
                        )
                        VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp)
                        Text(
                            text = item.itemName,
                            style = style,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp)
                        Text(
                            text = item.categoryName.ifEmpty { "-" },
                            style = style,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            ElevatedButton(onClick = {
                addItem(itemList.filter { it.isCheck }.map { it.toItemEntityMapper() }.toList())
            }, modifier = Modifier.align(Alignment.End)) {
                Text(text = "Add Item")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.CardItem(itemEntity: Item, remove: () -> Unit) {

    val backgroundColor by animateColorAsState(
        targetValue = if (itemEntity.isScan) {
            Color.Green.copy(alpha = .2f)
        } else {
            CardDefaults.cardColors().containerColor
        },

        label = "bgColor"
    )

    var deleteId: Int? by remember { mutableStateOf(null) }
    if (deleteId != null) {
        AlertDialog(
            icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Delete Alert") },
            onDismissRequest = {},
            dismissButton = {
                ElevatedButton(onClick = { deleteId = null }) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                ElevatedButton(onClick = { remove() }) {
                    Text(text = "Confirm")
                }
            },
            text = {
                Text(
                    buildAnnotatedString {
                        val defaultStyle = MaterialTheme.typography.bodyMedium
                        withStyle(defaultStyle.toSpanStyle()) {
                            append("Are you sure want to remove this item : ")
                        }
                        withStyle(
                            defaultStyle.toSpanStyle().copy(
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(itemEntity.itemName)
                        }
                    },
                )
            },
            title = { Text(text = "Confirm !", color = Color.Red) },
        )
    }

    Card(
        modifier = Modifier
            .animateItemPlacement(
                animationSpec = tween(
                    durationMillis = 500,
                    easing = LinearOutSlowInEasing,
                )
            )
            .padding(8.dp)
            .fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box {
            IconButton(
                onClick = { deleteId = itemEntity.id }, modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = .4f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(8.dp)) {
                Text(
                    text = itemEntity.itemName, style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Justify,
                    ), modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = (itemEntity.desc ?: "-").ifEmpty { "-" }, style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Justify,
                    ), modifier = Modifier.fillMaxWidth()
                )
                Text(
                    buildAnnotatedString {
                        val style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Justify, fontFamily = FontFamily.Serif
                        )
                        withStyle(
                            style.copy(
                                color = RFIDTextColor, fontWeight = FontWeight.Bold
                            ).toSpanStyle()
                        ) {
                            append("RFID : ")
                        }
                        withStyle(
                            style.copy().toSpanStyle()
                        ) {
                            append((itemEntity.rfid ?: "-").ifEmpty { "-" })
                        }
                    }, style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Justify,
                    ), modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
