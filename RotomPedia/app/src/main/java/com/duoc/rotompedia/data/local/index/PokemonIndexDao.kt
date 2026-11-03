package com.duoc.rotompedia.data.local.index

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonIndexDao {
    @Query("SELECT * FROM pokemon_species ORDER BY nationalNumber ASC")
    fun observeNational(): Flow<List<IndexedSpecies>>
    @Query("SELECT * FROM pokemon_species ORDER BY nationalNumber ASC")
    suspend fun national(): List<IndexedSpecies>
    @Query("SELECT * FROM pokemon_species WHERE speciesId = :id")
    suspend fun species(id: Int): IndexedSpecies?
    @Query("SELECT * FROM pokemon_varieties WHERE speciesId = :id ORDER BY isDefault DESC, slug ASC")
    fun observeVarieties(id: Int): Flow<List<IndexedVariety>>
    @Query("SELECT * FROM pokemon_varieties WHERE speciesId = :id ORDER BY isDefault DESC, slug ASC")
    suspend fun varieties(id: Int): List<IndexedVariety>
    @Query("SELECT * FROM pokemon_varieties WHERE pokemonId = :id")
    suspend fun variety(id: Int): IndexedVariety?
    @Query("SELECT * FROM pokemon_varieties WHERE slug = :slug")
    suspend fun varietyBySlug(slug: String): IndexedVariety?
    @Query("SELECT COUNT(*) FROM pokemon_varieties")
    suspend fun varietyCount(): Int
    @Query("SELECT * FROM pokemon_forms WHERE pokemonFormId = :id")
    suspend fun form(id: Int): IndexedForm?
    @Query("SELECT * FROM pokemon_index_state WHERE id = 1")
    suspend fun state(): PokemonIndexState?
    @Query("SELECT * FROM pokemon_index_state WHERE id = 1")
    fun observeState(): Flow<PokemonIndexState?>
    @Query("SELECT * FROM pokemon_names WHERE resourceType = :type AND resourceId = :id")
    suspend fun names(type: String, id: Int): List<IndexedName>
    @Upsert suspend fun putSpecies(rows: List<IndexedSpecies>)
    @Upsert suspend fun putVarieties(rows: List<IndexedVariety>)
    @Upsert suspend fun putForms(rows: List<IndexedForm>)
    @Upsert suspend fun putNames(rows: List<IndexedName>)
    @Upsert suspend fun putState(state: PokemonIndexState)
}
