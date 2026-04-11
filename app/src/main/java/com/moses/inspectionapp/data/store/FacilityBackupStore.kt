package com.moses.inspectionapp.data.store

import android.util.Log
import com.moses.inspectionapp.data.local.dao.FacilityDao
import com.moses.inspectionapp.data.local.entity.FacilityEntity
import org.json.JSONArray
import org.json.JSONObject

object FacilityBackupStore {
    private const val TAG = "FacilityBackupStore"

    suspend fun writeBackup(facilityDao: FacilityDao) {
        runCatching {
            val facilities = facilityDao.getAll()
            if (facilities.isEmpty()) {
                return
            }
            AppPreferences.facilitiesBackup = encode(facilities)
        }.onFailure { error ->
            Log.e(TAG, "Failed to write facility backup", error)
        }
    }

    suspend fun restoreIfEmpty(facilityDao: FacilityDao): Boolean {
        return runCatching {
            if (facilityDao.count() > 0) {
                return false
            }
            val raw = AppPreferences.facilitiesBackup
            if (raw.isNullOrBlank()) {
                return false
            }
            val decoded = decode(raw)
            if (decoded.isEmpty()) {
                return false
            }
            facilityDao.upsertAll(decoded)
            Log.i(TAG, "Restored ${decoded.size} facilities from backup")
            true
        }.getOrElse { error ->
            Log.e(TAG, "Failed to restore facility backup", error)
            false
        }
    }

    private fun encode(items: List<FacilityEntity>): String {
        val array = JSONArray()
        items.forEach { facility ->
            val obj = JSONObject()
            obj.put("id", facility.id)
            obj.put("serverId", facility.serverId ?: JSONObject.NULL)
            obj.put("name", facility.name)
            obj.put("tin", facility.tin)
            obj.put("ownerName", facility.ownerName)
            obj.put("ownerPhone", facility.ownerPhone)
            obj.put("ownerEmail", facility.ownerEmail)
            obj.put("district", facility.district)
            obj.put("sector", facility.sector)
            obj.put("cell", facility.cell)
            obj.put("village", facility.village)
            obj.put("latitude", facility.latitude ?: JSONObject.NULL)
            obj.put("longitude", facility.longitude ?: JSONObject.NULL)
            obj.put("photoPath", facility.photoPath ?: JSONObject.NULL)
            obj.put("createdAt", facility.createdAt)
            obj.put("createdBy", facility.createdBy)
            obj.put("updatedAt", facility.updatedAt)
            obj.put("syncStatus", facility.syncStatus)
            array.put(obj)
        }
        return array.toString()
    }

    private fun decode(raw: String): List<FacilityEntity> {
        val array = JSONArray(raw)
        val result = ArrayList<FacilityEntity>(array.length())
        for (index in 0 until array.length()) {
            val obj = array.getJSONObject(index)
            result.add(
                FacilityEntity(
                    id = obj.getString("id"),
                    serverId = obj.optString("serverId").takeIf { it.isNotBlank() && it != "null" },
                    name = obj.getString("name"),
                    tin = obj.getString("tin"),
                    ownerName = obj.getString("ownerName"),
                    ownerPhone = obj.getString("ownerPhone"),
                    ownerEmail = obj.getString("ownerEmail"),
                    district = obj.getString("district"),
                    sector = obj.getString("sector"),
                    cell = obj.getString("cell"),
                    village = obj.getString("village"),
                    latitude = if (obj.isNull("latitude")) null else obj.getDouble("latitude"),
                    longitude = if (obj.isNull("longitude")) null else obj.getDouble("longitude"),
                    photoPath = if (obj.isNull("photoPath")) null else obj.getString("photoPath"),
                    createdAt = obj.getLong("createdAt"),
                    createdBy = obj.getString("createdBy"),
                    updatedAt = obj.getLong("updatedAt"),
                    syncStatus = obj.getString("syncStatus"),
                ),
            )
        }
        return result
    }
}
