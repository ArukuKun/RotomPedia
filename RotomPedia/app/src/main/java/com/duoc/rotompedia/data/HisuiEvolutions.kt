package com.duoc.rotompedia.data

enum class HisuiEvolutionGame(val label: String) {
    ARCEUS("Leyendas: Arceus"), SCARLET_VIOLET("Escarlata / Púrpura")
}

data class EvolutionMethod(val text: String, val canEvolve: Boolean = true, val items: List<String> = emptyList())
data class HisuiEvolutionRule(val family: String, val from: String, val to: String,
    val arceus: EvolutionMethod, val scarletViolet: EvolutionMethod, val sources: List<String>) {
    fun method(game: HisuiEvolutionGame) = if (game == HisuiEvolutionGame.ARCEUS) arceus else scarletViolet
}

/** Verified contextual edges, not a second Pokemon index. Slugs resolve through the shared index.
 * Coverage: Legends Arceus and Scarlet/Violet only. Reviewed 2026-11-05; sources in the master plan.
 */
object HisuiEvolutions {
    private const val LA = "https://www.serebii.net/legendsarceus/evolution.shtml"
    private const val SV = "https://www.serebii.net/scarletviolet/evolution.shtml"
    private const val FORMS = "https://bulbapedia.bulbagarden.net/wiki/Hisuian_form"
    private fun dex(name: String) = "https://pokemondb.net/pokedex/$name"
    private fun level(n: Int) = EvolutionMethod("Nivel $n o superior.")
    private fun item(slug: String, label: String) = EvolutionMethod("Usar $label.", items = listOf(slug))
    private val inArceus = EvolutionMethod("No evoluciona a esta forma aquí. Evoluciona en Arceus y transfiere mediante Pokémon HOME.", false)
    private fun rule(family: String, from: String, to: String, la: EvolutionMethod, sv: EvolutionMethod = la,
        sources: List<String> = listOf(LA, SV, dex(family))) = HisuiEvolutionRule(family, from, to, la, sv, sources)

    val rules: List<HisuiEvolutionRule> = listOf(
        rule("wyrdeer", "stantler", "wyrdeer", EvolutionMethod("Usar Asalto Barrera 20 veces en estilo rápido; después elegir Evolucionar."), inArceus,
            listOf(LA, "https://bulbapedia.bulbagarden.net/wiki/Methods_of_Evolution")),
        rule("kleavor", "scyther", "kleavor", item("black-augurite", "Mineral Negro"), inArceus,
            listOf(LA, "https://bulbapedia.bulbagarden.net/wiki/Kleavor_(Pok%C3%A9mon)")),
        rule("ursaluna", "teddiursa", "ursaring", level(30)),
        rule("ursaluna", "ursaring", "ursaluna", EvolutionMethod("Usar Bloque de Turba durante una noche de luna llena.", items = listOf("peat-block")), inArceus),
        rule("overqwil", "qwilfish-hisui", "overqwil",
            EvolutionMethod("Usar Mil Púas Tóxicas 20 veces en estilo fuerte; después elegir Evolucionar."),
            EvolutionMethod("Subir de nivel con Mil Púas Tóxicas aprendido.")),
        rule("sneasler", "sneasel-hisui", "sneasler",
            EvolutionMethod("Usar Garra Afilada durante el día.", items = listOf("razor-claw")),
            EvolutionMethod("Subir de nivel durante el día con Garra Afilada equipada.", items = listOf("razor-claw"))),
        rule("basculegion", "basculin-white-striped", "basculegion-male",
            EvolutionMethod("Basculin Raya Blanca macho: acumular al menos 294 PS de daño de retroceso sin debilitarse; elegir Evolucionar."),
            EvolutionMethod("Basculin Raya Blanca macho: acumular al menos 294 PS de daño de retroceso sin debilitarse y subir de nivel.")),
        rule("basculegion", "basculin-white-striped", "basculegion-female",
            EvolutionMethod("Basculin Raya Blanca hembra: acumular al menos 294 PS de daño de retroceso sin debilitarse; elegir Evolucionar."),
            EvolutionMethod("Basculin Raya Blanca hembra: acumular al menos 294 PS de daño de retroceso sin debilitarse y subir de nivel.")),
        rule("arcanine", "growlithe-hisui", "arcanine-hisui", item("fire-stone", "Piedra Fuego")),
        rule("electrode", "voltorb-hisui", "electrode-hisui", item("leaf-stone", "Piedra Hoja")),
        rule("typhlosion", "cyndaquil", "quilava", level(17), level(14)),
        rule("typhlosion", "quilava", "typhlosion-hisui", level(36), inArceus, listOf(dex("typhlosion"), FORMS)),
        rule("samurott", "oshawott", "dewott", level(17)),
        rule("samurott", "dewott", "samurott-hisui", level(36), inArceus, listOf(dex("samurott"), FORMS)),
        rule("decidueye", "rowlet", "dartrix", level(17)),
        rule("decidueye", "dartrix", "decidueye-hisui", level(36), inArceus, listOf(dex("decidueye"), FORMS)),
        rule("lilligant", "petilil", "lilligant-hisui", item("sun-stone", "Piedra Solar"), inArceus, listOf(dex("lilligant"), FORMS)),
        rule("braviary", "rufflet", "braviary-hisui", level(54), inArceus, listOf(dex("braviary"), FORMS)),
        rule("avalugg", "bergmite", "avalugg-hisui", level(37), inArceus, listOf(dex("avalugg"), FORMS)),
        rule("zoroark", "zorua-hisui", "zoroark-hisui", level(30), level(30), listOf(dex("zoroark"), FORMS)),
        rule("goodra", "goomy", "sliggoo-hisui", level(40), inArceus, listOf(dex("sliggoo"), FORMS)),
        rule("goodra", "sliggoo-hisui", "goodra-hisui", EvolutionMethod("Nivel 50 o superior con lluvia natural; elegir Evolucionar."),
            EvolutionMethod("Subir de nivel desde el 50 con lluvia natural. Danza Lluvia no cuenta."),
            listOf(dex("goodra"), "https://bulbapedia.bulbagarden.net/wiki/Sliggoo_(Pok%C3%A9mon)"))
    )

    fun forVariety(slug: String): List<HisuiEvolutionRule> {
        val family = rules.firstOrNull { it.from == slug || it.to == slug }?.family ?: return emptyList()
        return rules.filter { it.family == family }
    }

    fun notice(slug: String): String? = if (slug == "ursaluna-bloodmoon")
        "Ursaluna Luna Carmesí no se obtiene evolucionando a Ursaring." else null

    /** Species-only upstream chains cannot assign these regional edges to their ordinary counterparts. */
    fun excludedTargets(slug: String): Set<String> = when (slug) {
        "qwilfish" -> setOf("904")
        "basculin-red-striped", "basculin-blue-striped" -> setOf("902")
        "sneasel", "weavile" -> setOf("903")
        else -> emptySet()
    }
}
