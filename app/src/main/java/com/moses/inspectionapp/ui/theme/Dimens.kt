package com.moses.inspectionapp.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

object Dimens {
    var xSmall by mutableStateOf(4.dp)
    var small by mutableStateOf(8.dp)
    var medium by mutableStateOf(16.dp)
    var large by mutableStateOf(24.dp)
    var xLarge by mutableStateOf(32.dp)
    var xxLarge by mutableStateOf(40.dp)
    var touchTarget by mutableStateOf(48.dp)
    var buttonHeight by mutableStateOf(52.dp)
    var inputHeight by mutableStateOf(52.dp)
    var cardRadius by mutableStateOf(16.dp)
    var avatarSize by mutableStateOf(56.dp)
    var stepDotSize by mutableStateOf(28.dp)
    var cardMaxWidth by mutableStateOf(560.dp)

    var screenPadding by mutableStateOf(20.dp)
    var screenPaddingV by mutableStateOf(16.dp)
    var cardPadding by mutableStateOf(16.dp)
    var sectionGap by mutableStateOf(20.dp)
    var itemGap by mutableStateOf(12.dp)
    var smallGap by mutableStateOf(8.dp)

    fun updateFromSpacing(spacing: AppSpacing) {
        screenPadding = spacing.screenPaddingH
        screenPaddingV = spacing.screenPaddingV
        cardPadding = spacing.cardPadding
        itemGap = spacing.itemSpacing
        sectionGap = spacing.sectionSpacing
        buttonHeight = spacing.buttonHeight
        inputHeight = spacing.inputHeight
        avatarSize = spacing.avatarSize
        stepDotSize = spacing.stepDotSize
        cardMaxWidth = spacing.cardMaxWidth
        medium = spacing.cardPadding
        large = spacing.sectionSpacing
        smallGap = spacing.itemSpacing / 2
    }
}
