package com.moses.inspectionapp.data.model

data class Inspection(
    val id: String,
    val facilityId: String,
    val facilityName: String,
    val visitType: VisitType,
    val teamMembers: List<String>,
    val inspectionTypeId: String? = null,
    val faultCount: Int,
    val totalFine: Int,
    val adjustmentAmount: Int,
    val adjustmentReason: String,
    val decision: Decision,
    val comments: String,
    val recommendations: String,
    val photoPaths: List<String> = emptyList(),
    val createdAt: Long,
    val createdBy: String,
    val updatedAt: Long,
    val syncStatus: SyncStatus,
    val serverId: String? = null,
)
