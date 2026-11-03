// PokemonViewModel.kt
package com.duoc.rotompedia.viewmodel

import android.util.Log
import com.duoc.rotompedia.data.repository.*
import com.duoc.rotompedia.data.PokemonFormCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.rotompedia.data.model.ChainLink
import com.duoc.rotompedia.data.model.EvolutionDetail
import com.duoc.rotompedia.data.model.MoveDetailResponse
import com.duoc.rotompedia.data.model.PokemonData
import com.duoc.rotompedia.data.model.PokemonDataWithMoves
import com.duoc.rotompedia.data.model.PokemonListItem
import com.duoc.rotompedia.data.model.PokemonSpeciesVarieties
import com.duoc.rotompedia.data.model.PokemonVariety
import com.duoc.rotompedia.data.model.Species
import com.duoc.rotompedia.data.model.TypesSlot
import com.duoc.rotompedia.data.remote.PokemonApi
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.data.local.CachedPokemon
import com.duoc.rotompedia.data.local.CachedPokemonDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.asStateFlow

// Lista de tipos disponibles para el filtro de UI
val allPokemonTypes = listOf(
    "all", "normal", "fire", "water", "electric", "grass", "ice", "fighting",
    "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon",
    "dark", "steel", "fairy"
)

// Estados
sealed class PokedexState {
    object Loading : PokedexState()
    data class Success(val pokemonList: List<PokemonListItem>) : PokedexState()
    data class Error(val message: String) : PokedexState()
}

sealed class PokemonDetailState {
    object Loading : PokemonDetailState()
    data class Success(val pokemonData: PokemonData) : PokemonDetailState()
    data class Error(val message: String) : PokemonDetailState()
}

sealed class EvolutionState {
    object Loading : EvolutionState()
    data class Success(
        val evolutionChain: List<EvolutionStage>,
        val description: String
    ) : EvolutionState()
    data class Error(val message: String) : EvolutionState()
}

sealed class TypeRelationsState {
    object Loading : TypeRelationsState()
    data class Success(
        val weakAgainst: List<String>,
        val strongAgainst: List<String>,
        val superWeakAgainst: List<String> = emptyList(),
        val resistantAgainst: List<String> = emptyList(),
        val superResistantAgainst: List<String> = emptyList(),
        val immuneAgainst: List<String> = emptyList()
    ) : TypeRelationsState()
    data class Error(val message: String) : TypeRelationsState()
}

data class EvolutionStage(
    val id: String,
    val name: String,
    val evolutionRequirement: String,
    val isBaseForm: Boolean = false,
    val evolvesFromId: String? = null
)

// Estado para movimientos de un Pokémon
sealed class PokemonMovesState {
    object Idle : PokemonMovesState()
    object Loading : PokemonMovesState()
    data class Success(
        val levelUpMoves: List<MoveDetailResponse>,
        val machineMoves: List<MoveDetailResponse>,
        val eggMoves: List<MoveDetailResponse>,
        val tutorMoves: List<MoveDetailResponse>
    ) : PokemonMovesState()
    data class Error(val message: String) : PokemonMovesState()
}

// Estado para formas de un Pokémon
sealed class PokemonFormsState {
    object Idle : PokemonFormsState()
    object Loading : PokemonFormsState()
    data class Success(val varieties: List<PokemonVariety>) : PokemonFormsState()
    data class Error(val message: String) : PokemonFormsState()
}

// Datos de un Pokémon forma alternativa
data class PokemonFormData(
    val id: Int,
    val name: String,
    val formDisplayName: String,
    val spriteUrl: String,
    val types: List<String>,
    val hp: Int, val atk: Int, val def: Int,
    val spa: Int, val spd: Int, val spe: Int,
    val isDefault: Boolean,
    val cryUrl: String? = null
)

