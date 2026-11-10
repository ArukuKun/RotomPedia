package com.duoc.rotompedia.data.maps

import com.squareup.moshi.JsonClass
import com.duoc.rotompedia.data.reference.GameContentScope

@JsonClass(generateAdapter = true)
data class LocationEnvelope(val formatVersion: Int, val sha256: String, val payloadJson: String)
@JsonClass(generateAdapter = true)
data class LocationSourceFile(val file: String, val sha256: String)
@JsonClass(generateAdapter = true)
data class LocationSource(val revision: String, val pokemonIndexSha256: String,
    val files: List<LocationSourceFile>, val licenseUrl: String, val licenseText: String)
@JsonClass(generateAdapter = true)
data class MapLocation(val id: Int, val slug: String, val names: Map<String, String>)
@JsonClass(generateAdapter = true)
data class MapArea(val id: Int, val locationId: Int, val identifier: String, val names: Map<String, String>)
@JsonClass(generateAdapter = true)
data class MapEncounterMethod(val id: Int, val slug: String, val names: Map<String, String>)
@JsonClass(generateAdapter = true)
data class MapEncounterCondition(val id: Int, val slug: String, val condition: String,
    val isDefault: Boolean, val names: Map<String, String>)
@JsonClass(generateAdapter = true)
data class MapEncounter(val id: Int, val areaId: Int, val versionId: Int, val pokemonId: Int,
    val methodId: Int, val slotId: Int, val slot: Int?, val chance: Int?,
    val minLevel: Int, val maxLevel: Int, val conditionIds: List<Int>)
@JsonClass(generateAdapter = true)
data class MapMethodRate(val areaId: Int, val methodId: Int, val versionId: Int, val rate: Int)
@JsonClass(generateAdapter = true)
data class LocationCoverage(val declaredAreas: Int, val areasWithEncounters: Int, val encounterRows: Int,
    val pokemonVarieties: Int, val objects: String, val cartography: String)
@JsonClass(generateAdapter = true)
data class LocationPilot(val regionId: Int, val region: String, val versionId: Int, val version: String,
    val versionGroup: String, val locations: List<MapLocation>, val areas: List<MapArea>,
    val methods: List<MapEncounterMethod>, val conditions: List<MapEncounterCondition>,
    val encounters: List<MapEncounter>, val methodRates: List<MapMethodRate>,
    val coverage: LocationCoverage, val source: LocationSource)

enum class AreaCoverage { AVAILABLE, NO_SOURCE_ROWS, NOT_IN_PACKAGE }
data class AreaEncounters(val areaId: Int, val versionId: Int, val coverage: AreaCoverage,
    val encounters: List<MapEncounter>, val methodRates: List<MapMethodRate>)

/** No network or parallel Pokémon catalogue: labels/ownership still come from PokemonRepository. */
class LocationCatalogue internal constructor(source: LocationPilot) {
    val sourceCoverage = source.coverage
    private val allowedMethods = source.methods.filter { GameContentScope.allowsEncounterMethod(it.slug) }
    private val allowedIds = allowedMethods.map { it.id }.toSet()
    private val allowedEncounters = source.encounters.filter { it.methodId in allowedIds }
    val pilot = source.copy(methods = allowedMethods, encounters = allowedEncounters,
        methodRates = source.methodRates.filter { it.methodId in allowedIds },
        coverage = source.coverage.copy(areasWithEncounters = allowedEncounters.map { it.areaId }.distinct().size,
            encounterRows = allowedEncounters.size, pokemonVarieties = allowedEncounters.map { it.pokemonId }.distinct().size))
    private val areas = pilot.areas.associateBy { it.id }
    private val encounters = pilot.encounters.groupBy { it.areaId }
    private val rates = pilot.methodRates.groupBy { it.areaId }

    fun area(areaId: Int, versionId: Int): AreaEncounters {
        if (areaId !in areas || versionId != pilot.versionId)
            return AreaEncounters(areaId, versionId, AreaCoverage.NOT_IN_PACKAGE, emptyList(), emptyList())
        val rows = encounters[areaId].orEmpty()
        return AreaEncounters(areaId, versionId, if (rows.isEmpty()) AreaCoverage.NO_SOURCE_ROWS else AreaCoverage.AVAILABLE,
            rows, rates[areaId].orEmpty())
    }
}
