package com.duoc.rotompedia.data.repository

import com.duoc.rotompedia.data.local.index.*
import com.duoc.rotompedia.data.model.PokemonData
import kotlinx.coroutines.flow.Flow

data class PokemonIdentity(val speciesId: Int, val nationalNumber: Int, val pokemonId: Int, val slug: String)
sealed interface IdentityResolution {
    data class Resolved(val identity: PokemonIdentity) : IdentityResolution
    data class Conflict(val identity: PokemonIdentity, val savedName: String) : IdentityResolution
    data object Unknown : IdentityResolution
}

interface PokemonRepository {
    fun observeNational(): Flow<List<IndexedSpecies>>
    fun observeVarieties(speciesId: Int): Flow<List<IndexedVariety>>
    fun observeIndexState(): Flow<PokemonIndexState?>
    suspend fun ensureIndex()
    suspend fun importIndex(json: String): Boolean
    suspend fun resolve(pokemonId: Int): PokemonIdentity?
    suspend fun resolveLegacy(pokemonId: Int, savedName: String): IdentityResolution
    suspend fun getPokemon(idOrSlug: String): PokemonData
    suspend fun refreshSpecies(speciesId: Int)
    suspend fun refreshForm(pokemonFormId: Int)
}
