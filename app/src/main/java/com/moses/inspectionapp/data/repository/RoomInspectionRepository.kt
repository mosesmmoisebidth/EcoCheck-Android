package com.moses.inspectionapp.data.repository

import android.util.Base64
import android.util.Log
import com.moses.inspectionapp.data.local.AppDatabase
import com.moses.inspectionapp.data.local.mapper.toDomain
import com.moses.inspectionapp.data.local.mapper.toEntity
import com.moses.inspectionapp.data.local.entity.FacilityEntity
import com.moses.inspectionapp.data.local.entity.TeamMemberEntity
import com.moses.inspectionapp.data.model.Facility
import com.moses.inspectionapp.data.model.FacilityDraft
import com.moses.inspectionapp.data.model.Fault
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.Inspection
import com.moses.inspectionapp.data.model.InspectionDraft
import com.moses.inspectionapp.data.model.InspectionType
import com.moses.inspectionapp.data.model.PendingCounts
import com.moses.inspectionapp.data.model.Stats
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.data.model.UserProfile
import com.moses.inspectionapp.data.model.parseDecision
import com.moses.inspectionapp.data.model.totalFine
import com.moses.inspectionapp.data.model.VisitType
import com.moses.inspectionapp.data.remote.ApiClient
import com.moses.inspectionapp.data.remote.CreateFacilityRequest
import com.moses.inspectionapp.data.remote.CreateInspectionRequest
import com.moses.inspectionapp.data.remote.FacilityResponse
import com.moses.inspectionapp.data.remote.UpdateFacilityRequest
import com.moses.inspectionapp.data.remote.UpdateInspectionRequest
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.store.FacilityBackupStore
import com.moses.inspectionapp.data.store.SyncStateStore
import com.moses.inspectionapp.data.store.UserSessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.util.UUID

