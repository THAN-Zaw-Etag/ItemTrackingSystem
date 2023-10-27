package com.tzh.itemTrackingSystem.screen.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tzh.itemTrackingSystem.R
import com.tzh.itemTrackingSystem.ui.theme.RFIDTextColor

@Composable
fun RFIDTextFieldWithTitle(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val style = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Justify, fontFamily = FontFamily.Serif, color = RFIDTextColor, fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(.3f)
        ) {
            Text(
                text = "RFID", style = style
            )
            Image(
                painter = painterResource(id = R.drawable.rfid), contentDescription = ""
            )
        }
        OutlinedTextField(
            modifier = Modifier
                .weight(.8f)
                .focusProperties {
                    canFocus = false
                },
            value = text, onValueChange = { }, shape = RoundedCornerShape(0),
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle.Default.copy(
                fontSize = 16.sp
            ),

            )
    }
}
