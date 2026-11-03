package com.duoc.rotompedia.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//Se usara JsonClass para que moshi cree el codigo que convierte la respuesta JSON de la api en este objeto kotlin

@JsonClass(generateAdapter = true)
data class  PokemonData(
    val id: Int,
    val name:String,
    val sprites: SpritesData,
    val types: List<TypesSlot>,
    val stats: List<StatInfo>,
    val cries: CriesData? = null,
    val species: NamedApiResource? = null
)

@JsonClass(generateAdapter = true)
data class CriesData(
    val latest: String? = null,
    val legacy: String? = null
)

//Clases anidadas para las Imagenes
@JsonClass(generateAdapter = true)
data class SpritesData(
    //nombre de la propiedad en la Api es 'front_default'
    //se mapea en 'urlImagen' para que sea claro
    @Json(name = "front_default") val urlImagen: String?,
    @Json(name = "front_shiny") val frontShiny: String? = null,
    val other: OtherSprites? = null
)

@JsonClass(generateAdapter = true)
data class OtherSprites(
    @Json(name = "official-artwork") val officialArtwork: SpritePair? = null,
    val home: SpritePair? = null
)

@JsonClass(generateAdapter = true)
data class SpritePair(
    @Json(name = "front_default") val frontDefault: String? = null,
    @Json(name = "front_shiny") val frontShiny: String? = null
)

//Clases anidadas para los tipos
@JsonClass(generateAdapter =true)
data class TypesSlot(
    val slot: Int,
    val type: TypeData
)

@JsonClass(generateAdapter = true)
data class TypeData(
    val name: String
)

//Soporte para la Interfaz UI
//Se usara más adelante para simplificar los datos mostrados
data class PokemonDisplayData(
    val id: Int,
    val nombre: String,
    val urlImagen: String,
    val tipos: List<String>
)
// Representa la respuesta completa de la API cuando pedimos una lista
@JsonClass(generateAdapter = true)
data class PokemonListResponse(
    val results: List<PokemonListItem>
)

//Representa cada Pokémon individual dentro de la lista
@JsonClass(generateAdapter = true)
data class PokemonListItem(
    val name: String,
    val url: String,
    val types: List<String> = emptyList()
)
//Contiene la información de una estadística individual
@JsonClass(generateAdapter = true)
data class StatInfo(
    // El valor de la estadística (ej: 45)
    @Json(name = "base_stat") val baseStat: Int,
    // El objeto que contiene el nombre de la estadística
    val stat: Stat
)

//Contiene el nombre de la estadística (ej: "hp")
@JsonClass(generateAdapter = true)
data class Stat(
    val name: String
)

