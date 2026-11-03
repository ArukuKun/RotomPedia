package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.imageLoader
import coil.compose.AsyncImage
import coil.network.HttpException
import com.duoc.rotompedia.data.images.*
import com.duoc.rotompedia.data.model.SpritesData

private val missingImages = MissingImageCache()

/** All candidates belong to the requested variety and colour. No species fallback. */
@Composable
fun PokemonImage(
    pokemonId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shiny: Boolean = false,
    style: PokemonImageStyle = PokemonImageStyle.SPRITE,
    sprites: SpritesData? = null,
    savedUrl: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
    missingCache: MissingImageCache = missingImages
) {
    val request = remember(pokemonId, shiny, style, sprites, savedUrl) {
        PokemonImageRequest(pokemonId, shiny, style, sprites, savedUrl)
    }
    val candidates = remember(request) { PokemonImageResolver.candidates(request) }
    var attempt by remember(request) { mutableIntStateOf(0) }
    var retry by remember(request) { mutableIntStateOf(0) }
    var loading by remember(request, attempt, retry) { mutableStateOf(true) }
    val candidate = candidates.drop(attempt).firstOrNull { !missingCache.isMissing(it.url) }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (candidate == null) {
            Box(modifier = Modifier.fillMaxSize().clickable(role = Role.Button) {
                missingCache.retry(candidates.map { it.url })
                attempt = 0
                retry++
            }.semantics { this.contentDescription = "Sin imagen para ${contentDescription.orEmpty()}. Reintentar" },
                contentAlignment = Alignment.Center) {
                Text("Sin\nimagen", fontSize = 10.sp)
            }
        } else key(request, candidate.url, retry) {
            if (loading) Text("…", modifier = Modifier.semantics {
                this.contentDescription = "Cargando imagen de ${contentDescription.orEmpty()}"
            })
            AsyncImage(model = candidate.url, contentDescription = contentDescription, imageLoader = imageLoader,
                modifier = Modifier.fillMaxSize(), contentScale = contentScale,
                onSuccess = { loading = false },
                onError = {
                    val error = it.result.throwable
                    val status = (error as? HttpException)?.response?.code
                    missingCache.record(candidate.url, status)
                    ImageDiagnostics.record(request, candidate, status, error)
                    attempt = candidates.indexOf(candidate) + 1
                })
        }
    }
}
