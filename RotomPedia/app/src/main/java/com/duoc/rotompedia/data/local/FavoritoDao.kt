package com.duoc.rotompedia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarFavorito(favorito: PokemonFavorito)

    @Query("DELETE FROM favoritos WHERE usuario = :usuario AND pokemonId = :id")
    suspend fun eliminarFavorito(usuario: String, id: Int)

    @Delete
    suspend fun eliminarFavorito(favorito: PokemonFavorito)

    // Obtener los IDs de favoritos para un usuario específico
    @Query("SELECT pokemonId FROM favoritos WHERE usuario = :usuario")
    fun obtenerTodosLosIdsPorUsuario(usuario: String): Flow<List<Int>>

    // Para compatibilidad general o cuando no se define usuario
    @Query("SELECT pokemonId FROM favoritos")
    fun obtenerTodosLosIds(): Flow<List<Int>>

    // Verificar si un Pokémon es favorito de un usuario específico
    @Query("SELECT * FROM favoritos WHERE usuario = :usuario AND pokemonId = :pokemonId")
    suspend fun obtenerPorId(usuario: String, pokemonId: Int): PokemonFavorito?

    // Obtener todos los favoritos como objetos completos para un usuario
    @Query("SELECT * FROM favoritos WHERE usuario = :usuario")
    fun obtenerTodosLosFavoritosPorUsuario(usuario: String): Flow<List<PokemonFavorito>>
}