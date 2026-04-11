package com.moses.inspectionapp.data.model

data class NotificationLogEntry(
    val id: Long,
    val title: String,
    val message: String,
    val createdAt: Long,
)
