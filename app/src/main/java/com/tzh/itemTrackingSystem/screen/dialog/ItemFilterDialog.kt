package com.tzh.itemTrackingSystem.screen.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.screen.common.MyAppDropDown
import com.tzh.itemTrackingSystem.ui.theme.RFIDTextColor
import com.tzh.itemTrackingSystem.ulti.Constant.filterCardItemList
import com.tzh.itemTrackingSystem.ulti.Extensions.cardIdToIsForShop
import com.tzh.itemTrackingSystem.ulti.Extensions.toFilterCardId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFilterDialog(
    isForShop: Boolean?,
    category: Category,
    categoryList: List<Category>,
    dismiss: () -> Unit,
    filter: (
        Category, Boolean?
    ) -> Unit,
) {

    var selectedCategory by remember {
        mutableStateOf(category)
    }
    var selectedCard by remember {
        mutableIntStateOf(isForShop.toFilterCardId())
    }
    val filterCardList = remember {
        mutableStateListOf<FilterCardItem>().apply {
            addAll(filterCardItemList)
        }
    }

    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = {},
    ) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
            ) {
                val style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif,
                    color = RFIDTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Filter", style = style, modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Justify,
                        fontFamily = FontFamily.Serif,
                        color = RFIDTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Category", modifier = Modifier.weight(.3f), style = style
                    )
                    MyAppDropDown(
                        Modifier.weight(.8f), list = categoryList.map { it.categoryName }, value = selectedCategory.categoryName
                    ) { categoryName ->
                        categoryList.find { it.categoryName == categoryName }?.let { selectedCategory = it }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    filterCardList.forEach { item ->
                        val selectedColor = if (item.id == selectedCard) {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            CardDefaults.cardColors()

                        }
                        ElevatedCard(
                            modifier = Modifier
                                .width(100.dp)
                                .padding(8.dp),
                            onClick = { selectedCard = item.id },
                            colors = selectedColor,
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = item.desc,
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    ElevatedButton(onClick = { dismiss() }) {
                        Text(text = "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ElevatedButton(
                        onClick = {
                            filter(
                                selectedCategory, selectedCard.cardIdToIsForShop()
                            )
                        },
                    ) {
                        Text(text = "Search")
                    }
                }
            }
        }
    }
}

class FilterCardItem(
    var id: Int, var desc: String
)