package com.duoc.rotompedia.data.explorer

import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.data.local.index.*

enum class VarietyCategory(val label: String) {
    ALL("Todas"), SPECIES("Especies"), STABLE("Forma alternativa"), REGIONAL("Regionales"),
    MEGA("Megaevoluciones"), GMAX("Gigamax"), BATTLE("Combate"), OTHER("Cosméticas / otras")
}

data class ExplorerEggGroup(val slug: String, val label: String)
data class ExplorerRegionalDex(val id: Int, val region: String, val slug: String, val name: String, val versionGroups: List<String>)
data class ExplorerDexNumber(val dex: ExplorerRegionalDex, val number: Int)
data class ExplorerMetadata(val eggGroups: Map<Int, List<ExplorerEggGroup>> = emptyMap(),
    val regionalNumbers: Map<Int, List<ExplorerDexNumber>> = emptyMap(), val ready: Boolean = false)

data class ExplorerPokemon(
    val speciesId: Int, val nationalNumber: Int, val pokemonId: Int, val pokemonFormId: Int?,
    val speciesName: String, val slug: String, val displayName: String, val isDefault: Boolean,
    val generationId: Int, val category: VarietyCategory, val regionalOrigin: String?,
    val types: List<String>, val formOrder: Int, val aliases: List<String> = emptyList(),
    val eggGroups: List<ExplorerEggGroup> = emptyList(), val regionalNumbers: List<ExplorerDexNumber> = emptyList()
) {
    val numberLabel get() = "#${nationalNumber.toString().padStart(3, '0')}"
    val isNamedDefault get() = isDefault && displayName != speciesName
    val isVariant get() = !isDefault || isNamedDefault
    val isTotem get() = "totem" in slug.split('-')
    val introducedRegion: String? get() = if (HisuiIntroductions.matches(this)) "hisui" else null
    val originLabel: String? get() = if (introducedRegion == "hisui") "Especie de Hisui"
        else regionalOrigin?.let { "Forma de ${it.replaceFirstChar(Char::uppercase)}" }
}

/** pokemon_forms.csv introduced_in_version_group_id = legends-arceus, pinned in the master plan.
 * Exact variety + slug: notably excludes Ursaluna Bloodmoon, introduced in The Teal Mask.
 */
internal object HisuiIntroductions {
    private val varieties = mapOf(899 to "wyrdeer", 900 to "kleavor", 901 to "ursaluna", 902 to "basculegion-male",
        903 to "sneasler", 904 to "overqwil", 905 to "enamorus-incarnate",
        10248 to "basculegion-female", 10249 to "enamorus-therian")
    fun matches(pokemon: ExplorerPokemon): Boolean {
        val species = when (pokemon.pokemonId) { 10248 -> 902; 10249 -> 905; else -> pokemon.pokemonId }
        return pokemon.speciesId == species && varieties[pokemon.pokemonId] == pokemon.slug
    }
}

data class ExplorerFilter(val query: String = "", val category: VarietyCategory = VarietyCategory.ALL,
    val generationId: Int? = null, val regionalOrigin: String? = null, val type: String? = null,
    val favoritesOnly: Boolean = false, val eggGroup: String? = null)
data class ExplorerGroup(val speciesId: Int, val nationalNumber: Int, val speciesName: String,
    val matching: List<ExplorerPokemon>, val primary: ExplorerPokemon, val autoExpanded: Boolean)
data class ExplorerResult(val groups: List<ExplorerGroup> = emptyList(), val speciesCount: Int = 0, val variantCount: Int = 0,
    val ready: Boolean = false, val regionalOrder: ExplorerRegionalDex? = null, val missingRegionalNumbers: Int = 0) {
    val entries get() = groups.flatMap { it.matching }
}

