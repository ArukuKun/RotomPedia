package com.duoc.rotompedia

import com.duoc.rotompedia.data.reference.*
import org.junit.Assert.*
import org.junit.Test

class AbilityPresentationTest {
    private fun ability(id: Int = 54, slug: String = "truant", es: String? = "Ausente", en: String = "Truant") =
        ReferenceAbility(id, slug, es, en, true, null, null, emptyList())
    private fun index(links: List<AbilityLink>) = ReferenceIndex("test", emptyList(), emptyList(), emptyList(), links, emptyList())

    @Test fun `counts deduplicate exact variety IDs per role and total is a union not sum`() {
        val links = listOf(AbilityLink(38, 1, false, 1), AbilityLink(38, 1, false, 2), AbilityLink(38, 1, false, 1),
            AbilityLink(10104, 1, false, 1), AbilityLink(10104, 1, true, 3), AbilityLink(25, 1, true, 3),
            AbilityLink(25, 2, false, 1))
        val result = AbilityPresentation.holders(index(links), 1)
        assertEquals(3, result.total)
        assertEquals(2, result.normal)
        assertEquals(2, result.hidden)
        assertEquals(1, result.both)
        assertEquals(listOf(25, 38, 10104), result.holders.map { it.pokemonId })
        assertEquals(listOf(1, 2), result.holders.single { it.pokemonId == 38 }.normalSlots)
        assertEquals("Normal · slots 1, 2", result.holders.single { it.pokemonId == 38 }.roleLabel)
        assertEquals("Normal · slot 1 · Oculta · slot 3", result.holders.single { it.pokemonId == 10104 }.roleLabel)
        assertEquals(7, links.size) // Source relations never rewritten or removed.
    }

    @Test fun `zero coverage and hidden-only abilities count honestly`() {
        val data = index(listOf(AbilityLink(10099, 9, true, 3)))
        assertEquals(0, AbilityPresentation.holders(data, 10).total)
        val hidden = AbilityPresentation.holders(data, 9)
        assertEquals(1, hidden.total)
        assertEquals(0, hidden.normal)
        assertEquals(1, hidden.hidden)
        assertEquals(0, hidden.both)
    }

    @Test fun `three verified revisions keep previous names searchable and do not mutate source`() {
        assertEquals(3, AbilityNames.revisions.size)
        AbilityNames.revisions.forEach { revision ->
            val row = ability(revision.id, revision.slug, revision.previous)
            assertEquals(revision.current, row.displayName)
            assertEquals(revision.previous, row.nameEs)
            assertTrue(AbilityNames.matches(row, revision.current.uppercase()))
            assertTrue(AbilityNames.matches(row, revision.previous))
            assertTrue(AbilityNames.matches(row, revision.slug))
            assertTrue(AbilityNames.matches(row, revision.id.toString()))
            assertTrue(revision.source.startsWith("https://www.wikidex.net/"))
            assertEquals(revision.current, row.copy(nameEs = revision.current).displayName)
        }
        assertTrue(AbilityNames.matches(ability(), "Truant"))
    }

    @Test fun `translation revisions require ID slug and known source name and never invent missing Spanish`() {
        assertEquals("Ausente", ability(slug = "wrong").displayName)
        assertEquals("Ausente", ability(id = 999).displayName)
        assertEquals("Otro nombre verificado", ability(es = "Otro nombre verificado").displayName)
        assertEquals("Truant", ability(es = null).displayName)
        assertNull(AbilityNames.revision(ability(es = null)))
        assertEquals("Unidad Ecuestre", ability(266, "as-one-glastrier", "Unidad Ecuestre").displayName)
        assertEquals("Unidad Ecuestre", ability(267, "as-one-spectrier", "Unidad Ecuestre").displayName)
    }
}
