package com.duoc.rotompedia.data.repository

import android.content.Context

class PreferencesSessionStore(context: Context) : SessionStore {
    private val preferences = context.applicationContext.getSharedPreferences("rotompedia_prefs", Context.MODE_PRIVATE)
    override fun read(): String? = preferences.getString("logged_in_user", null)
    override fun write(username: String?) {
        preferences.edit().apply {
            if (username == null) remove("logged_in_user") else putString("logged_in_user", username)
        }.apply()
    }
}
