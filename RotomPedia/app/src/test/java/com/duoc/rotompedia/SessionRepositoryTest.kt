package com.duoc.rotompedia

import com.duoc.rotompedia.data.repository.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SessionRepositoryTest {
    private class Store(var saved: String? = null) : SessionStore {
        override fun read() = saved
        override fun write(username: String?) { saved = username }
    }
    @Test fun `unknown remembered account does not restore a session`() = runTest {
        val store = Store("missing")
        val repo = DefaultSessionRepository(store) { it == "ash" }
        assertNull(repo.capture())
        repo.restore()
        assertTrue(repo.session.value.restored)
        assertNull(repo.capture())
        assertNull(store.saved)
    }
    @Test fun `not remembered login clears previous remembered user`() = runTest {
        val store = Store("ash")
        val repo = DefaultSessionRepository(store) { true }
        repo.signIn("misty", false)
        assertEquals("misty", repo.capture()?.username)
        assertNull(store.saved)
    }
    @Test fun `stale tokens are rejected even after logging into same user again`() = runTest {
        val repo = DefaultSessionRepository(Store()) { true }
        repo.signIn("ash", false)
        val first = repo.capture()
        repo.signOut()
        repo.signIn("ash", false)
        assertNull(repo.withUser(first) { error("Old action executed") })
        assertEquals("ash", repo.withUser(repo.capture()) { it })
    }
    @Test fun `session switch cannot reassign an in flight write`() = runTest {
        val repo = DefaultSessionRepository(Store()) { true }
        repo.signIn("ash", false)
        val token = repo.capture()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val write = async { repo.withUser(token) { entered.complete(Unit); release.await(); it } }
        entered.await()
        val switch = async { repo.signIn("misty", false) }
        runCurrent()
        assertFalse(switch.isCompleted)
        release.complete(Unit)
        assertEquals("ash", write.await())
        switch.await()
        assertEquals("misty", repo.capture()?.username)
    }
}
