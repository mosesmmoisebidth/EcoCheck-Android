package com.moses.inspectionapp.ui.screens.onboarding

import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moses.inspectionapp.R
import com.moses.inspectionapp.ui.components.BrandLogo
import com.moses.inspectionapp.ui.theme.DMSansFamily
import com.moses.inspectionapp.ui.theme.SoraFamily

private val C_BG = Color(0xFF0B1526)
private val C_CARD = Color(0xFF111C2F)
private val C_ACCENT = Color(0xFF4F8FFF)
private val C_GRAD_S = Color(0xFF2558E8)
private val C_GRAD_E = Color(0xFF4F8FFF)
private val C_TEXT1 = Color(0xFFEBF0FF)
private val C_TEXT2 = Color(0xFF5A6E90)
private val C_MUTED = Color(0xFF7A90B8)
private val C_DOT_ON = Color(0xFF4F8FFF)
private val C_DOT_OFF = Color(0x1FFFFFFF)
private val C_SKIP_BG = Color(0x0FFFFFFF)
private val C_SKIP_BR = Color(0x1FFFFFFF)
private val C_BACK_BG = Color(0x0DFFFFFF)
private val C_BACK_BR = Color(0x19FFFFFF)

data class OBPage(
    val step: String,
    val title: String,
    val subtitle: String,
    @RawRes val lottieRes: Int,
    val primaryBtn: String,
    val showBack: Boolean,
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OBPage(
            step = "STEP 1 OF 3",
            title = "Smart inspections,\ndone fast",
            subtitle = "Guided flow for faults and decisions.",
            lottieRes = R.raw.scene_inspection,
            primaryBtn = "Next",
            showBack = false,
        ),
        OBPage(
            step = "STEP 2 OF 3",
            title = "Work offline,\nanywhere",
            subtitle = "Save now, sync when back online.",
            lottieRes = R.raw.scene_offline,
            primaryBtn = "Next",
            showBack = true,
        ),
        OBPage(
            step = "STEP 3 OF 3",
            title = "Sync and share\nreports instantly",
            subtitle = "Share PDF reports in seconds.",
            lottieRes = R.raw.scene_sync,
            primaryBtn = "Get Started",
            showBack = true,
        ),
    )

    var currentPage by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(C_BG)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandLogo(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = C_TEXT1)) { append("Eco") }
                            withStyle(SpanStyle(color = C_ACCENT)) { append("Check") }
                        },
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 0.2.sp,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(C_SKIP_BG)
                        .border(0.5.dp, C_SKIP_BR, RoundedCornerShape(20.dp))
                        .clickable { onFinish() }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Skip",
                        color = C_MUTED,
                        fontFamily = DMSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                    )
                }
            }

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    val forward = targetState > initialState
                    val enter = slideInHorizontally(tween(280)) { if (forward) it else -it } +
                        fadeIn(tween(280))
                    val exit = slideOutHorizontally(tween(280)) { if (forward) -it else it } +
                        fadeOut(tween(280))
                    enter togetherWith exit
                },
                label = "onboarding_pages",
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                val page = pages[pageIndex]

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(C_CARD),
                        contentAlignment = Alignment.Center,
                    ) {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(page.lottieRes),
                        )
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = page.step,
                                color = C_ACCENT,
                                fontFamily = DMSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                letterSpacing = 1.4.sp,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = page.title,
                                color = C_TEXT1,
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                lineHeight = 30.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = page.subtitle,
                                color = C_TEXT2,
                                fontFamily = DMSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                repeat(3) { i ->
                                    val isActive = i == pageIndex
                                    Box(
                                        modifier = Modifier
                                            .height(4.dp)
                                            .width(if (isActive) 22.dp else 12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isActive) C_DOT_ON else C_DOT_OFF),
                                    )
                                    if (i < 2) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (page.showBack) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(C_BACK_BG)
                                        .border(0.5.dp, C_BACK_BR, RoundedCornerShape(16.dp))
                                        .clickable { if (currentPage > 0) currentPage -= 1 },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Back",
                                        color = C_MUTED,
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Brush.linearGradient(listOf(C_GRAD_S, C_GRAD_E)))
                                        .clickable {
                                            if (currentPage < pages.lastIndex) currentPage += 1
                                            else onFinish()
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = page.primaryBtn,
                                        color = Color.White,
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(C_GRAD_S, C_GRAD_E)))
                                    .clickable {
                                        if (currentPage < pages.lastIndex) currentPage += 1
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = page.primaryBtn,
                                    color = Color.White,
                                    fontFamily = SoraFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
