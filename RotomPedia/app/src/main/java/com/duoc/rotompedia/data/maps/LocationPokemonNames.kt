package com.duoc.rotompedia.data.maps

import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.data.index.IndexPayload

/** National numbers are presentation only; encounter links retain the exact pokemonId. */
data class LocationPokemonLabel(val nationalNumber: Int, val name: String)

object LocationPokemonNames {
    fun fromNationalIndex(index: IndexPayload): Map<Int, LocationPokemonLabel> {
        val species = index.species.associateBy { it.speciesId }
        return index.varieties.associate { variety ->
            val parent = requireNotNull(species[variety.speciesId])
            val nameEs = parent.names.firstOrNull { it.language == "es" }?.name
                ?: parent.names.firstOrNull { it.language == "en" }?.name ?: parent.slug
            val name = if (variety.isDefault) nameEs else PokemonFormCatalog.displayName(variety.slug)
            variety.pokemonId to LocationPokemonLabel(parent.nationalNumber, name)
        }
    }
}
