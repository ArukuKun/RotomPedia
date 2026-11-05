package com.duoc.rotompedia.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.duoc.rotompedia.data.explorer.ExplorerFilter
import com.duoc.rotompedia.data.explorer.VarietyCategory
import kotlinx.coroutines.flow.MutableStateFlow

internal val pokedexCategories = listOf(VarietyCategory.ALL, VarietyCategory.MEGA, VarietyCategory.GMAX, VarietyCategory.REGIONAL)

/** Route-local state: Navigation's SaveableStateHolder restores it BEFORE loading results.
 * Saving the filter and lazy positions separately without restoring the filter first
 * would apply a filtered-list position to the national catalogue on return.
 */
internal val PokedexFilterSaver = listSaver<MutableStateFlow<ExplorerFilter>, Any>(
    save = { state -> state.value.let { f -> listOf(f.query, f.category.name, f.generationId ?: 0,
        f.regionalOrigin.orEmpty(), f.type.orEmpty(), f.favoritesOnly, f.eggGroup.orEmpty()) } },
    restore = { values -> MutableStateFlow(ExplorerFilter(
        query = values[0] as String,
        category = pokedexCategories.firstOrNull { it.name == values[1] } ?: VarietyCategory.ALL,
        generationId = (values[2] as Int).takeIf { it > 0 },
        regionalOrigin = (values[3] as String).ifBlank { null },
        type = (values[4] as String).ifBlank { null },
        favoritesOnly = values[5] as Boolean,
        eggGroup = (values[6] as String).ifBlank { null })) }
)

@Composable
internal fun rememberPokedexFilter(): MutableStateFlow<ExplorerFilter> =
    rememberSaveable(saver = PokedexFilterSaver) { MutableStateFlow(ExplorerFilter()) }
