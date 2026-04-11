package com.moses.inspectionapp.ui.util

import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.mouseWheelScroll(scrollState: ScrollState): Modifier {
    val context = LocalContext.current
    val scrollFactor = remember(context) {
        ViewConfiguration.get(context).scaledVerticalScrollFactor
    }
    return pointerInteropFilter { event ->
        if (event.action == MotionEvent.ACTION_SCROLL &&
            event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)
        ) {
            val delta = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            if (delta != 0f) {
                scrollState.dispatchRawDelta(-delta * scrollFactor)
                return@pointerInteropFilter true
            }
        }
        false
    }
}
