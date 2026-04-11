package com.moses.inspectionapp.ui.screens.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.ErrorState
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun StatesDemoScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = stringResource(R.string.states), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium),
        ) {
            Text(text = stringResource(R.string.empty_state))
            EmptyState(
                title = stringResource(R.string.no_inspections),
                message = stringResource(R.string.create_new_assessment),
            )
            Text(text = stringResource(R.string.error_state))
            ErrorState(
                title = stringResource(R.string.failed_load),
                message = stringResource(R.string.please_try_again),
            )
        }
    }
}
