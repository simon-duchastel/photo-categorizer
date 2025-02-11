package com.duchastel.simon.photocategorizer.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class SharedPrefsLocalStorageRepository @Inject constructor(
    context: Context,
): LocalStorageRepository {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        SHARED_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun getString(key: String): String? {
        return sharedPrefs.getString("$SHARED_PREFS_KEY_PREFIX$key", null)
    }

    override fun putString(key: String, value: String) {
        sharedPrefs.edit(commit = true) {
            putString("$SHARED_PREFS_KEY_PREFIX$key", value)
        }
    }

    companion object {
        private const val SHARED_PREFS_NAME =
            "com.duchastel.simon.photocategorizer.storage.SharedPrefsLocalStorageRepository"
        private const val SHARED_PREFS_KEY_PREFIX =
            "com.duchastel.simon.photocategorizer.storage"
    }
}