package com.duoc.rotompedia

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.*
import com.duoc.rotompedia.data.index.*
import com.duoc.rotompedia.ui.screens.speciesEvolutionReference
import com.duoc.rotompedia.viewmodel.EvolutionStage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class HisuiEvolutionsTest {
    private fun edge(to: String) = HisuiEvolutions.rules.single { it.to == to }

    @Test fun `all contextual endpoints resolve exact existing varieties and cover every evolved Hisui form`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val envelope = moshi.adapter(IndexEnvelope::class.java).fromJson(context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() })!!
        val index = moshi.adapter(IndexPayload::class.java).fromJson(envelope.payloadJson)!!
        val bySlug = index.varieties.associateBy { it.slug }
        assertEquals(22, HisuiEvolutions.rules.size)
        assertEquals(22, HisuiEvolutions.rules.map { it.from to it.to }.distinct().size)
        HisuiEvolutions.rules.forEach {
            assertNotNull(it.from, bySlug[it.from])
            assertNotNull(it.to, bySlug[it.to])
            assertTrue(it.sources.all { url -> url.startsWith("https://") })
        }
        val hisuiEvolved = bySlug.keys.filter { it.endsWith("-hisui") } - setOf("growlithe-hisui", "voltorb-hisui", "qwilfish-hisui", "sneasel-hisui", "zorua-hisui")
        assertTrue(HisuiEvolutions.rules.map { it.to }.containsAll(hisuiEvolved))
        assertEquals(10234, bySlug[edge("overqwil").from]!!.pokemonId)
        assertEquals(10247, bySlug[edge("basculegion-female").from]!!.pokemonId)
    }

    @Test fun `Overqwil changes from strong style uses to known move level up without Johto Qwilfish`() {
        val rule = edge("overqwil")
        assertTrue(rule.arceus.text.contains("20 veces en estilo fuerte"))
        assertEquals("Subir de nivel con Mil Púas Tóxicas aprendido.", rule.scarletViolet.text)
        assertTrue(HisuiEvolutions.forVariety("qwilfish").isEmpty())
        assertTrue(HisuiEvolutions.forVariety("overqwil").contains(rule))
    }

    @Test fun `Wyrdeer Kleavor and ordinary Ursaluna cannot evolve locally in Scarlet Violet`() {
        listOf("wyrdeer", "kleavor", "ursaluna").forEach {
            assertFalse(edge(it).scarletViolet.canEvolve)
            assertTrue(edge(it).scarletViolet.text.contains("HOME"))
            assertTrue(edge(it).scarletViolet.items.isEmpty())
        }
        assertTrue(edge("wyrdeer").arceus.text.contains("20 veces en estilo rápido"))
        assertEquals(listOf("black-augurite"), edge("kleavor").arceus.items)
        assertTrue(edge("ursaluna").arceus.text.contains("luna llena"))
        assertTrue(HisuiEvolutions.forVariety("ursaluna-bloodmoon").isEmpty())
    }

    @Test fun `Sneasler distinguishes using claw from holding it and Basculegion retains both sexes`() {
        assertTrue(edge("sneasler").arceus.text.startsWith("Usar"))
        assertTrue(edge("sneasler").scarletViolet.text.contains("equipada"))
        val routes = HisuiEvolutions.forVariety("basculin-white-striped")
        assertEquals(setOf("basculegion-male", "basculegion-female"), routes.map { it.to }.toSet())
        routes.forEach {
            assertTrue(it.arceus.text.contains("294 PS"))
            assertTrue(it.arceus.text.contains("sin debilitarse"))
            assertFalse(it.arceus.text.contains("subir de nivel"))
            assertTrue(it.scarletViolet.text.contains("subir de nivel"))
        }
        assertTrue(HisuiEvolutions.forVariety("basculin-red-striped").isEmpty())
        assertTrue(HisuiEvolutions.forVariety("basculin-blue-striped").isEmpty())
    }

    @Test fun `regional location routes and starter thresholds are edition specific`() {
        listOf("typhlosion-hisui", "samurott-hisui", "decidueye-hisui", "lilligant-hisui", "braviary-hisui", "avalugg-hisui", "sliggoo-hisui").forEach {
            assertFalse(edge(it).scarletViolet.canEvolve)
            assertTrue(edge(it).arceus.canEvolve)
        }
        assertTrue(edge("quilava").arceus.text.contains("17"))
        assertTrue(edge("quilava").scarletViolet.text.contains("14"))
        assertTrue(edge("decidueye-hisui").arceus.text.contains("36"))
        assertEquals(listOf("leaf-stone"), edge("electrode-hisui").arceus.items)
        assertTrue(edge("goodra-hisui").scarletViolet.text.contains("lluvia natural"))
        assertFalse(edge("goodra-hisui").arceus.text.contains("niebla"))
    }

    @Test fun `ordinary regional counterparts cannot inherit regional species evolution branches`() {
        fun stage(id: String) = EvolutionStage(id, id, "test")
        assertEquals(listOf("211"), speciesEvolutionReference(listOf(stage("211"), stage("904")), "qwilfish").map { it.id })
        assertEquals(listOf("215", "461"), speciesEvolutionReference(listOf(stage("215"), stage("461"), stage("903")), "sneasel").map { it.id })
        val ordinary = listOf(stage("1"), stage("2"), stage("3"))
        assertEquals(ordinary, speciesEvolutionReference(ordinary, "bulbasaur"))
    }
}
