package com.moses.inspectionapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val visibleState = remember { mutableStateOf(false) }
    LaunchedEffect(index) {
        delay(index * 50L)
        visibleState.value = true
    }
    AnimatedVisibility(
        visible = visibleState.value,
        enter = slideInVertically(
            animationSpec = tween(280),
            initialOffsetY = { it / 2 },
        ) + fadeIn(animationSpec = tween(280)),
    ) {
        Box(modifier = modifier) {
            content()
        }
    }
}
