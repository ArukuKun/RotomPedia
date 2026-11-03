package com.duoc.rotompedia

import com.duoc.rotompedia.data.images.*
import com.duoc.rotompedia.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test

class PokemonImageResolverTest {
    @Test fun `variant identity never falls back to national species or form id`() {
        for (id in listOf(10104, 10034, 10126, 10026, 10301, 10264)) {
            val candidates = PokemonImageResolver.candidates(PokemonImageRequest(id, savedUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/718.png"))
            assertEquals(3, candidates.size)
            assertTrue(candidates.all { it.url.endsWith("/$id.png") })
        }
        assertTrue(PokemonImageResolver.candidates(PokemonImageRequest(0)).isEmpty())
        assertTrue(PokemonImageResolver.candidates(PokemonImageRequest(-1)).isEmpty())
    }

    @Test fun `sprite and artwork consumers use explicit deterministic source order`() {
        assertEquals(listOf(PokemonImageSource.STANDARD, PokemonImageSource.ARTWORK, PokemonImageSource.HOME),
            PokemonImageResolver.candidates(PokemonImageRequest(10301)).map { it.source })
        assertEquals(listOf(PokemonImageSource.ARTWORK, PokemonImageSource.HOME, PokemonImageSource.STANDARD),
            PokemonImageResolver.candidates(PokemonImageRequest(10301, style = PokemonImageStyle.ARTWORK)).map { it.source })
    }

    @Test fun `shiny never silently falls back to normal even with a saved normal url`() {
        val urls = PokemonImageResolver.candidates(PokemonImageRequest(10104, shiny = true,
            savedUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/10104.png"))
        assertTrue(urls.all { "/shiny/10104.png" in it.url })
    }

    @Test fun `verified API artwork is preferred when standard is explicitly absent`() {
        val artwork = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/10301.png"
        val candidates = PokemonImageResolver.candidates(PokemonImageRequest(10301,
            sprites = SpritesData(null, other = OtherSprites(officialArtwork = SpritePair(artwork)))))
        assertEquals(artwork, candidates.first().url)
        assertTrue(candidates.all { it.url.endsWith("/10301.png") })
    }

    @Test fun `missing response cache expires and transient errors are never negative cached`() {
        var now = 100L
        val cache = MissingImageCache { now }
        for (code in listOf(null, 403, 429, 500)) { cache.record("transient", code); assertFalse(cache.isMissing("transient")) }
        cache.record("a", 404); cache.record("b", 410)
        assertTrue(cache.isMissing("a")); assertTrue(cache.isMissing("b"))
        cache.retry(listOf("a")); assertFalse(cache.isMissing("a"))
        now += 600_001
        assertFalse(cache.isMissing("b"))
    }

    @Test fun `API artwork HOME and shiny metadata deserialize without breaking old cache constructor`() {
        val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(SpritesData::class.java)
        val sprites = adapter.fromJson("""{"front_default":null,"front_shiny":null,"other":{"official-artwork":{"front_default":"art","front_shiny":"shiny-art"},"home":{"front_default":"home"}}}""")!!
        assertNull(sprites.urlImagen)
        assertEquals("art", sprites.other!!.officialArtwork!!.frontDefault)
        assertEquals("shiny-art", sprites.other.officialArtwork!!.frontShiny)
        assertEquals("home", sprites.other.home!!.frontDefault)
        assertNull(SpritesData(null).other)
    }
}
