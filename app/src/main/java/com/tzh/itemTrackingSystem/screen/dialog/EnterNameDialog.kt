package com.tzh.itemTrackingSystem.screen.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun EnterNameDialog(showToast: (String) -> Unit, name: String, onSave: (String) -> Unit) {
    Dialog(onDismissRequest = {}) {
        ElevatedCard(
            modifier = Modifier.padding(8.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),

            ) {
            var enterName by remember {
                mutableStateOf(name)
            }
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter you name : ", style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 18.sp, letterSpacing = 4.sp
                    )
                )
                OutlinedTextField(
                    value = enterName,
                    onValueChange = {
                        enterName = it
                    },
                )
                Button(
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 8.dp),
                    onClick = {
                        if (enterName.isEmpty()) {
                            showToast("Please enter your name")
                            return@Button
                        }
                        onSave(enterName)
                    },
                ) {
                    Text("Save")
                }
            }
        }
    }
}