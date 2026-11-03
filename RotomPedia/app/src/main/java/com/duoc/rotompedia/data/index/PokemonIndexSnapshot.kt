package com.duoc.rotompedia.data.index

import com.squareup.moshi.JsonClass
import java.security.MessageDigest

@JsonClass(generateAdapter = true)
data class IndexEnvelope(val formatVersion: Int, val generatedAt: String, val generatorVersion: String,
    val contentSha256: String, val payloadJson: String)
@JsonClass(generateAdapter = true)
data class IndexPayload(val source: String, val speciesCount: Int, val varietyCount: Int,
    val species: List<SpeciesRecord>, val varieties: List<VarietyRecord>)
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
    }
}
