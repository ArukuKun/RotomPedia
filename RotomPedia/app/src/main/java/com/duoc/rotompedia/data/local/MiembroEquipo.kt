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
    val speed: Int = 0,
    // Estadísticas avanzadas competitivas (Sprint 3)
    val nature: String = "Fuerte", // Naturaleza (Firme, Alegre, Modesta, etc.)
    val level: Int = 50, // Nivel 50 por defecto (VGC / Competitivo)
    val evHp: Int = 0,
    val evAtk: Int = 0,
    val evDef: Int = 0,
    val evSpa: Int = 0,
    val evSpd: Int = 0,
    val evSpe: Int = 0,
    val ivHp: Int = 31,
    val ivAtk: Int = 31,
    val ivDef: Int = 31,
    val ivSpa: Int = 31,
    val ivSpd: Int = 31,
    val ivSpe: Int = 31,
    // Movimientos del Pokémon (Moveset competitivo)
    val move1: String? = null,
    val move2: String? = null,
    val move3: String? = null,
    val move4: String? = null
)
