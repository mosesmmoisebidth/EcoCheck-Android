package com.moses.inspectionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moses.inspectionapp.data.local.entity.InspectionTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionTypeDao {
    @Query("SELECT * FROM inspection_types ORDER BY name ASC")
    fun observeTypes(): Flow<List<InspectionTypeEntity>>

    @Query("SELECT * FROM inspection_types")
    suspend fun getAll(): List<InspectionTypeEntity>

    @Query("SELECT * FROM inspection_types WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): InspectionTypeEntity?

    @Query("SELECT COUNT(*) FROM inspection_types")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<InspectionTypeEntity>)
}
