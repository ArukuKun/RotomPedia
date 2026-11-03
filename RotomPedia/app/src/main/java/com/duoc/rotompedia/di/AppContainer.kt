package com.duoc.rotompedia.di

import android.content.Context
import com.duoc.rotompedia.data.index.PokemonIndexImporter
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.remote.PokemonApi
import com.duoc.rotompedia.data.repository.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppContainer private constructor(context: Context) {
    private val application = context.applicationContext
    val database = AppDatabase.getDatabase(application)
    val sessions: SessionRepository = DefaultSessionRepository(PreferencesSessionStore(application)) {
        database.usuarioDao().obtenerUsuarioPorNombre(it) != null
    }
    val favorites = FavoritesRepository(database.favoritoDao(), sessions)
    val teams = TeamRepository(database.equipoDao(), sessions)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val pokemon: PokemonRepository = DefaultPokemonRepository(database, PokemonApi.service,
        PokemonIndexImporter(database, moshi)) {
        withContext(Dispatchers.IO) { application.assets.open("pokemon-index.json").bufferedReader().use { it.readText() } }
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val indexFailure = MutableStateFlow<String?>(null)
    val indexImportError = indexFailure.asStateFlow()

    init {
        scope.launch {
            try { pokemon.ensureIndex() }
            catch (e: Exception) {
                if (e is CancellationException) throw e
                // A bad snapshot must never prevent login or destroy the user's database.
                indexFailure.value = "No se pudo importar el índice nacional: ${e.javaClass.simpleName}"
            }
        }
    }
    companion object {
        @Volatile private var instance: AppContainer? = null
        fun get(context: Context): AppContainer = instance ?: synchronized(this) {
            instance ?: AppContainer(context).also { instance = it }
        }
    }
}
