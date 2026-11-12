package com.duoc.rotompedia

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.duoc.rotompedia.data.images.MissingImageCache
import com.duoc.rotompedia.ui.screens.PokemonImage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Collections

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class PokemonImageUiTest {
    private val compose = createComposeRule()
    @get:Rule val resources = disposeComposeBeforeClosingResources(compose) { loader?.shutdown() }
    private val requests = Collections.synchronizedList(mutableListOf<String>())
    private var loader: ImageLoader? = null

    private fun show(shiny: Boolean = false, succeedArtwork: Boolean = false) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val imageLoader = ImageLoader.Builder(context).components {
            add(Interceptor { chain ->
                val url = chain.request.data.toString()
                requests.add(url)
                if (succeedArtwork && "/official-artwork/" in url)
                    SuccessResult(ColorDrawable(android.graphics.Color.GREEN), chain.request, DataSource.MEMORY)
                else ErrorResult(null, chain.request, IllegalStateException("Fixture: resource unavailable"))
            })
        }.build()
        loader = imageLoader
        val cache = MissingImageCache()
        compose.setContent {
            MaterialTheme {
                PokemonImage(10301, "Mega Zygarde", Modifier.size(96.dp), shiny = shiny,
                    imageLoader = imageLoader, missingCache = cache)
            }
        }
    }

    @Test fun `standard failure recovers exact-variety artwork without network metadata`() {
        show(succeedArtwork = true)
        compose.waitUntil(10_000) { requests.size == 2 }
        compose.onNodeWithContentDescription("Mega Zygarde").assertIsDisplayed()
        assertTrue(requests.all { it.endsWith("/10301.png") })
        assertTrue(requests.last().contains("/official-artwork/"))
        compose.onNodeWithText("Sin\nimagen").assertDoesNotExist()
    }

    @Test fun `all shiny sources absent yields honest placeholder and retry`() {
        show(shiny = true)
        compose.waitUntil(10_000) { requests.size == 3 }
        compose.onNodeWithContentDescription("Sin imagen para Mega Zygarde. Reintentar").assertIsDisplayed().performClick()
        compose.waitUntil(10_000) { requests.size == 6 }
        assertTrue(requests.all { "/shiny/10301.png" in it })
        compose.onNodeWithText("Sin\nimagen").assertIsDisplayed()
    }
}
