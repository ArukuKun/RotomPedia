package com.duoc.rotompedia.viewmodel

import com.duoc.rotompedia.data.explorer.*
import com.duoc.rotompedia.data.repository.PokemonRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Per-screen state. Both consumers use the same repository and pure filter engine. */
class PokemonExplorerController(scope: CoroutineScope, repository: PokemonRepository, favorites: Flow<Set<Int>>,
    val nationalOnly: Boolean = false, val filter: MutableStateFlow<ExplorerFilter> = MutableStateFlow(ExplorerFilter())) {
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
    val entries = repository.observeExplorer().flowOn(Dispatchers.Default).catch { e ->
        if (e is CancellationException) throw e
        error.value = "No se pudo consultar el índice nacional."
        emit(emptyList())
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    val result = combine(entries, filter, favorites) { entries, filters, ids ->
        // Do not mount/restore lazy positions against an empty, not-yet-loaded index.
        if (entries.isEmpty()) ExplorerResult() else PokemonExplorer.filter(entries, filters, ids, nationalOnly)
    }.flowOn(Dispatchers.Default).catch { e ->
        if (e is CancellationException) throw e
        error.value = "No se pudo consultar el índice nacional."
        emit(ExplorerResult())
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), ExplorerResult())
}
