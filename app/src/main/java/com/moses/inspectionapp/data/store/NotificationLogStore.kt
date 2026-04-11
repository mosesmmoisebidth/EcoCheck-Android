package com.moses.inspectionapp.data.store

import com.moses.inspectionapp.data.model.NotificationLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

object NotificationLogStore {
    private const val MAX_ENTRIES = 50
    private val _logs = MutableStateFlow<List<NotificationLogEntry>>(emptyList())
    val logs: StateFlow<List<NotificationLogEntry>> = _logs

    fun refresh() {
        _logs.value = load()
    }

    fun append(title: String, message: String) {
        val entry = NotificationLogEntry(
            id = System.currentTimeMillis(),
            title = title,
            message = message,
            createdAt = System.currentTimeMillis(),
        )
        val updated = listOf(entry) + load()
        val trimmed = updated.take(MAX_ENTRIES)
        save(trimmed)
        _logs.value = trimmed
    }

    fun clear() {
        save(emptyList())
        _logs.value = emptyList()
    }

    private fun load(): List<NotificationLogEntry> {
        val json = AppPreferences.notificationLogJson ?: return emptyList()
        if (json.isBlank()) return emptyList()
        return runCatching { decode(json) }.getOrDefault(emptyList())
    }

    private fun save(entries: List<NotificationLogEntry>) {
        AppPreferences.notificationLogJson = encode(entries)
    }

    private fun decode(json: String): List<NotificationLogEntry> {
        val array = JSONArray(json)
        val entries = ArrayList<NotificationLogEntry>(array.length())
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            val id = obj.optLong("id")
            val title = obj.optString("title")
            val message = obj.optString("message")
            val createdAt = obj.optLong("createdAt", id)
            entries.add(
                NotificationLogEntry(
                    id = id,
                    title = title,
                    message = message,
                    createdAt = createdAt,
                ),
            )
        }
        return entries
    }

    private fun encode(entries: List<NotificationLogEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("title", entry.title)
            obj.put("message", entry.message)
            obj.put("createdAt", entry.createdAt)
            array.put(obj)
        }
        return array.toString()
    }
}
