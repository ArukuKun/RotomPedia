package com.duoc.rotompedia.data.maps

import android.content.Context

/** A regional map is independent of its game-specific encounters. */
data class RegionalMap(val name: String, val slug: String, val available: Boolean = true)

object MapRegionCatalogue {
    // Kanto stays first. Disabled regions cannot become a remembered destination.
    val regions: List<RegionalMap> = listOf(
        RegionalMap("Kanto", "kanto"),
        RegionalMap("Johto", "johto"),
        RegionalMap("Hoenn", "hoenn"),
        RegionalMap("Sinnoh", "sinnoh"),
        RegionalMap("Teselia (Unova)", "teselia"),
        RegionalMap("Kalos", "kalos"),
        RegionalMap("Alola", "alola"),
        RegionalMap("Galar", "galar"),
        RegionalMap("Hisui", "hisui", available = false),
        RegionalMap("Paldea", "paldea")
    )

    /** Null means first visit -> show the selector. Never assume Kanto was opened. */
    fun restorableRegion(value: String?): String? =
        regions.firstOrNull { it.slug == value && it.available }?.slug
}

/** Lightweight, device-local UI preference. Does not modify Room or player data. */
class LastMapRegionStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "rotompedia_map_navigation", Context.MODE_PRIVATE)

    fun lastOpenedRegion(): String? =
        MapRegionCatalogue.restorableRegion(preferences.getString("last_opened_region", null))

    fun rememberRegion(region: String) {
        require(MapRegionCatalogue.restorableRegion(region) != null) {
            "Unsupported or unavailable map region: $region"
        }
        preferences.edit().putString("last_opened_region", region).apply()
    }
}