object PokemonExplorer {
    private val primalSlugs = setOf("kyogre-primal", "groudon-primal")
    private val browsableForms = setOf(VarietyCategory.MEGA, VarietyCategory.GMAX, VarietyCategory.REGIONAL)
    private fun isPrimal(variety: IndexedVariety, form: IndexedForm?): Boolean =
        variety.slug in primalSlugs && form?.slug == variety.slug

    fun category(speciesSlug: String, variety: IndexedVariety, form: IndexedForm?): VarietyCategory = when {
        // UI grouping requested by the user; upstream isMega stays false for primal forms.
        isPrimal(variety, form) -> VarietyCategory.MEGA
        form?.isMega == true -> VarietyCategory.MEGA
        // Upstream has no is_gmax flag; require its canonical form AND variety marker.
        form?.slug?.endsWith("-gmax") == true && variety.slug.endsWith("-gmax") -> VarietyCategory.GMAX
        "cap" in variety.slug.split('-') || "cosplay" in variety.slug.split('-') -> VarietyCategory.OTHER
        speciesSlug == "aegislash" || form?.isBattleOnly == true -> VarietyCategory.BATTLE
        region(variety.slug) != null -> VarietyCategory.REGIONAL
        (speciesSlug == "pikachu" && variety.slug.substringAfter("pikachu-") in
                setOf("rock-star", "belle", "pop-star", "phd", "libre")) ||
            variety.slug.endsWith("-build") || variety.slug.endsWith("-mode") || variety.slug.endsWith("-starter") -> VarietyCategory.OTHER
        variety.isDefault && variety.slug == speciesSlug -> VarietyCategory.SPECIES
        form != null -> VarietyCategory.STABLE
        else -> VarietyCategory.OTHER // No invented classification when upstream flags are not yet available.
    }
    fun region(slug: String): String? {
        val tokens = slug.split('-')
        // A hat associated with a region is not a regional biological variety.
        if ("cap" in tokens || "cosplay" in tokens) return null
        return listOf("alola", "galar", "hisui", "paldea").firstOrNull { it in tokens }
    }

