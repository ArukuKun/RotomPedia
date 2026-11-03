// PokemonViewModelFactory.kt
package com.duoc.rotompedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Every screen receives the same repositories, but keeps its own UI state.
class PokemonViewModelFactory(
    private val cachedPokemonDao: com.duoc.rotompedia.data.local.CachedPokemonDao? = null,
    private val sessions: com.duoc.rotompedia.data.repository.SessionRepository,
    private val favorites: com.duoc.rotompedia.data.repository.FavoritesRepository,
    private val pokemon: com.duoc.rotompedia.data.repository.PokemonRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokemonViewModel::class.java)) {
            return PokemonViewModel(cachedPokemonDao, sessions, favorites, pokemon) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
