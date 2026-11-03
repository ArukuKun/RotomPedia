package com.duoc.rotompedia

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.duoc.rotompedia.data.local.UsuarioDao
import com.duoc.rotompedia.viewmodel.AuthState
import com.duoc.rotompedia.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
// IMPORTAMOS LA CLASE MAESTRA DE MOCKITO
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var usuarioDao: UsuarioDao
    private lateinit var application: Application
    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // 1. CREACIÓN DE MOCKS (Sintaxis Clásica)
        // Esto le dice a Java explícitamente qué clase imitar
        usuarioDao = Mockito.mock(UsuarioDao::class.java)
        application = Mockito.mock(Application::class.java)

        // 2. Inyección
        val sessions = com.duoc.rotompedia.data.repository.DefaultSessionRepository(object : com.duoc.rotompedia.data.repository.SessionStore {
            override fun read(): String? = null
            override fun write(username: String?) = Unit
        }) { true }
        viewModel = AuthViewModel(application, usuarioDao, sessions)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registrarUsuario_camposVacios_retornaError() = runTest {
        // Acción
        viewModel.registrarUsuario("", "", "")
        advanceUntilIdle()

        // Verificación
        assertEquals(AuthState.EMPTY_FIELDS, viewModel.authState.value)
    }

    @Test
    fun registrarUsuario_usuarioExiste_retornaError() = runTest {
        // 3. DEFINIR COMPORTAMIENTO (Sintaxis Clásica)
        // "Cuando usuarioDao.obtener... sea llamado, entonces retorna un mock"
        Mockito.`when`(usuarioDao.obtenerUsuarioPorNombre("Ash"))
            .thenReturn(Mockito.mock(com.duoc.rotompedia.data.local.Usuario::class.java))

        // Acción
        viewModel.registrarUsuario("Ash", "ash@pueblo.paleta", "123456")
        advanceUntilIdle()

        // Verificación
        assertEquals(AuthState.USER_EXISTS, viewModel.authState.value)
    }
}
