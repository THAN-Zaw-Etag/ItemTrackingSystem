package com.tzh.itemTrackingSystem.screen.item

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tzh.itemTrackingSystem.R
import com.tzh.itemTrackingSystem.data.model.Item
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.screen.common.ControlBluetoothLifecycle
import com.tzh.itemTrackingSystem.screen.common.SearchView
import com.tzh.itemTrackingSystem.screen.dialog.ItemFilterDialog
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ui.theme.RFIDTextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    modifier: Modifier,
    bluetoothService: BluetoothService,
    itemRepository: ItemRepository,
    viewModel: ItemViewModel = viewModel(factory = ItemViewModel.Companion.FACTORY(itemRepository)),
    editItem: (Item) -> Unit,
    onClick: (Item) -> Unit
) {
    ControlBluetoothLifecycle(
        LocalLifecycleOwner.current,
        onCreate = {

        },
        onResume = {
            bluetoothService.setScanStateListener(viewModel)
            bluetoothService.setOnDataAvailableListener(viewModel)
        },
        onPause = {
            bluetoothService.removeOnDataAvailableListener()
            bluetoothService.removeScanStateListener(viewModel)
        },
    )
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val categoryList = viewModel.categoryList
    val itemList = viewModel.itemList
    var showFilter by remember {
        mutableStateOf(false)
    }
    if (showFilter) {
        ItemFilterDialog(
            isForShop = uiState.isForShop,
            category = uiState.filterCategory,
            categoryList = categoryList,
            dismiss = { showFilter = false },
            filter = { category, isForShop ->
                showFilter = false
                viewModel.updateFilterItem(category, isForShop)
            },
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchView(modifier = Modifier.weight(1f), defaultValue = uiState.searchText, label = "Search Item") {
                viewModel.updateSearchText(it)
            }
            IconButton(onClick = {
                showFilter = true
            }) {
                Image(painter = painterResource(id = R.drawable.filter), contentDescription = "Filter")
            }
        }
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Getting data please wait....", style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.Red, fontFamily = FontFamily.Serif, letterSpacing = 4.sp, textAlign = TextAlign.Center
                    )
                )
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        AnimatedVisibility(modifier = Modifier.weight(1f), visible = !uiState.isLoading) {
            if (itemList.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "No data not found",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.Red, fontFamily = FontFamily.Serif, letterSpacing = 4.sp, textAlign = TextAlign.Center
                        )
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(itemList, key = { item -> item.id }) { item: Item ->
                        CardItem(itemEntity = item, delete = {
                            viewModel.deleteItem(
                                it.id,
                                successListener = {
                                    Toast.makeText(context, "Successfully delete", Toast.LENGTH_LONG).show()
                                },
                                showToast = {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                },
                            )
                        }, editItem = editItem, onClick = {
                            onClick(item)
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LazyItemScope.CardItem(itemEntity: Item, delete: (Item) -> Unit, editItem: (Item) -> Unit, onClick: () -> Unit) {

    var deleteId: Int? by remember { mutableStateOf(null) }
    val backgroundColor by animateColorAsState(
        targetValue = if (itemEntity.isScan) {
            Color.Green.copy(alpha = .2f)
        } else {
            CardDefaults.cardColors().containerColor
        },

        label = "bgColor"
    )
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
                ElevatedButton(onClick = { delete(itemEntity) }) {
                    Text(text = "Confirm")
                }

            },
            text = {
                Text(
                    buildAnnotatedString {

                        val defaultStyle = MaterialTheme.typography.bodyMedium
                        withStyle(defaultStyle.toSpanStyle()) {
                            append("Are you sure want to delete this item : ")
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
            .fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = backgroundColor), onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
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
            Column(modifier = Modifier, verticalArrangement = Arrangement.SpaceEvenly) {
                IconButton(
                    onClick = { deleteId = itemEntity.id },
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = .4f))
                }
                IconButton(
                    onClick = { editItem(itemEntity) },
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Delete", tint = Color.DarkGray.copy(alpha = .8f))
                }
            }
        }
    }
}
