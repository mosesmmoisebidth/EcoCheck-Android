package com.moses.inspectionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moses.inspectionapp.data.local.entity.InspectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections")
    fun observeInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections")
    suspend fun getAll(): List<InspectionEntity>

    @Query("SELECT COUNT(*) FROM inspections")
    suspend fun count(): Int

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: String): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE facilityId = :facilityId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestForFacility(facilityId: String): InspectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<InspectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: InspectionEntity)

    @Query("UPDATE inspections SET syncStatus = :toStatus WHERE syncStatus = :fromStatus")
    suspend fun updateSyncStatus(fromStatus: String, toStatus: String)

    @Query("UPDATE inspections SET syncStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE inspections SET serverId = :serverId WHERE id = :id")
    suspend fun updateServerId(id: String, serverId: String)
}