class RoomInspectionRepository(
    database: AppDatabase,
    private val isOfflineFlow: StateFlow<Boolean> = MutableStateFlow(false),
    lastSyncFlow: StateFlow<String> = MutableStateFlow("Never"),
    externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : InspectionRepository {
    private val facilityDao = database.facilityDao()
    private val inspectionDao = database.inspectionDao()
    private val faultDao = database.faultDao()
    private val inspectionFaultDao = database.inspectionFaultDao()
    private val inspectionTypeDao = database.inspectionTypeDao()
    private val teamMemberDao = database.teamMemberDao()

    private val lastSyncFlow = lastSyncFlow
    private val logTag = "RoomInspectionRepository"

    private fun canSyncOnline(): Boolean {
        return !isOfflineFlow.value && !AppPreferences.accessToken.isNullOrBlank()
    }

    override val userProfile: StateFlow<UserProfile> = UserSessionStore.profile
    override val facilities: StateFlow<List<Facility>> =
        facilityDao.observeFacilities()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())

    override val inspections: StateFlow<List<Inspection>> =
        inspectionDao.observeInspections()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())

    override val faults: StateFlow<List<Fault>> =
        faultDao.observeFaults()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())

    override val inspectionTypes: StateFlow<List<InspectionType>> =
        inspectionTypeDao.observeTypes()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())

    override val customTeamMembers: StateFlow<List<String>> =
        userProfile.flatMapLatest { profile ->
            teamMemberDao.observeByOwnerAndSector(
                ownerUserId = profile.id,
                sectorKey = normalizeSectorKey(profile.sector),
            )
        }
            .map { members -> members.map { it.displayName } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())

    override val pendingCounts: StateFlow<PendingCounts> =
        combine(facilities, inspections) { facilityList, inspectionList ->
            PendingCounts(
                facilities = facilityList.count { it.syncStatus == SyncStatus.PENDING },
                inspections = inspectionList.count { it.syncStatus == SyncStatus.PENDING },
            )
        }.stateIn(externalScope, SharingStarted.Eagerly, SampleData.pending)

    override val stats: StateFlow<Stats> =
        inspections.map { list ->
            val now = System.currentTimeMillis()
            val dayStart = now - (now % (24 * 60 * 60 * 1000))
            val weekStart = now - 7 * 24 * 60 * 60 * 1000
            Stats(
                todayInspections = list.count { it.createdAt >= dayStart },
                weekInspections = list.count { it.createdAt >= weekStart },
                totalFines = list.sumOf { it.totalFine },
            )
        }.stateIn(externalScope, SharingStarted.Eagerly, SampleData.stats)

    override val isOffline: StateFlow<Boolean> = isOfflineFlow
    override val lastSyncLabel: StateFlow<String> = lastSyncFlow

    override suspend fun seedDefaults() {
        if (faultDao.count() == 0) {
            faultDao.upsertAll(SampleData.faults.map { it.toEntity() })
        }
        if (inspectionTypeDao.count() == 0) {
            inspectionTypeDao.upsertAll(SampleData.inspectionTypes.map { it.toEntity() })
        }
    }

    suspend fun refreshFacilitiesFromServer(): Int {
        return try {
            val response = ApiClient.api.getFacilities()
            if (!response.success) {
                Log.w(logTag, "Facilities refresh failed: ${response.message}")
                return 0
            }
            response.payload.forEach { remote ->
                upsertRemoteFacility(remote)
            }
            FacilityBackupStore.writeBackup(facilityDao)
            response.payload.size
        } catch (exception: Exception) {
            Log.e(logTag, "Facilities refresh error", exception)
            0
        }
    }

    suspend fun restoreFacilitiesBackupIfEmpty() {
        FacilityBackupStore.restoreIfEmpty(facilityDao)
    }

    override suspend fun saveFacility(draft: FacilityDraft): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val facility = Facility(
            id = id,
            name = draft.name,
            tin = draft.tin,
            ownerName = draft.ownerName,
            ownerPhone = draft.ownerPhone,
            ownerEmail = draft.ownerEmail,
            district = draft.district,
            sector = draft.sector,
            cell = draft.cell,
            village = draft.village,
            latitude = draft.latitude,
            longitude = draft.longitude,
            photoPath = draft.photoPath,
            createdAt = now,
            createdBy = UserSessionStore.profile.value.id,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING,
        )
        facilityDao.upsert(facility.toEntity())
        FacilityBackupStore.writeBackup(facilityDao)
        createFacilityOnServer(id)
        return id
    }

    override suspend fun updateFacility(id: String, draft: FacilityDraft) {
        val existing = facilityDao.getById(id) ?: return
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            name = draft.name,
            tin = draft.tin,
            ownerName = draft.ownerName,
            ownerPhone = draft.ownerPhone,
            ownerEmail = draft.ownerEmail,
            district = draft.district,
            sector = draft.sector,
            cell = draft.cell,
            village = draft.village,
            latitude = draft.latitude,
            longitude = draft.longitude,
            photoPath = draft.photoPath,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING.name,
        )
        facilityDao.upsert(updated)
        FacilityBackupStore.writeBackup(facilityDao)
        val serverId = existing.serverId
        if (canSyncOnline() && !serverId.isNullOrBlank()) {
            try {
                val response = ApiClient.api.updateFacility(
                    serverId,
                    UpdateFacilityRequest(
                        name = draft.name,
                        ownerName = draft.ownerName,
                        ownerPhone = draft.ownerPhone,
                        ownerEmail = draft.ownerEmail.takeIf { it.isNotBlank() },
                        cell = draft.cell,
                        village = draft.village,
                        latitude = draft.latitude,
                        longitude = draft.longitude,
                        photoPath = draft.photoPath,
                    ),
                )
                if (response.success) {
                    val remote = response.payload
                    val current = facilityDao.getById(id) ?: updated
                    facilityDao.upsert(
                        current.copy(
                            serverId = remote.id,
                            ownerEmail = remote.ownerEmail ?: current.ownerEmail,
                            latitude = remote.latitude ?: current.latitude,
                            longitude = remote.longitude ?: current.longitude,
                            photoPath = remote.photoPath ?: current.photoPath,
                            createdAt = remote.createdAt,
                            updatedAt = remote.updatedAt,
                            createdBy = remote.createdBy,
                            syncStatus = SyncStatus.SYNCED.name,
                        ),
                    )
                    FacilityBackupStore.writeBackup(facilityDao)
                } else {
                    Log.w(logTag, "Facility update failed: ${response.message}")
                }
            } catch (exception: Exception) {
                Log.e(logTag, "Facility update exception", exception)
            }
        }
    }

    override suspend fun addCustomTeamMember(name: String) {
        val profile = UserSessionStore.profile.value
        val displayName = normalizeName(name)
        if (displayName.isBlank()) return
        teamMemberDao.insert(
            TeamMemberEntity(
                id = UUID.randomUUID().toString(),
                ownerUserId = profile.id,
                sectorKey = normalizeSectorKey(profile.sector),
                nameKey = displayName.lowercase(),
                displayName = displayName,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun removeCustomTeamMember(name: String) {
        val profile = UserSessionStore.profile.value
        val normalized = normalizeName(name)
        if (normalized.isBlank()) return
        teamMemberDao.deleteByOwnerSectorAndNameKey(
            ownerUserId = profile.id,
            sectorKey = normalizeSectorKey(profile.sector),
            nameKey = normalized.lowercase(),
        )
    }

    override suspend fun saveInspection(draft: InspectionDraft): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val fine = draft.totalFine(faults.value)
        val resolvedInspectionTypeId = resolveInspectionTypeId(draft.inspectionTypeId)
        val inspection = Inspection(
            id = id,
            facilityId = draft.facilityId.orEmpty(),
            facilityName = draft.facilityName,
            visitType = draft.visitType ?: VisitType.FIRST_VISIT,
            teamMembers = draft.teamMembers,
            inspectionTypeId = resolvedInspectionTypeId,
            faultCount = draft.selectedFaultIds.size,
            totalFine = fine,
            adjustmentAmount = draft.adjustmentAmount,
            adjustmentReason = draft.adjustmentReason,
            decision = draft.decision ?: Decision.WARNING,
            comments = draft.comments,
            recommendations = draft.recommendations,
            photoPaths = draft.photoPaths,
            createdAt = now,
            createdBy = UserSessionStore.profile.value.id,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING,
        )
        inspectionDao.upsert(inspection.toEntity())
        Log.i(logTag, "Inspection saved locally: $id (type=${resolvedInspectionTypeId ?: "none"})")
        inspectionFaultDao.upsertAll(
            draft.selectedFaultIds.map { faultId ->
                com.moses.inspectionapp.data.local.entity.InspectionFaultEntity(
                    id = "$id-$faultId",
                    inspectionId = id,
                    faultId = faultId,
                )
            },
        )
        if (canSyncOnline()) {
            val facilityServerId = draft.facilityId?.let { createFacilityOnServer(it) }
            if (facilityServerId.isNullOrBlank()) {
                Log.w(logTag, "Inspection create skipped: missing facility server id")
                return id
            }
            try {
                val photoPayloads = preparePhotoPayloads(draft.photoPaths)
                val response = ApiClient.api.createInspection(
                    CreateInspectionRequest(
                        facilityId = facilityServerId,
                        visitType = (draft.visitType ?: VisitType.FIRST_VISIT).toApiValue(),
                        inspectionTypeId = resolvedInspectionTypeId,
                        teamMembers = draft.teamMembers,
                        selectedFaultIds = draft.selectedFaultIds.toList(),
                        adjustmentAmount = draft.adjustmentAmount,
                        adjustmentReason = draft.adjustmentReason.takeIf { it.isNotBlank() },
                        decision = (draft.decision ?: Decision.WARNING).name,
                        comments = draft.comments.takeIf { it.isNotBlank() },
                        recommendations = draft.recommendations.takeIf { it.isNotBlank() },
                        photoPaths = photoPayloads.takeIf { it.isNotEmpty() },
                    ),
                )
                if (response.success) {
                    val remote = response.payload
                    val current = inspectionDao.getById(id) ?: inspection.toEntity()
                    inspectionDao.upsert(
                        current.copy(
                            serverId = remote.id,
                            visitType = VisitType.fromApi(remote.visitType).name,
                            facilityName = remote.facilityName,
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
                            updatedAt = remote.updatedAt,
                            createdBy = remote.createdBy,
                            syncStatus = SyncStatus.SYNCED.name,
                        ),
                    )
                } else {
                    Log.w(logTag, "Inspection create failed: ${response.message}")
                }
            } catch (exception: Exception) {
                Log.e(logTag, "Inspection create exception", exception)
            }
        }
        return id
    }

    override suspend fun updateInspection(id: String, draft: InspectionDraft) {
        val existing = inspectionDao.getById(id) ?: return
        val now = System.currentTimeMillis()
        val fine = draft.totalFine(faults.value)
        val updatedPhotoPaths = if (draft.photoPaths.isEmpty()) {
            existing.photoPaths
        } else {
            draft.photoPaths.joinToString("|")
        }
        val resolvedInspectionTypeId = resolveInspectionTypeId(draft.inspectionTypeId)
        val updated = existing.copy(
            visitType = (draft.visitType ?: VisitType.fromApi(existing.visitType)).name,
            teamMembers = draft.teamMembers.joinToString("|"),
            inspectionTypeId = resolvedInspectionTypeId ?: existing.inspectionTypeId,
            faultCount = draft.selectedFaultIds.size,
            totalFine = fine,
            adjustmentAmount = draft.adjustmentAmount,
            adjustmentReason = draft.adjustmentReason,
            decision = (draft.decision ?: parseDecision(existing.decision)).name,
            comments = draft.comments,
            recommendations = draft.recommendations,
            photoPaths = updatedPhotoPaths,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING.name,
        )
        inspectionDao.upsert(updated)
        inspectionFaultDao.clearForInspection(id)
        inspectionFaultDao.upsertAll(
            draft.selectedFaultIds.map { faultId ->
                com.moses.inspectionapp.data.local.entity.InspectionFaultEntity(
                    id = "$id-$faultId",
                    inspectionId = id,
                    faultId = faultId,
                )
            },
        )
        val serverId = existing.serverId
        if (canSyncOnline() && !serverId.isNullOrBlank()) {
            try {
                val photoPayloads = preparePhotoPayloads(draft.photoPaths)
                val response = ApiClient.api.updateInspection(
                    serverId,
                    UpdateInspectionRequest(
                        visitType = (draft.visitType ?: VisitType.fromApi(existing.visitType)).toApiValue(),
                        inspectionTypeId = resolvedInspectionTypeId ?: existing.inspectionTypeId,
                        teamMembers = draft.teamMembers,
                        selectedFaultIds = draft.selectedFaultIds.toList(),
                        adjustmentAmount = draft.adjustmentAmount,
                        adjustmentReason = draft.adjustmentReason.takeIf { it.isNotBlank() },
                        decision = (draft.decision ?: parseDecision(existing.decision)).name,
                        comments = draft.comments.takeIf { it.isNotBlank() },
                        recommendations = draft.recommendations.takeIf { it.isNotBlank() },
                        photoPaths = photoPayloads.takeIf { it.isNotEmpty() },
                    ),
                )
                if (response.success) {
                    val remote = response.payload
                    val current = inspectionDao.getById(id) ?: updated
                    inspectionDao.upsert(
                        current.copy(
                            serverId = remote.id,
                            visitType = VisitType.fromApi(remote.visitType).name,
                            facilityName = remote.facilityName,
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
                            updatedAt = remote.updatedAt,
                            createdBy = remote.createdBy,
                            syncStatus = SyncStatus.SYNCED.name,
                        ),
                    )
                } else {
                    Log.w(logTag, "Inspection update failed: ${response.message}")
                }
            } catch (exception: Exception) {
                Log.e(logTag, "Inspection update exception", exception)
            }
        }
    }

    private suspend fun createFacilityOnServer(localId: String): String? {
        if (!canSyncOnline()) return null
        val current = facilityDao.getById(localId) ?: return null
        if (!current.serverId.isNullOrBlank()) return current.serverId
        return try {
            val response = ApiClient.api.createFacility(
                CreateFacilityRequest(
                    name = current.name,
                    tin = current.tin,
                    ownerName = current.ownerName,
                    ownerPhone = current.ownerPhone,
                    ownerEmail = current.ownerEmail.takeIf { it.isNotBlank() },
                    district = current.district,
                    sector = current.sector,
                    cell = current.cell,
                    village = current.village,
                    latitude = current.latitude,
                    longitude = current.longitude,
                    photoPath = current.photoPath,
                ),
            )
            if (!response.success) {
                Log.w(logTag, "Facility create failed: ${response.message}")
                return null
            }
            val remote = response.payload
            facilityDao.upsert(
                current.copy(
                    serverId = remote.id,
                    ownerEmail = remote.ownerEmail ?: current.ownerEmail,
                    latitude = remote.latitude ?: current.latitude,
                    longitude = remote.longitude ?: current.longitude,
                    photoPath = remote.photoPath ?: current.photoPath,
                    createdAt = remote.createdAt,
                    updatedAt = remote.updatedAt,
                    createdBy = remote.createdBy,
                    syncStatus = SyncStatus.SYNCED.name,
                ),
            )
            FacilityBackupStore.writeBackup(facilityDao)
            remote.id
        } catch (exception: Exception) {
            Log.e(logTag, "Facility create exception", exception)
            null
        }
    }

    override suspend fun getFacility(id: String): Facility? {
        return facilityDao.getById(id)?.toDomain()
    }

    override suspend fun getInspection(id: String): Inspection? {
        return inspectionDao.getById(id)?.toDomain()
    }

    override suspend fun getInspectionFaults(inspectionId: String): List<Fault> {
        val ids = inspectionFaultDao.getFaultIdsForInspection(inspectionId)
        return if (ids.isEmpty()) emptyList() else faultDao.getByIds(ids).map { it.toDomain() }
    }

    override suspend fun findFacilityByTin(tin: String): Facility? {
        return facilityDao.getByTin(tin)?.toDomain()
    }

    override suspend fun simulateSync() {
        facilityDao.updateSyncStatus(SyncStatus.PENDING.name, SyncStatus.SYNCED.name)
        inspectionDao.updateSyncStatus(SyncStatus.PENDING.name, SyncStatus.SYNCED.name)
        SyncStateStore.updateLastSync(System.currentTimeMillis())
    }

    override suspend fun resolveConflictForInspection(id: String) {
        val existing = inspectionDao.getById(id) ?: return
        inspectionDao.upsert(existing.copy(syncStatus = SyncStatus.SYNCED.name))
    }

    override suspend fun resolveConflictForFacility(id: String) {
        val existing = facilityDao.getById(id) ?: return
        facilityDao.upsert(existing.copy(syncStatus = SyncStatus.SYNCED.name))
    }

    private suspend fun upsertRemoteFacility(remote: FacilityResponse) {
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
            createdBy = remote.createdBy,
            updatedAt = remote.updatedAt,
            syncStatus = remote.syncStatus,
        )
        facilityDao.upsert(entity)
    }

    private suspend fun resolveInspectionTypeId(rawId: String?): String? {
        val trimmed = rawId?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (isUuid(trimmed)) {
            return trimmed
        }
        val normalized = normalizeInspectionTypeCode(trimmed)
        val matches = inspectionTypeDao.getAll().filter {
            it.code.equals(normalized, ignoreCase = true)
        }
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
}

private fun normalizeSectorKey(value: String): String {
    return value.trim().lowercase()
}

private fun normalizeName(value: String): String {
    return value.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString(" ")
}
