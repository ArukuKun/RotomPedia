package com.duoc.rotompedia.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.rotompedia.data.reference.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PokemonMovesData(val index: ReferenceIndex, val learnset: Learnset)

/** One load per navigation entry/refresh, never one repository request per pager page. */
class PokemonMovesViewModel(
    val pokemonId: Int,
    private val repository: MoveRepository,
    private val saved: SavedStateHandle
) : ViewModel() {
    val edition = saved.getStateFlow<String?>("edition", null)
    val query = saved.getStateFlow("query", "")
    val type = saved.getStateFlow<String?>("type", null)
    val category = saved.getStateFlow<String?>("category", null)
    val page = saved.getStateFlow("page", 0)
    private val _data = MutableStateFlow<PokemonMovesData?>(null)
    val data: StateFlow<PokemonMovesData?> = _data
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _failed = MutableStateFlow(false)
    val failed: StateFlow<Boolean> = _failed
    private var load: Job? = null

    init { require(pokemonId > 0); load(false) }

    fun setEdition(value: String?) { saved["edition"] = value }
    fun setQuery(value: String) { saved["query"] = value }
    fun setType(value: String?) { saved["type"] = value }
    fun setCategory(value: String?) { saved["category"] = value }
    fun setPage(value: Int) { saved["page"] = value.coerceIn(0, 3) }
    fun refresh() { if (!_loading.value) load(true) }

    private fun load(refresh: Boolean) {
        load?.cancel()
        load = viewModelScope.launch {
            _loading.value = true; _failed.value = false
            try {
                val index = repository.index()
                val learnset = repository.learnset(pokemonId, refresh).data
                require(learnset.pokemonId == pokemonId)
                _data.value = PokemonMovesData(index, learnset)
                if (edition.value != null && learnset.rows.none { it.versionGroup == edition.value }) setEdition(null)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _failed.value = true
            } finally { _loading.value = false }
        }
    }
}
