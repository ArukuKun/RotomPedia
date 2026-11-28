package com.duoc.rotompedia

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import com.duoc.rotompedia.di.AppContainer
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

/** Full production NavHost on the QA emulator. Requires a remembered rc4_qa_* account.
 * Never run destructive acceptance steps on a personal account. No account/database is cleared.
 */
@RunWith(AndroidJUnit4::class)
class Rc4AppAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun fixture() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.waitUntil(20_000) { AppContainer.get(context).sessions.session.value.restored }
        assertTrue("Sign in to a dedicated remembered rc4_qa_* account before this acceptance test",
            AppContainer.get(context).sessions.session.value.username?.startsWith("rc4_qa_") == true)
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("rotom_home_destinations").fetchSemanticsNodes().isNotEmpty() }
    }

    private fun tab(label: String) = compose.onNode(hasText(label) and hasAnyAncestor(hasTestTag("rotom_global_nav")))

    private fun capture(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "rc4-evidence").apply { mkdirs() }
        val image = instrumentation.uiAutomation.takeScreenshot()
        val variant = InstrumentationRegistry.getArguments().getString("evidenceVariant", "portrait")
        File(directory, "$variant-$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }

    @Test fun productionPokedexFiltersBackAndTabState() {
        fixture()
        compose.onNode(hasText("Pokédex") and hasAnyAncestor(hasTestTag("rotom_home_destinations"))).performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("pokemon_1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Filtrar Pokémon").performClick()
        compose.onNodeWithText("Tipo", substring = false).performScrollTo().performClick()
        compose.onNode(hasText("Fuego", substring = false) and isSelectable()).performScrollTo().performClick()
        // System Back must discard this draft, not navigate away or leave an invisible modal.
        pressBack()
        compose.onNodeWithText("Filtros de Pokémon").assertDoesNotExist()
        compose.onNodeWithTag("pokemon_1").assertExists()
        compose.onNodeWithContentDescription("Filtrar Pokémon").performClick()
        compose.onNodeWithText("Tipo", substring = false).performScrollTo().performClick()
        compose.onNode(hasText("Fuego", substring = false) and isSelectable()).performScrollTo().performClick()
        compose.onNodeWithText("Confirmar selección").performClick()
        capture("app-filters-draft")
        compose.onNodeWithText("Aplicar y cerrar").performClick()
        compose.onNodeWithText("Nombre, forma o número nacional").performTextInput("char")
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("pokemon_4").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Cuadrícula", substring = false).performClick()
        tab("Inicio").performClick()
        compose.onNodeWithText("Accesos a tus herramientas").assertIsDisplayed()
        tab("Pokédex").performClick()
        compose.onNodeWithText("char", substring = false).assertExists()
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("pokedex-grid").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("pokedex-grid").assertExists()
        compose.onNodeWithTag("pokemon_4").assertExists()
        capture("app-pokedex-restored")
        compose.activityRule.scenario.recreate()
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("pokedex-grid").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("char", substring = false).assertExists()
        compose.onNodeWithTag("pokemon_4").performClick()
        tab("Pokédex").assertIsSelected()
        pressBack()
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("pokedex-grid").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("pokedex-grid").assertExists()
        compose.onNodeWithContentDescription("Filtrar Pokémon").performClick()
        compose.onNodeWithText("Restablecer", substring = false).performClick()
        compose.onNodeWithText("Aplicar y cerrar").performClick()
        compose.onNodeWithText("Lista", substring = false).performClick()
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText("1025 resultados · 1025 especies", substring = false).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("pokedex-list").performScrollToIndex(120)
        val before = compose.onNodeWithTag("pokemon_121").getUnclippedBoundsInRoot().top.value
        tab("Equipos").performClick()
        tab("Pokédex").performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("pokemon_121").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(before, compose.onNodeWithTag("pokemon_121").getUnclippedBoundsInRoot().top.value, 1f)
        tab("Inicio").performClick()
        compose.onNodeWithText("Accesos a tus herramientas").assertIsDisplayed()
    }

    @Test fun productionDeleteReturnsToTeamSelector() {
        fixture()
        tab("Equipos").performClick()
        compose.onNodeWithContentDescription("Seleccionar equipo").performClick()
        compose.onNodeWithText("Nuevo Equipo", substring = false).performClick()
        val name = "RC4 QA eliminar ${System.currentTimeMillis()}"
        compose.onNodeWithText("Nombre del equipo").performTextInput(name)
        compose.onNodeWithText("Crear", substring = false).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(name, substring = false).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Renombrar equipo").performClick()
        compose.onNodeWithText("Nombre del equipo", substring = false).performTextClearance()
        compose.onNodeWithText("Guardar", substring = false).assertIsNotEnabled()
        val renamed = "$name renombrado"
        compose.onNodeWithText("Nombre del equipo", substring = false).performTextInput(renamed)
        compose.onNodeWithText("Guardar", substring = false).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(renamed, substring = false).fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(renamed, substring = false).assertExists()
        compose.onNodeWithContentDescription("Eliminar Equipo").performClick()
        compose.onNodeWithText("¿Eliminar «$renamed»? Esta acción no se puede deshacer.").assertExists()
        compose.onNodeWithText("Cancelar", substring = false).performClick()
        compose.onNodeWithContentDescription("Eliminar Equipo").performClick()
        compose.onNodeWithText("Eliminar", substring = false).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Constructor de Equipos", substring = false).fetchSemanticsNodes().isNotEmpty() }
        tab("Equipos").assertIsSelected()
        compose.onNodeWithText("RC3_Preservar", substring = false).assertExists()
        capture("app-selector-after-delete")
    }

    @Test fun productionMapRemembersRegionAndDetailOrigin() {
        fixture()
        tab("Mapas").performClick()
        compose.waitForIdle()
        if (compose.onAllNodesWithText("Elige una región", substring = false).fetchSemanticsNodes().isEmpty()) pressBack()
        compose.onNodeWithText("Hoenn", substring = false).performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Buscar zonas", substring = false).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Buscar zonas", substring = false).performClick()
        compose.onNodeWithText("Buscar zona o Pokémon", substring = false).performScrollTo().performTextInput("Ralts")
        compose.onNodeWithTag("hoenn_explorer").performScrollToNode(hasText("Ruta 102", substring = true))
        compose.onNodeWithText("Ruta 102", substring = true).performClick()
        compose.onNodeWithTag("atlas_encounters").performScrollToNode(hasText("#280 · Ralts"))
        compose.onNodeWithText("#280 · Ralts", substring = false).performClick()
        tab("Mapas").assertIsSelected()
        pressBack()
        compose.onNodeWithText("Mapa de Hoenn", substring = false).assertExists()
        tab("Inicio").performClick()
        tab("Mapas").performClick()
        compose.onNodeWithText("Mapa de Hoenn", substring = false).assertExists()
        capture("app-hoenn-restored")
        pressBack()
        compose.onNodeWithText("Elige una región", substring = false).assertExists()
        tab("Inicio").performClick()
    }

    @Test fun productionTeamMoveSelectionUsesApprovedPokemonScreen() {
        fixture()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val container = AppContainer.get(context)
        val name = "RC4 QA movimientos ${System.currentTimeMillis()}"
        val teamId = runBlocking {
            val owner = container.sessions.capture()
            val id = container.teams.create(owner, name)!!
            assertTrue(container.teams.saveMember(owner, com.duoc.rotompedia.data.local.MiembroEquipo(
                equipoId = id, pokemonId = 1, nombre = "Bulbasaur", spriteUrl = "", tipo1 = "grass", tipo2 = "poison",
                slot = 0, hp = 45, attack = 49, defense = 49, specialAttack = 65, specialDefense = 65, speed = 45), true))
            id
        }
        tab("Equipos").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(name, substring = false).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(name, substring = false).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Bulbasaur", substring = false).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Bulbasaur", substring = false).performClick()
        compose.onNode(hasText("Movimientos", substring = true) and isSelectable() and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText("Movimiento 1:", substring = true).performClick()
        compose.onNodeWithText("Asignar movimiento 1", substring = false).assertExists()
        compose.onNodeWithText("Buscar movimiento", substring = false).assertDoesNotExist()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("learn_tab_level-up").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("learn_tab_egg").assertExists()
        compose.onNodeWithTag("learn_tab_machine").assertExists()
        compose.onNodeWithTag("learn_tab_tutor").assertExists()
        compose.onNode(hasContentDescription("Juego / edición:", substring = true)).performClick()
        compose.onNodeWithText("Rojo / Azul", substring = false).performClick()
        compose.onNodeWithTag("learn_list_level-up").performScrollToNode(hasTestTag("learn_move_33"))
        capture("app-team-move-selector")
        compose.onNode(hasTestTag("learn_move_33") and hasAnyAncestor(hasTestTag("learn_list_level-up"))).performClick()
        compose.onNodeWithText("Asignar movimiento 1", substring = false).assertDoesNotExist()
        compose.onNodeWithText("Guardar", substring = false).performClick()
        compose.waitUntil(10_000) {
            runBlocking {
                container.database.equipoDao().obtenerEquipoPorId(teamId, container.sessions.capture()!!.username!!)!!
                    .miembros.single().move1 == "Tackle"
            }
        }
        capture("app-team-move-saved")
        compose.onNodeWithContentDescription("Eliminar Equipo").performClick()
        compose.onNodeWithText("¿Eliminar «$name»? Esta acción no se puede deshacer.").assertExists()
        compose.onNodeWithText("Eliminar", substring = false).performClick()
    }
}
