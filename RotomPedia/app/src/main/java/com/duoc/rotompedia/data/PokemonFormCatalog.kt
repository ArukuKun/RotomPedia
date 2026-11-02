package com.duoc.rotompedia.data

import java.text.Normalizer
import java.util.Locale

/** Metadata de UX. Los IDs, tipos y estadísticas se resuelven siempre por API/caché. */
data class PokemonFormMetadata(
    val nameEn: String,
    val speciesId: Int,
    val displayName: String,
    val formTag: String
) {
    fun matches(query: String): Boolean {
        val terms = PokemonFormCatalog.normalize(query).split(" ").filter { it.isNotBlank() }
        val text = PokemonFormCatalog.normalize("$nameEn $displayName $formTag")
        return terms.all { it in text }
    }
}

object PokemonFormCatalog {
    fun normalize(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).replace('-', ' ').trim()

    private fun form(species: Int, slug: String, label: String, tag: String) =
        PokemonFormMetadata(slug, species, label, tag)

    val allForms = listOf(
        form(6, "charizard-mega-x", "Mega Charizard X", "Mega"),
        form(6, "charizard-mega-y", "Mega Charizard Y", "Mega"),
        form(3, "venusaur-mega", "Mega Venusaur", "Mega"),
        form(9, "blastoise-mega", "Mega Blastoise", "Mega"),
        form(94, "gengar-mega", "Mega Gengar", "Mega"),
        form(115, "kangaskhan-mega", "Mega Kangaskhan", "Mega"),
        form(127, "pinsir-mega", "Mega Pinsir", "Mega"),
        form(130, "gyarados-mega", "Mega Gyarados", "Mega"),
        form(150, "mewtwo-mega-x", "Mega Mewtwo X", "Mega"),
        form(150, "mewtwo-mega-y", "Mega Mewtwo Y", "Mega"),
        form(212, "scizor-mega", "Mega Scizor", "Mega"),
        form(248, "tyranitar-mega", "Mega Tyranitar", "Mega"),
        form(254, "sceptile-mega", "Mega Sceptile", "Mega"),
        form(257, "blaziken-mega", "Mega Blaziken", "Mega"),
        form(260, "swampert-mega", "Mega Swampert", "Mega"),
        form(282, "gardevoir-mega", "Mega Gardevoir", "Mega"),
        form(303, "mawile-mega", "Mega Mawile", "Mega"),
        form(448, "lucario-mega", "Mega Lucario", "Mega"),
        form(445, "garchomp-mega", "Mega Garchomp", "Mega"),
        form(460, "abomasnow-mega", "Mega Abomasnow", "Mega"),
        form(475, "gallade-mega", "Mega Gallade", "Mega"),
        form(373, "salamence-mega", "Mega Salamence", "Mega"),
        form(376, "metagross-mega", "Mega Metagross", "Mega"),
        form(384, "rayquaza-mega", "Mega Rayquaza", "Mega"),
        form(428, "lopunny-mega", "Mega Lopunny", "Mega"),
        form(719, "diancie-mega", "Mega Diancie", "Mega"),
        form(681, "aegislash-shield", "Aegislash (Escudo)", "Combate"),
        form(681, "aegislash-blade", "Aegislash (Filo)", "Combate"),
        form(745, "lycanroc-midday", "Lycanroc (Mediodía)", "Combate"),
        form(745, "lycanroc-midnight", "Lycanroc (Medianoche)", "Combate"),
        form(745, "lycanroc-dusk", "Lycanroc (Crepúsculo)", "Combate"),
        form(658, "greninja-ash", "Greninja Ash", "Combate"),
        form(479, "rotom-wash", "Rotom Lavadora", "Combate"),
        form(479, "rotom-heat", "Rotom Calor", "Combate"),
        form(479, "rotom-mow", "Rotom Cortacésped", "Combate"),
        form(479, "rotom-fan", "Rotom Ventilador", "Combate"),
        form(479, "rotom-frost", "Rotom Frío", "Combate"),
        form(386, "deoxys-attack", "Deoxys (Ataque)", "Combate"),
        form(386, "deoxys-defense", "Deoxys (Defensa)", "Combate"),
        form(386, "deoxys-speed", "Deoxys (Velocidad)", "Combate"),
        form(892, "urshifu-single-strike", "Urshifu (Brusco)", "Combate"),
        form(892, "urshifu-rapid-strike", "Urshifu (Fluido)", "Combate"),
        form(898, "calyrex-ice", "Calyrex (Glacial)", "Combate"),
        form(898, "calyrex-shadow", "Calyrex (Espectral)", "Combate"),
        form(964, "palafin-hero", "Palafin (Heroico)", "Combate"),
        form(157, "typhlosion-hisui", "Typhlosion de Hisui", "Hisui"),
        form(571, "zoroark-hisui", "Zoroark de Hisui", "Hisui"),
        form(706, "goodra-hisui", "Goodra de Hisui", "Hisui"),
        form(59, "arcanine-hisui", "Arcanine de Hisui", "Hisui"),
        form(503, "samurott-hisui", "Samurott de Hisui", "Hisui"),
        form(724, "decidueye-hisui", "Decidueye de Hisui", "Hisui"),
        form(705, "sliggoo-hisui", "Sliggoo de Hisui", "Hisui"),
        form(628, "braviary-hisui", "Braviary de Hisui", "Hisui"),
        form(38, "ninetales-alola", "Ninetales de Alola", "Alola"),
        form(26, "raichu-alola", "Raichu de Alola", "Alola"),
        form(105, "marowak-alola", "Marowak de Alola", "Alola"),
        form(89, "muk-alola", "Muk de Alola", "Alola"),
        form(76, "golem-alola", "Golem de Alola", "Alola"),
        form(110, "weezing-galar", "Weezing de Galar", "Galar"),
        form(144, "articuno-galar", "Articuno de Galar", "Galar"),
        form(145, "zapdos-galar", "Zapdos de Galar", "Galar"),
        form(146, "moltres-galar", "Moltres de Galar", "Galar"),
        form(222, "corsola-galar", "Corsola de Galar", "Galar"),
        form(555, "darmanitan-galar-standard", "Darmanitan de Galar", "Galar"),
        form(194, "wooper-paldea", "Wooper de Paldea", "Paldea"),
        form(128, "tauros-paldea-combat-breed", "Tauros de Paldea (Lucha)", "Paldea"),
        form(128, "tauros-paldea-blaze-breed", "Tauros de Paldea (Fuego)", "Paldea"),
        form(128, "tauros-paldea-aqua-breed", "Tauros de Paldea (Agua)", "Paldea")
    )

    fun find(name: String) = allForms.find { it.nameEn == name.lowercase(Locale.ROOT) }

    fun displayName(name: String): String = find(name)?.displayName ?: run {
        val parts = name.split('-')
        val base = parts.first().replaceFirstChar { it.uppercase() }
        val regional = mapOf("alola" to "Alola", "galar" to "Galar", "hisui" to "Hisui", "paldea" to "Paldea")
        val region = parts.firstOrNull { it in regional }
        when {
            "mega" in parts -> "Mega $base" + parts.last().takeIf { it == "x" || it == "y" }?.let { " ${it.uppercase()}" }.orEmpty()
            region != null -> "$base de ${regional[region]}"
            else -> name.split('-').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
    }
}
