package com.duoc.rotompedia.data.index

import com.squareup.moshi.JsonClass
import java.security.MessageDigest

@JsonClass(generateAdapter = true)
data class IndexEnvelope(val formatVersion: Int, val generatedAt: String, val generatorVersion: String,
    val contentSha256: String, val payloadJson: String)
@JsonClass(generateAdapter = true)
data class IndexPayload(val source: String, val speciesCount: Int, val varietyCount: Int,
    val species: List<SpeciesRecord>, val varieties: List<VarietyRecord>,
    val varietyTypes: List<VarietyTypeRecord> = emptyList(), val forms: List<FormRecord> = emptyList(),
    val formNames: List<FormNameRecord> = emptyList(), val referenceRevision: String? = null,
    val explorerComplete: Boolean = false, val eggGroups: List<EggGroupRecord> = emptyList(),
    val speciesEggGroups: List<SpeciesEggGroupRecord> = emptyList(), val eggGroupsComplete: Boolean = false)
@JsonClass(generateAdapter = true)
data class EggGroupRecord(val id: Int, val slug: String, val nameEs: String?)
@JsonClass(generateAdapter = true)
data class SpeciesEggGroupRecord(val speciesId: Int, val eggGroupId: Int)
@JsonClass(generateAdapter = true)
data class VarietyTypeRecord(val pokemonId: Int, val typeName: String, val slot: Int)
@JsonClass(generateAdapter = true)
data class FormRecord(val pokemonFormId: Int, val pokemonId: Int, val slug: String, val isMega: Boolean,
    val isBattleOnly: Boolean, val isDefault: Boolean, val formOrder: Int)
@JsonClass(generateAdapter = true)
data class FormNameRecord(val resourceId: Int, val language: String, val name: String)
@JsonClass(generateAdapter = true)
data class SpeciesRecord(val speciesId: Int, val nationalNumber: Int, val slug: String,
    val generationId: Int, val evolutionChainUrl: String?, val names: List<NameRecord>)
@JsonClass(generateAdapter = true)
data class VarietyRecord(val pokemonId: Int, val speciesId: Int, val slug: String, val isDefault: Boolean)
@JsonClass(generateAdapter = true)
data class NameRecord(val language: String, val name: String)

object IndexValidator {
    fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    fun validate(envelope: IndexEnvelope, payload: IndexPayload) {
        require(envelope.formatVersion == 1 && envelope.generatorVersion == "1") { "Formato de índice no compatible" }
        require(envelope.contentSha256 == sha256(envelope.payloadJson)) { "Hash de índice incorrecto" }
        java.time.Instant.parse(envelope.generatedAt)
        require(payload.source == "https://pokeapi.co/api/v2/pokedex/national/")
        require(payload.speciesCount > 0 && payload.speciesCount == payload.species.size)
        require(payload.varietyCount == payload.varieties.size)
        require(payload.species.map { it.speciesId }.toSet().size == payload.speciesCount)
        require(payload.species.map { it.slug }.toSet().size == payload.speciesCount)
        require(payload.species.map { it.nationalNumber }.toSet() == (1..payload.speciesCount).toSet())
        require(payload.species.all { it.speciesId > 0 && it.generationId > 0 && it.slug.isNotBlank() && it.names.isNotEmpty() })
        require(payload.varieties.map { it.pokemonId }.toSet().size == payload.varietyCount)
        require(payload.varieties.map { it.slug }.toSet().size == payload.varietyCount)
        val ids = payload.species.map { it.speciesId }.toSet()
        require(payload.varieties.all { it.pokemonId > 0 && it.speciesId in ids && it.slug.isNotBlank() })
        require(payload.varieties.groupBy { it.speciesId }.keys == ids)
        require(payload.varieties.groupBy { it.speciesId }.values.all { list -> list.count { it.isDefault } == 1 })
        if (payload.eggGroupsComplete || payload.eggGroups.isNotEmpty() || payload.speciesEggGroups.isNotEmpty()) {
            val groups = payload.eggGroups
            val links = payload.speciesEggGroups
            val groupIds = groups.map { it.id }.toSet()
            require(payload.referenceRevision?.matches(Regex("[a-f0-9]{40}")) == true)
            require(groups.isNotEmpty() && groups.distinctBy { it.id }.size == groups.size && groups.distinctBy { it.slug }.size == groups.size)
            require(groups.all { it.id > 0 && it.slug.isNotBlank() })
            require(links.all { it.speciesId in ids && it.eggGroupId in groupIds })
            require(links.distinct().size == links.size && links.map { it.speciesId }.toSet() == ids)
            require(links.groupBy { it.speciesId }.values.all { it.size in 1..2 })
        }
        if (payload.explorerComplete) {
            val pokemonIds = payload.varieties.map { it.pokemonId }.toSet()
            require(payload.referenceRevision?.matches(Regex("[a-f0-9]{40}")) == true)
            require(payload.varietyTypes.map { it.pokemonId }.toSet() == pokemonIds)
            require(payload.varietyTypes.all { it.slot in 1..2 && it.typeName.isNotBlank() })
            require(payload.varietyTypes.distinctBy { it.pokemonId to it.slot }.size == payload.varietyTypes.size)
            require(payload.forms.all { it.pokemonId in pokemonIds && it.pokemonFormId > 0 })
            require(payload.forms.distinctBy { it.pokemonFormId }.size == payload.forms.size)
            require(payload.forms.map { it.pokemonId }.toSet() == pokemonIds)
            require(payload.forms.filter { it.isDefault }.groupBy { it.pokemonId }.values.all { it.size == 1 })
            val formIds = payload.forms.map { it.pokemonFormId }.toSet()
            require(payload.formNames.all { it.resourceId in formIds && it.name.isNotBlank() })
        }
    }
}
