package com.tzh.itemTrackingSystem.screen.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchView(
    modifier: Modifier = Modifier, defaultValue: String, label: String, updateState: (String) -> Unit,
) {
    OutlinedTextField(
        value = defaultValue,
        onValueChange = { value ->
            updateState(value)
        },
        label = { Text(label, color = Color.DarkGray.copy(alpha = 0.5f)) },
        modifier = modifier, textStyle = MaterialTheme.typography.bodyMedium,
        leadingIcon = {
            Icon(
                Icons.Default.Search, contentDescription = "", modifier = Modifier
                    .padding(15.dp)
                    .size(24.dp)
            )
        },
        trailingIcon = {
            if (defaultValue != "") {
                IconButton(onClick = {
                    updateState("") // Remove text from TextField when you press the 'X' icon
                }) {
                    Icon(
                        Icons.Default.Close, contentDescription = "", modifier = Modifier
                            .padding(15.dp)
                            .size(24.dp)
                    )
                }
            }
        },
        singleLine = true, shape = CircleShape,
    )
}
