// PokemonViewModelFactory.kt
package com.duoc.rotompedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duoc.rotompedia.data.local.FavoritoDao

// Factory para inyectar FavoritoDao en PokemonViewModel
class PokemonViewModelFactory(
    private val favoritoDao: FavoritoDao,
    private val cachedPokemonDao: com.duoc.rotompedia.data.local.CachedPokemonDao? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokemonViewModel::class.java)) {
            return PokemonViewModel(favoritoDao, cachedPokemonDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}