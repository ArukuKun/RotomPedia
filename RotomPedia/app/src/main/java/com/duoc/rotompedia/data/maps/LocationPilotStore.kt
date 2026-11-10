package com.duoc.rotompedia.data.maps

import com.duoc.rotompedia.data.index.IndexValidator
import com.duoc.rotompedia.data.reference.GameContentScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Validated local pilot reader. Area browsing is allowed; geographical pins still require audited geometry. */
class LocationPilotStore(moshi: Moshi, private val source: suspend () -> String,
    private val pokemonIdentity: suspend () -> Pair<String, Set<Int>>) {
    private val envelope = moshi.adapter(LocationEnvelope::class.java)
    private val payload = moshi.adapter(LocationPilot::class.java)
    private val lock = Mutex()
    @Volatile private var cached: LocationCatalogue? = null

    suspend fun catalogue(): LocationCatalogue = withContext(Dispatchers.Default) {
        cached ?: lock.withLock {
            cached ?: run {
                val e = requireNotNull(envelope.fromJson(source()))
                require(e.formatVersion == 1 && IndexValidator.sha256(e.payloadJson) == e.sha256)
                val p = requireNotNull(payload.fromJson(e.payloadJson))
                val (hash, pokemonIds) = pokemonIdentity()
                validate(p, hash, pokemonIds)
                LocationCatalogue(p).also { cached = it }
            }
        }
    }

    companion object {
        private val hashPattern = Regex("[a-f0-9]{64}")
        fun validate(p: LocationPilot, pokemonHash: String, pokemonIds: Set<Int>) {
            require(p.regionId > 0 && p.versionId > 0 && p.region.isNotBlank() && p.version.isNotBlank() && p.versionGroup.isNotBlank())
            require(GameContentScope.allowsVersion(p.version) && GameContentScope.allowsVersion(p.versionGroup))
            val s = p.source
            require(s.revision.matches(Regex("[a-f0-9]{40}")) && s.pokemonIndexSha256 == pokemonHash && pokemonHash.matches(hashPattern))
            require(s.licenseText.isNotBlank() && s.licenseUrl == "https://github.com/PokeAPI/pokeapi/blob/${s.revision}/LICENSE.md")
            val files = setOf("regions", "versions", "version_groups", "version_group_regions", "locations", "location_names",
                "location_areas", "location_area_prose", "encounters", "encounter_slots", "encounter_methods", "encounter_method_prose",
                "encounter_conditions", "encounter_condition_values", "encounter_condition_value_prose",
                "encounter_condition_value_map", "location_area_encounter_rates").map { "$it.csv" }.toSet()
            require(s.files.size == files.size && s.files.map { it.file }.toSet() == files && s.files.all { it.sha256.matches(hashPattern) })
            fun identities(ids: List<Int>) { require(ids.all { it > 0 } && ids.distinct().size == ids.size) }
            fun names(names: Map<String, String>) { require(names.all { it.key in setOf("es", "en") && it.value.isNotBlank() }) }
            identities(p.locations.map { it.id }); identities(p.areas.map { it.id }); identities(p.methods.map { it.id })
            identities(p.conditions.map { it.id }); identities(p.encounters.map { it.id })
            require(p.locations.isNotEmpty() && p.areas.isNotEmpty() && p.encounters.isNotEmpty())
            p.locations.forEach { require(it.slug.isNotBlank()); names(it.names) }
            p.areas.forEach { names(it.names) }
            p.methods.forEach { require(it.slug.isNotBlank()); names(it.names) }
            p.conditions.forEach { require(it.slug.isNotBlank() && it.condition.isNotBlank()); names(it.names) }
            val locations = p.locations.map { it.id }.toSet()
            val areas = p.areas.map { it.id }.toSet()
            val methods = p.methods.map { it.id }.toSet()
            val conditions = p.conditions.map { it.id }.toSet()
            require(p.areas.all { it.locationId in locations })
            p.encounters.forEach { r ->
                require(r.areaId in areas && r.versionId == p.versionId && r.pokemonId in pokemonIds && r.methodId in methods)
                require(r.slotId > 0 && (r.slot == null || r.slot >= 0) && (r.chance == null || r.chance in 0..100))
                require(r.minLevel in 1..100 && r.maxLevel in r.minLevel..100)
                require(r.conditionIds.distinct().size == r.conditionIds.size && r.conditionIds.all { it in conditions })
            }
            require(p.methodRates.distinctBy { Triple(it.areaId, it.methodId, it.versionId) }.size == p.methodRates.size)
            require(p.methodRates.all { it.areaId in areas && it.methodId in methods && it.versionId == p.versionId && it.rate in 0..100 })
            require(p.coverage == LocationCoverage(p.areas.size, p.encounters.map { it.areaId }.distinct().size,
                p.encounters.size, p.encounters.map { it.pokemonId }.distinct().size, "not-sourced", "not-sourced"))
        }
    }
}
