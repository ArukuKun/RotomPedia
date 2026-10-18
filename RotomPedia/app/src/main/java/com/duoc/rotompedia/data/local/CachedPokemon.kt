package com.duoc.rotompedia.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_pokemon")
data class CachedPokemon(
    @PrimaryKey
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: String, // Tipos separados por comas: "grass,poison"
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val specialAttack: Int = 0,
    val specialDefense: Int = 0,
    val speed: Int = 0,
    val description: String = "",
    val cryUrl: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
