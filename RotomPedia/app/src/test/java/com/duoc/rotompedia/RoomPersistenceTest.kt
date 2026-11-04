package com.duoc.rotompedia

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.local.*
import com.duoc.rotompedia.data.local.migrations.DatabaseMigrations
import com.duoc.rotompedia.data.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class RoomPersistenceTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    private fun legacy(name: String, version: Int) {
        context.deleteDatabase(name)
        val json = JSONObject(java.io.File(System.getProperty("rotompedia.schemaDir"),
            "com.duoc.rotompedia.data.local.AppDatabase/${if (version >= 7) version else 6}.json").readText())
        val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name).callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    val entities = json.getJSONObject("database").getJSONArray("entities")
                    for (i in 0 until entities.length()) {
                        val entity = entities.getJSONObject(i)
                        val table = entity.getString("tableName")
                        var sql = entity.getString("createSql").replace("\${TABLE_NAME}", table)
                        if (table == "miembros_equipo") {
                            if (version <= 5) (1..4).forEach { sql = sql.replace(", `move$it` TEXT", "") }
                            if (version <= 4) {
                                sql = sql.replace(", `nature` TEXT NOT NULL", "").replace(", `level` INTEGER NOT NULL", "")
                                listOf("evHp", "evAtk", "evDef", "evSpa", "evSpd", "evSpe", "ivHp", "ivAtk", "ivDef", "ivSpa", "ivSpd", "ivSpe")
                                    .forEach { sql = sql.replace(", `$it` INTEGER NOT NULL", "") }
                            }
                        }
                        db.execSQL(sql)
                        val indices = entity.getJSONArray("indices")
                        for (j in 0 until indices.length()) db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                    }
                    db.execSQL("INSERT INTO usuarios VALUES(1,'ash','a@example.com','legacy-password')")
                    db.execSQL("INSERT INTO favoritos VALUES('ash',10051,'Mega Blaziken')")
                    db.execSQL("INSERT INTO favoritos VALUES('default_user',3,'Venusaur')")
                    if (version >= 9) {
                        db.execSQL("INSERT INTO item_index VALUES(168,'shuca-berry','Baya Acardo','es','type-protection','holdable-active')")
                        db.execSQL("INSERT INTO item_detail_cache VALUES(168,'legacy-cache',1234)")
                    }
                    db.execSQL("INSERT INTO equipos VALUES(42,'ash','Equipo antiguo',12345)")
                    val advanced = if (version >= 5) ",nature,level,evHp,evAtk,evDef,evSpa,evSpd,evSpe,ivHp,ivAtk,ivDef,ivSpa,ivSpd,ivSpe" else ""
                    val advancedValues = if (version >= 5) ",'Firme',73,4,252,0,0,0,252,30,31,29,28,27,26" else ""
                    val moves = if (version >= 6) ",move1,move2,move3,move4" else ""
                    val moveValues = if (version >= 6) ",'Movimiento antiguo','Protect','Surf','Unknown Move'" else ""
                    db.execSQL("INSERT INTO miembros_equipo(id,equipoId,pokemonId,nombre,spriteUrl,tipo1,tipo2,slot,hp,attack,defense,specialAttack,specialDefense,speed$advanced$moves) VALUES(21,42,10051,'Mega Blaziken','legacy-url','fire','fighting',0,80,160,80,130,80,100$advancedValues$moveValues)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = error("Not used")
            }).build())
        helper.writableDatabase
        helper.close()
    }

    private fun open(name: String) = Room.databaseBuilder(context, AppDatabase::class.java, name)
        .addMigrations(*DatabaseMigrations.ALL).allowMainThreadQueries().build()

    @Test fun `v9 upgrade preserves all personal moves and item cache while adding learnsets`() = runTest {
        val name = "migration9.db"
        legacy(name, 9)
        val db = open(name)
        try {
            db.openHelper.writableDatabase
            val member = db.equipoDao().obtenerEquipoPorId(42, "ash")!!.miembros.single()
            assertEquals(10051, member.pokemonId)
            assertEquals(73, member.level)
            assertEquals(252, member.evAtk)
            assertEquals(26, member.ivSpe)
            assertEquals("Unknown Move", member.move4)
            assertEquals("legacy-cache", db.itemDao().cached(168)!!.json)
            assertEquals("Baya Acardo", db.itemDao().item(168)!!.name)
            assertNotNull(db.favoritoDao().obtenerPorId("ash", 10051))
            assertNull(db.learnsetDao().cached(10104))
        } finally { db.close() }
    }

    @Test fun `v8 upgrade only adds item tables and preserves personal data exactly`() = runTest {
        val name = "migration8.db"
        legacy(name, 8)
        val db = open(name)
        try {
            db.openHelper.writableDatabase
            val member = db.equipoDao().obtenerEquipoPorId(42, "ash")!!.miembros.single()
            assertEquals(10051, member.pokemonId)
            assertEquals("Mega Blaziken", member.nombre)
            assertEquals(73, member.level)
            assertEquals(252, member.evAtk)
            assertEquals(26, member.ivSpe)
            assertEquals("Unknown Move", member.move4)
            assertNotNull(db.favoritoDao().obtenerPorId("ash", 10051))
            assertEquals("legacy-password", db.usuarioDao().obtenerUsuarioPorNombre("ash")!!.contrasena)
            assertTrue(db.itemDao().items().isEmpty())
            assertNull(db.itemDao().cached(168))
        } finally { db.close() }
    }

    @Test fun `v7 upgrade adds explorer metadata without rewriting saved identities`() = runTest {
        val name = "migration7.db"
        legacy(name, 7)
        val db = open(name)
        try {
            db.openHelper.writableDatabase
            val member = db.equipoDao().obtenerEquipoPorId(42, "ash")!!.miembros.single()
            assertEquals(10051, member.pokemonId)
            assertEquals("Mega Blaziken", member.nombre)
            assertEquals(73, member.level)
            assertEquals("Unknown Move", member.move4)
            assertNotNull(db.favoritoDao().obtenerPorId("ash", 10051))
            assertEquals("legacy-password", db.usuarioDao().obtenerUsuarioPorNombre("ash")!!.contrasena)
            assertTrue(db.pokemonIndexDao().observeExplorer().first().isEmpty())
        } finally { db.close() }
    }

    @Test fun `v6 upgrade preserves personal data and contradictory legacy IDs exactly`() = runTest {
        val name = "migration6.db"
        legacy(name, 6)
        val db = open(name)
        try {
            db.openHelper.writableDatabase
            val team = db.equipoDao().obtenerEquipoPorId(42, "ash")!!
            val member = team.miembros.single()
            assertEquals(10051, member.pokemonId)
            assertEquals("Mega Blaziken", member.nombre)
            assertEquals(73, member.level)
            assertEquals(252, member.evAtk)
            assertEquals(26, member.ivSpe)
            assertEquals("Movimiento antiguo", member.move1)
            assertEquals("Unknown Move", member.move4)
            assertEquals("legacy-password", db.usuarioDao().obtenerUsuarioPorNombre("ash")!!.contrasena)
            assertNotNull(db.favoritoDao().obtenerPorId("default_user", 3))
            assertNull(db.pokemonIndexDao().state())
        } finally { db.close() }
    }

    @Test fun `verified v4 and v5 historical paths preserve members`() = runTest {
        for (version in listOf(4, 5)) {
            val name = "migration$version.db"
            legacy(name, version)
            val db = open(name)
            try {
                db.openHelper.writableDatabase
                val member = db.equipoDao().obtenerEquipoPorId(42, "ash")!!.miembros.single()
                assertEquals(10051, member.pokemonId)
                assertEquals(if (version == 4) "Fuerte" else "Firme", member.nature)
                assertEquals(if (version == 4) 31 else 26, member.ivSpe)
                assertNull(member.move1)
            } finally { db.close() }
        }
    }

    @Test fun `unsupported historical version is not wiped`() {
        val name = "unsupported.db"
        legacy(name, 3)
        val db = open(name)
        try {
            try { db.openHelper.writableDatabase; fail("Missing migration must fail safely") }
            catch (_: IllegalStateException) { /* no destructive fallback */ }
        } finally { db.close() }
        val raw = android.database.sqlite.SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY)
        raw.rawQuery("SELECT nombre FROM equipos WHERE id=42", null).use {
            assertTrue(it.moveToFirst()); assertEquals("Equipo antiguo", it.getString(0))
        }
        raw.close()
    }

    @Test fun `owner checks prevent foreign team reads writes and member id spoofing`() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val sessions = DefaultSessionRepository(object : SessionStore {
            override fun read(): String? = null
            override fun write(username: String?) = Unit
        }) { true }
        val teams = TeamRepository(db.equipoDao(), sessions)
        val favorites = FavoritesRepository(db.favoritoDao(), sessions)
        try {
            sessions.signIn("ash", false)
            val ash = sessions.capture()
            val id = teams.create(ash, "Ash")!!
            val member = MiembroEquipo(equipoId = id, pokemonId = 3, nombre = "Venusaur", spriteUrl = "", tipo1 = "grass", slot = 0)
            assertTrue(teams.saveMember(ash, member, true))
            favorites.toggle(ash, 3, "Venusaur")
            sessions.signIn("misty", false)
            val misty = sessions.capture()
            assertNull(teams.observe(id).first { it == null })
            assertFalse(teams.saveMember(misty, member, true))
            teams.rename(misty, id, "Hacked"); teams.delete(misty, id); teams.removeSlot(misty, id, 0)
            assertNull(teams.create(ash, "Delayed Ash action"))
            val other = teams.create(misty, "Misty")!!
            assertEquals(listOf(other), teams.teams.drop(1).first().map { it.equipo.id })
            val original = db.equipoDao().obtenerEquipoPorId(id, "ash")!!.miembros.single()
            assertFalse(teams.saveMember(misty, original.copy(equipoId = other)))
            assertEquals("Ash", db.equipoDao().obtenerEquipoPorId(id, "ash")!!.equipo.nombre)
            assertEquals(1, db.equipoDao().obtenerEquipoPorId(id, "ash")!!.miembros.size)
            assertNull(db.equipoDao().obtenerEquipoPorId(id, "misty"))
            assertEquals(emptySet<Int>(), favorites.ids.first())
            sessions.signOut()
            assertNull(teams.create(sessions.capture(), "No owner"))
            assertNotNull(db.favoritoDao().obtenerPorId("ash", 3))
        } finally { db.close() }
    }

    @Test fun `an active favorites collector clears on account switch and logout`() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val sessions = DefaultSessionRepository(object : SessionStore {
            override fun read(): String? = null
            override fun write(username: String?) = Unit
        }) { true }
        val favorites = FavoritesRepository(db.favoritoDao(), sessions)
        val events = Channel<Set<Int>>(Channel.UNLIMITED)
        val collection = backgroundScope.launch { favorites.ids.collect { events.send(it) } }
        try {
            db.favoritoDao().agregarFavorito(PokemonFavorito("ash", 3, "Venusaur"))
            db.favoritoDao().agregarFavorito(PokemonFavorito("misty", 25, "Pikachu"))
            db.favoritoDao().agregarFavorito(PokemonFavorito("default_user", 999, "Historical"))
            sessions.signIn("ash", false)
            var ash = events.receive()
            while (ash.isEmpty()) ash = events.receive()
            assertEquals(setOf(3), ash)
            sessions.signIn("misty", false)
            assertEquals(emptySet<Int>(), events.receive())
            assertEquals(setOf(25), events.receive())
            sessions.signOut()
            assertEquals(emptySet<Int>(), events.receive())
            assertNotNull(db.favoritoDao().obtenerPorId("default_user", 999))
        } finally { collection.cancel(); db.close() }
    }
}
