package com.tzh.itemTrackingSystem.screen.createCategory

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.screen.common.TextFieldWithTitle
import com.tzh.itemTrackingSystem.screen.common.TitleText
import java.util.Locale

@Composable
fun CreateCategoryScreen(save: (Category) -> Unit) {
    val context = LocalContext.current

    var categoryName by remember {
        mutableStateOf("")
    }
    var categoryDesc by remember {
        mutableStateOf("")
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        TitleText("ADD Category")
        TextFieldWithTitle(textChange = {
            categoryName = it
        }, title = "Name", value = categoryName)

        TextFieldWithTitle(textChange = {
            categoryDesc = it
        }, title = "Description", value = categoryDesc)

        ElevatedButton(onClick = {
            if (categoryName.isEmpty()) {
                Toast.makeText(context, "Please enter categoryName", Toast.LENGTH_LONG).show()
                return@ElevatedButton
            }
            save(
                Category(
                    categoryName = categoryName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() },
                    description = categoryDesc
                )
            )
        }, modifier = Modifier.align(Alignment.End)) {
            Text(text = "Save")
        }
    }

}

