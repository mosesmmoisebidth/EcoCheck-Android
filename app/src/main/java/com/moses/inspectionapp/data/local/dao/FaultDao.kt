package com.moses.inspectionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moses.inspectionapp.data.local.entity.FaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FaultDao {
    @Query("SELECT * FROM faults")
    fun observeFaults(): Flow<List<FaultEntity>>

    @Query("SELECT COUNT(*) FROM faults")
    suspend fun count(): Int

    @Query("SELECT * FROM faults WHERE inspectionTypeId = :inspectionTypeId AND active = 1")
    suspend fun getActiveByType(inspectionTypeId: String): List<FaultEntity>

    @Query("SELECT * FROM faults WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<FaultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<FaultEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: FaultEntity)
}
