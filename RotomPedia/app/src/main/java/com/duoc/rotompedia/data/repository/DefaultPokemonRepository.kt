package com.duoc.rotompedia.data.repository

import androidx.room.withTransaction
import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.data.index.PokemonIndexImporter
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.local.CachedPokemon
import com.duoc.rotompedia.data.local.index.IndexedVariety
import com.duoc.rotompedia.data.local.index.IndexedForm
import com.duoc.rotompedia.data.model.*
import com.duoc.rotompedia.data.remote.PokeApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class DefaultPokemonRepository(
    private val db: AppDatabase,
    private val api: PokeApiService,
    private val importer: PokemonIndexImporter,
    private val bundledIndex: suspend () -> String
) : PokemonRepository {
    private val indexMutex = Mutex()
    private val lockMutex = Mutex()
    private val locks = mutableMapOf<String, Mutex>()
    private val memory = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, PokemonData>>()
    private val dao get() = db.pokemonIndexDao()
    override fun observeNational() = dao.observeNational()
    override fun observeVarieties(speciesId: Int) = dao.observeVarieties(speciesId)
    override fun observeIndexState() = dao.observeState()
    override fun observeExplorer() = combine(dao.observeExplorer(), dao.observeFormNames(), importer.eggGroups) { rows, names, eggs ->
        com.duoc.rotompedia.data.explorer.PokemonExplorer.entries(rows, names, eggs)
    }.flowOn(Dispatchers.Default)
    override suspend fun ensureIndex() { indexMutex.withLock { importer.importJson(bundledIndex()) } }
    override suspend fun importIndex(json: String): Boolean = indexMutex.withLock { importer.importJson(json) }

    override suspend fun resolve(pokemonId: Int): PokemonIdentity? {
        val variety = dao.variety(pokemonId) ?: return null
        val species = dao.species(variety.speciesId) ?: return null
        return PokemonIdentity(species.speciesId, species.nationalNumber, variety.pokemonId, variety.slug)
    }
    override suspend fun resolveLegacy(pokemonId: Int, savedName: String): IdentityResolution {
        val identity = resolve(pokemonId) ?: return IdentityResolution.Unknown
        val candidates = dao.names("variety", pokemonId).map { it.normalizedName } +
            PokemonFormCatalog.normalize(identity.slug) + PokemonFormCatalog.normalize(PokemonFormCatalog.displayName(identity.slug))
        return if (PokemonFormCatalog.normalize(savedName) in candidates) IdentityResolution.Resolved(identity)
        else IdentityResolution.Conflict(identity, savedName)
    }

    override suspend fun getPokemon(idOrSlug: String): PokemonData = withContext(Dispatchers.IO) {
        val input = idOrSlug.trim().lowercase(java.util.Locale.ROOT)
        val knownId = input.toIntOrNull() ?: dao.varietyBySlug(input)?.pokemonId
        val key = knownId?.toString() ?: input
        val lock = lockMutex.withLock { locks.getOrPut(key) { Mutex() } }
        lock.withLock {
            memory[key]?.takeIf { System.currentTimeMillis() - it.first < 300_000 }?.let { return@withLock it.second }
            val cached = knownId?.let { db.cachedPokemonDao().getPokemonById(it) }
            try {
                val data = api.getPokemonByName(key)
                require(if (knownId != null) data.id == knownId else data.name == input) { "Identidad de respuesta no coincidente" }
                val stat = data.stats.associate { it.stat.name to it.baseStat }
                require(data.types.isNotEmpty() && listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed").all { it in stat })
                try {
                    db.cachedPokemonDao().insertOrUpdate(CachedPokemon(data.id, data.name,
                        data.sprites.urlImagen.orEmpty(), data.types.joinToString(",") { it.type.name },
                        stat.getValue("hp"), stat.getValue("attack"), stat.getValue("defense"),
                        stat.getValue("special-attack"), stat.getValue("special-defense"), stat.getValue("speed"),
                        description = cached?.description.orEmpty(), cryUrl = data.cries?.latest ?: data.cries?.legacy))
                } catch (e: Exception) { if (e is CancellationException) throw e /* network data remains usable */ }
                val entry = System.currentTimeMillis() to data
                memory[key] = entry; memory[data.id.toString()] = entry; memory[data.name] = entry
                data
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val fallback = cached ?: throw e
                val identity = resolve(fallback.id)
                PokemonData(fallback.id, fallback.name, SpritesData(fallback.imageUrl),
                    fallback.types.split(",").filter { it.isNotBlank() }.mapIndexed { i, t -> TypesSlot(i + 1, TypeData(t)) },
                    listOf("hp" to fallback.hp, "attack" to fallback.attack, "defense" to fallback.defense,
                        "special-attack" to fallback.specialAttack, "special-defense" to fallback.specialDefense, "speed" to fallback.speed)
                        .map { StatInfo(it.second, Stat(it.first)) }, CriesData(fallback.cryUrl),
                    identity?.let { NamedApiResource("", "https://pokeapi.co/api/v2/pokemon-species/${it.speciesId}/") })
            }
        }
    }

    override suspend fun refreshSpecies(speciesId: Int) = indexMutex.withLock {
        requireNotNull(dao.species(speciesId)) { "La especie debe existir en el índice nacional" }
        val response = api.getPokemonSpeciesWithVarieties(speciesId)
        require(response.id == speciesId && response.name == dao.species(speciesId)!!.slug && response.varieties.count { it.isDefault } == 1)
        val rows = response.varieties.map {
            val uri = java.net.URI(it.pokemon.url)
            require(uri.scheme == "https" && uri.host == "pokeapi.co" && uri.path.matches(Regex("/api/v2/pokemon/[1-9][0-9]*/?")))
            val id = uri.path.trimEnd('/').substringAfterLast('/').toInt()
            require(id > 0)
            IndexedVariety(id, speciesId, it.pokemon.name, it.isDefault)
        }
        require(rows.map { it.pokemonId }.distinct().size == rows.size)
        db.withTransaction {
            dao.varieties(speciesId).forEach { old -> require(rows.any { it.pokemonId == old.pokemonId && it.slug == old.slug }) }
            rows.forEach { row -> require(dao.variety(row.pokemonId)?.speciesId?.let { it == speciesId } != false) }
            dao.putVarieties(rows)
            dao.state()?.let { dao.putState(it.copy(varietyCount = dao.varietyCount())) }
            Unit
        }
    }

    override suspend fun refreshForm(pokemonFormId: Int) = indexMutex.withLock {
        require(pokemonFormId > 0)
        val response = api.getPokemonFormIdentity(pokemonFormId)
        require(response.id == pokemonFormId && response.name.isNotBlank())
        val uri = java.net.URI(response.pokemon.url)
        require(uri.scheme == "https" && uri.host == "pokeapi.co" && uri.path.matches(Regex("/api/v2/pokemon/[1-9][0-9]*/?")))
        val pokemonId = uri.path.trimEnd('/').substringAfterLast('/').toInt()
        db.withTransaction {
            requireNotNull(dao.variety(pokemonId)) { "Variedad no indexada" }
            dao.form(pokemonFormId)?.let { require(it.pokemonId == pokemonId && it.slug == response.name) }
            val previous = dao.form(pokemonFormId)
            dao.putForms(listOf(IndexedForm(pokemonFormId, pokemonId, response.name, response.isMega, response.isBattleOnly,
                previous?.isDefault ?: false, previous?.formOrder ?: 0)))
        }
    }
}
