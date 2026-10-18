package com.duoc.rotompedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.rotompedia.data.local.*
import com.duoc.rotompedia.data.remote.PokemonApi
import com.duoc.rotompedia.util.TypeEffectiveness
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamBuilderViewModel(
    private val equipoDao: EquipoDao,
    private val cachedPokemonDao: CachedPokemonDao? = null
) : ViewModel() {

    private val _currentUsername = MutableStateFlow("")
    val currentUsername: StateFlow<String> = _currentUsername

    fun setCurrentUser(username: String) {
        _currentUsername.value = username
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val equipos: StateFlow<List<EquipoConMiembros>> = _currentUsername
        .flatMapLatest { user ->
            val activeUser = user.ifBlank { "default_user" }
            equipoDao.obtenerEquiposPorUsuario(activeUser)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedEquipoId = MutableStateFlow<Long?>(null)
    val selectedEquipoId: StateFlow<Long?> = _selectedEquipoId

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEquipo: StateFlow<EquipoConMiembros?> = _selectedEquipoId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else equipoDao.observarEquipoPorId(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val teamAnalysis: StateFlow<TypeEffectiveness.TeamAnalysisResult> = selectedEquipo
        .map { equipo ->
            val members = equipo?.miembros ?: emptyList()
            TypeEffectiveness.analyzeTeam(members)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TypeEffectiveness.analyzeTeam(emptyList())
        )

    fun seleccionarEquipo(equipoId: Long?) {
        _selectedEquipoId.value = equipoId
    }

    fun crearEquipo(nombre: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUsername.value.ifBlank { "default_user" }
            val nuevo = EquipoPokemon(
                usuario = user,
                nombre = nombre.ifBlank { "Mi Equipo" }
            )
            val newId = equipoDao.insertarEquipo(nuevo)
            _selectedEquipoId.value = newId
        }
    }

    fun eliminarEquipo(equipoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            equipoDao.eliminarEquipoPorId(equipoId)
            if (_selectedEquipoId.value == equipoId) {
                _selectedEquipoId.value = null
            }
        }
    }

    fun actualizarNombreEquipo(equipoId: Long, nuevoNombre: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUsername.value.ifBlank { "default_user" }
            val existing = equipoDao.obtenerEquipoPorId(equipoId)
            if (existing != null) {
                equipoDao.actualizarEquipo(
                    existing.equipo.copy(nombre = nuevoNombre.ifBlank { "Mi Equipo" })
                )
            }
        }
    }

    fun agregarPokemonAlSlot(
        equipoId: Long,
        slot: Int,
        pokemonId: Int,
        nombre: String,
        spriteUrl: String,
        tipo1: String,
        tipo2: String?,
        hp: Int = 0,
        atk: Int = 0,
        def: Int = 0,
        spa: Int = 0,
        spd: Int = 0,
        spe: Int = 0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Eliminar cualquier miembro previo en ese slot
            equipoDao.eliminarMiembroPorSlot(equipoId, slot)

            val miembro = MiembroEquipo(
                equipoId = equipoId,
                pokemonId = pokemonId,
                nombre = nombre.replaceFirstChar { it.uppercase() },
                spriteUrl = spriteUrl,
                tipo1 = tipo1.lowercase(),
                tipo2 = tipo2?.lowercase(),
                slot = slot,
                hp = hp,
                attack = atk,
                defense = def,
                specialAttack = spa,
                specialDefense = spd,
                speed = spe
            )
            equipoDao.insertarMiembro(miembro)
        }
    }

    fun eliminarMiembroPorSlot(equipoId: Long, slot: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            equipoDao.eliminarMiembroPorSlot(equipoId, slot)
        }
    }
}
