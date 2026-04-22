package com.moses.inspectionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moses.inspectionapp.data.local.entity.TeamMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamMemberDao {
    @Query(
        """
        SELECT * FROM team_members
        WHERE ownerUserId = :ownerUserId AND sectorKey = :sectorKey
        ORDER BY displayName COLLATE NOCASE
        """,
    )
    fun observeByOwnerAndSector(ownerUserId: String, sectorKey: String): Flow<List<TeamMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: TeamMemberEntity): Long

    @Query(
        """
        DELETE FROM team_members
        WHERE ownerUserId = :ownerUserId
          AND sectorKey = :sectorKey
          AND nameKey = :nameKey
        """,
    )
    suspend fun deleteByOwnerSectorAndNameKey(
        ownerUserId: String,
        sectorKey: String,
        nameKey: String,
    ): Int
}
