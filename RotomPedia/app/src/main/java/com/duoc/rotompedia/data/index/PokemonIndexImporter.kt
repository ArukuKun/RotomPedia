package com.duoc.rotompedia.data.index

import androidx.room.withTransaction
import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.data.explorer.ExplorerEggGroup
import com.duoc.rotompedia.data.explorer.ExplorerMetadata
import com.duoc.rotompedia.data.explorer.ExplorerRegionalDex
import com.duoc.rotompedia.data.explorer.ExplorerDexNumber
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.local.index.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PokemonIndexImporter(private val db: AppDatabase, moshi: Moshi) {
    // Immutable metadata from the validated bundled index, not personal Room data.
    // Rehydrated even when Room already contains this snapshot (offline reopening).
    private val explorer = MutableStateFlow(ExplorerMetadata())
    val metadata: StateFlow<ExplorerMetadata> = explorer
    private val envelopeAdapter = moshi.adapter(IndexEnvelope::class.java)
    private val payloadAdapter = moshi.adapter(IndexPayload::class.java)

    suspend fun importJson(json: String): Boolean = withContext(Dispatchers.IO) {
        val envelope = requireNotNull(envelopeAdapter.fromJson(json))
        val payload = requireNotNull(payloadAdapter.fromJson(envelope.payloadJson))
        IndexValidator.validate(envelope, payload)
        val metadataRevision = IndexValidator.sha256(PokemonFormCatalog.allForms.joinToString("\n") {
            "${it.nameEn}|${it.speciesId}|${it.displayName}|${it.formTag}"
        })
        val imported = db.withTransaction {
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
        val groups = payload.eggGroups.associateBy { it.id }
        val eggs = payload.speciesEggGroups.groupBy { it.speciesId }.mapValues { (_, links) -> links.map { link ->
            val group = groups.getValue(link.eggGroupId)
            ExplorerEggGroup(group.slug, group.nameEs ?: "${group.slug} (sin nombre español)")
        } }
        val dexes = payload.regionalDexes.associate { it.id to ExplorerRegionalDex(it.id, it.region, it.slug, it.nameEs, it.versionGroups) }
        val numbers = payload.regionalDexNumbers.groupBy { it.speciesId }.mapValues { (_, rows) ->
            rows.map { ExplorerDexNumber(dexes.getValue(it.dexId), it.number) }
        }
        explorer.value = ExplorerMetadata(eggs, numbers, ready = true)
        imported
    }
}
