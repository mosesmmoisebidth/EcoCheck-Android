package com.moses.inspectionapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "team_members",
    indices = [
        Index(value = ["ownerUserId", "sectorKey"]),
        Index(value = ["ownerUserId", "sectorKey", "nameKey"], unique = true),
    ],
)
data class TeamMemberEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val sectorKey: String,
    val nameKey: String,
    val displayName: String,
    val createdAt: Long,
)
