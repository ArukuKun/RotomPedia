package com.duoc.rotompedia.data.images

import com.duoc.rotompedia.data.model.SpritesData

enum class PokemonImageStyle { SPRITE, ARTWORK }
enum class PokemonImageSource { STANDARD, ARTWORK, HOME }

/** pokemonId is the selectable variety ID, NEVER a national number or pokemon-form ID. */
data class PokemonImageRequest(
    val pokemonId: Int,
    val shiny: Boolean = false,
    val style: PokemonImageStyle = PokemonImageStyle.SPRITE,
    val sprites: SpritesData? = null,
    val savedUrl: String? = null
)
data class PokemonImageCandidate(val source: PokemonImageSource, val url: String)

object PokemonImageResolver {
    private const val ROOT = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"

    /** Pure and local: Coil attempts candidates lazily; no per-row metadata/HEAD requests. */
    fun candidates(request: PokemonImageRequest): List<PokemonImageCandidate> {
        if (request.pokemonId <= 0) return emptyList()
        val id = request.pokemonId
        val shiny = if (request.shiny) "shiny/" else ""
        val paths = mapOf(
            PokemonImageSource.STANDARD to "$ROOT$shiny$id.png",
            PokemonImageSource.ARTWORK to "${ROOT}other/official-artwork/$shiny$id.png",
            PokemonImageSource.HOME to "${ROOT}other/home/$shiny$id.png"
        )
        val sprites = request.sprites
        val declared = mapOf(
            PokemonImageSource.STANDARD to if (request.shiny) sprites?.frontShiny else sprites?.urlImagen,
            PokemonImageSource.ARTWORK to if (request.shiny) sprites?.other?.officialArtwork?.frontShiny else sprites?.other?.officialArtwork?.frontDefault,
            PokemonImageSource.HOME to if (request.shiny) sprites?.other?.home?.frontShiny else sprites?.other?.home?.frontDefault
        )
        val order = if (request.style == PokemonImageStyle.ARTWORK)
            listOf(PokemonImageSource.ARTWORK, PokemonImageSource.HOME, PokemonImageSource.STANDARD)
        else listOf(PokemonImageSource.STANDARD, PokemonImageSource.ARTWORK, PokemonImageSource.HOME)
        val prioritized = if (sprites == null) order else order.sortedBy { source ->
            if (declared[source] == paths[source]) 0 else 1
        }
        return prioritized.map { source ->
            val expected = paths.getValue(source)
            // Strict identity/source/colour verification also rejects contradictory legacy saved URLs.
            val declaredUrl = declared[source]?.takeIf { it == expected }
            val savedUrl = request.savedUrl?.takeIf { it == expected }
            PokemonImageCandidate(source, declaredUrl ?: savedUrl ?: expected)
        }
    }
}

/** Only authoritative missing responses are cached. Offline/timeouts remain retryable. */
class MissingImageCache(private val now: () -> Long = System::currentTimeMillis) {
    private val missingUntil = java.util.concurrent.ConcurrentHashMap<String, Long>()
    fun record(url: String, httpStatus: Int?) {
        if (httpStatus == 404 || httpStatus == 410) missingUntil[url] = now() + 10 * 60_000
    }
    fun isMissing(url: String): Boolean {
        val until = missingUntil[url] ?: return false
        if (until > now()) return true
        missingUntil.remove(url, until)
        return false
    }
    fun retry(urls: List<String>) { urls.forEach(missingUntil::remove) }
}
