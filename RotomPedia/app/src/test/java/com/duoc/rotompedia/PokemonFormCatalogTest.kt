package com.duoc.rotompedia

import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.data.buildPickerForms
import com.duoc.rotompedia.data.model.PokemonListItem
import org.junit.Assert.*
import org.junit.Test

class PokemonFormCatalogTest {
    @Test fun `el catalogo tiene slugs unicos y especies validas`() {
        val forms = PokemonFormCatalog.allForms
        assertEquals(forms.size, forms.map { it.nameEn }.toSet().size)
        assertTrue(forms.all { it.speciesId in 1..1025 })
    }

    @Test fun `hisui y mega permiten descubrir multiples formas sin API`() {
        assertTrue(buildPickerForms(emptyList()).count { it.metadata.matches("hisui") } >= 8)
        assertTrue(buildPickerForms(emptyList()).count { it.metadata.matches("mega") } >= 26)
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

    @Test fun `no se reutilizan los ids incorrectos del catalogo antiguo`() {
        val forms = buildPickerForms(listOf(
            item("blaziken-mega", 10050), item("gardevoir-mega", 10051), item("golem-alola", 10111)
        ))
        assertEquals(10050, forms.first { it.nameEn == "blaziken-mega" }.apiId)
        assertEquals(10051, forms.first { it.nameEn == "gardevoir-mega" }.apiId)
        assertEquals(10111, forms.first { it.nameEn == "golem-alola" }.apiId)
        assertEquals(forms.size, forms.map { it.nameEn }.toSet().size)
    }

    @Test fun `un id no disponible no se inventa`() {
        assertNull(buildPickerForms(emptyList()).first { it.nameEn == "venusaur-mega" }.apiId)
    }

    @Test fun `el indice API amplia el catalogo sin duplicar`() {
        val forms = buildPickerForms(listOf(item("rattata-alola", 10091), item("rattata-alola", 10091), item("venusaur", 3)))
        assertEquals(1, forms.count { it.nameEn == "rattata-alola" })
        assertEquals("Rattata de Alola", forms.first { it.nameEn == "rattata-alola" }.displayName)
        assertFalse(forms.any { it.nameEn == "venusaur" })
    }

    @Test fun `nombres amigables consistentes entre picker y detalle`() {
        assertEquals("Mega Venusaur", PokemonFormCatalog.displayName("venusaur-mega"))
        assertEquals("Aegislash (Filo)", PokemonFormCatalog.displayName("aegislash-blade"))
        assertEquals("Golem de Alola", PokemonFormCatalog.displayName("golem-alola"))
    }

    @Test fun `tauros utiliza slugs reales de PokeAPI`() {
        assertEquals(3, PokemonFormCatalog.allForms.count { it.speciesId == 128 && it.nameEn.endsWith("-breed") })
    }

    private fun item(name: String, id: Int) = PokemonListItem(name, "https://pokeapi.co/api/v2/pokemon/$id/")
}
