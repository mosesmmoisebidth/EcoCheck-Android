package com.moses.inspectionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspection_types")
data class InspectionTypeEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val active: Boolean,
)
