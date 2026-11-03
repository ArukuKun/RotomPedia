package com.duoc.rotompedia.data.local.index

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "pokemon_species", indices = [Index(value = ["nationalNumber"], unique = true), Index(value = ["slug"], unique = true)])
data class IndexedSpecies(
    @PrimaryKey val speciesId: Int,
    val nationalNumber: Int,
    val slug: String,
    val generationId: Int,
    val evolutionChainUrl: String?
)

@Entity(tableName = "pokemon_varieties",
    foreignKeys = [ForeignKey(entity = IndexedSpecies::class, parentColumns = ["speciesId"], childColumns = ["speciesId"], onDelete = ForeignKey.NO_ACTION)],
    indices = [Index("speciesId"), Index(value = ["slug"], unique = true)])
data class IndexedVariety(@PrimaryKey val pokemonId: Int, val speciesId: Int, val slug: String, val isDefault: Boolean)

@Entity(tableName = "pokemon_forms",
    foreignKeys = [ForeignKey(entity = IndexedVariety::class, parentColumns = ["pokemonId"], childColumns = ["pokemonId"], onDelete = ForeignKey.NO_ACTION)],
    indices = [Index("pokemonId"), Index(value = ["slug"], unique = true)])
data class IndexedForm(@PrimaryKey val pokemonFormId: Int, val pokemonId: Int, val slug: String, val isMega: Boolean, val isBattleOnly: Boolean,
    @ColumnInfo(defaultValue = "0") val isDefault: Boolean = false, @ColumnInfo(defaultValue = "0") val formOrder: Int = 0)

@Entity(tableName = "pokemon_variety_types", primaryKeys = ["pokemonId", "slot"],
    foreignKeys = [ForeignKey(entity = IndexedVariety::class, parentColumns = ["pokemonId"], childColumns = ["pokemonId"])],
    indices = [Index("typeName")])
data class IndexedVarietyType(val pokemonId: Int, val slot: Int, val typeName: String)

@Entity(tableName = "pokemon_names", primaryKeys = ["resourceType", "resourceId", "language", "name", "source"],
    indices = [Index("normalizedName")])
data class IndexedName(val resourceType: String, val resourceId: Int, val language: String,
    val name: String, val normalizedName: String, val source: String)

@Entity(tableName = "pokemon_index_state")
data class PokemonIndexState(@PrimaryKey val id: Int = 1, val formatVersion: Int,
    val contentSha256: String, val metadataRevision: String, val generatedAt: String, val speciesCount: Int,
    val varietyCount: Int, val formsComplete: Boolean, val importedAt: Long)
