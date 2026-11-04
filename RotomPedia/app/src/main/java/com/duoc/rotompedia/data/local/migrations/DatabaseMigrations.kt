package com.duoc.rotompedia.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    // Historical layouts verified at d77be19 (v4), d2232c9 (v5), 644ca48 (v6).
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val columns = linkedMapOf("nature" to "TEXT NOT NULL DEFAULT 'Fuerte'", "level" to "INTEGER NOT NULL DEFAULT 50")
            listOf("evHp", "evAtk", "evDef", "evSpa", "evSpd", "evSpe").forEach { columns[it] = "INTEGER NOT NULL DEFAULT 0" }
            listOf("ivHp", "ivAtk", "ivDef", "ivSpa", "ivSpd", "ivSpe").forEach { columns[it] = "INTEGER NOT NULL DEFAULT 31" }
            columns.forEach { (name, type) -> db.execSQL("ALTER TABLE miembros_equipo ADD COLUMN `$name` $type") }
        }
    }
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            (1..4).forEach { db.execSQL("ALTER TABLE miembros_equipo ADD COLUMN move$it TEXT") }
        }
    }
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS pokemon_species (speciesId INTEGER NOT NULL PRIMARY KEY, nationalNumber INTEGER NOT NULL, slug TEXT NOT NULL, generationId INTEGER NOT NULL, evolutionChainUrl TEXT)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pokemon_species_nationalNumber ON pokemon_species(nationalNumber)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pokemon_species_slug ON pokemon_species(slug)")
            db.execSQL("CREATE TABLE IF NOT EXISTS pokemon_varieties (pokemonId INTEGER NOT NULL PRIMARY KEY, speciesId INTEGER NOT NULL, slug TEXT NOT NULL, isDefault INTEGER NOT NULL, FOREIGN KEY(speciesId) REFERENCES pokemon_species(speciesId) ON UPDATE NO ACTION ON DELETE NO ACTION)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_pokemon_varieties_speciesId ON pokemon_varieties(speciesId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pokemon_varieties_slug ON pokemon_varieties(slug)")
            db.execSQL("CREATE TABLE IF NOT EXISTS pokemon_forms (pokemonFormId INTEGER NOT NULL PRIMARY KEY, pokemonId INTEGER NOT NULL, slug TEXT NOT NULL, isMega INTEGER NOT NULL, isBattleOnly INTEGER NOT NULL, FOREIGN KEY(pokemonId) REFERENCES pokemon_varieties(pokemonId) ON UPDATE NO ACTION ON DELETE NO ACTION)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_pokemon_forms_pokemonId ON pokemon_forms(pokemonId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pokemon_forms_slug ON pokemon_forms(slug)")
            db.execSQL("CREATE TABLE IF NOT EXISTS pokemon_names (resourceType TEXT NOT NULL, resourceId INTEGER NOT NULL, language TEXT NOT NULL, name TEXT NOT NULL, normalizedName TEXT NOT NULL, source TEXT NOT NULL, PRIMARY KEY(resourceType,resourceId,language,name,source))")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_pokemon_names_normalizedName ON pokemon_names(normalizedName)")
            db.execSQL("CREATE TABLE IF NOT EXISTS pokemon_index_state (id INTEGER NOT NULL PRIMARY KEY, formatVersion INTEGER NOT NULL, contentSha256 TEXT NOT NULL, metadataRevision TEXT NOT NULL, generatedAt TEXT NOT NULL, speciesCount INTEGER NOT NULL, varietyCount INTEGER NOT NULL, formsComplete INTEGER NOT NULL, importedAt INTEGER NOT NULL)")
        }
    }
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE pokemon_forms ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE pokemon_forms ADD COLUMN formOrder INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE TABLE IF NOT EXISTS pokemon_variety_types (pokemonId INTEGER NOT NULL, slot INTEGER NOT NULL, typeName TEXT NOT NULL, PRIMARY KEY(pokemonId,slot), FOREIGN KEY(pokemonId) REFERENCES pokemon_varieties(pokemonId) ON UPDATE NO ACTION ON DELETE NO ACTION)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_pokemon_variety_types_typeName ON pokemon_variety_types(typeName)")
        }
    }
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS item_index (itemId INTEGER NOT NULL PRIMARY KEY, slug TEXT NOT NULL, name TEXT NOT NULL, nameLanguage TEXT NOT NULL, category TEXT NOT NULL, attributes TEXT NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_item_index_slug ON item_index(slug)")
            db.execSQL("CREATE TABLE IF NOT EXISTS item_detail_cache (itemId INTEGER NOT NULL PRIMARY KEY, json TEXT NOT NULL, fetchedAt INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS item_index_state (id INTEGER NOT NULL PRIMARY KEY, sha256 TEXT NOT NULL)")
        }
    }
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS pokemon_learnset_cache (pokemonId INTEGER NOT NULL PRIMARY KEY, json TEXT NOT NULL, fetchedAt INTEGER NOT NULL)")
        }
    }
    val ALL = arrayOf(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
}
