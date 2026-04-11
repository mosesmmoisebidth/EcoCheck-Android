package com.moses.inspectionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_logs")
data class SmsLogEntity(
    @PrimaryKey val id: String,
    val inspectionId: String,
    val facilityName: String,
    val phone: String,
    val message: String,
    val status: String,
    val createdAt: Long,
)
