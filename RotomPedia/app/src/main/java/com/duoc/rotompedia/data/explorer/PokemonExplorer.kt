package com.duoc.rotompedia.data.explorer

import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.data.local.index.*

enum class VarietyCategory(val label: String) {
    ALL("Todas"), SPECIES("Especies"), STABLE("Variedades estables"), REGIONAL("Regionales"),
    MEGA("Megaevoluciones"), BATTLE("Combate"), OTHER("Cosméticas / otras")
}

data class ExplorerPokemon(
    val speciesId: Int, val nationalNumber: Int, val pokemonId: Int, val pokemonFormId: Int?,
    val speciesName: String, val slug: String, val displayName: String, val isDefault: Boolean,
    val generationId: Int, val category: VarietyCategory, val regionalOrigin: String?,
    val types: List<String>, val formOrder: Int, val aliases: List<String> = emptyList()
) {
    val numberLabel get() = "#${nationalNumber.toString().padStart(3, '0')}"
    val isNamedDefault get() = isDefault && displayName != speciesName
    val isVariant get() = !isDefault || isNamedDefault
}

data class ExplorerFilter(val query: String = "", val category: VarietyCategory = VarietyCategory.ALL,
    val generationId: Int? = null, val regionalOrigin: String? = null, val type: String? = null,
    val favoritesOnly: Boolean = false)
data class ExplorerGroup(val speciesId: Int, val nationalNumber: Int, val speciesName: String,
    val matching: List<ExplorerPokemon>, val primary: ExplorerPokemon, val autoExpanded: Boolean)
data class ExplorerResult(val groups: List<ExplorerGroup> = emptyList(), val speciesCount: Int = 0, val variantCount: Int = 0)

object PokemonExplorer {
    fun category(speciesSlug: String, variety: IndexedVariety, form: IndexedForm?): VarietyCategory = when {
        form?.isMega == true -> VarietyCategory.MEGA
        speciesSlug == "aegislash" || form?.isBattleOnly == true -> VarietyCategory.BATTLE
        region(variety.slug) != null -> VarietyCategory.REGIONAL
        variety.slug.contains("-cap") || variety.slug.contains("-cosplay") ||
            (speciesSlug == "pikachu" && variety.slug.substringAfter("pikachu-") in
                setOf("rock-star", "belle", "pop-star", "phd", "libre")) ||
            variety.slug.endsWith("-build") || variety.slug.endsWith("-mode") || variety.slug.endsWith("-starter") -> VarietyCategory.OTHER
        variety.isDefault && variety.slug == speciesSlug -> VarietyCategory.SPECIES
        form != null -> VarietyCategory.STABLE
        else -> VarietyCategory.OTHER // No invented classification when upstream flags are not yet available.
    }
    fun region(slug: String): String? = listOf("alola", "galar", "hisui", "paldea").firstOrNull { it in slug.split('-') }

    fun entries(rows: List<ExplorerSpeciesRelation>, formNames: List<IndexedName>): List<ExplorerPokemon> {
        val localizedForms = formNames.filter { it.language == "es" }.groupBy { it.resourceId }
        return rows.flatMap { row ->
            val speciesName = row.names.firstOrNull { it.resourceType == "species" && it.language == "es" }?.name
                ?: row.species.slug.replaceFirstChar { it.uppercase() }
            row.varieties.map { relation ->
                val v = relation.variety
                val form = relation.forms.sortedWith(compareByDescending<IndexedForm> { it.isDefault }.thenBy { it.formOrder }.thenBy { it.pokemonFormId }).firstOrNull()
                val category = category(row.species.slug, v, form)
                val localized = form?.let { localizedForms[it.pokemonFormId]?.firstOrNull()?.name }
                val name = PokemonFormCatalog.find(v.slug)?.displayName ?: when {
                    v.slug == row.species.slug -> speciesName
                    category == VarietyCategory.MEGA -> "Mega $speciesName" + v.slug.split('-').last().takeIf { it in listOf("x", "y", "z") }?.let { " ${it.uppercase()}" }.orEmpty()
                    region(v.slug) != null -> "$speciesName de ${region(v.slug)!!.replaceFirstChar { it.uppercase() }}"
                    localized != null -> if (PokemonFormCatalog.normalize(localized).contains(PokemonFormCatalog.normalize(speciesName))) localized else "$speciesName ($localized)"
                    else -> PokemonFormCatalog.displayName(v.slug)
                }
                ExplorerPokemon(row.species.speciesId, row.species.nationalNumber, v.pokemonId, form?.pokemonFormId,
                    speciesName, v.slug, name, v.isDefault, row.species.generationId, category, region(v.slug),
                    relation.types.sortedBy { it.slot }.map { it.typeName }, form?.formOrder ?: 0,
                    relation.names.filter { it.resourceType == "variety" }.map { it.name } + row.names.filter { it.resourceType == "species" }.map { it.name })
            }
        }.sortedWith(compareBy<ExplorerPokemon> { it.nationalNumber }.thenByDescending { it.isDefault }.thenBy { it.formOrder }.thenBy { it.slug })
    }

    fun filter(entries: List<ExplorerPokemon>, filter: ExplorerFilter, favorites: Set<Int> = emptySet()): ExplorerResult {
        val q = PokemonFormCatalog.normalize(filter.query).replace(Regex("\\s+"), " ")
        val numeric = q.removePrefix("#").takeIf { it.isNotEmpty() && it.all(Char::isDigit) }?.toIntOrNull()
        val words = q.split(' ').filter { it.isNotBlank() }
        val active = q.isNotEmpty() || filter.category != VarietyCategory.ALL || filter.type != null ||
            filter.regionalOrigin != null || filter.generationId != null || filter.favoritesOnly
        val groups = entries.groupBy { it.speciesId }.values.mapNotNull { all ->
            val primary = all.firstOrNull { it.isDefault } ?: return@mapNotNull null
            val matching = all.filter { pokemon ->
                val text = PokemonFormCatalog.normalize(listOf(pokemon.slug, pokemon.displayName, pokemon.speciesName,
                    pokemon.category.label, pokemon.regionalOrigin.orEmpty()).plus(pokemon.aliases)
                    .plus(pokemon.types.flatMap { listOf(it, com.duoc.rotompedia.util.traducirTipo(it)) }).joinToString(" "))
                (if (numeric != null) pokemon.nationalNumber == numeric else words.all { it in text }) &&
                    (q != "mega" || pokemon.category == VarietyCategory.MEGA) &&
                    (filter.category == VarietyCategory.ALL || if (filter.category == VarietyCategory.SPECIES) pokemon.isDefault else pokemon.category == filter.category) &&
                    (filter.generationId == null || pokemon.generationId == filter.generationId) &&
                    (filter.regionalOrigin == null || pokemon.regionalOrigin == filter.regionalOrigin) &&
                    (filter.type == null || filter.type in pokemon.types) &&
                    (!filter.favoritesOnly || pokemon.pokemonId in favorites)
            }
            if (matching.isEmpty()) null else ExplorerGroup(primary.speciesId, primary.nationalNumber, primary.speciesName,
                matching, primary, active)
        }.sortedBy { it.nationalNumber }
        return ExplorerResult(groups, groups.size, groups.sumOf { it.matching.count(ExplorerPokemon::isVariant) })
    }
}
