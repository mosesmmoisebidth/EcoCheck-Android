package com.moses.inspectionapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moses.inspectionapp.data.local.dao.FacilityDao
import com.moses.inspectionapp.data.local.dao.FaultDao
import com.moses.inspectionapp.data.local.dao.InspectionDao
import com.moses.inspectionapp.data.local.dao.InspectionFaultDao
import com.moses.inspectionapp.data.local.dao.InspectionTypeDao
import com.moses.inspectionapp.data.local.dao.SmsLogDao
import com.moses.inspectionapp.data.local.dao.TeamMemberDao
import com.moses.inspectionapp.data.local.entity.FacilityEntity
import com.moses.inspectionapp.data.local.entity.FaultEntity
import com.moses.inspectionapp.data.local.entity.InspectionEntity
import com.moses.inspectionapp.data.local.entity.InspectionFaultEntity
import com.moses.inspectionapp.data.local.entity.InspectionTypeEntity
import com.moses.inspectionapp.data.local.entity.SmsLogEntity
import com.moses.inspectionapp.data.local.entity.TeamMemberEntity

@Database(
    entities = [
        FacilityEntity::class,
        InspectionEntity::class,
        FaultEntity::class,
        InspectionFaultEntity::class,
        SmsLogEntity::class,
        InspectionTypeEntity::class,
        TeamMemberEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun facilityDao(): FacilityDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun faultDao(): FaultDao
    abstract fun inspectionFaultDao(): InspectionFaultDao
    abstract fun inspectionTypeDao(): InspectionTypeDao
    abstract fun smsLogDao(): SmsLogDao
    abstract fun teamMemberDao(): TeamMemberDao
}