    fun entries(rows: List<ExplorerSpeciesRelation>, formNames: List<IndexedName>,
        eggGroups: Map<Int, List<ExplorerEggGroup>> = emptyMap(), regionalNumbers: Map<Int, List<ExplorerDexNumber>> = emptyMap()): List<ExplorerPokemon> {
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
                    isPrimal(v, form) -> "$speciesName (${localized ?: "Regresión Primigenia"})"
                    category == VarietyCategory.MEGA -> "Mega $speciesName" + v.slug.split('-').last().takeIf { it in listOf("x", "y", "z") }?.let { " ${it.uppercase()}" }.orEmpty()
                    category == VarietyCategory.GMAX -> "$speciesName Gigamax"
                    region(v.slug) != null -> "$speciesName de ${region(v.slug)!!.replaceFirstChar { it.uppercase() }}"
                    localized != null -> if (PokemonFormCatalog.normalize(localized).contains(PokemonFormCatalog.normalize(speciesName))) localized else "$speciesName ($localized)"
                    else -> PokemonFormCatalog.displayName(v.slug)
                }
                ExplorerPokemon(row.species.speciesId, row.species.nationalNumber, v.pokemonId, form?.pokemonFormId,
                    speciesName, v.slug, name, v.isDefault, row.species.generationId, category, region(v.slug),
                    relation.types.sortedBy { it.slot }.map { it.typeName }, form?.formOrder ?: 0,
                    relation.names.filter { it.resourceType == "variety" }.map { it.name } + row.names.filter { it.resourceType == "species" }.map { it.name },
                    eggGroups[row.species.speciesId].orEmpty(), regionalNumbers[row.species.speciesId].orEmpty())
            }
        }.sortedWith(compareBy<ExplorerPokemon> { it.nationalNumber }.thenByDescending { it.isDefault }.thenBy { it.formOrder }.thenBy { it.slug })
    }

    fun filter(entries: List<ExplorerPokemon>, filter: ExplorerFilter, favorites: Set<Int> = emptySet(),
        nationalOnly: Boolean = false): ExplorerResult {
        val q = PokemonFormCatalog.normalize(filter.query).replace(Regex("\\s+"), " ")
        val numeric = q.removePrefix("#").takeIf { it.isNotEmpty() && it.all(Char::isDigit) }?.toIntOrNull()
        val words = q.split(' ').filter { it.isNotBlank() }
        val active = q.isNotEmpty() || filter.category != VarietyCategory.ALL || filter.type != null ||
            filter.regionalOrigin != null || filter.generationId != null || filter.favoritesOnly || filter.eggGroup != null
        // Pokédex: national species unless the user explicitly requests a form category/origin.
        // Picker: retains all exact varieties and the existing grouped behaviour.
        val speciesOnly = nationalOnly && filter.category in setOf(VarietyCategory.ALL, VarietyCategory.SPECIES) && filter.regionalOrigin == null
        val regionalOrder = if (nationalOnly && filter.regionalOrigin != null) entries.asSequence().flatMap { it.regionalNumbers }
            .firstOrNull { it.dex.region == filter.regionalOrigin }?.dex else null
        val groups = entries.groupBy { it.speciesId }.values.mapNotNull { all ->
            val primary = all.firstOrNull { it.isDefault } ?: return@mapNotNull null
            val matching = all.filter { pokemon ->
                val text = PokemonFormCatalog.normalize(listOf(pokemon.slug, pokemon.displayName, pokemon.speciesName,
                    pokemon.category.label, pokemon.regionalOrigin.orEmpty(), pokemon.introducedRegion.orEmpty()).plus(pokemon.aliases)
                    .plus(pokemon.types.flatMap { listOf(it, com.duoc.rotompedia.util.traducirTipo(it)) }).joinToString(" "))
                (if (numeric != null) pokemon.nationalNumber == numeric else words.all { it in text }) &&
                    (!speciesOnly || pokemon.isDefault) &&
                    (!nationalOnly || !pokemon.isTotem) &&
                    (!nationalOnly || pokemon.isDefault || pokemon.category in browsableForms ||
                        (filter.regionalOrigin == "hisui" && pokemon.introducedRegion == "hisui")) &&
                    (q != "mega" || pokemon.category == VarietyCategory.MEGA) &&
                    (filter.category == VarietyCategory.ALL || if (filter.category == VarietyCategory.SPECIES) pokemon.isDefault
                        else pokemon.category == filter.category || (filter.category == VarietyCategory.REGIONAL &&
                            filter.regionalOrigin == "hisui" && pokemon.introducedRegion == "hisui")) &&
                    (filter.generationId == null || pokemon.generationId == filter.generationId) &&
                    (filter.regionalOrigin == null || pokemon.regionalOrigin == filter.regionalOrigin || pokemon.introducedRegion == filter.regionalOrigin) &&
                    (filter.type == null || filter.type in pokemon.types) &&
                    (filter.eggGroup == null || pokemon.eggGroups.any { it.slug == filter.eggGroup }) &&
                    (!filter.favoritesOnly || pokemon.pokemonId in favorites)
            }
            if (matching.isEmpty()) null else ExplorerGroup(primary.speciesId, primary.nationalNumber, primary.speciesName,
                matching, primary, active)
        }.sortedWith(compareBy<ExplorerGroup> { group ->
            if (regionalOrder == null) group.nationalNumber else group.primary.regionalNumbers.firstOrNull { it.dex.id == regionalOrder.id }?.number ?: Int.MAX_VALUE
        }.thenBy { it.nationalNumber })
        val missing = if (regionalOrder == null) 0 else groups.count { group -> group.primary.regionalNumbers.none { it.dex.id == regionalOrder.id } }
        return ExplorerResult(groups, groups.size, if (speciesOnly) 0 else groups.sumOf { it.matching.count(ExplorerPokemon::isVariant) },
            ready = true, regionalOrder = regionalOrder, missingRegionalNumbers = missing)
    }
}
