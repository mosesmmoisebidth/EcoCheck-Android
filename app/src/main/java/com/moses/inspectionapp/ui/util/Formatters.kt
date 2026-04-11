package com.moses.inspectionapp.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun enumLabel(value: String): String {
    return value
        .lowercase()
        .split('_')
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
}

fun formatDateTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
