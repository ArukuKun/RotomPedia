package com.duoc.rotompedia.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "miembros_equipo",
    foreignKeys = [
        ForeignKey(
            entity = EquipoPokemon::class,
            parentColumns = ["id"],
            childColumns = ["equipoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["equipoId"])]
)
data class MiembroEquipo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val equipoId: Long,
    val pokemonId: Int,
    val nombre: String,
    val spriteUrl: String,
    val tipo1: String,
    val tipo2: String? = null,
    val slot: Int, // 0..5
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val specialAttack: Int = 0,
    val specialDefense: Int = 0,
    val speed: Int = 0
)
