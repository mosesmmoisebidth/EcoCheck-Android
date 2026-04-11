package com.moses.inspectionapp.data.store

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "inspection_prefs"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_HAS_SESSION = "has_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_ACCESS_TOKEN_EXPIRY = "access_token_expiry"
    private const val KEY_API_BASE_URL = "api_base_url"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_DISTRICT = "user_district"
    private const val KEY_USER_SECTOR = "user_sector"
    private const val KEY_AUTO_SYNC = "auto_sync"
    private const val KEY_WIFI_ONLY = "wifi_only"
    private const val KEY_LAST_SYNC = "last_sync"
    private const val KEY_HAS_ONBOARDED = "has_onboarded"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_NOTIFY_LOGIN = "notify_login"
    private const val KEY_NOTIFY_FACILITY = "notify_facility"
    private const val KEY_NOTIFY_INSPECTION = "notify_inspection"
    private const val KEY_FACILITIES_BACKUP = "facilities_backup"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var languageCode: String?
        get() = prefs.getString(KEY_LANGUAGE, null)
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value).apply()
        }

    var hasSession: Boolean
        get() = prefs.getBoolean(KEY_HAS_SESSION, false)
        set(value) {
            prefs.edit().putBoolean(KEY_HAS_SESSION, value).apply()
        }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()
        }

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()
        }

    var accessTokenExpiry: Long
        get() = prefs.getLong(KEY_ACCESS_TOKEN_EXPIRY, 0L)
        set(value) {
            prefs.edit().putLong(KEY_ACCESS_TOKEN_EXPIRY, value).apply()
        }

    var apiBaseUrl: String?
        get() = prefs.getString(KEY_API_BASE_URL, null)
        set(value) {
            prefs.edit().putString(KEY_API_BASE_URL, value).apply()
        }

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) {
            prefs.edit().putString(KEY_USER_ID, value).apply()
        }

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) {
            prefs.edit().putString(KEY_USER_NAME, value).apply()
        }

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) {
            prefs.edit().putString(KEY_USER_EMAIL, value).apply()
        }

    var userRole: String?
        get() = prefs.getString(KEY_USER_ROLE, null)
        set(value) {
            prefs.edit().putString(KEY_USER_ROLE, value).apply()
        }

    var userDistrict: String?
        get() = prefs.getString(KEY_USER_DISTRICT, null)
        set(value) {
            prefs.edit().putString(KEY_USER_DISTRICT, value).apply()
        }

    var userSector: String?
        get() = prefs.getString(KEY_USER_SECTOR, null)
        set(value) {
            prefs.edit().putString(KEY_USER_SECTOR, value).apply()
        }

    var autoSync: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC, true)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_SYNC, value).apply()
        }

    var wifiOnly: Boolean
        get() = prefs.getBoolean(KEY_WIFI_ONLY, true)
        set(value) {
            prefs.edit().putBoolean(KEY_WIFI_ONLY, value).apply()
        }

    var lastSyncEpoch: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_SYNC, value).apply()
        }

    var hasOnboarded: Boolean
        get() = prefs.getBoolean(KEY_HAS_ONBOARDED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_HAS_ONBOARDED, value).apply()
        }

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
        }

    var notifyLogin: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_LOGIN, true)
        set(value) {
            prefs.edit().putBoolean(KEY_NOTIFY_LOGIN, value).apply()
        }

    var notifyFacility: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_FACILITY, true)
        set(value) {
            prefs.edit().putBoolean(KEY_NOTIFY_FACILITY, value).apply()
        }

    var notifyInspection: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_INSPECTION, true)
        set(value) {
            prefs.edit().putBoolean(KEY_NOTIFY_INSPECTION, value).apply()
        }

    var facilitiesBackup: String?
        get() = prefs.getString(KEY_FACILITIES_BACKUP, null)
        set(value) {
            prefs.edit().putString(KEY_FACILITIES_BACKUP, value).apply()
        }

    val hasLanguage: Boolean
        get() = !languageCode.isNullOrBlank()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_HAS_SESSION)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ACCESS_TOKEN_EXPIRY)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_DISTRICT)
            .remove(KEY_USER_SECTOR)
            .apply()
    }
}
