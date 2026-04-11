package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.CardShape
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun AssessmentStepHeader(
    step: Int,
    totalSteps: Int,
    title: String,
    subtitle: String? = null,
) {
    val steps = listOf(
        stringResource(R.string.facility),
        stringResource(R.string.visit_type),
        stringResource(R.string.inspection_team),
        stringResource(R.string.fault_checklist),
        stringResource(R.string.manual_adjustment),
        stringResource(R.string.decision),
        stringResource(R.string.comments_recommendations),
        stringResource(R.string.review_and_submit),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        StepProgressBar(steps = steps, currentStep = step, modifier = Modifier.padding(bottom = 12.dp))
        Surface(
            color = AppColors.CardSurface,
            shadowElevation = 4.dp,
            shape = CardShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.SteelBlue,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
