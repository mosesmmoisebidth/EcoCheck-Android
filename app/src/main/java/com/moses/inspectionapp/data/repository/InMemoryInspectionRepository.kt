package com.moses.inspectionapp.data.repository

import com.moses.inspectionapp.data.model.Facility
import com.moses.inspectionapp.data.model.FacilityDraft
import com.moses.inspectionapp.data.model.Fault
import com.moses.inspectionapp.data.model.Inspection
import com.moses.inspectionapp.data.model.InspectionDraft
import com.moses.inspectionapp.data.model.InspectionType
import com.moses.inspectionapp.data.model.PendingCounts
import com.moses.inspectionapp.data.model.Stats
import com.moses.inspectionapp.data.model.UserProfile
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.data.model.totalFine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class InMemoryInspectionRepository : InspectionRepository {
    private val userFlow = MutableStateFlow<UserProfile>(SampleData.user)
    private val facilityFlow = MutableStateFlow<List<Facility>>(SampleData.facilities)
    private val inspectionFlow = MutableStateFlow<List<Inspection>>(SampleData.inspections)
    private val faultFlow = MutableStateFlow<List<Fault>>(SampleData.faults)
    private val typeFlow = MutableStateFlow<List<InspectionType>>(SampleData.inspectionTypes)
    private val customMembersFlow = MutableStateFlow<List<String>>(emptyList())
    private val pendingFlow = MutableStateFlow<PendingCounts>(SampleData.pending)
    private val statsFlow = MutableStateFlow<Stats>(SampleData.stats)
    private val offlineFlow = MutableStateFlow(false)
    private val lastSyncFlow = MutableStateFlow("Today 09:24")

    override val userProfile: StateFlow<UserProfile> = userFlow
    override val facilities: StateFlow<List<Facility>> = facilityFlow
    override val inspections: StateFlow<List<Inspection>> = inspectionFlow
    override val faults: StateFlow<List<Fault>> = faultFlow
    override val inspectionTypes: StateFlow<List<InspectionType>> = typeFlow
    override val customTeamMembers: StateFlow<List<String>> = customMembersFlow
    override val pendingCounts: StateFlow<PendingCounts> = pendingFlow
    override val stats: StateFlow<Stats> = statsFlow
    override val isOffline: StateFlow<Boolean> = offlineFlow
    override val lastSyncLabel: StateFlow<String> = lastSyncFlow

    override suspend fun seedDefaults() = Unit

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
            createdBy = userFlow.value.id,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING,
        )
        facilityFlow.value = facilityFlow.value + facility
        refreshStats()
        return id
    }

    override suspend fun updateFacility(id: String, draft: FacilityDraft) {
        val now = System.currentTimeMillis()
        facilityFlow.value = facilityFlow.value.map { facility ->
            if (facility.id == id) {
                facility.copy(
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
                    syncStatus = SyncStatus.PENDING,
                )
            } else {
                facility
            }
        }
        refreshStats()
    }

    override suspend fun addCustomTeamMember(name: String) {
        val normalized = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString(" ")
        if (normalized.isBlank()) return
        if (customMembersFlow.value.none { it.equals(normalized, ignoreCase = true) }) {
            customMembersFlow.value = customMembersFlow.value + normalized
        }
    }

    override suspend fun removeCustomTeamMember(name: String) {
        val normalized = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString(" ")
        if (normalized.isBlank()) return
        customMembersFlow.value = customMembersFlow.value.filterNot { it.equals(normalized, ignoreCase = true) }
    }

    override suspend fun saveInspection(draft: InspectionDraft): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val fine = draft.totalFine(faultFlow.value)
        val inspection = Inspection(
            id = id,
            facilityId = draft.facilityId.orEmpty(),
            facilityName = draft.facilityName,
            visitType = draft.visitType ?: throw IllegalStateException("Visit type required"),
            teamMembers = draft.teamMembers,
            inspectionTypeId = draft.inspectionTypeId,
            faultCount = draft.selectedFaultIds.size,
            totalFine = fine,
            adjustmentAmount = draft.adjustmentAmount,
            adjustmentReason = draft.adjustmentReason,
            decision = draft.decision ?: throw IllegalStateException("Decision required"),
            comments = draft.comments,
            recommendations = draft.recommendations,
            photoPaths = draft.photoPaths,
            createdAt = now,
            createdBy = userFlow.value.id,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING,
        )
        inspectionFlow.value = inspectionFlow.value + inspection
        refreshStats()
        return id
    }

    override suspend fun updateInspection(id: String, draft: InspectionDraft) {
        val now = System.currentTimeMillis()
        val fine = draft.totalFine(faultFlow.value)
        inspectionFlow.value = inspectionFlow.value.map { inspection ->
            if (inspection.id == id) {
                inspection.copy(
                    visitType = draft.visitType ?: inspection.visitType,
                    teamMembers = draft.teamMembers,
                    inspectionTypeId = draft.inspectionTypeId ?: inspection.inspectionTypeId,
                    faultCount = draft.selectedFaultIds.size,
                    totalFine = fine,
                    adjustmentAmount = draft.adjustmentAmount,
                    adjustmentReason = draft.adjustmentReason,
                    decision = draft.decision ?: inspection.decision,
                    comments = draft.comments,
                    recommendations = draft.recommendations,
                    photoPaths = draft.photoPaths,
                    updatedAt = now,
                    syncStatus = SyncStatus.PENDING,
                )
            } else {
                inspection
            }
        }
        refreshStats()
    }

    override suspend fun getFacility(id: String): Facility? {
        return facilityFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun getInspection(id: String): Inspection? {
        return inspectionFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun getInspectionFaults(inspectionId: String): List<Fault> {
        return faultFlow.value
    }

    override suspend fun findFacilityByTin(tin: String): Facility? {
        return facilityFlow.value.firstOrNull { it.tin == tin }
    }

    override suspend fun simulateSync() {
        facilityFlow.value = facilityFlow.value.map { it.copy(syncStatus = SyncStatus.SYNCED) }
        inspectionFlow.value = inspectionFlow.value.map { it.copy(syncStatus = SyncStatus.SYNCED) }
        lastSyncFlow.value = "Just now"
        refreshStats()
    }

    override suspend fun resolveConflictForInspection(id: String) {
        inspectionFlow.value = inspectionFlow.value.map { inspection ->
            if (inspection.id == id) {
                inspection.copy(syncStatus = SyncStatus.SYNCED)
            } else {
                inspection
            }
        }
    }

    override suspend fun resolveConflictForFacility(id: String) {
        facilityFlow.value = facilityFlow.value.map { facility ->
            if (facility.id == id) {
                facility.copy(syncStatus = SyncStatus.SYNCED)
            } else {
                facility
            }
        }
    }

    private fun refreshStats() {
        val todayCount = inspectionFlow.value.size
        val weekCount = inspectionFlow.value.size
        val totalFines = inspectionFlow.value.sumOf { it.totalFine }
        statsFlow.value = Stats(
            todayInspections = todayCount,
            weekInspections = weekCount,
            totalFines = totalFines,
        )
        pendingFlow.value = PendingCounts(
            facilities = facilityFlow.value.count { it.syncStatus == SyncStatus.PENDING },
            inspections = inspectionFlow.value.count { it.syncStatus == SyncStatus.PENDING },
        )
    }
}
