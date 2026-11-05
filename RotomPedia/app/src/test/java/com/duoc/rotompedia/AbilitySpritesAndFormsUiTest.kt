package com.duoc.rotompedia

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.SuccessResult
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.ui.screens.*
import com.duoc.rotompedia.viewmodel.PokemonFormData
import com.duoc.rotompedia.viewmodel.PokemonFormsState
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Collections

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class AbilitySpritesAndFormsUiTest {
    @get:Rule val compose = createComposeRule()
    private var loader: ImageLoader? = null
    @After fun close() { loader?.shutdown() }
    private fun form(id: Int, name: String, default: Boolean = false) = PokemonFormData(id, name, name,
        "", listOf("water"), 1, 1, 1, 1, 1, 1, default)

    @Test fun `ability row loads exact variety sprite and keeps readable name and exact navigation`() {
        val requests = Collections.synchronizedList(mutableListOf<String>())
        val imageLoader = ImageLoader.Builder(ApplicationProvider.getApplicationContext<Context>()).components {
            add(Interceptor { chain ->
                requests.add(chain.request.data.toString())
                SuccessResult(ColorDrawable(android.graphics.Color.GREEN), chain.request, DataSource.MEMORY)
            })
        }.build().also { loader = it }
        var chosen: String? = null
        val label = "#038 · Ninetales de Alola"
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.6f)) {
                MaterialTheme { Column(Modifier.width(280.dp)) {
                    AbilityPokemonRow(AbilityLink(10104, 117, true, 3), label, { chosen = it }, imageLoader)
                } }
            }
        }
        compose.waitUntil(10_000) { requests.isNotEmpty() }
        assertTrue(requests.all { it.endsWith("/10104.png") })
        compose.onNodeWithTag("ability_sprite_10104_3", useUnmergedTree = true)
            .assertWidthIsEqualTo(40.dp).assertHeightIsEqualTo(40.dp)
        compose.onNodeWithText("$label · Oculta · slot 3").assertIsDisplayed().performClick()
        assertEquals("10104", chosen)
    }

    @Test fun `ability sheet retains progressive access beyond forty rows and includes sprites`() {
        val ability = ReferenceAbility(1, "test", "Habilidad", "Ability", true, null, null, emptyList())
        val links = (1..41).map { AbilityLink(it, 1, false, 1) }
        val index = ReferenceIndex("test", emptyList(), emptyList(), listOf(ability), links, emptyList())
        var chosen: String? = null
        compose.setContent { MaterialTheme {
            AbilityReferenceSheet(ability, index, links.associate { it.pokemonId to "Pokémon ${it.pokemonId}" }, {}, { chosen = it })
        } }
        compose.onNodeWithText("Pokémon asociados · 41").assertExists()
        compose.onNodeWithText("Cargar 40 más").performScrollTo().performClick()
        compose.onNodeWithText("Pokémon 41 · Normal · slot 1").performScrollTo().performClick()
        assertEquals("41", chosen)
        compose.onNodeWithTag("ability_sprite_41_1", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Cargar 40 más").assertDoesNotExist()
    }

    @Test fun `single variety has no Forms title notice container or reserved space`() {
        compose.setContent { MaterialTheme { Column {
            Text("Antes")
            PokemonFormsSection(904, listOf(form(904, "overqwil", true)), PokemonFormsState.Success(emptyList()), {}, {})
            Text("Después")
        } } }
        compose.onNodeWithText("Formas").assertDoesNotExist()
        compose.onNodeWithTag("pokemon_forms").assertDoesNotExist()
        compose.onNodeWithText("PokeAPI", substring = true).assertDoesNotExist()
        val before = compose.onNodeWithText("Antes").fetchSemanticsNode().boundsInRoot
        val after = compose.onNodeWithText("Después").fetchSemanticsNode().boundsInRoot
        assertEquals(before.bottom, after.top, 0.1f)
    }

    @Test fun `real alternatives preserve selected form and navigation back to default`() {
        var chosen: Int? = null
        compose.setContent { MaterialTheme {
            PokemonFormsSection(10104, listOf(form(38, "ninetales", true), form(10104, "ninetales-alola")),
                PokemonFormsState.Success(emptyList()), {}, { chosen = it })
        } }
        compose.onNodeWithText("Formas").assertExists()
        compose.onNodeWithText("ninetales").performClick()
        assertEquals(38, chosen)
        compose.onNodeWithText("✓ Seleccionada").assertExists()
    }

    @Test fun `pending forms hide stale entries and error offers retry without technical notice`() {
        val state = mutableStateOf<PokemonFormsState>(PokemonFormsState.Loading)
        var retry = false
        compose.setContent { MaterialTheme {
            PokemonFormsSection(904, listOf(form(38, "stale")), state.value, { retry = true }, {})
        } }
        compose.onNodeWithText("Formas").assertDoesNotExist()
        compose.onNodeWithText("stale").assertDoesNotExist()
        compose.runOnIdle { state.value = PokemonFormsState.Error("Raw PokeAPI failure") }
        compose.onNodeWithText("Raw PokeAPI failure").assertDoesNotExist()
        compose.onNodeWithText("Reintentar formas").performClick()
        assertTrue(retry)
    }
}
