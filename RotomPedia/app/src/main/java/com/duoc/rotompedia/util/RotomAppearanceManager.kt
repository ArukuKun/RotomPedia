package com.duoc.rotompedia.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppearanceMode(val label: String) {
    SYSTEM("Seguir configuración del sistema"), LIGHT("Claro"), DARK("Oscuro");

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }
}

/** Device presentation only: deliberately independent of accounts, Room and rotom_form. */
object RotomAppearanceManager {
    private const val PREFS = "rotom_appearance_prefs"
    private const val KEY_MODE = "appearance_mode"
    private val current = MutableStateFlow(AppearanceMode.SYSTEM)
    val mode: StateFlow<AppearanceMode> = current

    fun init(context: Context) {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MODE, null)
        current.value = AppearanceMode.entries.firstOrNull { it.name == stored } ?: AppearanceMode.SYSTEM
    }

    fun setMode(context: Context, mode: AppearanceMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_MODE, mode.name).apply()
        current.value = mode
    }
}
