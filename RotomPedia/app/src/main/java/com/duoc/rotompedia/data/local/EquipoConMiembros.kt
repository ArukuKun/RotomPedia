package com.duoc.rotompedia.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class EquipoConMiembros(
    @Embedded
    val equipo: EquipoPokemon,

    @Relation(
        parentColumn = "id",
        entityColumn = "equipoId"
    )
    val miembros: List<MiembroEquipo>
)
