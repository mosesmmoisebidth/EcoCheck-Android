package com.moses.inspectionapp.data

import android.content.Context
import androidx.room.Room
import com.moses.inspectionapp.data.local.AppDatabase
import com.moses.inspectionapp.data.repository.InspectionRepository
import com.moses.inspectionapp.data.repository.RoomInspectionRepository
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.store.NetworkMonitor
import com.moses.inspectionapp.data.store.SyncStateStore
import com.moses.inspectionapp.data.store.UserSessionStore
import com.moses.inspectionapp.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AppContainer {
    private lateinit var database: AppDatabase
    private lateinit var networkMonitor: NetworkMonitor
    lateinit var repository: InspectionRepository
        private set

    fun init(context: Context) {
        if (::repository.isInitialized) return
        AppPreferences.init(context)
        UserSessionStore.loadFromPrefs()
        SyncStateStore.initFromPrefs()
        networkMonitor = NetworkMonitor(context)
        networkMonitor.start()
        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "inspection.db",
        )
            .fallbackToDestructiveMigration()
            .enableMultiInstanceInvalidation()
            .build()
        repository = RoomInspectionRepository(
            database,
            isOfflineFlow = networkMonitor.isOffline,
            lastSyncFlow = SyncStateStore.lastSyncLabel,
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            repository.seedDefaults()
            val roomRepository = repository as? RoomInspectionRepository
            roomRepository?.restoreFacilitiesBackupIfEmpty()
        }
        scope.launch {
            networkMonitor.isOffline.collect { offline ->
                if (!offline &&
                    AppPreferences.autoSync &&
                    !AppPreferences.accessToken.isNullOrBlank()
                ) {
                    SyncManager.enqueue(context)
                }
            }
        }
    }
}
