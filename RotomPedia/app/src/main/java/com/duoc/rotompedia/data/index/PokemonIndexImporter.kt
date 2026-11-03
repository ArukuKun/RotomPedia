package com.duoc.rotompedia.data.index

import androidx.room.withTransaction
import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.local.index.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PokemonIndexImporter(private val db: AppDatabase, moshi: Moshi) {
    private val envelopeAdapter = moshi.adapter(IndexEnvelope::class.java)
    private val payloadAdapter = moshi.adapter(IndexPayload::class.java)

    suspend fun importJson(json: String): Boolean = withContext(Dispatchers.IO) {
        val envelope = requireNotNull(envelopeAdapter.fromJson(json))
        val payload = requireNotNull(payloadAdapter.fromJson(envelope.payloadJson))
        IndexValidator.validate(envelope, payload)
        val metadataRevision = IndexValidator.sha256(PokemonFormCatalog.allForms.joinToString("\n") {
            "${it.nameEn}|${it.speciesId}|${it.displayName}|${it.formTag}"
        })
        db.withTransaction {
            val dao = db.pokemonIndexDao()
            val previous = dao.state()
            if (previous?.contentSha256 == envelope.contentSha256 && previous.metadataRevision == metadataRevision) return@withTransaction false
            require(previous == null || java.time.Instant.parse(envelope.generatedAt) >= java.time.Instant.parse(previous.generatedAt)) { "Índice obsoleto" }
            // Reject disappearing or reassigned identities, rather than silently deleting them.
            dao.national().forEach { old ->
                require(payload.species.any { it.speciesId == old.speciesId && it.nationalNumber == old.nationalNumber && it.slug == old.slug })
            }
            for (species in dao.national()) {
                // Check all existing varieties as well, including species-specific refreshes.
                dao.varieties(species.speciesId).forEach { old ->
                    require(payload.varieties.any { it.pokemonId == old.pokemonId && it.speciesId == old.speciesId && it.slug == old.slug })
                }
            }
            dao.putSpecies(payload.species.map { IndexedSpecies(it.speciesId, it.nationalNumber, it.slug, it.generationId, it.evolutionChainUrl) })
            dao.putVarieties(payload.varieties.map { IndexedVariety(it.pokemonId, it.speciesId, it.slug, it.isDefault) })
            dao.putForms(payload.forms.map { IndexedForm(it.pokemonFormId, it.pokemonId, it.slug, it.isMega, it.isBattleOnly, it.isDefault, it.formOrder) })
            if (payload.explorerComplete) {
                payload.varieties.forEach { dao.clearTypes(it.pokemonId) }
                dao.putTypes(payload.varietyTypes.map { IndexedVarietyType(it.pokemonId, it.slot, it.typeName) })
            }
            val names = payload.species.flatMap { species -> species.names.map {
                IndexedName("species", species.speciesId, it.language, it.name, PokemonFormCatalog.normalize(it.name), "pokeapi")
            } } + payload.varieties.map { variety ->
                val metadata = PokemonFormCatalog.find(variety.slug)
                require(metadata == null || metadata.speciesId == variety.speciesId) { "Metadata local incompatible con especie API" }
                val name = metadata?.displayName ?: variety.slug
                IndexedName("variety", variety.pokemonId, if (metadata != null) "es" else "slug",
                    name, PokemonFormCatalog.normalize(name), if (metadata != null) "curated:$metadataRevision" else "pokeapi")
            }
            dao.putNames((names + payload.formNames.map {
                IndexedName("form", it.resourceId, it.language, it.name, PokemonFormCatalog.normalize(it.name), "pokeapi-csv")
            }).distinct())
            dao.putState(PokemonIndexState(formatVersion = 1, contentSha256 = envelope.contentSha256, metadataRevision = metadataRevision,
                generatedAt = envelope.generatedAt, speciesCount = payload.speciesCount,
                varietyCount = payload.varietyCount, formsComplete = false, importedAt = System.currentTimeMillis()))
            true
        }
    }
}
