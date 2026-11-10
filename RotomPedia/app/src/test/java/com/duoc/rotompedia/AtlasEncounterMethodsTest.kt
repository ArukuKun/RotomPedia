package com.duoc.rotompedia

import com.duoc.rotompedia.data.maps.*
import org.junit.Assert.*
import org.junit.Test

class AtlasEncounterMethodsTest {
    @Test fun `each encounter mechanic remains separate`() {
        assertEquals(AtlasEncounterFamily.WALKING, AtlasEncounterMethods.family("walk"))
        assertEquals(AtlasEncounterFamily.HEADBUTT, AtlasEncounterMethods.family("headbutt-high"))
        assertEquals(AtlasEncounterFamily.ROCK_SMASH, AtlasEncounterMethods.family("rock-smash"))
        assertEquals(AtlasEncounterFamily.STATIC, AtlasEncounterMethods.family("static"))
        assertEquals(AtlasEncounterFamily.FISHING, AtlasEncounterMethods.family("super-rod"))
        assertEquals(AtlasEncounterFamily.SURF, AtlasEncounterMethods.family("surf"))
        assertEquals(AtlasEncounterFamily.GIFT, AtlasEncounterMethods.family("npc-trade"))
        assertEquals(AtlasEncounterFamily.SPECIAL, AtlasEncounterMethods.family("unknown-source-method"))
    }

    @Test fun `NPC gifts are not ground pickups and are edition scoped`() {
        val items = listOf(
            AtlasSourcedItem(100, 9, "potion", AtlasItemAcquisition.NPC_GIFT, "https://example.com/source"),
            AtlasSourcedItem(100, 10, "potion", AtlasItemAcquisition.PICKUP, "https://example.com/source"),
            AtlasSourcedItem(101, 9, "potion", AtlasItemAcquisition.HIDDEN, "https://example.com/source")
        )
        val filtered = AtlasItemFilter.forEdition(items, 100, 9)
        assertEquals(1, filtered.size)
        assertEquals(AtlasItemAcquisition.NPC_GIFT, filtered.single().acquisition)
        assertTrue(AtlasItemFilter.forEdition(emptyList(), 100, 9).isEmpty())
    }
}
