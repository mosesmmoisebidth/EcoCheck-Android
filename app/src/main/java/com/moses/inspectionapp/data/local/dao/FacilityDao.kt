package com.moses.inspectionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moses.inspectionapp.data.local.entity.FacilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacilityDao {
    @Query("SELECT * FROM facilities")
    fun observeFacilities(): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities")
    suspend fun getAll(): List<FacilityEntity>

    @Query("SELECT COUNT(*) FROM facilities")
    suspend fun count(): Int

    @Query("SELECT * FROM facilities WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FacilityEntity?

    @Query("SELECT * FROM facilities WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: String): FacilityEntity?

    @Query("SELECT * FROM facilities WHERE tin = :tin LIMIT 1")
    suspend fun getByTin(tin: String): FacilityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<FacilityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: FacilityEntity)

    @Query("UPDATE facilities SET syncStatus = :toStatus WHERE syncStatus = :fromStatus")
    suspend fun updateSyncStatus(fromStatus: String, toStatus: String)

    @Query("UPDATE facilities SET syncStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE facilities SET serverId = :serverId WHERE id = :id")
    suspend fun updateServerId(id: String, serverId: String)
}
