package com.duoc.rotompedia.data.maps

/** Only sourced rows in the selected area/edition may create method controls. */
object AtlasEncounterSelection {
    fun methods(rows: List<MapEncounter>, methods: Map<Int, MapEncounterMethod>): List<MapEncounterMethod> =
        rows.map { it.methodId }.distinct().sorted().mapNotNull(methods::get)
}

data class AtlasEdition(val versionId: Int, val title: String, val verified: Boolean = false)

/** Individual versions deliberately stay separate: never combine incompatible encounter slots. */
object AtlasEditions {
    val hoenn = listOf(AtlasEdition(9, "Esmeralda", verified = true), AtlasEdition(7, "Rubí"),
        AtlasEdition(8, "Zafiro"), AtlasEdition(25, "Rubí Omega"), AtlasEdition(26, "Zafiro Alfa"))
    val kanto = listOf(AtlasEdition(1, "Rojo"), AtlasEdition(2, "Azul"), AtlasEdition(3, "Amarillo"),
        AtlasEdition(10, "Rojo Fuego"), AtlasEdition(11, "Verde Hoja"),
        AtlasEdition(15, "HeartGold"), AtlasEdition(16, "SoulSilver"))
}
