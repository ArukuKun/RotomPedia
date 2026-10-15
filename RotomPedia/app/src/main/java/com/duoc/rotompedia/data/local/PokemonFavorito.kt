package com.duoc.rotompedia.data.local

import androidx.room.Entity

@Entity(
    tableName = "favoritos",
    primaryKeys = ["usuario", "pokemonId"]
)
data class PokemonFavorito(
    val usuario: String,
    val pokemonId: Int,
    val nombre: String
)