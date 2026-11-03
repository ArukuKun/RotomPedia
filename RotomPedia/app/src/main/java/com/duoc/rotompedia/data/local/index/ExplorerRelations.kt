package com.duoc.rotompedia.data.local.index

import androidx.room.Embedded
import androidx.room.Relation

data class ExplorerVarietyRelation(
    @Embedded val variety: IndexedVariety,
    @Relation(parentColumn = "pokemonId", entityColumn = "pokemonId") val forms: List<IndexedForm>,
    @Relation(parentColumn = "pokemonId", entityColumn = "pokemonId") val types: List<IndexedVarietyType>,
    @Relation(parentColumn = "pokemonId", entityColumn = "resourceId") val names: List<IndexedName>
)

data class ExplorerSpeciesRelation(
    @Embedded val species: IndexedSpecies,
    @Relation(parentColumn = "speciesId", entityColumn = "speciesId", entity = IndexedVariety::class)
    val varieties: List<ExplorerVarietyRelation>,
    @Relation(parentColumn = "speciesId", entityColumn = "resourceId") val names: List<IndexedName>
)
