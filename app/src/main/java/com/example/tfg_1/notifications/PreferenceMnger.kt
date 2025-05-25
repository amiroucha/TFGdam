package com.example.tfg_1.notifications

import android.content.Context

class PreferenceMnger (context: Context) {

    private val prefs = context.getSharedPreferences("flowhome_prefs", Context.MODE_PRIVATE)

    fun getLastNotifiedTimestamp(): Long {
        return prefs.getLong("last_notified_timestamp", 0L)
    }

    fun setLastNotifiedTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_notified_timestamp", timestamp).apply()
    }
}