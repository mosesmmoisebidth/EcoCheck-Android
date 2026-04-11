package com.moses.inspectionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faults")
data class FaultEntity(
    @PrimaryKey val id: String,
    val inspectionTypeId: String,
    val name: String,
    val standardFine: Int,
    val active: Boolean,
)
