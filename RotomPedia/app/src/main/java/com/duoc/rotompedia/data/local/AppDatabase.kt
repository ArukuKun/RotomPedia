package com.duoc.rotompedia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.duoc.rotompedia.data.local.index.*
import com.duoc.rotompedia.data.local.migrations.DatabaseMigrations

@Database(
    entities = [
        Usuario::class,
        PokemonFavorito::class,
        CachedPokemon::class,
        EquipoPokemon::class,
        MiembroEquipo::class,
        IndexedSpecies::class, IndexedVariety::class, IndexedForm::class,
        IndexedName::class, PokemonIndexState::class, IndexedVarietyType::class
    ],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun cachedPokemonDao(): CachedPokemonDao
    abstract fun equipoDao(): EquipoDao
    abstract fun pokemonIndexDao(): PokemonIndexDao



    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rotompedia_database"
                )
                    .addMigrations(*DatabaseMigrations.ALL)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
