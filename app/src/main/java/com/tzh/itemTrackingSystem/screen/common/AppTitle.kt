package com.tzh.itemTrackingSystem.screen.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tzh.itemTrackingSystem.R
import com.tzh.itemTrackingSystem.ui.theme.agbalumoFamily

@Composable
fun AppTitle() {
    Text(
        text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = agbalumoFamily
        )
    )
}