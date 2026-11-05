package com.duoc.rotompedia.data

import com.duoc.rotompedia.data.model.PokemonGenus
import java.math.BigDecimal

data class SpeciesCategoryState(val speciesId: Int? = null, val label: String? = null,
    val loading: Boolean = true, val failed: Boolean = false)

object PokemonBiology {
    fun spanishCategory(genera: List<PokemonGenus>): String? =
        genera.firstOrNull { it.language.name == "es" && it.genus.isNotBlank() }?.genus?.trim()

    // Keep one decimal: the source expresses both quantities in tenths of SI units.
    fun measurement(value: Int?, unit: String): String = value?.takeIf { it > 0 }
        ?.let { "${BigDecimal.valueOf(it.toLong(), 1).toPlainString().replace('.', ',')} $unit" } ?: "—"
}
