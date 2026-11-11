package com.duoc.rotompedia

import com.duoc.rotompedia.ui.components.primaryNavRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class RotomGlobalNavigationTest {
    @Test fun `all destinations map to the approved five tabs`() {
        assertEquals("menu_principal_screen", primaryNavRoute("menu_principal_screen"))
        assertEquals("pokedex", primaryNavRoute("pokedex"))
        assertEquals("pokedex", primaryNavRoute("pokemon_detail/{pokemonId}"))
        assertEquals("map_feature_screen", primaryNavRoute("map_feature_screen"))
        assertEquals("map_feature_screen", primaryNavRoute("map_viewer/{region}"))
        assertEquals("team_builder", primaryNavRoute("team_builder"))
        assertEquals("more_tools", primaryNavRoute("gym_leaders"))
        assertEquals("more_tools", primaryNavRoute("more_tools"))
        assertEquals("more_tools", primaryNavRoute("abilities_reference?abilityId={abilityId}"))
    }
}
