package com.moses.inspectionapp.ui.screens.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.ErrorState
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun LockedRecordScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = stringResource(R.string.record_locked), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium),
        ) {
            ErrorState(
                title = stringResource(R.string.editing_disabled),
                message = stringResource(R.string.locked_message),
            )
            OutlinedButton(onClick = onBack) {
                Text(text = stringResource(R.string.back))
            }
        }
    }
}
