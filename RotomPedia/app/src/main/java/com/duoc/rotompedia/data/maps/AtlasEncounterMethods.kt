package com.duoc.rotompedia.data.maps

/**
 * UI groupings for encounter methods. A region has one atlas, while each game edition
 * contributes its own encounters; grouping never changes source method IDs.
 * Unknown methods stay visible and must not be silently discarded.
 */
enum class AtlasEncounterFamily(val title: String) {
    WALKING("Hierba alta y cuevas"),
    HEADBUTT("Cabezazo en árboles"),
    ROCK_SMASH("Golpe Roca"),
    SURF("Surf y buceo"),
    FISHING("Pesca"),
    STATIC("Pokémon estáticos"),
    GIFT("Regalos e intercambios"),
    ROAMING("Pokémon errantes"),
    SPECIAL("Otros encuentros")
}

object AtlasEncounterMethods {
    fun family(slug: String): AtlasEncounterFamily = when {
        slug == "walk" || slug == "walking" -> AtlasEncounterFamily.WALKING
        slug.startsWith("headbutt") -> AtlasEncounterFamily.HEADBUTT
        slug == "rock-smash" -> AtlasEncounterFamily.ROCK_SMASH
        slug == "surf" || slug == "seaweed" || slug == "dive" -> AtlasEncounterFamily.SURF
        slug in setOf("old-rod", "good-rod", "super-rod", "feebas-tile-fishing") -> AtlasEncounterFamily.FISHING
        slug == "static" -> AtlasEncounterFamily.STATIC
        slug in setOf("gift", "gift-egg", "npc-trade") -> AtlasEncounterFamily.GIFT
        slug.startsWith("roaming-") -> AtlasEncounterFamily.ROAMING
        else -> AtlasEncounterFamily.SPECIAL
    }

    fun displayName(slug: String, fallback: String? = null): String = when (slug) {
        "walk" -> "Caminar por hierba alta o cueva"
        "surf" -> "Surf"
        "seaweed" -> "Bucear entre algas"
        "dive" -> "Bucear"
        "headbutt" -> "Cabezazo en árbol"
        "headbutt-low" -> "Cabezazo · árbol de aparición baja"
        "headbutt-normal" -> "Cabezazo · árbol normal"
        "headbutt-high" -> "Cabezazo · árbol de aparición alta"
        "rock-smash" -> "Golpe Roca"
        "old-rod" -> "Pesca · Caña Vieja"
        "good-rod" -> "Pesca · Caña Buena"
        "super-rod" -> "Pesca · Supercaña"
        "feebas-tile-fishing" -> "Pesca en casilla de Feebas"
        "static" -> "Encuentro fijo"
        "gift" -> "Pokémon regalado"
        "gift-egg" -> "Huevo regalado"
        "npc-trade" -> "Intercambio con NPC"
        "roaming-grass" -> "Errante · hierba"
        "roaming-water" -> "Errante · agua"
        "devon-scope" -> "Detector Devon"
        "wailmer-pail" -> "Regadera Wailmer"
        else -> fallback?.takeIf { it.isNotBlank() } ?: slug.ifBlank { "Método no identificado" }
    }
}

/** Acquisition is not an encounter method: e.g. a potion GIVEN by an NPC is not
 * a collectible lying on the floor. No records may be shown without a source. */
enum class AtlasItemAcquisition(val title: String) {
    PICKUP("Objeto recogido"), HIDDEN("Objeto oculto"), NPC_GIFT("Regalo de NPC"),
    ROCK_SMASH("Objeto de Golpe Roca"), EVENT("Recompensa o evento"), OTHER("Otra forma de obtención")
}

data class AtlasSourcedItem(
    val areaId: Int,
    val versionId: Int,
    val itemSlug: String,
    val acquisition: AtlasItemAcquisition,
    val sourceUrl: String,
    val note: String? = null
) {
    init {
        require(areaId > 0 && versionId > 0 && itemSlug.isNotBlank())
        require(sourceUrl.startsWith("https://") && sourceUrl.length > 10)
    }
}

object AtlasItemFilter {
    /** A missing row means NOT YET DOCUMENTED, not proof that no items are present. */
    fun forEdition(items: List<AtlasSourcedItem>, areaId: Int, versionId: Int): List<AtlasSourcedItem> =
        items.filter { it.areaId == areaId && it.versionId == versionId }
}
