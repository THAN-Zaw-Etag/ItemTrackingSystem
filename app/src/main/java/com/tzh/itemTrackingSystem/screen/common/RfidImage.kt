package com.tzh.itemTrackingSystem.screen.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.tzh.itemTrackingSystem.R

@Composable
fun RfidImage() {
    Image(
        painter = painterResource(id = R.drawable.rfid),
        contentDescription = "Scan RFID",
    )
}