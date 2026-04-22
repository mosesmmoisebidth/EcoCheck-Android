package com.moses.inspectionapp.data.local.mapper

import com.moses.inspectionapp.data.local.entity.FacilityEntity
import com.moses.inspectionapp.data.local.entity.FaultEntity
import com.moses.inspectionapp.data.local.entity.InspectionEntity
import com.moses.inspectionapp.data.local.entity.InspectionTypeEntity
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.Facility
import com.moses.inspectionapp.data.model.Fault
import com.moses.inspectionapp.data.model.Inspection
import com.moses.inspectionapp.data.model.InspectionType
import com.moses.inspectionapp.data.model.parseDecision
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.data.model.VisitType

private fun parseSyncStatus(raw: String): SyncStatus {
    return SyncStatus.values().firstOrNull { it.name.equals(raw, ignoreCase = true) }
        ?: SyncStatus.PENDING
}

fun FacilityEntity.toDomain(): Facility {
    return Facility(
        id = id,
        name = name,
        tin = tin,
        ownerName = ownerName,
        ownerPhone = ownerPhone,
        ownerEmail = ownerEmail,
        district = district,
        sector = sector,
        cell = cell,
        village = village,
        latitude = latitude,
        longitude = longitude,
        photoPath = photoPath,
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        syncStatus = parseSyncStatus(syncStatus),
        serverId = serverId,
    )
}

fun InspectionEntity.toDomain(): Inspection {
    return Inspection(
        id = id,
        facilityId = facilityId,
        facilityName = facilityName,
        visitType = VisitType.fromApi(visitType),
        teamMembers = teamMembers.split("|").filter { it.isNotBlank() },
        inspectionTypeId = inspectionTypeId,
        faultCount = faultCount,
        totalFine = totalFine,
        adjustmentAmount = adjustmentAmount,
        adjustmentReason = adjustmentReason,
        decision = parseDecision(decision),
        comments = comments,
        recommendations = recommendations,
        photoPaths = photoPaths.split("|").filter { it.isNotBlank() },
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        syncStatus = parseSyncStatus(syncStatus),
        serverId = serverId,
    )
}

fun FaultEntity.toDomain(): Fault {
    return Fault(
        id = id,
        inspectionTypeId = inspectionTypeId,
        name = name,
        standardFine = standardFine,
        active = active,
    )
}

fun InspectionTypeEntity.toDomain(): InspectionType {
    return InspectionType(
        id = id,
        code = code,
        name = name,
        active = active,
    )
}

fun Facility.toEntity(): FacilityEntity {
    return FacilityEntity(
        id = id,
        serverId = serverId,
        name = name,
        tin = tin,
        ownerName = ownerName,
        ownerPhone = ownerPhone,
        ownerEmail = ownerEmail,
        district = district,
        sector = sector,
        cell = cell,
        village = village,
        latitude = latitude,
        longitude = longitude,
        photoPath = photoPath,
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        syncStatus = syncStatus.name,
    )
}

fun Inspection.toEntity(): InspectionEntity {
    return InspectionEntity(
        id = id,
        serverId = serverId,
        facilityId = facilityId,
        facilityName = facilityName,
        visitType = visitType.name,
        teamMembers = teamMembers.joinToString("|"),
        inspectionTypeId = inspectionTypeId,
        faultCount = faultCount,
        totalFine = totalFine,
        adjustmentAmount = adjustmentAmount,
        adjustmentReason = adjustmentReason,
        decision = decision.name,
        comments = comments,
        recommendations = recommendations,
        photoPaths = photoPaths.joinToString("|"),
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        syncStatus = syncStatus.name,
    )
}

fun Fault.toEntity(): FaultEntity {
    return FaultEntity(
        id = id,
        inspectionTypeId = inspectionTypeId,
        name = name,
        standardFine = standardFine,
        active = active,
    )
}

fun InspectionType.toEntity(): InspectionTypeEntity {
    return InspectionTypeEntity(
        id = id,
        code = code,
        name = name,
        active = active,
    )
}
