package com.duoc.rotompedia.data

/**
 * Catálogo maestro de Formas Alternativas, Megas, Formas Regionales y Cambios de Combate.
 * Permite seleccionar variantes competitivas vitales en Team Builder, Pokédex y Calculadoras.
 */
data class AlternateFormItem(
    val id: Int,
    val speciesId: Int,
    val nameEn: String,
    val displayName: String,
    val spriteUrl: String,
    val tipo1: String,
    val tipo2: String?,
    val hp: Int,
    val atk: Int,
    val def: Int,
    val spa: Int,
    val spd: Int,
    val spe: Int,
    val formTag: String // "Mega", "Alola", "Galar", "Hisui", "Paldea", "Combate"
)

object PokemonFormsData {

    private fun sprite(id: Int): String =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

    val allForms: List<AlternateFormItem> = listOf(
        // ==================== MEGAS ====================
        AlternateFormItem(10034, 6, "charizard-mega-x", "Mega Charizard X", sprite(10034), "fire", "dragon", 78, 130, 111, 130, 85, 100, "Mega"),
        AlternateFormItem(10035, 6, "charizard-mega-y", "Mega Charizard Y", sprite(10035), "fire", "flying", 78, 104, 78, 159, 115, 100, "Mega"),
        AlternateFormItem(10033, 3, "venusaur-mega", "Mega Venusaur", sprite(10033), "grass", "poison", 80, 100, 123, 122, 120, 80, "Mega"),
        AlternateFormItem(10036, 9, "blastoise-mega", "Mega Blastoise", sprite(10036), "water", null, 79, 103, 120, 135, 115, 78, "Mega"),
        AlternateFormItem(10038, 94, "gengar-mega", "Mega Gengar", sprite(10038), "ghost", "poison", 60, 65, 80, 170, 95, 130, "Mega"),
        AlternateFormItem(10039, 115, "kangaskhan-mega", "Mega Kangaskhan", sprite(10039), "normal", null, 105, 125, 100, 60, 100, 100, "Mega"),
        AlternateFormItem(10041, 127, "pinsir-mega", "Mega Pinsir", sprite(10041), "bug", "flying", 65, 155, 120, 65, 90, 105, "Mega"),
        AlternateFormItem(10042, 130, "gyarados-mega", "Mega Gyarados", sprite(10042), "water", "dark", 95, 155, 109, 70, 130, 81, "Mega"),
        AlternateFormItem(10044, 150, "mewtwo-mega-x", "Mega Mewtwo X", sprite(10044), "psychic", "fighting", 106, 190, 100, 154, 100, 130, "Mega"),
        AlternateFormItem(10045, 150, "mewtwo-mega-y", "Mega Mewtwo Y", sprite(10045), "psychic", null, 106, 150, 70, 194, 120, 140, "Mega"),
        AlternateFormItem(10046, 212, "scizor-mega", "Mega Scizor", sprite(10046), "bug", "steel", 70, 150, 140, 65, 100, 75, "Mega"),
        AlternateFormItem(10049, 248, "tyranitar-mega", "Mega Tyranitar", sprite(10049), "rock", "dark", 100, 164, 150, 95, 120, 71, "Mega"),
        AlternateFormItem(10050, 254, "sceptile-mega", "Mega Sceptile", sprite(10050), "grass", "dragon", 70, 110, 75, 145, 85, 145, "Mega"),
        AlternateFormItem(10051, 257, "blaziken-mega", "Mega Blaziken", sprite(10051), "fire", "fighting", 80, 160, 80, 130, 80, 100, "Mega"),
        AlternateFormItem(10052, 260, "swampert-mega", "Mega Swampert", sprite(10052), "water", "ground", 100, 150, 110, 95, 110, 70, "Mega"),
        AlternateFormItem(10051, 282, "gardevoir-mega", "Mega Gardevoir", sprite(10051), "psychic", "fairy", 68, 85, 65, 165, 135, 100, "Mega"),
        AlternateFormItem(10055, 303, "mawile-mega", "Mega Mawile", sprite(10055), "steel", "fairy", 50, 105, 125, 55, 95, 50, "Mega"),
        AlternateFormItem(10059, 448, "lucario-mega", "Mega Lucario", sprite(10059), "fighting", "steel", 70, 145, 88, 140, 70, 112, "Mega"),
        AlternateFormItem(10058, 445, "garchomp-mega", "Mega Garchomp", sprite(10058), "dragon", "ground", 108, 170, 115, 120, 95, 92, "Mega"),
        AlternateFormItem(10060, 460, "abomasnow-mega", "Mega Abomasnow", sprite(10060), "grass", "ice", 90, 132, 105, 132, 105, 30, "Mega"),
        AlternateFormItem(10068, 475, "gallade-mega", "Mega Gallade", sprite(10068), "psychic", "fighting", 68, 165, 95, 65, 115, 110, "Mega"),
        AlternateFormItem(10076, 373, "salamence-mega", "Mega Salamence", sprite(10076), "dragon", "flying", 95, 145, 130, 120, 90, 120, "Mega"),
        AlternateFormItem(10077, 376, "metagross-mega", "Mega Metagross", sprite(10077), "steel", "psychic", 80, 145, 150, 105, 110, 110, "Mega"),
        AlternateFormItem(10079, 384, "rayquaza-mega", "Mega Rayquaza", sprite(10079), "dragon", "flying", 105, 180, 100, 180, 100, 115, "Mega"),
        AlternateFormItem(10088, 428, "lopunny-mega", "Mega Lopunny", sprite(10088), "normal", "fighting", 65, 136, 94, 54, 96, 135, "Mega"),
        AlternateFormItem(10075, 719, "diancie-mega", "Mega Diancie", sprite(10075), "rock", "fairy", 50, 160, 110, 160, 110, 110, "Mega"),

        // ==================== FORMAS DE COMBATE / DUALES ====================
        // Aegislash
        AlternateFormItem(681, 681, "aegislash-shield", "Aegislash (Escudo)", sprite(681), "steel", "ghost", 60, 50, 140, 50, 140, 60, "Combate"),
        AlternateFormItem(10061, 681, "aegislash-blade", "Aegislash (Filo)", sprite(10061), "steel", "ghost", 60, 140, 50, 140, 50, 60, "Combate"),

        // Lycanroc
        AlternateFormItem(745, 745, "lycanroc-midday", "Lycanroc (Mediodía)", sprite(745), "rock", null, 75, 115, 65, 55, 65, 112, "Combate"),
        AlternateFormItem(10126, 745, "lycanroc-midnight", "Lycanroc (Medianoche)", sprite(10126), "rock", null, 85, 115, 75, 55, 75, 82, "Combate"),
        AlternateFormItem(10152, 745, "lycanroc-dusk", "Lycanroc (Crepúsculo)", sprite(10152), "rock", null, 75, 117, 65, 55, 65, 110, "Combate"),

        // Greninja
        AlternateFormItem(10116, 658, "greninja-ash", "Greninja Ash", sprite(10116), "water", "dark", 72, 145, 67, 153, 71, 132, "Combate"),

        // Rotom
        AlternateFormItem(10008, 479, "rotom-wash", "Rotom Lavadora", sprite(10008), "electric", "water", 50, 65, 107, 105, 107, 86, "Combate"),
        AlternateFormItem(10007, 479, "rotom-heat", "Rotom Calor", sprite(10007), "electric", "fire", 50, 65, 107, 105, 107, 86, "Combate"),
        AlternateFormItem(10009, 479, "rotom-mow", "Rotom Cortacésped", sprite(10009), "electric", "grass", 50, 65, 107, 105, 107, 86, "Combate"),
        AlternateFormItem(10010, 479, "rotom-fan", "Rotom Ventilador", sprite(10010), "electric", "flying", 50, 65, 107, 105, 107, 86, "Combate"),
        AlternateFormItem(10011, 479, "rotom-frost", "Rotom Frío", sprite(10011), "electric", "ice", 50, 65, 107, 105, 107, 86, "Combate"),

        // Deoxys
        AlternateFormItem(10001, 386, "deoxys-attack", "Deoxys (Ataque)", sprite(10001), "psychic", null, 50, 180, 20, 180, 20, 150, "Combate"),
        AlternateFormItem(10002, 386, "deoxys-defense", "Deoxys (Defensa)", sprite(10002), "psychic", null, 50, 70, 160, 70, 160, 90, "Combate"),
        AlternateFormItem(10003, 386, "deoxys-speed", "Deoxys (Velocidad)", sprite(10003), "psychic", null, 50, 95, 90, 95, 90, 180, "Combate"),

        // Urshifu
        AlternateFormItem(892, 892, "urshifu-single-strike", "Urshifu (Brusco)", sprite(892), "fighting", "dark", 100, 130, 100, 63, 60, 97, "Combate"),
        AlternateFormItem(10183, 892, "urshifu-rapid-strike", "Urshifu (Fluido)", sprite(10183), "fighting", "water", 100, 130, 100, 63, 60, 97, "Combate"),

        // Calyrex
        AlternateFormItem(10193, 898, "calyrex-ice", "Calyrex (Glacial)", sprite(10193), "psychic", "ice", 100, 165, 150, 85, 130, 50, "Combate"),
        AlternateFormItem(10194, 898, "calyrex-shadow", "Calyrex (Espectral)", sprite(10194), "psychic", "ghost", 100, 85, 80, 165, 100, 150, "Combate"),

        // Palafin
        AlternateFormItem(10256, 964, "palafin-hero", "Palafin (Heroico)", sprite(10256), "water", null, 100, 160, 97, 106, 87, 100, "Combate"),

        // ==================== FORMAS DE HISUI ====================
        AlternateFormItem(10237, 157, "typhlosion-hisui", "Typhlosion de Hisui", sprite(10237), "fire", "ghost", 73, 84, 78, 119, 85, 95, "Hisui"),
        AlternateFormItem(10239, 571, "zoroark-hisui", "Zoroark de Hisui", sprite(10239), "normal", "ghost", 55, 100, 60, 125, 60, 110, "Hisui"),
        AlternateFormItem(10242, 706, "goodra-hisui", "Goodra de Hisui", sprite(10242), "steel", "dragon", 80, 100, 100, 110, 150, 60, "Hisui"),
        AlternateFormItem(10230, 59, "arcanine-hisui", "Arcanine de Hisui", sprite(10230), "fire", "rock", 95, 115, 80, 95, 80, 90, "Hisui"),
        AlternateFormItem(10236, 503, "samurott-hisui", "Samurott de Hisui", sprite(10236), "water", "dark", 90, 108, 80, 100, 65, 85, "Hisui"),
        AlternateFormItem(10244, 724, "decidueye-hisui", "Decidueye de Hisui", sprite(10244), "grass", "fighting", 88, 112, 80, 95, 95, 60, "Hisui"),
        AlternateFormItem(10241, 705, "sliggoo-hisui", "Sliggoo de Hisui", sprite(10241), "steel", "dragon", 58, 75, 83, 83, 113, 40, "Hisui"),
        AlternateFormItem(10240, 628, "braviary-hisui", "Braviary de Hisui", sprite(10240), "psychic", "flying", 110, 83, 70, 112, 70, 65, "Hisui"),

        // ==================== FORMAS DE ALOLA ====================
        AlternateFormItem(10103, 38, "ninetales-alola", "Ninetales de Alola", sprite(10103), "ice", "fairy", 73, 67, 75, 81, 100, 109, "Alola"),
        AlternateFormItem(10100, 26, "raichu-alola", "Raichu de Alola", sprite(10100), "electric", "psychic", 60, 85, 50, 95, 85, 110, "Alola"),
        AlternateFormItem(10115, 105, "marowak-alola", "Marowak de Alola", sprite(10115), "fire", "ghost", 60, 80, 110, 50, 80, 45, "Alola"),
        AlternateFormItem(10113, 89, "muk-alola", "Muk de Alola", sprite(10113), "poison", "dark", 105, 105, 75, 65, 100, 50, "Alola"),

        // ==================== FORMAS DE GALAR ====================
        AlternateFormItem(10167, 110, "weezing-galar", "Weezing de Galar", sprite(10167), "poison", "fairy", 65, 90, 120, 85, 70, 60, "Galar"),
        AlternateFormItem(10169, 144, "articuno-galar", "Articuno de Galar", sprite(10169), "psychic", "flying", 90, 85, 85, 125, 100, 95, "Galar"),
        AlternateFormItem(10170, 145, "zapdos-galar", "Zapdos de Galar", sprite(10170), "fighting", "flying", 90, 125, 90, 85, 90, 100, "Galar"),
        AlternateFormItem(10171, 146, "moltres-galar", "Moltres de Galar", sprite(10171), "dark", "flying", 90, 85, 90, 100, 125, 90, "Galar"),
        AlternateFormItem(10173, 222, "corsola-galar", "Corsola de Galar", sprite(10173), "ghost", null, 60, 55, 100, 65, 100, 30, "Galar"),
        AlternateFormItem(10177, 555, "darmanitan-galar", "Darmanitan de Galar", sprite(10177), "ice", null, 105, 140, 55, 30, 55, 95, "Galar"),

        // ==================== FORMAS DE PALDEA ====================
        AlternateFormItem(10253, 194, "wooper-paldea", "Wooper de Paldea", sprite(10253), "poison", "ground", 55, 45, 45, 25, 25, 15, "Paldea"),
        AlternateFormItem(10250, 128, "tauros-paldea-combat", "Tauros de Paldea (Lucha)", sprite(10250), "fighting", null, 75, 110, 105, 30, 70, 100, "Paldea"),
        AlternateFormItem(10251, 128, "tauros-paldea-blaze", "Tauros de Paldea (Fuego)", sprite(10251), "fighting", "fire", 75, 110, 105, 30, 70, 100, "Paldea"),
        AlternateFormItem(10252, 128, "tauros-paldea-aqua", "Tauros de Paldea (Agua)", sprite(10252), "fighting", "water", 75, 110, 105, 30, 70, 100, "Paldea")
    )

    fun getFormsForSpecies(speciesId: Int): List<AlternateFormItem> {
        return allForms.filter { it.speciesId == speciesId }
    }

    fun findFormById(id: Int): AlternateFormItem? {
        return allForms.find { it.id == id }
    }
}

