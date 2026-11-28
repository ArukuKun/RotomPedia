package com.duoc.rotompedia

import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.repository.*
import com.duoc.rotompedia.viewmodel.TeamBuilderViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class Rc4TeamSelectionTest {
    @Test fun `deleting selected team returns to selector preserves others and last deletion leaves empty`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
        val sessions = DefaultSessionRepository(object : SessionStore {
            override fun read(): String? = null
            override fun write(username: String?) = Unit
        }) { true }
        val repository = TeamRepository(db.equipoDao(), sessions)
        val store = ViewModelStore()
        try {
            sessions.signIn("rc4-ash", false)
            val owner = sessions.capture()
            val first = repository.create(owner, "Mismo nombre")!!
            val second = repository.create(owner, "Mismo nombre")!!
            val model = TeamBuilderViewModel(repository, sessions)
            store.put("rc4", model)
            withTimeout(10_000) { model.equipos.first { it.size == 2 } }
            model.seleccionarEquipo(first)
            withTimeout(10_000) { model.selectedEquipoId.first { it == first } }
            model.eliminarEquipo(first)
            withTimeout(10_000) { model.equipos.first { it.size == 1 }; model.selectedEquipoId.first { it == null } }
            assertEquals(second, model.equipos.value.single().equipo.id)
            assertNotNull(db.equipoDao().obtenerEquipoPorId(second, "rc4-ash"))
            model.seleccionarEquipo(second)
            withTimeout(10_000) { model.selectedEquipoId.first { it == second } }
            model.eliminarEquipo(second)
            withTimeout(10_000) { model.equipos.first { it.isEmpty() }; model.selectedEquipoId.first { it == null } }
            assertNull(model.selectedEquipoId.value)
        } finally {
            store.clear()
            db.close()
            Dispatchers.resetMain()
        }
    }
}