// 1. Para obtener la URL de la cadena de evolución
@JsonClass(generateAdapter = true)
data class PokemonSpeciesResponse(
    @Json(name = "evolution_chain") val evolutionChain: EvolutionChainUrl,
    @Json(name = "flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>,
    @Json(name = "capture_rate") val captureRate: Int? = null
)

@JsonClass(generateAdapter = true)
data class EvolutionChainUrl(
    val url: String
)

// 2. Para procesar la cadena de evolución (que es recursiva)
@JsonClass(generateAdapter = true)
data class EvolutionChainResponse(
    val chain: ChainLink
)

@JsonClass(generateAdapter = true)
data class ChainLink(
    @Json(name = "is_baby") val isBaby: Boolean,
    @Json(name = "species") val species: Species,
    @Json(name = "evolution_details") val evolutionDetails: List<EvolutionDetail>, // <- Asegúrate de que esto existe
    @Json(name = "evolves_to") val evolvesTo: List<ChainLink>
)

// 3. 'Species' es similar a 'PokemonListItem', pero lo usamos
// para ser claros con lo que recibimos de la API
@JsonClass(generateAdapter = true)
data class Species(
    val name: String,
    val url: String
)

// Para la descripción (flavor text)
@JsonClass(generateAdapter = true)
data class FlavorTextEntry(
    @Json(name = "flavor_text") val flavorText: String,
    val language: Language
)

@JsonClass(generateAdapter = true)
data class Language(
    val name: String
)

// --- CLASES NUEVAS PARA LAS RELACIONES DE TIPO ---

// La respuesta completa de la API para un tipo
@JsonClass(generateAdapter = true)
data class TypeDetailResponse(
    @Json(name = "damage_relations") val damageRelations: DamageRelations,
    @Json(name = "pokemon") val pokemon: List<TypePokemonSlot> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TypePokemonSlot(
    val slot: Int,
    val pokemon: NamedApiResource
)

// Contiene las listas de debilidades, fortalezas, etc.
@JsonClass(generateAdapter = true)
data class DamageRelations(
    @Json(name = "double_damage_from") val doubleDamageFrom: List<TypeRelation> = emptyList(),
    @Json(name = "double_damage_to") val doubleDamageTo: List<TypeRelation> = emptyList(),
    @Json(name = "half_damage_from") val halfDamageFrom: List<TypeRelation> = emptyList(),
    @Json(name = "half_damage_to") val halfDamageTo: List<TypeRelation> = emptyList(),
    @Json(name = "no_damage_from") val noDamageFrom: List<TypeRelation> = emptyList(),
    @Json(name = "no_damage_to") val noDamageTo: List<TypeRelation> = emptyList()
)

// Representa un tipo individual en las listas de relaciones
@JsonClass(generateAdapter = true)
data class TypeRelation(
    val name: String
)

@JsonClass(generateAdapter = true)
data class EvolutionDetail(
    // Nivel mínimo
    @Json(name = "min_level") val minLevel: Int?,

    // Item requerido
    @Json(name = "item") val item: NamedApiResource?,

    // Disparador de evolución
    @Json(name = "trigger") val trigger: NamedApiResource?,

    // Felicidad mínima
    @Json(name = "min_happiness") val minHappiness: Int?,

    // Belleza mínima
    @Json(name = "min_beauty") val minBeauty: Int?,

    // Afecto mínimo (Pokémon Amie)
    @Json(name = "min_affection") val minAffection: Int?,

    // Hora del día
    @Json(name = "time_of_day") val timeOfDay: String?, // "day" or "night"

    // Género (1: female, 2: male)
    @Json(name = "gender") val gender: Int?,

    // Localización específica
    @Json(name = "location") val location: NamedApiResource?,

    // Pokémon que debe estar en el equipo
    @Json(name = "known_move") val knownMove: NamedApiResource?,

    // Tipo de movimiento conocido
    @Json(name = "known_move_type") val knownMoveType: NamedApiResource?,

    // Pokémon que debe sostenerse
    @Json(name = "held_item") val heldItem: NamedApiResource?,

    // Tipo de Pokémon requerido
    @Json(name = "party_type") val partyType: NamedApiResource?,

    // Valor específico de Pokémon en el equipo
    @Json(name = "party_species") val partySpecies: NamedApiResource?,

    // Relación física (para Tyrogue)
    @Json(name = "relative_physical_stats") val relativePhysicalStats: Int?, // -1, 0, 1

    // ¿Debe estar lloviendo?
    @Json(name = "needs_overworld_rain") val needsOverworldRain: Boolean?,

    // Intercambio por Pokémon específico
    @Json(name = "trade_species") val tradeSpecies: NamedApiResource?,

    // Girar (para Alcremie)
    @Json(name = "turn_upside_down") val turnUpsideDown: Boolean?
)

@JsonClass(generateAdapter = true)
data class NamedApiResource(
    val name: String,
    val url: String
)

@JsonClass(generateAdapter = true)
data class PokemonFormIdentityResponse(
    val id: Int, val name: String, val pokemon: NamedApiResource,
    @Json(name = "is_mega") val isMega: Boolean,
    @Json(name = "is_battle_only") val isBattleOnly: Boolean
)

@JsonClass(generateAdapter = true)
data class PokemonEncounter(
    @Json(name = "location_area") val locationArea: NamedApiResource,
    @Json(name = "version_details") val versionDetails: List<EncounterVersion>
)

@JsonClass(generateAdapter = true)
data class EncounterVersion(val version: NamedApiResource)

// ==================== FORMAS DE POKÉMON ====================

@JsonClass(generateAdapter = true)
data class PokemonVariety(
    @Json(name = "is_default") val isDefault: Boolean,
    @Json(name = "pokemon") val pokemon: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class PokemonSpeciesVarieties(
    val id: Int,
    val name: String,
    @Json(name = "evolution_chain") val evolutionChain: EvolutionChainUrl,
    @Json(name = "flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>,
    @Json(name = "capture_rate") val captureRate: Int? = null,
    @Json(name = "varieties") val varieties: List<PokemonVariety> = emptyList()
)

// ==================== MOVIMIENTOS ====================

@JsonClass(generateAdapter = true)
data class MoveVersionGroupDetail(
    @Json(name = "level_learned_at") val levelLearnedAt: Int,
    @Json(name = "move_learn_method") val moveLearnMethod: NamedApiResource,
    @Json(name = "version_group") val versionGroup: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class PokemonMoveEntry(
    @Json(name = "move") val move: NamedApiResource,
    @Json(name = "version_group_details") val versionGroupDetails: List<MoveVersionGroupDetail>
)

// PokemonData extendida con moves (nueva versión con moves)
@JsonClass(generateAdapter = true)
data class PokemonDataWithMoves(
    val id: Int,
    val name: String,
    val sprites: SpritesData,
    val types: List<TypesSlot>,
    val stats: List<StatInfo>,
    val cries: CriesData? = null,
    val moves: List<PokemonMoveEntry> = emptyList()
)

// Detalle de un movimiento individual
@JsonClass(generateAdapter = true)
data class MoveDetailResponse(
    val id: Int,
    val name: String,
    val accuracy: Int? = null,
    val power: Int? = null,
    val pp: Int? = null,
    @Json(name = "damage_class") val damageClass: NamedApiResource? = null,
    val type: NamedApiResource? = null,
    @Json(name = "effect_entries") val effectEntries: List<MoveEffectEntry> = emptyList(),
    @Json(name = "names") val names: List<MoveName> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MoveEffectEntry(
    @Json(name = "short_effect") val shortEffect: String,
    val language: Language
)

@JsonClass(generateAdapter = true)
data class MoveName(
    val name: String,
    val language: Language
)
