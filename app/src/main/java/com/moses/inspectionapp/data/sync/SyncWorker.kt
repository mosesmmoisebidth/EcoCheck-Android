package com.moses.inspectionapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moses.inspectionapp.data.local.DatabaseProvider
import com.moses.inspectionapp.data.local.entity.FacilityEntity
import com.moses.inspectionapp.data.local.entity.FaultEntity
import com.moses.inspectionapp.data.local.entity.InspectionEntity
import com.moses.inspectionapp.data.local.entity.InspectionTypeEntity
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.data.model.VisitType
import com.moses.inspectionapp.data.remote.ApiClient
import com.moses.inspectionapp.data.remote.FacilityResponse
import com.moses.inspectionapp.data.remote.InspectionResponse
import com.moses.inspectionapp.data.remote.SyncFacilityPayload
import com.moses.inspectionapp.data.remote.SyncInspectionPayload
import com.moses.inspectionapp.data.remote.SyncPushRequest
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.store.FacilityBackupStore
import com.moses.inspectionapp.data.store.SyncStateStore
import android.util.Base64
import android.util.Log
import java.io.File

class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    private val tag = "SyncWorker"

    override suspend fun doWork(): Result {
        AppPreferences.init(applicationContext)
        if (!NetworkUtils.hasInternet(applicationContext)) {
            Log.w(tag, "No internet; retrying sync")
            return Result.retry()
        }
        if (AppPreferences.wifiOnly && !NetworkUtils.isOnWifi(applicationContext)) {
            Log.w(tag, "Wifi-only enabled and not on wifi; retrying sync")
            return Result.retry()
        }
        if (AppPreferences.accessToken.isNullOrBlank()) {
            Log.w(tag, "No access token; skipping sync")
            return Result.failure()
        }
        SyncStateStore.isSyncing.value = true
        return try {
            val database = DatabaseProvider.get(applicationContext)
            val facilityDao = database.facilityDao()
            val inspectionDao = database.inspectionDao()
            val inspectionFaultDao = database.inspectionFaultDao()
            val faultDao = database.faultDao()
            val inspectionTypeDao = database.inspectionTypeDao()
            val inspectionTypes = inspectionTypeDao.getAll()
            val facilities = facilityDao.getAll()
            val inspections = inspectionDao.getAll()
            val hasLocalData = facilities.isNotEmpty() || inspections.isNotEmpty()
            val pendingFacilities = facilities.filter { it.syncStatus == SyncStatus.PENDING.name }
            val pendingInspections = inspections.filter { it.syncStatus == SyncStatus.PENDING.name }
            val facilityById = facilities.associateBy { it.id }
            val facilityPayloads = pendingFacilities.map { facility ->
                SyncFacilityPayload(
                    clientId = facility.id,
                    serverId = facility.serverId,
                    name = facility.name,
                    tin = facility.tin,
                    ownerName = facility.ownerName,
                    ownerPhone = facility.ownerPhone,
                    ownerEmail = facility.ownerEmail.takeIf { it.isNotBlank() },
                    district = facility.district,
                    sector = facility.sector,
                    cell = facility.cell,
                    village = facility.village,
                    latitude = facility.latitude,
                    longitude = facility.longitude,
                    photoPath = facility.photoPath,
                    createdAt = facility.createdAt,
                    updatedAt = facility.updatedAt,
                )
            }
            val inspectionPayloads = mutableListOf<SyncInspectionPayload>()
            for (inspection in pendingInspections) {
                val facility = facilityById[inspection.facilityId]
                val faultIds = inspectionFaultDao.getFaultIdsForInspection(inspection.id)
                val photoPaths = inspection.photoPaths.split("|").filter { it.isNotBlank() }
                val photoPayloads = preparePhotoPayloads(photoPaths)
                inspectionPayloads.add(
                    SyncInspectionPayload(
                        clientId = inspection.id,
                        serverId = inspection.serverId,
                        facilityId = facility?.serverId,
                        facilityClientId = if (facility?.serverId == null) inspection.facilityId else null,
                        visitType = VisitType.fromApi(inspection.visitType).toApiValue(),
                        teamMembers = inspection.teamMembers.split("|").filter { it.isNotBlank() },
                        selectedFaultIds = faultIds,
                        inspectionTypeId = resolveInspectionTypeId(inspection.inspectionTypeId, inspectionTypes),
                        adjustmentAmount = inspection.adjustmentAmount,
                        adjustmentReason = inspection.adjustmentReason.takeIf { it.isNotBlank() },
                        decision = inspection.decision,
                        comments = inspection.comments.takeIf { it.isNotBlank() },
                        recommendations = inspection.recommendations.takeIf { it.isNotBlank() },
                        photoPaths = photoPayloads.takeIf { it.isNotEmpty() },
                        createdAt = inspection.createdAt,
                        updatedAt = inspection.updatedAt,
                    ),
                )
            }

            val since = AppPreferences.lastSyncEpoch.takeIf { it > 0L && hasLocalData }
            if (facilityPayloads.isNotEmpty() || inspectionPayloads.isNotEmpty()) {
                val pushResponse = ApiClient.api.syncPush(
                    SyncPushRequest(
                        facilities = facilityPayloads,
                        inspections = inspectionPayloads,
                        lastSyncAt = since,
                    ),
                )
                if (!pushResponse.success) {
                    Log.w(tag, "Sync push failed: ${pushResponse.message}")
                    return Result.retry()
                }
                val payload = pushResponse.payload
                pendingFacilities.forEach { facility ->
                    facilityDao.updateStatus(facility.id, SyncStatus.SYNCED.name)
                }
                pendingInspections.forEach { inspection ->
                    inspectionDao.updateStatus(inspection.id, SyncStatus.SYNCED.name)
                }
                payload.facilities.mapped.forEach { map ->
                    facilityDao.updateServerId(map.clientId, map.serverId)
                }
                payload.inspections.mapped?.forEach { map ->
                    inspectionDao.updateServerId(map.clientId, map.serverId)
                }
                payload.facilities.conflicts.forEach { conflict ->
                    facilityDao.updateStatus(conflict.clientId, SyncStatus.CONFLICT.name)
                }
                payload.inspections.conflicts.forEach { conflict ->
                    inspectionDao.updateStatus(conflict.clientId, SyncStatus.CONFLICT.name)
                }
            }

            val pullResponse = ApiClient.api.syncPull(since)
            var pulledFacilitiesCount = 0
            if (pullResponse.success) {
                val payload = pullResponse.payload
                pulledFacilitiesCount = payload.facilities.size
                payload.facilities.forEach { remote ->
                    upsertFacilityFromRemote(facilityDao, remote)
                }
                payload.inspections.forEach { remote ->
                    upsertInspectionFromRemote(inspectionDao, facilityDao, remote)
                }
            } else {
                Log.w(tag, "Sync pull failed: ${pullResponse.message}")
            }

            val shouldRefreshFacilities =
                facilityDao.count() == 0 || pulledFacilitiesCount == 0
            if (shouldRefreshFacilities) {
                val facilitiesResponse = ApiClient.api.getFacilities()
                if (facilitiesResponse.success) {
                    facilitiesResponse.payload.forEach { remote ->
                        upsertFacilityFromRemote(facilityDao, remote)
                    }
                } else {
                    Log.w(tag, "Facilities refresh failed: ${facilitiesResponse.message}")
                }
            }
            Log.i(
                tag,
                "Facilities after sync: ${facilityDao.count()} (pulled=$pulledFacilitiesCount)",
            )
            Log.i(
                tag,
                "Inspections after sync: ${inspectionDao.count()}",
            )

            FacilityBackupStore.writeBackup(facilityDao)

            val faultsResponse = ApiClient.api.getFaults()
            if (faultsResponse.success) {
                val faultEntities = faultsResponse.payload.map { remote ->
                    FaultEntity(
                        id = remote.id,
                        inspectionTypeId = remote.inspectionTypeId,
                        name = remote.name,
                        standardFine = remote.standardFine,
                        active = remote.active,
                    )
                }
                if (faultEntities.isNotEmpty()) {
                    faultDao.upsertAll(faultEntities)
                }
            }

            val typesResponse = ApiClient.api.getInspectionTypes()
            if (typesResponse.success) {
                val typeEntities = typesResponse.payload.map { remote ->
                    InspectionTypeEntity(
                        id = remote.id,
                        code = remote.code,
                        name = remote.name,
                        active = remote.active,
                    )
                }
                if (typeEntities.isNotEmpty()) {
                    inspectionTypeDao.upsertAll(typeEntities)
                }
            }

            SyncStateStore.updateLastSync(System.currentTimeMillis())
            Result.success()
            } catch (exception: Exception) {
            Log.e(tag, "Sync failed", exception)
            Result.retry()
        } finally {
            SyncStateStore.isSyncing.value = false
        }
    }

    private suspend fun upsertFacilityFromRemote(
        facilityDao: com.moses.inspectionapp.data.local.dao.FacilityDao,
        remote: FacilityResponse,
    ) {
        val existing = facilityDao.getByServerId(remote.id) ?: facilityDao.getById(remote.id)
        val entity = FacilityEntity(
            id = existing?.id ?: remote.id,
            serverId = remote.id,
            name = remote.name,
            tin = remote.tin,
            ownerName = remote.ownerName,
            ownerPhone = remote.ownerPhone,
            ownerEmail = remote.ownerEmail ?: "",
            district = remote.district,
            sector = remote.sector,
            cell = remote.cell,
            village = remote.village,
            latitude = remote.latitude,
            longitude = remote.longitude,
            photoPath = remote.photoPath,
            createdAt = remote.createdAt,
            createdBy = remote.createdBy.takeIf { it.isNotBlank() } ?: existing?.createdBy.orEmpty(),
            updatedAt = remote.updatedAt,
            syncStatus = remote.syncStatus,
        )
        facilityDao.upsert(entity)
    }

    private suspend fun upsertInspectionFromRemote(
        inspectionDao: com.moses.inspectionapp.data.local.dao.InspectionDao,
        facilityDao: com.moses.inspectionapp.data.local.dao.FacilityDao,
        remote: InspectionResponse,
    ) {
        val existing = inspectionDao.getByServerId(remote.id) ?: inspectionDao.getById(remote.id)
        val facilityLocalId = facilityDao.getByServerId(remote.facilityId)?.id
            ?: facilityDao.getById(remote.facilityId)?.id
            ?: remote.facilityId
        val entity = InspectionEntity(
            id = existing?.id ?: remote.id,
            serverId = remote.id,
            facilityId = facilityLocalId,
            facilityName = remote.facilityName,
            visitType = VisitType.fromApi(remote.visitType).name,
            teamMembers = remote.teamMembers.joinToString("|"),
            inspectionTypeId = remote.inspectionTypeId,
            faultCount = remote.faultCount,
            totalFine = remote.totalFine,
            adjustmentAmount = remote.adjustmentAmount,
            adjustmentReason = remote.adjustmentReason,
            decision = remote.decision,
            comments = remote.comments,
            recommendations = remote.recommendations,
            photoPaths = remote.photoPaths.joinToString("|"),
            createdAt = remote.createdAt,
            createdBy = remote.createdBy.takeIf { it.isNotBlank() } ?: existing?.createdBy.orEmpty(),
            updatedAt = remote.updatedAt,
            syncStatus = remote.syncStatus,
        )
        inspectionDao.upsert(entity)
    }

    private fun preparePhotoPayloads(paths: List<String>): List<String> {
        return paths.mapNotNull { path ->
            val trimmed = path.trim()
            if (trimmed.isBlank()) {
                null
            } else if (trimmed.startsWith("data:image/") || trimmed.startsWith("http")) {
                trimmed
            } else {
                val normalized = trimmed.removePrefix("file://")
                val file = File(normalized)
                if (!file.exists()) {
                    null
                } else {
                    val extension = file.extension.lowercase()
                    val contentType = when (extension) {
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        "jpg", "jpeg" -> "image/jpeg"
                        else -> "image/jpeg"
                    }
                    val encoded = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
                    "data:$contentType;base64,$encoded"
                }
            }
        }
    }

    private fun resolveInspectionTypeId(
        rawId: String?,
        types: List<InspectionTypeEntity>,
    ): String? {
        val trimmed = rawId?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (isUuid(trimmed)) {
            return trimmed
        }
        val normalized = normalizeInspectionTypeCode(trimmed)
        val matches = types.filter { it.code.equals(normalized, ignoreCase = true) }
        val preferred = matches.firstOrNull { isUuid(it.id) } ?: matches.firstOrNull()
        return preferred?.id ?: trimmed
    }

    private fun isUuid(value: String): Boolean {
        return Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
        ).matches(value)
    }

    private fun normalizeInspectionTypeCode(value: String): String {
        return value.trim().uppercase().replace(Regex("[\\s-]+"), "_")
    }
}
