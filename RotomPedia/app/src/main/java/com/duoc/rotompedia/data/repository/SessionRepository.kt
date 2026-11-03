package com.duoc.rotompedia.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SessionOwner(val username: String? = null, val revision: Long = 0, val restored: Boolean = false)

interface SessionStore {
    fun read(): String?
    fun write(username: String?)
}

interface SessionRepository {
    val session: StateFlow<SessionOwner>
    suspend fun restore()
    suspend fun signIn(username: String, remember: Boolean)
    suspend fun signOut()
    fun capture(): SessionOwner? = session.value.takeIf { it.restored && !it.username.isNullOrBlank() }
    suspend fun <T> withUser(owner: SessionOwner?, block: suspend (String) -> T): T?
}

class DefaultSessionRepository(
    private val store: SessionStore,
    private val accountExists: suspend (String) -> Boolean
) : SessionRepository {
    private val mutex = Mutex()
    private val state = MutableStateFlow(SessionOwner())
    override val session = state.asStateFlow()

    override suspend fun restore() = mutex.withLock {
        if (!state.value.restored) {
            val saved = store.read()?.takeIf { it.isNotBlank() && accountExists(it) }
            if (saved == null) store.write(null)
            state.value = SessionOwner(saved, state.value.revision + 1, true)
        }
    }

    override suspend fun signIn(username: String, remember: Boolean) = mutex.withLock {
        require(username.isNotBlank() && accountExists(username)) { "Usuario no válido" }
        // Also clear a previous remembered account when this login is not remembered.
        store.write(username.takeIf { remember })
        state.value = SessionOwner(username, state.value.revision + 1, true)
    }

    override suspend fun signOut() = mutex.withLock {
        store.write(null)
        state.value = SessionOwner(null, state.value.revision + 1, true)
    }

    override suspend fun <T> withUser(owner: SessionOwner?, block: suspend (String) -> T): T? = mutex.withLock {
        if (owner == null || owner != state.value || owner.username.isNullOrBlank()) null
        else block(owner.username)
    }
}
