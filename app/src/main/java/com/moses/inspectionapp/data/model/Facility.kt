package com.moses.inspectionapp.data.model

data class Facility(
    val id: String,
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
    val syncStatus: SyncStatus,
    val serverId: String? = null,
)
