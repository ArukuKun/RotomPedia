package com.duoc.rotompedia

import com.duoc.rotompedia.data.maps.*
import org.junit.Assert.*
import org.junit.Test

class AtlasEncounterSelectionTest {
    private val walk = MapEncounterMethod(1, "walk", emptyMap())
    private val surf = MapEncounterMethod(5, "surf", emptyMap())
    private val methods = listOf(walk, surf).associateBy { it.id }
    @Test fun `controls are derived only from supplied edition and area rows`() {
        val row = MapEncounter(1, 394, 9, 280, 1, 1, 0, 4, 4, 4, emptyList())
        assertEquals(listOf(walk), AtlasEncounterSelection.methods(listOf(row, row.copy(id = 2)), methods))
        assertEquals(emptyList<MapEncounterMethod>(), AtlasEncounterSelection.methods(emptyList(), methods))
        assertEquals(listOf(walk, surf), AtlasEncounterSelection.methods(listOf(row.copy(methodId = 5), row), methods))
    }
    @Test fun `edition identities are individual and only documented Emerald is marked verified`() {
        assertEquals(listOf(9), AtlasEditions.hoenn.filter { it.verified }.map { it.versionId })
        assertEquals(listOf(1, 2, 3, 10, 11, 15, 16), AtlasEditions.kanto.map { it.versionId })
        assertTrue(AtlasEditions.kanto.none { it.verified })
    }
}
