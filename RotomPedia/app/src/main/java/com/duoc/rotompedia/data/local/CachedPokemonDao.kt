package com.duoc.rotompedia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedPokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(pokemon: CachedPokemon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemonList: List<CachedPokemon>)

    @Query("SELECT * FROM cached_pokemon WHERE id = :id")
    suspend fun getPokemonById(id: Int): CachedPokemon?

    @Query("SELECT * FROM cached_pokemon WHERE id = :id")
    fun observePokemonById(id: Int): Flow<CachedPokemon?>

    @Query("SELECT * FROM cached_pokemon ORDER BY id ASC")
    fun getAllCachedPokemon(): Flow<List<CachedPokemon>>

    @Query("SELECT * FROM cached_pokemon ORDER BY id ASC")
    suspend fun getAllCachedPokemonDirect(): List<CachedPokemon>

    @Query("SELECT COUNT(*) FROM cached_pokemon")
    suspend fun getCount(): Int
}
