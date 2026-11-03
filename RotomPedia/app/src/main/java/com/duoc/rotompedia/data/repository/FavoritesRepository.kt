package com.duoc.rotompedia.data.repository

import com.duoc.rotompedia.data.local.FavoritoDao
import com.duoc.rotompedia.data.local.PokemonFavorito
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class FavoritesRepository(private val dao: FavoritoDao, private val sessions: SessionRepository) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val ids = sessions.session.flatMapLatest { owner ->
        owner.username?.takeIf { owner.restored && it.isNotBlank() }?.let { user ->
            dao.obtenerTodosLosIdsPorUsuario(user).map { it.toSet() }.onStart { emit(emptySet()) }
        } ?: flowOf(emptySet())
    }

    suspend fun toggle(owner: SessionOwner?, id: Int, name: String) = sessions.withUser(owner) { user ->
        require(id > 0)
        if (dao.obtenerPorId(user, id) != null) dao.eliminarFavorito(user, id)
        else dao.agregarFavorito(PokemonFavorito(user, id, name))
    }
}
