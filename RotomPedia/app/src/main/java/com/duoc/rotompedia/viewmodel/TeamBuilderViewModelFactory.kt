package com.duoc.rotompedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duoc.rotompedia.data.local.CachedPokemonDao
import com.duoc.rotompedia.data.local.EquipoDao

class TeamBuilderViewModelFactory(
    private val equipoDao: EquipoDao,
    private val cachedPokemonDao: CachedPokemonDao? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamBuilderViewModel::class.java)) {
            return TeamBuilderViewModel(equipoDao, cachedPokemonDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
