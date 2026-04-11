package com.moses.inspectionapp.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.components.BrandLogo
import com.moses.inspectionapp.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "splash")
    val scale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logoScale",
    )

    LaunchedEffect(Unit) {
        delay(900)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.NavyDark),
        contentAlignment = Alignment.Center,
    ) {
        BrandLogo(
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                ),
        )
    }
}
