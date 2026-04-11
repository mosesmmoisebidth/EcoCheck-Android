package com.moses.inspectionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey val id: String,
    val serverId: String?,
    val facilityId: String,
    val facilityName: String,
    val visitType: String,
    val teamMembers: String,
    val inspectionTypeId: String?,
    val faultCount: Int,
    val totalFine: Int,
    val adjustmentAmount: Int,
    val adjustmentReason: String,
    val decision: String,
    val comments: String,
    val recommendations: String,
    val photoPaths: String,
    val createdAt: Long,
    val createdBy: String,
    val updatedAt: Long,
    val syncStatus: String,
)
