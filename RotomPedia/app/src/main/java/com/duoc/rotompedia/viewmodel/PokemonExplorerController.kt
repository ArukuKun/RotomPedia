package com.duoc.rotompedia.viewmodel

import com.duoc.rotompedia.data.explorer.*
import com.duoc.rotompedia.data.repository.PokemonRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Per-screen state. Both consumers use the same repository and pure filter engine. */
class PokemonExplorerController(scope: CoroutineScope, repository: PokemonRepository, favorites: Flow<Set<Int>>) {
    val filter = MutableStateFlow(ExplorerFilter())
    private val initializing = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)
    val loading: StateFlow<Boolean> = initializing
    val failure: StateFlow<String?> = error
    init {
        scope.launch {
            try { repository.ensureIndex() }
            catch (e: Exception) { if (e is CancellationException) throw e; error.value = "No se pudo preparar el índice nacional. Cierra y vuelve a abrir esta pantalla para reintentar." }
            finally { initializing.value = false }
        }
    }
    val result = combine(repository.observeExplorer(), filter, favorites) { entries, filters, ids ->
        PokemonExplorer.filter(entries, filters, ids)
    }.flowOn(Dispatchers.Default).catch { e ->
        if (e is CancellationException) throw e
        error.value = "No se pudo consultar el índice nacional."
        emit(ExplorerResult())
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), ExplorerResult())
}
