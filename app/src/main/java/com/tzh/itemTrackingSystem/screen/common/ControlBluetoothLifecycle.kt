package com.tzh.itemTrackingSystem.screen.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun ControlBluetoothLifecycle(
    lifecycleOwner: LifecycleOwner, onCreate: () -> Unit, onResume: () -> Unit, onPause: () -> Unit, onDestroy: () -> Unit = {}
) {
    DisposableEffect(key1 = lifecycleOwner) {
        val lifecycleEventObserver = LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> onCreate()

                Lifecycle.Event.ON_RESUME -> onResume()

                Lifecycle.Event.ON_PAUSE -> onPause()

                Lifecycle.Event.ON_DESTROY -> onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
        }
    }
}