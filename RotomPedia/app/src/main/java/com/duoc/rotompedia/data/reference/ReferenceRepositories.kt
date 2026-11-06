package com.duoc.rotompedia.data.reference

import com.duoc.rotompedia.data.index.IndexValidator
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.data.repository.PokemonRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** One immutable bundled reference shared by moves, abilities and item/machine links. */
class ReferenceStore(moshi: Moshi, private val source: suspend () -> String) {
    private val envelope = moshi.adapter(ReferenceEnvelope::class.java)
    private val payload = moshi.adapter(ReferenceIndex::class.java)
    private val lock = Mutex()
    @Volatile private var cached: ReferenceIndex? = null
    suspend fun index(): ReferenceIndex = withContext(Dispatchers.Default) {
        cached ?: lock.withLock {
            cached ?: run {
                val e = requireNotNull(envelope.fromJson(source()))
                require(e.formatVersion == 1 && IndexValidator.sha256(e.payloadJson) == e.sha256)
                val p = requireNotNull(payload.fromJson(e.payloadJson))
                require(p.revision.matches(Regex("[a-f0-9]{40}")))
                require(p.moves.isNotEmpty() && p.abilities.isNotEmpty())
                require(p.moves.all { it.id > 0 && it.slug.isNotBlank() } && p.moves.distinctBy { it.id }.size == p.moves.size)
                require(p.abilities.all { it.id > 0 && it.slug.isNotBlank() } && p.abilities.distinctBy { it.id }.size == p.abilities.size)
                val moves = p.moves.map { it.id }.toSet(); val abilities = p.abilities.map { it.id }.toSet()
                val versions = p.versions.map { it.slug }.toSet()
                require(p.machines.all { it.itemId > 0 && it.moveId in moves && it.versionGroup in versions })
                require(p.pokemonAbilities.all { it.pokemonId > 0 && it.abilityId in abilities && it.slot > 0 })
                require((p.moves.flatMap { it.texts } + p.abilities.flatMap { it.texts }).all {
                    it.versionGroup in versions && it.language in setOf("es", "en") && it.text.isNotBlank()
                })
                com.duoc.rotompedia.data.PokemonMoveData.registerMoves(p.moves.filter(GameContentScope::allowsMove).map { ReferencePresentation.legacy(it) })
                cached = p
                p
            }
        }
    }
}

interface MoveRepository {
    suspend fun index(): ReferenceIndex
    suspend fun learnset(pokemonId: Int, refresh: Boolean = false): LearnsetResult
}
class DefaultMoveRepository(private val store: ReferenceStore, private val db: AppDatabase,
    private val api: PokeApiService, private val pokemon: PokemonRepository, moshi: Moshi) : MoveRepository {
    private val adapter = moshi.adapter(Learnset::class.java)
    private val locks = java.util.concurrent.ConcurrentHashMap<Int, Mutex>()
    override suspend fun index() = GameContentScope.catalogue(store.index())
    override suspend fun learnset(pokemonId: Int, refresh: Boolean): LearnsetResult = withContext(Dispatchers.IO) {
        require(pokemonId > 0)
        pokemon.ensureIndex()
        val identity = requireNotNull(pokemon.resolve(pokemonId)) { "Variedad desconocida" }
        // Validate against the complete source even when the client hides a game/move.
        val knownMoves = store.index().moves.associateBy { it.id }
        locks.getOrPut(pokemonId) { Mutex() }.withLock {
            val saved = db.learnsetDao().cached(pokemonId)
            val cached = saved?.let { runCatching { adapter.fromJson(it.json) }.getOrNull() }?.takeIf { data ->
                data.pokemonId == pokemonId && data.rows.all { row -> row.moveId > 0 && row.level >= 0 && row.method.isNotBlank() &&
                    row.versionGroup.isNotBlank() && row.slug.isNotBlank() && knownMoves[row.moveId]?.slug?.let { it == row.slug } != false }
            }
            if (!refresh && cached != null && System.currentTimeMillis() - saved.fetchedAt < 24 * 60 * 60_000)
                return@withLock LearnsetResult(cached, false, saved.fetchedAt)
            try {
                val data = api.getPokemonWithMoves(pokemonId.toString())
                require(data.id == pokemonId && data.name == identity.slug) { "Identidad de learnset incompatible" }
                val rows = data.moves.flatMap { entry ->
                    val id = Regex("https://pokeapi.co/api/v2/move/([1-9][0-9]*)/?").matchEntire(entry.move.url)?.groupValues?.get(1)?.toInt()
                        ?: error("Identidad de movimiento inválida")
                    require(entry.move.name.isNotBlank() && (knownMoves[id]?.slug?.let { it == entry.move.name } != false)) { "Movimiento reasignado" }
                    entry.versionGroupDetails.map { version ->
                        require(version.versionGroup.name.isNotBlank() && version.moveLearnMethod.name.isNotBlank() && version.levelLearnedAt >= 0)
                        LearnRow(id, entry.move.name, version.versionGroup.name, version.moveLearnMethod.name, version.levelLearnedAt)
                    }
                }.distinct()
                val result = Learnset(pokemonId, rows)
                val time = System.currentTimeMillis()
                db.learnsetDao().put(CachedLearnset(pokemonId, adapter.toJson(result), time))
                LearnsetResult(result, false, time)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (cached == null) throw e
                LearnsetResult(cached, true, saved.fetchedAt)
            }
        }
    }
}

interface AbilityRepository {
    suspend fun index(): ReferenceIndex
    suspend fun forPokemon(pokemonId: Int): List<AbilityLink>
}
class DefaultAbilityRepository(private val store: ReferenceStore, private val pokemon: PokemonRepository) : AbilityRepository {
    override suspend fun index() = GameContentScope.catalogue(store.index())
    override suspend fun forPokemon(pokemonId: Int): List<AbilityLink> {
        pokemon.ensureIndex()
        requireNotNull(pokemon.resolve(pokemonId)) { "Variedad desconocida" }
        return index().pokemonAbilities.filter { it.pokemonId == pokemonId }.sortedBy { it.slot }
    }
}
