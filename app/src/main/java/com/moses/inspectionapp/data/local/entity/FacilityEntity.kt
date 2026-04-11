package com.moses.inspectionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facilities")
data class FacilityEntity(
    @PrimaryKey val id: String,
    val serverId: String?,
    val name: String,
    val tin: String,
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String,
    val district: String,
    val sector: String,
    val cell: String,
    val village: String,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
    val createdAt: Long,
    val createdBy: String,
    val updatedAt: Long,
    val syncStatus: String,
)
