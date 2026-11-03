package com.duoc.rotompedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.rotompedia.data.local.*
import com.duoc.rotompedia.data.repository.*
import com.duoc.rotompedia.util.TypeEffectiveness
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TeamBuilderViewModel(
    private val teams: TeamRepository,
    private val sessions: SessionRepository
) : ViewModel() {
    val currentUsername = sessions.session.map { it.username.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val equipos = teams.teams.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val selection = MutableStateFlow<Pair<SessionOwner, Long>?>(null)
    val selectedEquipoId = combine(selection, sessions.session) { selected, owner ->
        selected?.takeIf { it.first == owner }?.second
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEquipo: StateFlow<EquipoConMiembros?> = combine(selection, sessions.session) { selected, owner ->
        selected?.takeIf { it.first == owner }?.second
    }.flatMapLatest { id ->
        if (id == null) flowOf(null) else teams.observe(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val teamAnalysis = selectedEquipo.map { TypeEffectiveness.analyzeTeam(it?.miembros.orEmpty()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TypeEffectiveness.analyzeTeam(emptyList()))

    fun seleccionarEquipo(equipoId: Long?) {
        selection.value = equipoId?.let { id -> sessions.capture()?.let { it to id } }
    }
    fun crearEquipo(nombre: String) {
        val owner = sessions.capture()
        viewModelScope.launch(Dispatchers.IO) {
            val id = teams.create(owner, nombre)
            if (id != null && owner != null && sessions.capture() == owner) selection.value = owner to id
        }
    }
    fun eliminarEquipo(equipoId: Long) {
        val owner = sessions.capture()
        viewModelScope.launch(Dispatchers.IO) {
            teams.delete(owner, equipoId)
            if (selection.value == owner?.let { it to equipoId }) selection.value = null
        }
    }
    fun actualizarNombreEquipo(equipoId: Long, nuevoNombre: String) {
        val owner = sessions.capture()
        viewModelScope.launch(Dispatchers.IO) { teams.rename(owner, equipoId, nuevoNombre) }
    }
    fun agregarPokemonAlSlot(equipoId: Long, slot: Int, pokemonId: Int, nombre: String,
        spriteUrl: String, tipo1: String, tipo2: String?, hp: Int = 0, atk: Int = 0,
        def: Int = 0, spa: Int = 0, spd: Int = 0, spe: Int = 0) {
        val owner = sessions.capture()
        val value = MiembroEquipo(equipoId = equipoId, pokemonId = pokemonId, nombre = nombre,
            spriteUrl = spriteUrl, tipo1 = tipo1.lowercase(), tipo2 = tipo2?.lowercase(), slot = slot,
            hp = hp, attack = atk, defense = def, specialAttack = spa, specialDefense = spd, speed = spe)
        viewModelScope.launch(Dispatchers.IO) { teams.saveMember(owner, value, replaceSlot = true) }
    }
    fun eliminarMiembroPorSlot(equipoId: Long, slot: Int) {
        val owner = sessions.capture()
        viewModelScope.launch(Dispatchers.IO) { teams.removeSlot(owner, equipoId, slot) }
    }
    fun actualizarMiembro(miembro: MiembroEquipo) {
        val owner = sessions.capture()
        viewModelScope.launch(Dispatchers.IO) { teams.saveMember(owner, miembro) }
    }
}
