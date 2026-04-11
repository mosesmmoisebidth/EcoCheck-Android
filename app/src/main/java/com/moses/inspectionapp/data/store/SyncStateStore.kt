package com.moses.inspectionapp.data.store

import com.moses.inspectionapp.ui.util.formatDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SyncStateStore {
    private val _lastSyncLabel = MutableStateFlow("Never")
    val lastSyncLabel: StateFlow<String> = _lastSyncLabel
    val isSyncing = MutableStateFlow(false)

    fun initFromPrefs() {
        val epoch = AppPreferences.lastSyncEpoch
        _lastSyncLabel.value = if (epoch == 0L) "Never" else formatDateTime(epoch)
    }

    fun updateLastSync(epochMillis: Long) {
        AppPreferences.lastSyncEpoch = epochMillis
        _lastSyncLabel.value = formatDateTime(epochMillis)
    }
}
