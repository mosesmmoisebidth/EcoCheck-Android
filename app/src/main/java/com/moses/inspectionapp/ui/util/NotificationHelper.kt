package com.moses.inspectionapp.ui.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.store.AppPreferences

object NotificationHelper {
    private const val CHANNEL_ID = "inspection_updates"
    private const val CHANNEL_NAME = "Inspection Updates"

    fun notifyLogin(context: Context, displayName: String?) {
        if (!AppPreferences.notificationsEnabled || !AppPreferences.notifyLogin) {
            return
        }
        val title = context.getString(R.string.notification_login_title)
        val message = if (displayName.isNullOrBlank()) {
            context.getString(R.string.notification_login_body)
        } else {
            context.getString(R.string.notification_login_body_named, displayName)
        }
        post(context, title, message)
    }

    fun notifyFacilitySaved(context: Context, facilityName: String, isOffline: Boolean) {
        if (!AppPreferences.notificationsEnabled || !AppPreferences.notifyFacility) {
            return
        }
        val safeName = facilityName.ifBlank { context.getString(R.string.facility) }
        val title = context.getString(R.string.notification_facility_title)
        val message = if (isOffline) {
            context.getString(R.string.notification_facility_body_offline, safeName)
        } else {
            context.getString(R.string.notification_facility_body, safeName)
        }
        post(context, title, message)
    }

    fun notifyInspectionSubmitted(context: Context, facilityName: String, isOffline: Boolean) {
        if (!AppPreferences.notificationsEnabled || !AppPreferences.notifyInspection) {
            return
        }
        val safeName = facilityName.ifBlank { context.getString(R.string.facility) }
        val title = context.getString(R.string.notification_inspection_title)
        val message = if (isOffline) {
            context.getString(R.string.notification_inspection_body_offline, safeName)
        } else {
            context.getString(R.string.notification_inspection_body, safeName)
        }
        post(context, title, message)
    }

    private fun post(context: Context, title: String, message: String) {
        if (!canPostNotifications(context)) {
            return
        }
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) {
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
