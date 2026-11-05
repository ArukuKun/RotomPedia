package com.duoc.rotompedia.data.reference

import androidx.room.*
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReferenceEnvelope(val formatVersion: Int, val sha256: String, val payloadJson: String)
@JsonClass(generateAdapter = true)
data class ReferenceIndex(val revision: String, val versions: List<ReferenceVersion>, val moves: List<ReferenceMove>,
    val abilities: List<ReferenceAbility>, val pokemonAbilities: List<AbilityLink>, val machines: List<MachineLink>)
@JsonClass(generateAdapter = true)
data class ReferenceVersion(val id: Int, val slug: String, val order: Int)
@JsonClass(generateAdapter = true)
data class ReferenceText(val versionGroup: String, val language: String, val text: String)
@JsonClass(generateAdapter = true)
data class ReferenceMove(val id: Int, val slug: String, val nameEs: String?, val nameEn: String,
    val type: String, val category: String, val power: Int?, val pp: Int?, val accuracy: Int?, val priority: Int,
    val effectEs: String?, val effectEn: String?, val texts: List<ReferenceText>) {
    val displayName get() = nameEs ?: nameEn
}
@JsonClass(generateAdapter = true)
data class ReferenceAbility(val id: Int, val slug: String, val nameEs: String?, val nameEn: String, val mainSeries: Boolean,
    val effectEs: String?, val effectEn: String?, val texts: List<ReferenceText>) {
    val displayName get() = AbilityNames.displayName(this)
}
@JsonClass(generateAdapter = true)
data class AbilityLink(val pokemonId: Int, val abilityId: Int, val hidden: Boolean, val slot: Int)
@JsonClass(generateAdapter = true)
data class MachineLink(val itemId: Int, val moveId: Int, val versionGroup: String)
@JsonClass(generateAdapter = true)
data class LearnRow(val moveId: Int, val slug: String, val versionGroup: String, val method: String, val level: Int)
@JsonClass(generateAdapter = true)
data class Learnset(val pokemonId: Int, val rows: List<LearnRow>)
data class LearnsetResult(val data: Learnset, val offline: Boolean, val fetchedAt: Long)

@Entity(tableName = "pokemon_learnset_cache")
data class CachedLearnset(@PrimaryKey val pokemonId: Int, val json: String, val fetchedAt: Long)
@Dao
interface LearnsetDao {
    @Query("SELECT * FROM pokemon_learnset_cache WHERE pokemonId=:id") suspend fun cached(id: Int): CachedLearnset?
    @Upsert suspend fun put(row: CachedLearnset)
}
