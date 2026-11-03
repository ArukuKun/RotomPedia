package com.duoc.rotompedia.data.repository

import com.duoc.rotompedia.data.local.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class TeamRepository(private val dao: EquipoDao, private val sessions: SessionRepository) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val teams: Flow<List<EquipoConMiembros>> = sessions.session.flatMapLatest { owner ->
        owner.username?.takeIf { owner.restored && it.isNotBlank() }?.let {
            dao.obtenerEquiposPorUsuario(it).onStart { emit(emptyList()) }
        } ?: flowOf(emptyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(id: Long): Flow<EquipoConMiembros?> = sessions.session.flatMapLatest { owner ->
        owner.username?.takeIf { owner.restored && it.isNotBlank() }?.let {
            dao.observarEquipoPorId(id, it).onStart { emit(null) }
        } ?: flowOf(null)
    }

    suspend fun create(owner: SessionOwner?, name: String): Long? = sessions.withUser(owner) {
        dao.insertarEquipo(EquipoPokemon(usuario = it, nombre = name.ifBlank { "Mi Equipo" }))
    }
    suspend fun delete(owner: SessionOwner?, id: Long) = sessions.withUser(owner) { dao.eliminarEquipoPorId(id, it) }
    suspend fun rename(owner: SessionOwner?, id: Long, name: String) = sessions.withUser(owner) { dao.renombrar(id, it, name.ifBlank { "Mi Equipo" }) }
    suspend fun removeSlot(owner: SessionOwner?, id: Long, slot: Int) = sessions.withUser(owner) { dao.eliminarMiembroPorSlot(id, slot, it) }
    suspend fun saveMember(owner: SessionOwner?, value: MiembroEquipo, replaceSlot: Boolean = false): Boolean =
        sessions.withUser(owner) { dao.guardarMiembro(it, value, replaceSlot) } ?: false
}