class PokemonViewModel(
    private val cachedPokemonDao: CachedPokemonDao? = null,
    private val sessions: SessionRepository,
    private val favorites: FavoritesRepository,
    private val pokemonRepository: PokemonRepository
) : ViewModel() {
    private val TAG = "RotomPedia_ViewModel"

    // StateFlows principales
    private val _pokedexState = MutableStateFlow<PokedexState>(PokedexState.Loading)
    val pokedexState: StateFlow<PokedexState> = _pokedexState

    private val _pokemonDetailState = MutableStateFlow<PokemonDetailState>(PokemonDetailState.Loading)
    val pokemonDetailState: StateFlow<PokemonDetailState> = _pokemonDetailState

    private val _evolutionState = MutableStateFlow<EvolutionState>(EvolutionState.Loading)
    val evolutionState: StateFlow<EvolutionState> = _evolutionState

    private val _typeRelationsState = MutableStateFlow<TypeRelationsState>(TypeRelationsState.Loading)
    val typeRelationsState: StateFlow<TypeRelationsState> = _typeRelationsState

    // Cache y listas
    private var fullPokemonList = emptyList<PokemonListItem>()
    private var currentLoadJob: Job? = null
    private var detailJob: Job? = null
    private var evolutionJob: Job? = null
    private var relationsJob: Job? = null
    private var varietiesJob: Job? = null
    private var movesJob: Job? = null
    private var pickerSearchJob: Job? = null
    private var pickerIndexCache: List<PokemonListItem>? = null

    suspend fun getPickerIndex(): List<PokemonListItem> {
        pickerIndexCache?.let { return it }
        return PokemonApi.service.getPokemonIndex().results.distinctBy { it.name }
            .also { pickerIndexCache = it }
    }

    suspend fun getPickerTypeNames(type: String): Set<String> =
        PokemonApi.service.getTypeDetails(type).pokemon.flatMap {
            listOf(it.pokemon.name, it.pokemon.url.trimEnd('/').substringAfterLast('/'))
        }.toSet()
    private val _pokemonTypesCache = mutableMapOf<Int, List<String>>()

    // Filtros
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedTypeFilter = MutableStateFlow("all")
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter

    private val _selectedRegionFilter = MutableStateFlow("Todos")
    val selectedRegionFilter: StateFlow<String> = _selectedRegionFilter

    // Usuario activo actual para favoritos aislados
    val currentUsername = sessions.session.map { it.username.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    suspend fun getCachedPokemonDirect(pokemonId: Int): CachedPokemon? {
        return withContext(Dispatchers.IO) {
            cachedPokemonDao?.getPokemonById(pokemonId)
        }
    }

    val allCachedPokemon: Flow<List<CachedPokemon>> = cachedPokemonDao?.getAllCachedPokemon()
        ?: flowOf(emptyList())

    // ==================== MOVIMIENTOS ====================
    private val _pokemonMovesState = MutableStateFlow<PokemonMovesState>(PokemonMovesState.Idle)
    val pokemonMovesState: StateFlow<PokemonMovesState> = _pokemonMovesState.asStateFlow()

    // Cache de movimientos para no refetchear (pokemonId -> lista de moves por método)
    private val _movesCache = mutableMapOf<Int, PokemonMovesState.Success>()

    // ==================== FORMAS ====================
    private val _pokemonFormsState = MutableStateFlow<PokemonFormsState>(PokemonFormsState.Idle)
    val pokemonFormsState: StateFlow<PokemonFormsState> = _pokemonFormsState.asStateFlow()

    // Variedades con datos completos (sprite + tipos + stats) para la sección de Formas en PokemonDetailScreen
    private val _formVarieties = MutableStateFlow<List<PokemonFormData>>(emptyList())
    val formVarieties: StateFlow<List<PokemonFormData>> = _formVarieties.asStateFlow()

    // Resultados completos para consumidores que necesitan fichas, no solo metadata.
    private val _pickerForms = MutableStateFlow<List<PokemonFormData>>(emptyList())
    val pickerForms: StateFlow<List<PokemonFormData>> = _pickerForms.asStateFlow()
    private var _pickerFormsLoading = MutableStateFlow(false)
    val pickerFormsLoading: StateFlow<Boolean> = _pickerFormsLoading.asStateFlow()

    private val _pokemonFormDataCache = mutableMapOf<String, PokemonFormData>()

    // ==================== BÚSQUEDA DIRECTA POR NOMBRE ====================
    private val _searchedPokemon = MutableStateFlow<PokemonFormData?>(null)
    val searchedPokemon: StateFlow<PokemonFormData?> = _searchedPokemon.asStateFlow()

    suspend fun getAllCachedPokemonDirect(): List<CachedPokemon> {
        return withContext(Dispatchers.IO) {
            cachedPokemonDao?.getAllCachedPokemonDirect() ?: emptyList()
        }
    }

    // ✅ ACTUALIZADO: Favoritos aislados reactivamente por usuario activo
    val favoritePokemonIds: StateFlow<Set<Int>> = favorites.ids
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    // ✅ CORREGIDO: Estado para filtro de favoritos
    private val _isFavoritesOnly = MutableStateFlow(false)
    val isFavoritesOnly: StateFlow<Boolean> = _isFavoritesOnly

    init {
        loadPokemonList()
    }

    // Cache de IDs por tipo y Flow de IDs activos para filtrado
    private val _typePokemonIdsCache = mutableMapOf<String, Set<Int>>()
    private val _typeFilterPokemonIds = MutableStateFlow<Set<Int>?>(null)

    // ✅ ACTUALIZADO: Lista filtrada con filtro de tipo completo y favoritos
    val filteredPokemonList: StateFlow<List<PokemonListItem>> = combine(
        _pokedexState,
        _searchQuery,
        _selectedTypeFilter,
        _selectedRegionFilter,
        _isFavoritesOnly,
        favoritePokemonIds,
        _typeFilterPokemonIds
    ) { flows ->
        val pokedexStateValue = flows[0] as PokedexState
        val searchQueryValue = flows[1] as String
        val selectedTypeValue = flows[2] as String
        val selectedRegionValue = flows[3] as String
        val favoritesOnly = flows[4] as Boolean
        val favIds = flows[5] as Set<Int>
        @Suppress("UNCHECKED_CAST")
        val typeIds = flows[6] as? Set<Int>

        // Si el estado no es Success, devolver lista vacía
        val baseList = (pokedexStateValue as? PokedexState.Success)?.pokemonList ?: return@combine emptyList()

        // Aplicar todos los filtros
        baseList.filter { item ->
            val id = extractIdFromUrl(item.url) ?: return@filter false

            // Filtro de búsqueda por nombre, ID o tipo (español e inglés)
            val matchesSearch = if (searchQueryValue.isBlank()) {
                true
            } else {
                val q = searchQueryValue.trim().lowercase()
                val nameMatch = item.name.contains(q, ignoreCase = true)
                val idMatch = id.toString().contains(q)
                val typeKey = mapQueryToTypeKey(q)
                val typeMatch = if (typeKey != null) {
                    _typePokemonIdsCache[typeKey]?.contains(id) == true
                } else false

                nameMatch || idMatch || typeMatch
            }

            // Filtro por tipo (consulta directa a todos los Pokémon de ese tipo)
            val matchesType = if (selectedTypeValue == "all" || typeIds == null) {
                true
            } else {
                typeIds.contains(id)
            }

            // Filtro por región
            val matchesRegion = if (selectedRegionValue == "Todos") {
                true
            } else {
                val (startId, endId) = getRegionRange(selectedRegionValue)
                id in startId..endId
            }

            // Filtro de favoritos
            val matchesFavorites = if (favoritesOnly) {
                favIds.contains(id)
            } else {
                true
            }

            matchesSearch && matchesType && matchesRegion && matchesFavorites
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private fun loadPokemonList() {
        currentLoadJob?.cancel()

        currentLoadJob = viewModelScope.launch {
            _pokedexState.value = PokedexState.Loading
            try {
                // Cargar Kanto primero
                val kantoResponse = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemonList(limit = 151, offset = 0)
                }

                val formattedKantoList = kantoResponse.results.map {
                    it.copy(name = it.name.replaceFirstChar { char -> char.uppercase() })
                }

                fullPokemonList = formattedKantoList
                _pokedexState.value = PokedexState.Success(fullPokemonList)
                Log.d(TAG, "Lista de Kanto cargada: ${formattedKantoList.size} items")

                // Precargar tipos de los primeros 20
                preloadPokemonTypes(formattedKantoList.take(20))

                // Cargar lista completa en segundo plano
                val fullResponse = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemonList(limit = 10000, offset = 0)
                }

                val formattedFullList = fullResponse.results.map {
                    it.copy(name = it.name.replaceFirstChar { char -> char.uppercase() })
                }

                if (isActive) {
                    fullPokemonList = formattedFullList
                    _pokedexState.value = PokedexState.Success(fullPokemonList)
                    Log.d(TAG, "Lista completa cargada: ${fullPokemonList.size} items")

                    preloadPokemonTypes(formattedFullList.take(50))
                }

            } catch (e: Exception) {
                // Modo Offline: intentar cargar desde Room
                val cached = try {
                    withContext(Dispatchers.IO) {
                        cachedPokemonDao?.getAllCachedPokemonDirect().orEmpty()
                    }
                } catch (ce: Exception) {
                    emptyList()
                }

                if (cached.isNotEmpty()) {
                    fullPokemonList = cached.map {
                        PokemonListItem(
                            name = it.name,
                            url = "https://pokeapi.co/api/v2/pokemon/${it.id}/",
                            types = it.types.split(",").filter { t -> t.isNotBlank() }
                        )
                    }
                    cached.forEach { item ->
                        _pokemonTypesCache[item.id] = item.types.split(",").filter { t -> t.isNotBlank() }
                    }
                    _pokedexState.value = PokedexState.Success(fullPokemonList)
                    Log.d(TAG, "Modo Offline: cargados ${cached.size} Pokémon desde caché Room")
                } else {
                    val errorMessage = "Error al cargar la lista de Pokémon: ${e.message}"
                    _pokedexState.value = PokedexState.Error(errorMessage)
                    Log.e(TAG, errorMessage, e)
                }
            }
        }
    }

    private fun getRegionRange(region: String): Pair<Int, Int> {
        return when (region) {
            "Kanto" -> 1 to 151
            "Johto" -> 152 to 251
            "Hoenn" -> 252 to 386
            "Sinnoh" -> 387 to 493
            "Unova" -> 494 to 649
            "Kalos" -> 650 to 721
            "Alola" -> 722 to 809
            "Galar" -> 810 to 905
            "Paldea" -> 906 to 1025
            else -> 1 to 1000
        }
    }

    private suspend fun loadPokemonTypes(pokemonId: Int): List<String> {
        return try {
            val pokemonData = withContext(Dispatchers.IO) {
                pokemonRepository.getPokemon(pokemonId.toString())
            }
            // Guardar en Room para consulta offline
            try {
                cachedPokemonDao?.let { dao ->
                    val typesStr = pokemonData.types.joinToString(",") { it.type.name }
                    val hp = pokemonData.stats.find { it.stat.name == "hp" }?.baseStat ?: 0
                    val atk = pokemonData.stats.find { it.stat.name == "attack" }?.baseStat ?: 0
                    val def = pokemonData.stats.find { it.stat.name == "defense" }?.baseStat ?: 0
                    val spa = pokemonData.stats.find { it.stat.name == "special-attack" }?.baseStat ?: 0
                    val spd = pokemonData.stats.find { it.stat.name == "special-defense" }?.baseStat ?: 0
                    val spe = pokemonData.stats.find { it.stat.name == "speed" }?.baseStat ?: 0
                    dao.insertOrUpdate(
                        CachedPokemon(
                            id = pokemonData.id,
                            name = pokemonData.name.replaceFirstChar { it.uppercase() },
                            imageUrl = pokemonData.sprites.urlImagen ?: "",
                            types = typesStr,
                            hp = hp, attack = atk, defense = def,
                            specialAttack = spa, specialDefense = spd, speed = spe,
                            cryUrl = pokemonData.cries?.latest ?: pokemonData.cries?.legacy
                        )
                    )
                }
            } catch (_: Exception) {}
            pokemonData.types.map { it.type.name }
        } catch (e: Exception) {
            // Intentar leer de caché offline
            val cached = cachedPokemonDao?.getPokemonById(pokemonId)
            if (cached != null) {
                cached.types.split(",").filter { it.isNotBlank() }
            } else {
                Log.e(TAG, "Error al cargar tipos para Pokémon $pokemonId: ${e.message}")
                emptyList()
            }
        }
    }

    private fun preloadPokemonTypes(pokemonList: List<PokemonListItem>) {
        viewModelScope.launch {
            pokemonList.forEach { pokemon ->
                val id = extractIdFromUrl(pokemon.url)
                if (id != null && !_pokemonTypesCache.containsKey(id)) {
                    val types = loadPokemonTypes(id)
                    _pokemonTypesCache[id] = types
                }
            }
        }
    }

    private fun extractIdFromUrl(url: String): Int? {
        return try {
            url.split("/").dropLast(1).last().toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // ✅ ACTUALIZADO: Favoritos aislados por usuario
    fun toggleFavorite(pokemonId: Int, pokemonName: String, isCurrentlyFavorite: Boolean) {
        val owner = sessions.capture()
        viewModelScope.launch(Dispatchers.IO) {
            favorites.toggle(owner, pokemonId, pokemonName)
        }
    }

    fun getIsFavoriteFlow(pokemonId: Int): StateFlow<Boolean> {
        return favoritePokemonIds
            .map { it.contains(pokemonId) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )
    }

    // ✅ FUNCIONES DE FILTRADO
    fun toggleFavoritesOnly(enable: Boolean) {
        _isFavoritesOnly.value = enable
        Log.d(TAG, "Filtro de favoritos: ${if (enable) "activado" else "desactivado"}")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val q = query.trim().lowercase()
        val typeKey = mapQueryToTypeKey(q)
        if (typeKey != null && !_typePokemonIdsCache.containsKey(typeKey)) {
            viewModelScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        PokemonApi.service.getTypeDetails(typeKey)
                    }
                    val ids = response.pokemon.mapNotNull { extractIdFromUrl(it.pokemon.url) }.toSet()
                    _typePokemonIdsCache[typeKey] = ids
                } catch (e: Exception) {
                    Log.e(TAG, "Error precargando tipo para búsqueda $typeKey: ${e.message}")
                }
            }
        }
    }

    private fun mapQueryToTypeKey(q: String): String? {
        return when (q) {
            "fuego", "fire" -> "fire"
            "agua", "water" -> "water"
            "planta", "grass" -> "grass"
            "electrico", "eléctrico", "electric" -> "electric"
            "normal" -> "normal"
            "hielo", "ice" -> "ice"
            "lucha", "fighting" -> "fighting"
            "veneno", "poison" -> "poison"
            "tierra", "ground" -> "ground"
            "volador", "flying" -> "flying"
            "psiquico", "psíquico", "psychic" -> "psychic"
            "bicho", "bug" -> "bug"
            "roca", "rock" -> "rock"
            "fantasma", "ghost" -> "ghost"
            "dragon", "dragón" -> "dragon"
            "siniestro", "dark" -> "dark"
            "acero", "steel" -> "steel"
            "hada", "fairy" -> "fairy"
            else -> null
        }
    }

    fun updateTypeFilter(type: String) {
        val lowerType = type.lowercase()
        _selectedTypeFilter.value = lowerType
        if (lowerType == "all") {
            _typeFilterPokemonIds.value = null
        } else {
            val cached = _typePokemonIdsCache[lowerType]
            if (cached != null) {
                _typeFilterPokemonIds.value = cached
            } else {
                viewModelScope.launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            PokemonApi.service.getTypeDetails(lowerType)
                        }
                        val ids = response.pokemon.mapNotNull { extractIdFromUrl(it.pokemon.url) }.toSet()
                        _typePokemonIdsCache[lowerType] = ids
                        if (_selectedTypeFilter.value == lowerType) {
                            _typeFilterPokemonIds.value = ids
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al cargar Pokémon del tipo $lowerType: ${e.message}", e)
                    }
                }
            }
        }
    }

    fun updateRegionFilter(regionName: String) {
        _selectedRegionFilter.value = regionName
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedTypeFilter.value = "all"
        _selectedRegionFilter.value = "Todos"
        _isFavoritesOnly.value = false
        _typeFilterPokemonIds.value = null
        Log.d(TAG, "Todos los filtros han sido limpiados")
    }

    suspend fun getPokemonTypes(pokemonId: Int): List<String> {
        return _pokemonTypesCache[pokemonId] ?: loadPokemonTypes(pokemonId).also {
            _pokemonTypesCache[pokemonId] = it
        }
    }

    // ✅ LÓGICA DE DETALLES Y EVOLUCIONES
    fun loadPokemonDetails(pokemonId: String) {
        detailJob?.cancel()
        relationsJob?.cancel()
        _typeRelationsState.value = TypeRelationsState.Loading
        detailJob = viewModelScope.launch {
            _pokemonDetailState.value = PokemonDetailState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    pokemonRepository.getPokemon(pokemonId)
                }
                _pokemonDetailState.value = PokemonDetailState.Success(response)
                Log.d(TAG, "Detalles de ${response.name} cargados exitosamente.")

                _pokemonTypesCache[response.id] = response.types.map { it.type.name }

                // Guardar en caché offline Room
                withContext(Dispatchers.IO) {
                    try {
                        val typesStr = response.types.joinToString(",") { it.type.name }
                        val hp = response.stats.find { it.stat.name == "hp" }?.baseStat ?: 0
                        val atk = response.stats.find { it.stat.name == "attack" }?.baseStat ?: 0
                        val def = response.stats.find { it.stat.name == "defense" }?.baseStat ?: 0
                        val spa = response.stats.find { it.stat.name == "special-attack" }?.baseStat ?: 0
                        val spd = response.stats.find { it.stat.name == "special-defense" }?.baseStat ?: 0
                        val spe = response.stats.find { it.stat.name == "speed" }?.baseStat ?: 0
                        cachedPokemonDao?.insertOrUpdate(
                            CachedPokemon(
                                id = response.id,
                                name = response.name.replaceFirstChar { it.uppercase() },
                                imageUrl = response.sprites.urlImagen ?: "",
                                types = typesStr,
                                hp = hp, attack = atk, defense = def,
                                specialAttack = spa, specialDefense = spd, speed = spe,
                                cryUrl = response.cries?.latest ?: response.cries?.legacy
                            )
                        )
                    } catch (ce: Exception) {
                        Log.e(TAG, "Error guardando en caché Room: ${ce.message}")
                    }
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Modo Offline: recuperar datos de Room
                val cached = try {
                    withContext(Dispatchers.IO) {
                        cachedPokemonDao?.getPokemonById(pokemonId.toIntOrNull() ?: 0)
                    }
                } catch (ce: Exception) {
                    null
                }

                if (cached != null) {
                    val fallbackData = PokemonData(
                        id = cached.id,
                        name = cached.name,
                        sprites = com.duoc.rotompedia.data.model.SpritesData(cached.imageUrl),
                        types = cached.types.split(",").filter { it.isNotBlank() }.mapIndexed { idx, t ->
                            com.duoc.rotompedia.data.model.TypesSlot(idx + 1, com.duoc.rotompedia.data.model.TypeData(t))
                        },
                        stats = listOf(
                            com.duoc.rotompedia.data.model.StatInfo(cached.hp, com.duoc.rotompedia.data.model.Stat("hp")),
                            com.duoc.rotompedia.data.model.StatInfo(cached.attack, com.duoc.rotompedia.data.model.Stat("attack")),
                            com.duoc.rotompedia.data.model.StatInfo(cached.defense, com.duoc.rotompedia.data.model.Stat("defense")),
                            com.duoc.rotompedia.data.model.StatInfo(cached.specialAttack, com.duoc.rotompedia.data.model.Stat("special-attack")),
                            com.duoc.rotompedia.data.model.StatInfo(cached.specialDefense, com.duoc.rotompedia.data.model.Stat("special-defense")),
                            com.duoc.rotompedia.data.model.StatInfo(cached.speed, com.duoc.rotompedia.data.model.Stat("speed"))
                        ),
                        cries = com.duoc.rotompedia.data.model.CriesData(latest = cached.cryUrl)
                    )
                    _pokemonDetailState.value = PokemonDetailState.Success(fallbackData)
                    _pokemonTypesCache[cached.id] = cached.types.split(",").filter { it.isNotBlank() }
                    Log.d(TAG, "Modo Offline: detalles de ${cached.name} recuperados de Room")
                } else {
                    val errorMessage = "Error al cargar detalles: ${e.message}"
                    _pokemonDetailState.value = PokemonDetailState.Error(errorMessage)
                    Log.e(TAG, errorMessage, e)
                }
            }
        }
    }

    fun loadEvolutionChain(pokemonId: String) {
        evolutionJob?.cancel()
        evolutionJob = viewModelScope.launch {
            _evolutionState.value = EvolutionState.Loading
            try {
                val speciesResponse = withContext(Dispatchers.IO) {
                    val pokemon = pokemonRepository.getPokemon(pokemonId)
                    val speciesId = pokemon.species?.url?.trimEnd('/')?.substringAfterLast('/')?.toIntOrNull()
                        ?: pokemonId.toInt()
                    PokemonApi.service.getPokemonSpecies(speciesId)
                }

                val description = speciesResponse.flavorTextEntries
                    .find { it.language.name == "es" }
                    ?.flavorText
                    ?.replace("\n", " ")
                    ?: "No se encontró descripción en español."

                val evolutionChainUrl = speciesResponse.evolutionChain.url
                val evolutionResponse = withContext(Dispatchers.IO) {
                    PokemonApi.service.getEvolutionChain(evolutionChainUrl)
                }

                val evolutionStages = parseEvolutionChainWithStages(evolutionResponse.chain)
                    .distinctBy { it.name }

                _evolutionState.value = EvolutionState.Success(evolutionStages, description)
                Log.d(TAG, "Evolución y descripción cargadas. Cadena: ${evolutionStages.size} etapas")

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val errorMessage = "Error al cargar evolución/descripción: ${e.message}"
                _evolutionState.value = EvolutionState.Error(errorMessage)
                Log.e(TAG, errorMessage, e)
            }
        }
    }

    private fun parseEvolutionChainWithStages(
        chainLink: ChainLink,
        isBaseForm: Boolean = true,
        evolvesFromId: String? = null
    ): List<EvolutionStage> {
        val evolutionStages = mutableListOf<EvolutionStage>()

        val currentId = chainLink.species.url.split("/").dropLast(1).last()
        val currentName = chainLink.species.name.replaceFirstChar { char -> char.uppercase() }

        val evolutionRequirement = if (isBaseForm) {
            "Forma Base"
        } else {
            getEvolutionRequirementText(chainLink.evolutionDetails.firstOrNull())
        }

        evolutionStages.add(
            EvolutionStage(
                id = currentId,
                name = currentName,
                evolutionRequirement = evolutionRequirement,
                isBaseForm = isBaseForm,
                evolvesFromId = evolvesFromId
            )
        )

        for (nextEvolution in chainLink.evolvesTo) {
            evolutionStages.addAll(
                parseEvolutionChainWithStages(
                    chainLink = nextEvolution,
                    isBaseForm = false,
                    evolvesFromId = currentId
                )
            )
        }

        return evolutionStages
    }

    private fun getEvolutionRequirementText(details: EvolutionDetail?): String {
        if (details == null) return "Evoluciona"

        return when (details.trigger?.name) {
            "level-up" -> getLevelUpRequirement(details)
            "use-item" -> getItemRequirement(details)
            "trade" -> getTradeRequirement(details)
            "shed" -> "Evoluciona al dejar espacio en equipo"
            "spin" -> "Girar${if (details.turnUpsideDown == true) " boca abajo" else ""}"
            "tower-of-darkness" -> "Torre de la Oscuridad"
            "tower-of-waters" -> "Torre de las Aguas"
            "three-critical-hits" -> "3 golpes críticos en una batalla"
            "take-damage" -> "Recibir daño y retroceder"
            "other" -> getOtherRequirement(details)
            else -> "Condición especial"
        }
    }

    private fun getLevelUpRequirement(details: EvolutionDetail): String {
        val requirements = mutableListOf<String>()

        details.minLevel?.let { requirements.add("Nivel $it") }
        details.minHappiness?.let { requirements.add("Felicidad $it") }
        details.minBeauty?.let { requirements.add("Belleza $it") }
        details.minAffection?.let { requirements.add("Afecto $it") }
        details.timeOfDay?.takeIf { it.isNotBlank() }?.let {
            requirements.add(if (it == "day") "Día" else "Noche")
        }
        details.knownMove?.let {
            requirements.add("Movimiento: ${it.name.replaceFirstChar { char -> char.uppercase() }}")
        }
        details.knownMoveType?.let {
            requirements.add("Mov. tipo: ${traducirTipo(it.name)}")
        }
        details.location?.let {
            requirements.add("En: ${it.name.replaceFirstChar { char -> char.uppercase() }}")
        }
        if (details.needsOverworldRain == true) {
            requirements.add("Con lluvia")
        }
        details.relativePhysicalStats?.let {
            val statReq = when (it) {
                -1 -> "Ataque < Defensa"
                0 -> "Ataque = Defensa"
                1 -> "Ataque > Defensa"
                else -> ""
            }
            if (statReq.isNotEmpty()) requirements.add(statReq)
        }

        return if (requirements.isEmpty()) "Subir nivel" else requirements.joinToString(" + ")
    }

    private fun getItemRequirement(details: EvolutionDetail): String {
        val itemName = details.item?.name?.replaceFirstChar { it.uppercase() } ?: "Objeto"
        val additionalReqs = mutableListOf<String>()

        details.timeOfDay?.takeIf { it.isNotBlank() }?.let {
            additionalReqs.add(if (it == "day") "de día" else "de noche")
        }
        details.gender?.let {
            additionalReqs.add(if (it == 1) "hembra" else "macho")
        }

        val additionalText = if (additionalReqs.isNotEmpty()) " (${additionalReqs.joinToString(", ")})" else ""
        return "Usar $itemName$additionalText"
    }

    private fun getTradeRequirement(details: EvolutionDetail): String {
        val requirements = mutableListOf<String>()

        details.heldItem?.let {
            requirements.add("con ${it.name.replaceFirstChar { char -> char.uppercase() }}")
        }
        details.tradeSpecies?.let {
            requirements.add("por ${it.name.replaceFirstChar { char -> char.uppercase() }}")
        }

        val requirementsText = if (requirements.isNotEmpty()) " ${requirements.joinToString(" ")}" else ""
        return "Intercambio$requirementsText"
    }

    private fun getOtherRequirement(details: EvolutionDetail): String {
        return when {
            details.minBeauty != null -> "Belleza ${details.minBeauty}"
            details.minAffection != null -> "Afecto ${details.minAffection}"
            details.minHappiness != null -> "Felicidad ${details.minHappiness}"
            details.gender != null -> {
                val gender = if (details.gender == 1) "hembra" else "macho"
                "Género: $gender"
            }
            else -> "Condición especial"
        }
    }

    fun loadTypeRelations(types: List<TypesSlot>) {
        relationsJob?.cancel()
        relationsJob = viewModelScope.launch {
            _typeRelationsState.value = TypeRelationsState.Loading
            try {
                require(types.isNotEmpty()) { "No hay tipos disponibles" }
                val type1Details = async(Dispatchers.IO) {
                    PokemonApi.service.getTypeDetails(types[0].type.name)
                }

                val type2Details = if (types.size > 1) {
                    async(Dispatchers.IO) {
                        PokemonApi.service.getTypeDetails(types[1].type.name)
                    }
                } else {
                    null
                }

                val type1Relations = type1Details.await().damageRelations
                val type2Relations = type2Details?.await()?.damageRelations

                val allTypes = listOf(
                    "normal", "fire", "water", "grass", "electric", "ice", "fighting",
                    "poison", "ground", "flying", "psychic", "bug", "rock", "ghost",
                    "dragon", "dark", "steel", "fairy"
                )

                fun getMultiplier(typeRel: com.duoc.rotompedia.data.model.DamageRelations, attackingType: String): Float {
                    return when {
                        typeRel.noDamageFrom.any { it.name.equals(attackingType, ignoreCase = true) } -> 0.0f
                        typeRel.doubleDamageFrom.any { it.name.equals(attackingType, ignoreCase = true) } -> 2.0f
                        typeRel.halfDamageFrom.any { it.name.equals(attackingType, ignoreCase = true) } -> 0.5f
                        else -> 1.0f
                    }
                }

                val superWeak = mutableListOf<String>()
                val weak = mutableListOf<String>()
                val resistant = mutableListOf<String>()
                val superResistant = mutableListOf<String>()
                val immune = mutableListOf<String>()

                for (atk in allTypes) {
                    val m1 = getMultiplier(type1Relations, atk)
                    val m2 = if (type2Relations != null) getMultiplier(type2Relations, atk) else 1.0f
                    val total = m1 * m2

                    when {
                        total >= 4.0f -> superWeak.add(atk)
                        total in 1.9f..3.9f -> weak.add(atk)
                        total == 0.0f -> immune.add(atk)
                        total in 0.01f..0.3f -> superResistant.add(atk)
                        total in 0.31f..0.6f -> resistant.add(atk)
                    }
                }

                val strongAgainst = mutableSetOf<String>()
                strongAgainst.addAll(type1Relations.doubleDamageTo.map { it.name })
                if (type2Relations != null) {
                    strongAgainst.addAll(type2Relations.doubleDamageTo.map { it.name })
                }

                _typeRelationsState.value = TypeRelationsState.Success(
                    weakAgainst = weak,
                    strongAgainst = strongAgainst.toList(),
                    superWeakAgainst = superWeak,
                    resistantAgainst = resistant,
                    superResistantAgainst = superResistant,
                    immuneAgainst = immune
                )
                Log.d(TAG, "Relaciones de tipo calculadas matemáticamente con daño dual.")

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val errorMessage = "Error al cargar relaciones de tipo: ${e.message}"
                _typeRelationsState.value = TypeRelationsState.Error(errorMessage)
                Log.e(TAG, errorMessage, e)
            }
        }
    }

    // Carga los movimientos reales de un Pokémon desde PokeAPI
    fun loadPokemonMoves(pokemonId: Int) {
        movesJob?.cancel()
        // Retornar caché si ya se cargó
        _movesCache[pokemonId]?.let {
            _pokemonMovesState.value = it
            return
        }

        movesJob = viewModelScope.launch {
            _pokemonMovesState.value = PokemonMovesState.Loading
            try {
                val pokemonWithMoves = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemonWithMoves(pokemonId.toString())
                }

                // Un movimiento puede pertenecer a varios métodos. No perder MT/huevo
                // por escoger solamente un detalle arbitrario de versión.
                val movesByMethod = pokemonWithMoves.moves
                    .flatMap { entry ->
                        entry.versionGroupDetails.groupBy { it.moveLearnMethod.name }.map { (method, versions) ->
                            Triple(method, entry.move.name, versions.maxByOrNull {
                                it.versionGroup.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
                            }?.levelLearnedAt ?: 0)
                        }
                    }
                    .groupBy { it.first }

                val levelUpNames = movesByMethod["level-up"]
                    ?.sortedBy { it.third }?.map { it.second } ?: emptyList()

                val machineNames = movesByMethod["machine"]?.map { it.second } ?: emptyList()
                val eggNames = movesByMethod["egg"]?.map { it.second } ?: emptyList()
                val tutorNames = movesByMethod["tutor"]?.map { it.second } ?: emptyList()

                // Concurrencia acotada y errores visibles en vez de cachear listas incompletas.
                val maxMoves = 60
                val semaphore = Semaphore(4)
                val detailsCache = mutableMapOf<String, MoveDetailResponse>()
                suspend fun fetchMoveDetails(names: List<String>, limit: Int = maxMoves): List<MoveDetailResponse> {
                    return coroutineScope {
                        names.distinct().take(limit).map { moveName ->
                            async {
                                semaphore.withPermit {
                                    detailsCache[moveName] ?: PokemonApi.service.getMoveDetail(moveName)
                                        .also { detailsCache[moveName] = it }
                                }
                            }
                        }.awaitAll()
                    }
                }

                val levelUpMoves = fetchMoveDetails(levelUpNames)
                val machineMoves = fetchMoveDetails(machineNames)
                val eggMoves = fetchMoveDetails(eggNames)
                val tutorMoves = fetchMoveDetails(tutorNames, 30)

                val result = PokemonMovesState.Success(levelUpMoves, machineMoves, eggMoves, tutorMoves)
                _movesCache[pokemonId] = result
                _pokemonMovesState.value = result
                Log.d(TAG, "Movimientos de #$pokemonId cargados: ${levelUpMoves.size} nivel, ${machineMoves.size} MT, ${eggMoves.size} huevo")

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _pokemonMovesState.value = PokemonMovesState.Error("Error al cargar movimientos: ${e.message}")
                Log.e(TAG, "Error al cargar movimientos de #$pokemonId: ${e.message}")
            }
        }
    }

    // Un único flujo de carga de formas para todos los consumidores.
    fun loadPokemonForms(speciesId: Int) = loadFormVarieties(speciesId)

    // Carga datos completos de una forma específica (para mostrar sus stats/sprite)
    suspend fun loadFormData(formName: String): PokemonFormData? {
        _pokemonFormDataCache[formName]?.let { return it }
        return try {
            val data = withContext(Dispatchers.IO) {
                pokemonRepository.getPokemon(formName)
            }
            val formData = PokemonFormData(
                id = data.id,
                name = data.name,
                formDisplayName = PokemonFormCatalog.displayName(data.name),
                spriteUrl = data.sprites.urlImagen ?: "",
                types = data.types.map { it.type.name },
                hp = data.stats.find { it.stat.name == "hp" }?.baseStat ?: 0,
                atk = data.stats.find { it.stat.name == "attack" }?.baseStat ?: 0,
                def = data.stats.find { it.stat.name == "defense" }?.baseStat ?: 0,
                spa = data.stats.find { it.stat.name == "special-attack" }?.baseStat ?: 0,
                spd = data.stats.find { it.stat.name == "special-defense" }?.baseStat ?: 0,
                spe = data.stats.find { it.stat.name == "speed" }?.baseStat ?: 0,
                isDefault = false,
                cryUrl = data.cries?.latest ?: data.cries?.legacy
            )
            try {
                cachedPokemonDao?.insertOrUpdate(CachedPokemon(
                    id = data.id, name = data.name, imageUrl = formData.spriteUrl,
                    types = formData.types.joinToString(","), hp = formData.hp,
                    attack = formData.atk, defense = formData.def, specialAttack = formData.spa,
                    specialDefense = formData.spd, speed = formData.spe, cryUrl = formData.cryUrl
                ))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "No se pudo guardar la forma en caché", e)
            }
            _pokemonFormDataCache[formName] = formData
            formData
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error cargando forma $formName: ${e.message}")
            val cached = withContext(Dispatchers.IO) {
                try {
                    formName.toIntOrNull()?.let { cachedPokemonDao?.getPokemonById(it) }
                        ?: cachedPokemonDao?.getAllCachedPokemonDirect()?.find { it.name.equals(formName, ignoreCase = true) }
                } catch (cacheError: Exception) {
                    if (cacheError is CancellationException) throw cacheError
                    null
                }
            }
            cached?.let {
                PokemonFormData(it.id, it.name, PokemonFormCatalog.displayName(it.name), it.imageUrl,
                    it.types.split(",").filter { type -> type.isNotBlank() }, it.hp, it.attack, it.defense,
                    it.specialAttack, it.specialDefense, it.speed, false, it.cryUrl)
            }
        }
    }

    // Carga todas las variedades con datos completos (sprite+tipos+stats) para sección de Formas en detalle
    fun loadFormVarieties(speciesId: Int) {
        varietiesJob?.cancel()
        varietiesJob = viewModelScope.launch {
            _formVarieties.value = emptyList()
            _pokemonFormsState.value = PokemonFormsState.Loading
            try {
                val species = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemonSpeciesWithVarieties(speciesId)
                }
                // Incluye la variedad predeterminada para poder volver a ella. Concurrencia
                // acotada, no un límite arbitrario que esconda variedades de la especie.
                val semaphore = Semaphore(4)
                val loaded = coroutineScope {
                    species.varieties.distinctBy { it.pokemon.name }.map { variety ->
                        async {
                            semaphore.withPermit {
                                loadFormData(variety.pokemon.name)?.copy(isDefault = variety.isDefault)
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
                _formVarieties.value = loaded
                _pokemonFormsState.value = if (loaded.size < species.varieties.size) {
                    PokemonFormsState.Error("Algunas formas no se pudieron cargar. Toca Reintentar.")
                } else PokemonFormsState.Success(species.varieties)
                Log.d(TAG, "Variedades de especie #$speciesId: ${loaded.size} cargadas")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _formVarieties.value = emptyList()
                _pokemonFormsState.value = PokemonFormsState.Error("No se pudieron cargar las formas.")
                Log.e(TAG, "Error cargando variedades de #$speciesId: ${e.message}")
            }
        }
    }

    // Búsqueda parcial híbrida: etiquetas locales + índice de variedades de API.
    fun searchPickerForms(query: String) {
        pickerSearchJob?.cancel()
        if (query.isBlank()) {
            _pickerForms.value = emptyList()
            _pickerFormsLoading.value = false
            return
        }
        pickerSearchJob = viewModelScope.launch {
            _pickerFormsLoading.value = true
            try {
                val q = query.trim().lowercase()
                val matches = com.duoc.rotompedia.data.buildPickerForms(getPickerIndex())
                    .filter { it.metadata.matches(q) }
                _pickerForms.value = matches.mapNotNull { loadFormData(it.nameEn) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _pickerForms.value = emptyList()
            } finally {
                _pickerFormsLoading.value = false
            }
        }
    }

    fun clearPickerForms() {
        pickerSearchJob?.cancel()
        _pickerForms.value = emptyList()
        _pickerFormsLoading.value = false
    }

    // Búsqueda directa de un Pokémon por nombre (para calculadoras)
    fun searchAndFetchPokemon(nameOrId: String, onResult: (PokemonFormData?) -> Unit) {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemonWithMoves(nameOrId.lowercase().trim())
                }
                val formData = PokemonFormData(
                    id = data.id,
                    name = data.name,
                    formDisplayName = data.name.replaceFirstChar { it.uppercase() },
                    spriteUrl = data.sprites.urlImagen ?: "",
                    types = data.types.map { it.type.name },
                    hp = data.stats.find { it.stat.name == "hp" }?.baseStat ?: 0,
                    atk = data.stats.find { it.stat.name == "attack" }?.baseStat ?: 0,
                    def = data.stats.find { it.stat.name == "defense" }?.baseStat ?: 0,
                    spa = data.stats.find { it.stat.name == "special-attack" }?.baseStat ?: 0,
                    spd = data.stats.find { it.stat.name == "special-defense" }?.baseStat ?: 0,
                    spe = data.stats.find { it.stat.name == "speed" }?.baseStat ?: 0,
                    isDefault = true
                )
                // Guardar en cache Room
                withContext(Dispatchers.IO) {
                    try {
                        cachedPokemonDao?.insertOrUpdate(
                            CachedPokemon(
                                id = data.id,
                                name = data.name.replaceFirstChar { it.uppercase() },
                                imageUrl = data.sprites.urlImagen ?: "",
                                types = data.types.joinToString(",") { it.type.name },
                                hp = formData.hp, attack = formData.atk, defense = formData.def,
                                specialAttack = formData.spa, specialDefense = formData.spd, speed = formData.spe
                            )
                        )
                    } catch (ce: Exception) { /* ignore cache errors */ }
                }
                onResult(formData)
            } catch (e: Exception) {
                Log.e(TAG, "Error buscando pokemon '$nameOrId': ${e.message}")
                onResult(null)
            }
        }
    }

    fun resetMovesState() {
        _pokemonMovesState.value = PokemonMovesState.Idle
    }
}
