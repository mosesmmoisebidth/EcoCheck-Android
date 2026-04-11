package com.moses.inspectionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moses.inspectionapp.data.local.entity.InspectionFaultEntity

@Dao
interface InspectionFaultDao {
    @Query("SELECT faultId FROM inspection_faults WHERE inspectionId = :inspectionId")
    suspend fun getFaultIdsForInspection(inspectionId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<InspectionFaultEntity>)

    @Query("DELETE FROM inspection_faults WHERE inspectionId = :inspectionId")
    suspend fun clearForInspection(inspectionId: String)
}
