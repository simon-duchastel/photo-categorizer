package com.duchastel.simon.photocategorizer.storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface LocalStorageRepository {
    fun getString(key: String): String?

    fun putString(key: String, value: String)
}

inline fun <reified T> LocalStorageRepository.get(key: String): T? {
    val stringValue = getString(key) ?: return null
    return Json.decodeFromString<T>(stringValue)
}

inline fun <reified T> LocalStorageRepository.put(key: String, value: T) {
    val stringValue = Json.encodeToString<T>(value)
    putString(key, stringValue)
}