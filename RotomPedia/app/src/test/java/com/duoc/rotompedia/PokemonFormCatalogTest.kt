package com.duoc.rotompedia

import com.duoc.rotompedia.data.PokemonFormCatalog
import org.junit.Assert.*
import org.junit.Test

class PokemonFormCatalogTest {
    @Test fun `el catalogo tiene slugs unicos y especies validas`() {
        val forms = PokemonFormCatalog.allForms
        assertEquals(forms.size, forms.map { it.nameEn }.toSet().size)
        assertTrue(forms.all { it.speciesId in 1..1025 })
    }

    @Test fun `hisui y mega permiten descubrir multiples formas sin API`() {
        assertTrue(PokemonFormCatalog.allForms.count { it.matches("hisui") } >= 8)
        assertTrue(PokemonFormCatalog.allForms.count { it.matches("mega") } >= 26)
    }

    @Test fun `lycanroc y aegislash conservan sus variedades predeterminadas`() {
        assertEquals(3, PokemonFormCatalog.allForms.count { it.matches("lycanroc") })
        assertEquals(2, PokemonFormCatalog.allForms.count { it.matches("aegislash") })
    }

    @Test fun `la busqueda normaliza tildes espacios y guiones`() {
        assertTrue(PokemonFormCatalog.find("lycanroc-dusk")!!.matches("  CREPUSCULO "))
        assertTrue(PokemonFormCatalog.find("golem-alola")!!.matches("alola golem"))
        assertTrue(PokemonFormCatalog.find("charizard-mega-x")!!.matches("mega-x"))
        assertFalse(PokemonFormCatalog.find("golem-alola")!!.matches("hisui"))
    }


    @Test fun `nombres amigables consistentes entre picker y detalle`() {
        assertEquals("Mega Venusaur", PokemonFormCatalog.displayName("venusaur-mega"))
        assertEquals("Aegislash (Filo)", PokemonFormCatalog.displayName("aegislash-blade"))
        assertEquals("Golem de Alola", PokemonFormCatalog.displayName("golem-alola"))
    }

    @Test fun `tauros utiliza slugs reales de PokeAPI`() {
        assertEquals(3, PokemonFormCatalog.allForms.count { it.speciesId == 128 && it.nameEn.endsWith("-breed") })
    }

}
