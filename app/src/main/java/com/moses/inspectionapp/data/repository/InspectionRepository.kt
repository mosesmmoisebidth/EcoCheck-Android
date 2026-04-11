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
import kotlinx.coroutines.flow.StateFlow

interface InspectionRepository {
    val userProfile: StateFlow<UserProfile>
    val facilities: StateFlow<List<Facility>>
    val inspections: StateFlow<List<Inspection>>
    val faults: StateFlow<List<Fault>>
    val inspectionTypes: StateFlow<List<InspectionType>>
    val pendingCounts: StateFlow<PendingCounts>
    val stats: StateFlow<Stats>
    val isOffline: StateFlow<Boolean>
    val lastSyncLabel: StateFlow<String>

    suspend fun seedDefaults()
    suspend fun saveFacility(draft: FacilityDraft): String
    suspend fun updateFacility(id: String, draft: FacilityDraft)
    suspend fun saveInspection(draft: InspectionDraft): String
    suspend fun updateInspection(id: String, draft: InspectionDraft)
    suspend fun getFacility(id: String): Facility?
    suspend fun getInspection(id: String): Inspection?
    suspend fun getInspectionFaults(inspectionId: String): List<Fault>
    suspend fun findFacilityByTin(tin: String): Facility?
    suspend fun simulateSync()
    suspend fun resolveConflictForInspection(id: String)
    suspend fun resolveConflictForFacility(id: String)
}
