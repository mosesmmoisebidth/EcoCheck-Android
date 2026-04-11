package com.moses.inspectionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moses.inspectionapp.data.local.entity.SmsLogEntity

@Dao
interface SmsLogDao {
    @Query("SELECT * FROM sms_logs ORDER BY createdAt DESC")
    suspend fun getAll(): List<SmsLogEntity>

    @Query("SELECT COUNT(*) FROM sms_logs WHERE inspectionId = :inspectionId")
    suspend fun countForInspection(inspectionId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SmsLogEntity)
}
