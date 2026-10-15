// PokemonViewModel.kt
package com.duoc.rotompedia.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.rotompedia.data.model.ChainLink
import com.duoc.rotompedia.data.model.EvolutionDetail
import com.duoc.rotompedia.data.model.PokemonData
import com.duoc.rotompedia.data.model.PokemonListItem
import com.duoc.rotompedia.data.model.Species
import com.duoc.rotompedia.data.model.TypesSlot
import com.duoc.rotompedia.data.remote.PokemonApi
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.data.local.FavoritoDao
import com.duoc.rotompedia.data.local.PokemonFavorito
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

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

class PokemonViewModel(
    private val favoritoDao: FavoritoDao
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
    private val _pokemonTypesCache = mutableMapOf<Int, List<String>>()

    // Filtros
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedTypeFilter = MutableStateFlow("all")
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter

    private val _selectedRegionFilter = MutableStateFlow("Todos")
    val selectedRegionFilter: StateFlow<String> = _selectedRegionFilter

    // ✅ ACTUALIZADO: Nueva implementación de favoritos
    val favoritePokemonIds: StateFlow<Set<Int>> = favoritoDao.obtenerTodosLosIds()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
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

            // Filtro de búsqueda por nombre o ID
            val matchesSearch = if (searchQueryValue.isBlank()) {
                true
            } else {
                item.name.contains(searchQueryValue, ignoreCase = true) ||
                        id.toString().contains(searchQueryValue)
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
                    PokemonApi.service.getPokemonList(limit = 1000, offset = 0)
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
                val errorMessage = "Error al cargar la lista de Pokémon: ${e.message}"
                _pokedexState.value = PokedexState.Error(errorMessage)
                Log.e(TAG, errorMessage, e)
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
                PokemonApi.service.getPokemon(pokemonId)
            }
            pokemonData.types.map { it.type.name }
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar tipos para Pokémon $pokemonId: ${e.message}")
            emptyList()
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

    // ✅ ACTUALIZADO: Nuevas funciones de favoritos
    fun toggleFavorite(pokemonId: Int, pokemonName: String, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isCurrentlyFavorite) {
                // Eliminar de favoritos
                favoritoDao.eliminarFavorito(pokemonId)
                Log.d(TAG, "Pokémon $pokemonName ($pokemonId) eliminado de favoritos.")
            } else {
                // Agregar a favoritos
                val favorito = PokemonFavorito(
                    pokemonId = pokemonId,
                    nombre = pokemonName.replaceFirstChar { it.uppercase() }
                )
                favoritoDao.agregarFavorito(favorito)
                Log.d(TAG, "Pokémon $pokemonName ($pokemonId) agregado a favoritos.")
            }
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
        viewModelScope.launch {
            _pokemonDetailState.value = PokemonDetailState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemon(pokemonId.toInt())
                }
                _pokemonDetailState.value = PokemonDetailState.Success(response)
                Log.d(TAG, "Detalles de ${response.name} cargados exitosamente.")

                _pokemonTypesCache[response.id] = response.types.map { it.type.name }

            } catch (e: Exception) {
                val errorMessage = "Error al cargar detalles: ${e.message}"
                _pokemonDetailState.value = PokemonDetailState.Error(errorMessage)
                Log.e(TAG, errorMessage, e)
            }
        }
    }

    fun loadEvolutionChain(pokemonId: String) {
        viewModelScope.launch {
            _evolutionState.value = EvolutionState.Loading
            try {
                val speciesResponse = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemonSpecies(pokemonId.toInt())
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
        details.timeOfDay?.let {
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

        details.timeOfDay?.let {
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
        viewModelScope.launch {
            _typeRelationsState.value = TypeRelationsState.Loading
            try {
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
                val errorMessage = "Error al cargar relaciones de tipo: ${e.message}"
                _typeRelationsState.value = TypeRelationsState.Error(errorMessage)
                Log.e(TAG, errorMessage, e)
            }
        }
    }
}