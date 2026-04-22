package com.moses.inspectionapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
private fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeletonOffset",
    )
    return Brush.linearGradient(
        colors = listOf(
            AppColors.SteelBlueTint,
            AppColors.BorderLight,
            AppColors.SteelBlueTint,
        ),
        start = Offset(translate - 600f, 0f),
        end = Offset(translate, 200f),
    )
}

@Composable
fun WavySkeletonBlock(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
) {
    Spacer(modifier = modifier.background(shimmerBrush(), shape))
}

@Composable
fun FacilitySkeletonList(count: Int = 4) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
        repeat(count) {
            FacilitySkeletonItem()
        }
    }
}

@Composable
fun InspectionSkeletonList(count: Int = 4) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
        repeat(count) {
            InspectionSkeletonItem()
        }
    }
}

@Composable
private fun FacilitySkeletonItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardSurface, RoundedCornerShape(16.dp))
            .padding(Dimens.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
    ) {
        WavySkeletonBlock(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            WavySkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp),
                shape = RoundedCornerShape(8.dp),
            )
            WavySkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(14.dp),
                shape = RoundedCornerShape(8.dp),
            )
            WavySkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(14.dp),
                shape = RoundedCornerShape(8.dp),
            )
        }
    }
}

@Composable
private fun InspectionSkeletonItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardSurface, RoundedCornerShape(16.dp))
            .padding(Dimens.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
    ) {
        WavySkeletonBlock(
            modifier = Modifier.size(4.dp, 56.dp),
            shape = RoundedCornerShape(50.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            WavySkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(18.dp),
                shape = RoundedCornerShape(8.dp),
            )
            WavySkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp),
                shape = RoundedCornerShape(8.dp),
            )
            WavySkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp),
                shape = RoundedCornerShape(50.dp),
            )
        }
        WavySkeletonBlock(
            modifier = Modifier.size(18.dp),
            shape = RoundedCornerShape(6.dp),
        )
    }
}
