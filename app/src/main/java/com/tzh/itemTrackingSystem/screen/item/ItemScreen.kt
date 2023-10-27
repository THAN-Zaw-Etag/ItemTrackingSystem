package com.tzh.itemTrackingSystem.screen.item

import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import com.tzh.itemTrackingSystem.data.entity.Item
import com.tzh.itemTrackingSystem.data.repository.ItemRepository
import com.tzh.itemTrackingSystem.screen.common.ControlBluetoothLifecycle
import com.tzh.itemTrackingSystem.screen.common.SearchView
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ui.theme.RFIDTextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    modifier: Modifier,
    bluetoothService: BluetoothService,
    itemRepository: ItemRepository,
    viewModel: ItemViewModel = viewModel(factory = ItemViewModel.Companion.FACTORY(itemRepository)),
    editItem: (Item) -> Unit
) {
    ControlBluetoothLifecycle(
        LocalLifecycleOwner.current,
        onCreate = {
            bluetoothService.setOnDataAvailableListener(viewModel)
        },
        onPause = {
            bluetoothService.removeOnDataAvailableListener()
        },
    )
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val itemList = uiState.list
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchView(modifier = Modifier.weight(1f), defaultValue = uiState.searchText, label = "Search Item") {
                viewModel.updateSearchText(it)
            }
            IconButton(onClick = {

            }) {
                Image(painter = painterResource(id = R.drawable.filter), contentDescription = "Filter")
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(itemList.size, key = { index -> itemList[index].id }) { index ->

                CardItem(
                    item = itemList[index], delete = {
                        viewModel.deleteItem(
                            it.id,
                            successListener = {
                                Toast.makeText(context, "Successfully delete", Toast.LENGTH_LONG).show()
                            },
                            showToast = {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            },
                        )
                    }, editItem = editItem
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.CardItem(item: Item, delete: (Item) -> Unit, editItem: (Item) -> Unit) {

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
                ElevatedButton(onClick = { delete(item) }) {
                    Text(text = "Confirm")
                }

            },
            text = { Text(text = "Are you sure want to delete this item : ${item.itemName}") },
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
            .fillMaxWidth()
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
                    text = item.itemName, style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Justify,
                    ), modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = (item.desc ?: "-").ifEmpty { "-" }, style = MaterialTheme.typography.bodyMedium.copy(
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
                            append((item.rfid ?: "-").ifEmpty { "-" })
                        }
                    }, style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Justify,
                    ), modifier = Modifier.fillMaxWidth()
                )
            }
            Column(modifier = Modifier, verticalArrangement = Arrangement.SpaceEvenly) {
                IconButton(
                    onClick = { deleteId = item.id },
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = .8f))
                }
                IconButton(
                    onClick = { editItem(item) },
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Delete", tint = Color.DarkGray.copy(alpha = .8f))
                }
            }
        }
    }
}
