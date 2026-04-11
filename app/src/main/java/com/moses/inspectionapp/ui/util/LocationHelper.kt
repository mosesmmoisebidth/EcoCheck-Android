package com.moses.inspectionapp.ui.util

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.moses.inspectionapp.data.model.GeoPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun getCurrentLocation(context: Context): GeoPoint? {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val tokenSource = CancellationTokenSource()

    return suspendCancellableCoroutine { continuation ->
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                if (!continuation.isActive) return@addOnSuccessListener
                if (location == null) {
                    continuation.resume(null)
                } else {
                    continuation.resume(GeoPoint(location.latitude, location.longitude))
                }
            }
            .addOnFailureListener {
                if (!continuation.isActive) return@addOnFailureListener
                continuation.resume(null)
            }
    }
}
