package com.duoc.rotompedia.data.maps

import java.text.Normalizer
import java.util.Locale

/** Search and category filters over the *validated, edition-specific* client projection.
 * No network requests, no geographic position guesses, and no new Pokémon catalogue. */
enum class HoennEncounterCategory(val title: String) {
    ALL("Todos"), WALK("Hierba / cueva"), HEADBUTT("Cabezazo"),
    ROCK_SMASH("Golpe Roca"), WATER("Surf / buceo"),
    FISH("Pesca"), STATIC("Estáticos"), GIFT("Regalos / NPC"), OTHER("Otros métodos");

    /** Categorías de interfaz: no cambian métodos de la fuente ni fabrican registros. */
    fun includes(method: String): Boolean = when (this) {
        ALL -> true
        WALK -> AtlasEncounterMethods.family(method) == AtlasEncounterFamily.WALKING
        HEADBUTT -> AtlasEncounterMethods.family(method) == AtlasEncounterFamily.HEADBUTT
        ROCK_SMASH -> AtlasEncounterMethods.family(method) == AtlasEncounterFamily.ROCK_SMASH
        WATER -> AtlasEncounterMethods.family(method) == AtlasEncounterFamily.SURF
        FISH -> AtlasEncounterMethods.family(method) == AtlasEncounterFamily.FISHING
        STATIC -> AtlasEncounterMethods.family(method) == AtlasEncounterFamily.STATIC
        GIFT -> AtlasEncounterMethods.family(method) == AtlasEncounterFamily.GIFT
        OTHER -> AtlasEncounterMethods.family(method) in setOf(
            AtlasEncounterFamily.SPECIAL, AtlasEncounterFamily.ROAMING)
    }
}

data class HoennAreaResult(val area: MapArea, val recordCount: Int, val varietyCount: Int)

class HoennAreaSearch(catalogue: LocationCatalogue, labels: Map<Int, LocationPokemonLabel>) {
    private val pilot = catalogue.pilot
    private val locations = pilot.locations.associateBy { it.id }
    private val areasById = pilot.areas.associateBy { it.id }
    private val methods = pilot.methods.associateBy { it.id }
    private val encountersByArea = pilot.encounters.groupBy { it.areaId }
    private val labelsById = labels

    /** Method filter is always applied; a Pokémon search narrows rows ONLY when the query is
     * not an area/location name. This keeps geographical searches useful while making results
     * like "Ralts" and "#280" show only the requested Pokémon in the bottom sheet.
     */
    fun encountersForArea(areaId: Int, query: String = "",
        category: HoennEncounterCategory = HoennEncounterCategory.ALL): List<MapEncounter> {
        val area = areasById[areaId] ?: return emptyList()
        val matchingMethod = encountersByArea[areaId].orEmpty().filter { row ->
            category.includes(methods[row.methodId]?.slug.orEmpty())
        }
        val term = normalize(query.trim())
        if (term.isBlank() || matchesArea(area, term)) return matchingMethod
        val number = query.trim().removePrefix("#").toIntOrNull()
        return matchingMethod.filter { matchesPokemon(it, term, number) }
    }

    fun filter(query: String, category: HoennEncounterCategory = HoennEncounterCategory.ALL,
        onlyWithEncounters: Boolean = true): List<HoennAreaResult> {
        val term = normalize(query.trim())
        return pilot.areas.asSequence().mapNotNull { area ->
            val rows = encountersForArea(area.id, query, category)
            if (onlyWithEncounters && rows.isEmpty()) return@mapNotNull null
            if (term.isNotEmpty() && !matchesArea(area, term) && rows.isEmpty()) return@mapNotNull null
            HoennAreaResult(area, rows.size, rows.map { it.pokemonId }.distinct().size)
        }.sortedWith(compareBy<HoennAreaResult> {
            normalize(locations[it.area.locationId]?.names?.get("es")
                ?: locations[it.area.locationId]?.names?.get("en") ?: "")
        }.thenBy { it.area.id }).toList()
    }

    private fun matchesArea(area: MapArea, term: String): Boolean {
        val location = locations[area.locationId]
        val names = buildList {
            add(area.identifier)
            addAll(area.names.values)
            location?.let { add(it.slug); addAll(it.names.values) }
        }
        return names.any { normalize(it).contains(term) }
    }

    private fun matchesPokemon(row: MapEncounter, term: String, nationalNumber: Int?): Boolean {
        val label = labelsById[row.pokemonId] ?: return false
        return normalize(label.name).contains(term) || nationalNumber == label.nationalNumber
    }

    companion object {
        fun normalize(value: String): String = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").replace('-', ' ')
    }
}
