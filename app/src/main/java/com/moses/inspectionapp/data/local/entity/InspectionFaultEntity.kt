package com.moses.inspectionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspection_faults")
data class InspectionFaultEntity(
    @PrimaryKey val id: String,
    val inspectionId: String,
    val faultId: String,
)
