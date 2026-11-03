package com.duoc.rotompedia.data.explorer

import java.util.concurrent.ConcurrentHashMap

data class MissingSprite(val pokemonId: Int, val speciesId: Int, val nationalNumber: Int,
    val slug: String, val url: String, val reason: String)

object MissingSpriteRegistry {
    private val missing = ConcurrentHashMap<Int, MissingSprite>()
    fun record(pokemon: ExplorerPokemon, url: String, error: Throwable) {
        val entry = MissingSprite(pokemon.pokemonId, pokemon.speciesId, pokemon.nationalNumber, pokemon.slug, url,
            "${error.javaClass.simpleName}: ${error.message.orEmpty()}")
        if (missing.put(pokemon.pokemonId, entry) != entry) android.util.Log.w("RotomPedia_Sprites", entry.toString())
    }
    fun snapshot(): List<MissingSprite> = missing.values.sortedBy { it.nationalNumber }
}
