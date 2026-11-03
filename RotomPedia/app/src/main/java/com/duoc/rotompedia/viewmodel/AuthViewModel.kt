package com.duoc.rotompedia.viewmodel

import android.app.Application
import com.duoc.rotompedia.data.repository.SessionRepository
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.rotompedia.data.local.Usuario
import com.duoc.rotompedia.data.local.UsuarioDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.Job

enum class AuthState {
    IDLE, LOADING, REGISTRATION_SUCCESS, USER_EXISTS, EMAIL_EXISTS, INVALID_CREDENTIALS, EMPTY_FIELDS
}

sealed class UserSession {
    data object Undetermined : UserSession()
    data object Inactive : UserSession()
    data class Active(val username: String) : UserSession()
}

// CAMBIO AQUÍ: Ahora recibe 'usuarioDao' en el constructor
class AuthViewModel(
    application: Application,
    private val usuarioDao: UsuarioDao,
    private val sessions: SessionRepository
) : AndroidViewModel(application) {

    val userSession = sessions.session.map { owner ->
        if (!owner.restored) UserSession.Undetermined
        else owner.username?.let { UserSession.Active(it) } ?: UserSession.Inactive
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UserSession.Undetermined)
    private var loginJob: Job? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.IDLE)
    val authState = _authState.asStateFlow()

    companion object {
        const val LOGGED_IN_USER_KEY = "logged_in_user"
    }

    // El resto de tu lógica sigue IGUAL...
    fun checkSession() {
        viewModelScope.launch { sessions.restore() }
    }

    fun iniciarSesion(nombreUsuario: String, contrasena: String, recordar: Boolean) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            val usuario = usuarioDao.obtenerUsuarioPorNombre(nombreUsuario)
            if (usuario != null && usuario.contrasena == contrasena) {
                sessions.signIn(nombreUsuario, recordar)
            } else {
                _authState.value = AuthState.INVALID_CREDENTIALS
            }
        }
    }

    fun cerrarSesion() {
        loginJob?.cancel()
        viewModelScope.launch {
            sessions.signOut()
            _authState.value = AuthState.IDLE
        }
    }

    fun registrarUsuario(nombre: String, correo: String, contrasena: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING

            if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                _authState.value = AuthState.EMPTY_FIELDS
                return@launch
            }
            if (usuarioDao.obtenerUsuarioPorNombre(nombre) != null) {
                _authState.value = AuthState.USER_EXISTS
                return@launch
            }
            if (usuarioDao.obtenerUsuarioPorCorreo(correo) != null) {
                _authState.value = AuthState.EMAIL_EXISTS
                return@launch
            }

            val nuevoUsuario = Usuario(
                nombreUsuario = nombre,
                correo = correo,
                contrasena = contrasena
            )
            usuarioDao.insertarUsuario(nuevoUsuario)

            _authState.value = AuthState.REGISTRATION_SUCCESS
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.IDLE
    }
}
