package com.tzh.itemTrackingSystem.screen.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun DeviceConnectionText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text, style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = 16.sp,
            color = Color.Green,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Justify,
        ), modifier = modifier
    )
}