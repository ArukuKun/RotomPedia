package com.duoc.rotompedia.data

/**
 * Métodos canónicos de encuentro de Pokémon salvajes.
 */
enum class EncounterMethod(val displayName: String, val iconEmoji: String) {
    WALK("Hierba Alta", "🌿"),
    SURF("Surfeando", "🌊"),
    OLD_ROD("Caña Vieja", "🎣"),
    GOOD_ROD("Caña Buena", "🎣"),
    SUPER_ROD("Súper Caña", "🎣"),
    CAVE("Cueva / Caverna", "🦇"),
    SPECIAL("Encuentro Legendario / Fijo", "✨")
}

/**
 * Modelo de datos para un Pokémon salvaje en una ruta o zona.
 */
data class WildEncounter(
    val pokemonId: Int,
    val pokemonName: String,
    val spriteUrl: String,
    val types: List<String>,
    val method: EncounterMethod,
    val chancePercent: Int,
    val minLevel: Int,
    val maxLevel: Int
)

/**
 * Modelo de datos para un punto de interés interactivo en el mapa.
 */
data class RouteLocation(
    val id: String,
    val name: String,
    val region: String,
    val typeEmoji: String,
    val description: String,
    val relativeX: Float, // 0.0f a 1.0f
    val relativeY: Float, // 0.0f a 1.0f
    val keyItems: List<String> = emptyList(),
    val wildEncounters: List<WildEncounter> = emptyList()
)

/**
 * Catálogo maestro de rutas, ciudades y zonas interactivas de las regiones Pokémon.
 */
object RegionLocationData {

    fun getLocationsForRegion(region: String): List<RouteLocation> {
        val reg = region.lowercase()
        return allLocations.filter { it.region.equals(reg, ignoreCase = true) }
    }

    private fun sprite(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    }

    private val allLocations: List<RouteLocation> = listOf(
        // ==================== KANTO (Región Piloto Completa) ====================
        RouteLocation(
            id = "kanto_pallet",
            name = "Pueblo Paleta",
            region = "kanto",
            typeEmoji = "🏡",
            description = "Pintoresco pueblo de partida. Hogar del Laboratorio del Profesor Oak y punto de partida de los entrenadores de Kanto.",
            relativeX = 0.24f,
            relativeY = 0.76f,
            keyItems = listOf("Pokédex Inicial", "5 Poké Balls", "Mapa de Kanto"),
            wildEncounters = listOf(
                WildEncounter(129, "Magikarp", sprite(129), listOf("water"), EncounterMethod.OLD_ROD, 100, 5, 5),
                WildEncounter(60, "Poliwag", sprite(60), listOf("water"), EncounterMethod.GOOD_ROD, 50, 10, 10),
                WildEncounter(118, "Goldeen", sprite(118), listOf("water"), EncounterMethod.GOOD_ROD, 50, 10, 10),
                WildEncounter(72, "Tentacool", sprite(72), listOf("water", "poison"), EncounterMethod.SURF, 100, 5, 20)
            )
        ),
        RouteLocation(
            id = "kanto_route_1",
            name = "Ruta 1",
            region = "kanto",
            typeEmoji = "🌿",
            description = "Ruta serena que conecta Pueblo Paleta con Ciudad Verde. Abundante en Pokémon pequeños de tipo Normal y Volador.",
            relativeX = 0.24f,
            relativeY = 0.66f,
            keyItems = listOf("Poción (obsequio del empleado de la tienda)"),
            wildEncounters = listOf(
                WildEncounter(16, "Pidgey", sprite(16), listOf("normal", "flying"), EncounterMethod.WALK, 50, 2, 5),
                WildEncounter(19, "Rattata", sprite(19), listOf("normal"), EncounterMethod.WALK, 50, 2, 4)
            )
        ),
        RouteLocation(
            id = "kanto_viridian_city",
            name = "Ciudad Verde",
            region = "kanto",
            typeEmoji = "🏙️",
            description = "Una hermosa ciudad de eterna vegetación. Cuenta con el gimnasio del misterioso líder de tipo Tierra, Giovanni.",
            relativeX = 0.24f,
            relativeY = 0.55f,
            keyItems = listOf("Correo de Oak", "Medalla Tierra (8º Gimnasio)"),
            wildEncounters = listOf(
                WildEncounter(129, "Magikarp", sprite(129), listOf("water"), EncounterMethod.OLD_ROD, 100, 5, 5),
                WildEncounter(60, "Poliwag", sprite(60), listOf("water"), EncounterMethod.GOOD_ROD, 50, 10, 10)
            )
        ),
        RouteLocation(
            id = "kanto_route_2",
            name = "Ruta 2",
            region = "kanto",
            typeEmoji = "🌿",
            description = "Camino dividido por el Bosque Verde. En la sección oriental se encuentra el ayudante del Profesor Oak que entrega la MO05 Destello.",
            relativeX = 0.24f,
            relativeY = 0.44f,
            keyItems = listOf("MO05 Destello", "Piedra Lunar"),
            wildEncounters = listOf(
                WildEncounter(16, "Pidgey", sprite(16), listOf("normal", "flying"), EncounterMethod.WALK, 40, 3, 5),
                WildEncounter(19, "Rattata", sprite(19), listOf("normal"), EncounterMethod.WALK, 30, 2, 5),
                WildEncounter(10, "Caterpie", sprite(10), listOf("bug"), EncounterMethod.WALK, 15, 3, 4),
                WildEncounter(13, "Weedle", sprite(13), listOf("bug", "poison"), EncounterMethod.WALK, 15, 3, 4)
            )
        ),
        RouteLocation(
            id = "kanto_viridian_forest",
            name = "Bosque Verde",
            region = "kanto",
            typeEmoji = "🌲",
            description = "Un laberinto natural frondoso lleno de cazabichos. Es famoso por ser el primer lugar donde encontrar al escurridizo Pikachu.",
            relativeX = 0.24f,
            relativeY = 0.38f,
            keyItems = listOf("Antídoto", "Poké Ball", "Poción"),
            wildEncounters = listOf(
                WildEncounter(10, "Caterpie", sprite(10), listOf("bug"), EncounterMethod.WALK, 30, 3, 5),
                WildEncounter(11, "Metapod", sprite(11), listOf("bug"), EncounterMethod.WALK, 20, 4, 6),
                WildEncounter(13, "Weedle", sprite(13), listOf("bug", "poison"), EncounterMethod.WALK, 30, 3, 5),
                WildEncounter(14, "Kakuna", sprite(14), listOf("bug", "poison"), EncounterMethod.WALK, 15, 4, 6),
                WildEncounter(25, "Pikachu", sprite(25), listOf("electric"), EncounterMethod.WALK, 5, 3, 5)
            )
        ),
        RouteLocation(
            id = "kanto_pewter_city",
            name = "Ciudad Plateada",
            region = "kanto",
            typeEmoji = "🪨",
            description = "Ubicada entre montañas rocosas. Alberga el Museo de la Ciencia de Kanto y el Gimnasio de tipo Roca dirigido por Brock.",
            relativeX = 0.24f,
            relativeY = 0.26f,
            keyItems = listOf("Ámbar Viejo", "Medalla Roca (1º Gimnasio)", "MT34 Venganza"),
            wildEncounters = emptyList()
        ),
        RouteLocation(
            id = "kanto_route_3",
            name = "Ruta 3",
            region = "kanto",
            typeEmoji = "🌿",
            description = "Sendero pedregoso que conduce a la entrada del Monte Moon. Frecuentada por numerosos jóvenes entrenadores.",
            relativeX = 0.36f,
            relativeY = 0.26f,
            keyItems = listOf("Magikarp por 500 Pokécuartos en el Centro Pokémon"),
            wildEncounters = listOf(
                WildEncounter(21, "Spearow", sprite(21), listOf("normal", "flying"), EncounterMethod.WALK, 40, 5, 8),
                WildEncounter(16, "Pidgey", sprite(16), listOf("normal", "flying"), EncounterMethod.WALK, 30, 6, 8),
                WildEncounter(39, "Jigglypuff", sprite(39), listOf("normal", "fairy"), EncounterMethod.WALK, 10, 3, 7),
                WildEncounter(56, "Mankey", sprite(56), listOf("fighting"), EncounterMethod.WALK, 20, 5, 7)
            )
        ),
        RouteLocation(
            id = "kanto_mt_moon",
            name = "Monte Moon",
            region = "kanto",
            typeEmoji = "🦇",
            description = "Mística caverna montañosa famosa por la caída de meteoritos y los bailes de Clefairy bajo la luna llena. Dominada por el Team Rocket.",
            relativeX = 0.47f,
            relativeY = 0.24f,
            keyItems = listOf("Fósil Domo (Kabuto)", "Fósil Hélix (Omanyte)", "Piedra Lunar", "MT12 Pistola Agua"),
            wildEncounters = listOf(
                WildEncounter(41, "Zubat", sprite(41), listOf("poison", "flying"), EncounterMethod.CAVE, 55, 7, 10),
                WildEncounter(74, "Geodude", sprite(74), listOf("rock", "ground"), EncounterMethod.CAVE, 25, 7, 10),
                WildEncounter(46, "Paras", sprite(46), listOf("bug", "grass"), EncounterMethod.CAVE, 15, 8, 11),
                WildEncounter(35, "Clefairy", sprite(35), listOf("fairy"), EncounterMethod.CAVE, 5, 8, 12)
            )
        ),
        RouteLocation(
            id = "kanto_cerulean_city",
            name = "Ciudad Celeste",
            region = "kanto",
            typeEmoji = "💧",
            description = "Hermosa ciudad costera rodeada de canales. Destaca la tienda de bicis y el gimnasio acuático de Misty.",
            relativeX = 0.68f,
            relativeY = 0.24f,
            keyItems = listOf("Bono Bici", "Medalla Cascada (2º Gimnasio)", "MT11 Rayo Burbuja"),
            wildEncounters = listOf(
                WildEncounter(129, "Magikarp", sprite(129), listOf("water"), EncounterMethod.OLD_ROD, 100, 5, 5),
                WildEncounter(60, "Poliwag", sprite(60), listOf("water"), EncounterMethod.GOOD_ROD, 50, 10, 10)
            )
        ),
        RouteLocation(
            id = "kanto_route_24",
            name = "Ruta 24 (Puente Pepita)",
            region = "kanto",
            typeEmoji = "🌉",
            description = "Famoso por el desafío de los 5 entrenadores del Puente Pepita. Al norte conduce a la casa del investigador Bill.",
            relativeX = 0.68f,
            relativeY = 0.14f,
            keyItems = listOf("Pepita de Oro", "Charmander regalado por entrenador"),
            wildEncounters = listOf(
                WildEncounter(43, "Oddish", sprite(43), listOf("grass", "poison"), EncounterMethod.WALK, 35, 12, 14),
                WildEncounter(69, "Bellsprout", sprite(69), listOf("grass", "poison"), EncounterMethod.WALK, 35, 12, 14),
                WildEncounter(63, "Abra", sprite(63), listOf("psychic"), EncounterMethod.WALK, 15, 8, 12),
                WildEncounter(16, "Pidgey", sprite(16), listOf("normal", "flying"), EncounterMethod.WALK, 15, 11, 13)
            )
        ),
        RouteLocation(
            id = "kanto_cerulean_cave",
            name = "Cueva Celeste (Mewtwo)",
            region = "kanto",
            typeEmoji = "🔮",
            description = "Cueva prohibida donde solo se autoriza la entrada a quienes hayan derrotado al Alto Mando. En lo más profundo aguarda el legendario Mewtwo.",
            relativeX = 0.63f,
            relativeY = 0.19f,
            keyItems = listOf("Master Ball", "Restos", "MT26 Terremoto"),
            wildEncounters = listOf(
                WildEncounter(150, "Mewtwo", sprite(150), listOf("psychic"), EncounterMethod.SPECIAL, 100, 70, 70),
                WildEncounter(132, "Ditto", sprite(132), listOf("normal"), EncounterMethod.CAVE, 30, 55, 60),
                WildEncounter(112, "Rhydon", sprite(112), listOf("ground", "rock"), EncounterMethod.CAVE, 25, 55, 60),
                WildEncounter(113, "Chansey", sprite(113), listOf("normal"), EncounterMethod.CAVE, 15, 55, 62),
                WildEncounter(65, "Alakazam", sprite(65), listOf("psychic"), EncounterMethod.CAVE, 10, 58, 64)
            )
        ),
        RouteLocation(
            id = "kanto_diglett_cave",
            name = "Cueva Diglett",
            region = "kanto",
            typeEmoji = "🕳️",
            description = "Un túnel kilométrico excavado enteramente por Diglett que conecta Ciudad Carmín directamente con las afueras de Ciudad Plateada.",
            relativeX = 0.65f,
            relativeY = 0.58f,
            keyItems = listOf("Piedra Fuego"),
            wildEncounters = listOf(
                WildEncounter(50, "Diglett", sprite(50), listOf("ground"), EncounterMethod.CAVE, 95, 15, 22),
                WildEncounter(51, "Dugtrio", sprite(51), listOf("ground"), EncounterMethod.CAVE, 5, 29, 31)
            )
        ),
        RouteLocation(
            id = "kanto_power_plant",
            name = "Central de Energía (Zapdos)",
            region = "kanto",
            typeEmoji = "⚡",
            description = "Antigua planta eléctrica abandonada, ahora infestada por Pokémon de tipo Eléctrico. En su interior anida el pájaro legendario Zapdos.",
            relativeX = 0.84f,
            relativeY = 0.34f,
            keyItems = listOf("Piedra Trueno", "MT25 Trueno"),
            wildEncounters = listOf(
                WildEncounter(145, "Zapdos", sprite(145), listOf("electric", "flying"), EncounterMethod.SPECIAL, 100, 50, 50),
                WildEncounter(81, "Magnemite", sprite(81), listOf("electric", "steel"), EncounterMethod.WALK, 40, 21, 24),
                WildEncounter(100, "Voltorb", sprite(100), listOf("electric"), EncounterMethod.WALK, 35, 20, 24),
                WildEncounter(125, "Electabuzz", sprite(125), listOf("electric"), EncounterMethod.WALK, 15, 33, 36),
                WildEncounter(82, "Magneton", sprite(82), listOf("electric", "steel"), EncounterMethod.WALK, 10, 32, 35)
            )
        ),
        RouteLocation(
            id = "kanto_lavender_town",
            name = "Pueblo Lavanda",
            region = "kanto",
            typeEmoji = "👻",
            description = "Famoso por la Torre Pokémon, un camposanto sagrado. Cuenta con una atmósfera misteriosa y la emisora de radio regional.",
            relativeX = 0.84f,
            relativeY = 0.46f,
            keyItems = listOf("Poké Flauta (entregada por el Sr. Fuji)"),
            wildEncounters = listOf(
                WildEncounter(92, "Gastly", sprite(92), listOf("ghost", "poison"), EncounterMethod.CAVE, 70, 18, 24),
                WildEncounter(93, "Haunter", sprite(93), listOf("ghost", "poison"), EncounterMethod.CAVE, 20, 22, 26),
                WildEncounter(104, "Cubone", sprite(104), listOf("ground"), EncounterMethod.CAVE, 10, 17, 20)
            )
        ),
        RouteLocation(
            id = "kanto_saffron_city",
            name = "Ciudad Azafrán",
            region = "kanto",
            typeEmoji = "🏢",
            description = "La metrópolis comercial más grande de Kanto. Sede del rascacielos de Silph S.A., el Dojo Kárate y el Gimnasio Psíquico de Sabrina.",
            relativeX = 0.68f,
            relativeY = 0.46f,
            keyItems = listOf("Master Ball (del Presidente de Silph S.A.)", "Medalla Pantano (6º Gimnasio)", "Hitmonlee o Hitmonchan"),
            wildEncounters = emptyList()
        ),
        RouteLocation(
            id = "kanto_celadon_city",
            name = "Ciudad Azulona",
            region = "kanto",
            typeEmoji = "🛍️",
            description = "La ciudad de los sueños arcoíris. Cuenta con el gran Centro Comercial, el Casino Rocket y el Gimnasio Planta de Erika.",
            relativeX = 0.50f,
            relativeY = 0.46f,
            keyItems = listOf("Eevee (en la azotea de la mansión)", "Monedero", "Medalla Arcoíris (4º Gimnasio)", "Té para guardias"),
            wildEncounters = listOf(
                WildEncounter(88, "Grimer", sprite(88), listOf("poison"), EncounterMethod.SURF, 85, 23, 26),
                WildEncounter(89, "Muk", sprite(89), listOf("poison"), EncounterMethod.SURF, 15, 30, 33)
            )
        ),
        RouteLocation(
            id = "kanto_fuchsia_city",
            name = "Ciudad Fucsia",
            region = "kanto",
            typeEmoji = "🦩",
            description = "Ciudad del Gimnasio Veneno de Koga. Entrada a la Zona Safari, reserva natural donde capturar Pokémon exóticos.",
            relativeX = 0.56f,
            relativeY = 0.76f,
            keyItems = listOf("MO03 Surf", "Dientes de Oro", "MO04 Fuerza", "Medalla Alma (5º Gimnasio)"),
            wildEncounters = listOf(
                WildEncounter(115, "Kangaskhan", sprite(115), listOf("normal"), EncounterMethod.WALK, 15, 25, 28),
                WildEncounter(123, "Scyther", sprite(123), listOf("bug", "flying"), EncounterMethod.WALK, 15, 23, 26),
                WildEncounter(127, "Pinsir", sprite(127), listOf("bug"), EncounterMethod.WALK, 15, 23, 26),
                WildEncounter(128, "Tauros", sprite(128), listOf("normal"), EncounterMethod.WALK, 10, 25, 28),
                WildEncounter(113, "Chansey", sprite(113), listOf("normal"), EncounterMethod.WALK, 10, 26, 28),
                WildEncounter(147, "Dratini", sprite(147), listOf("dragon"), EncounterMethod.SUPER_ROD, 25, 15, 15)
            )
        ),
        RouteLocation(
            id = "kanto_seafoam_islands",
            name = "Islas Espuma (Articuno)",
            region = "kanto",
            typeEmoji = "❄️",
            description = "Archipiélago de cavernas heladas donde las corrientes marinas son tan fuertes que se requiere empujar rocas gigantes. Hogar de Articuno.",
            relativeX = 0.34f,
            relativeY = 0.88f,
            keyItems = listOf("Piedra Agua", "Antiderretir"),
            wildEncounters = listOf(
                WildEncounter(144, "Articuno", sprite(144), listOf("ice", "flying"), EncounterMethod.SPECIAL, 100, 50, 50),
                WildEncounter(86, "Seel", sprite(86), listOf("water"), EncounterMethod.CAVE, 40, 28, 30),
                WildEncounter(79, "Slowpoke", sprite(79), listOf("water", "psychic"), EncounterMethod.CAVE, 30, 28, 32),
                WildEncounter(124, "Jynx", sprite(124), listOf("ice", "psychic"), EncounterMethod.CAVE, 15, 30, 32),
                WildEncounter(90, "Shellder", sprite(90), listOf("water"), EncounterMethod.SUPER_ROD, 40, 20, 25)
            )
        ),
        RouteLocation(
            id = "kanto_cinnabar_island",
            name = "Isla Canela",
            region = "kanto",
            typeEmoji = "🌋",
            description = "Isla volcánica con la Mansión Pokémon en ruinas, el Laboratorio de Fósiles y el Gimnasio de Fuego de Blaine.",
            relativeX = 0.24f,
            relativeY = 0.88f,
            keyItems = listOf("Llave Secreta", "Medalla Volcán (7º Gimnasio)", "Fósil revivido (Omanyte/Kabuto/Aerodactyl)"),
            wildEncounters = listOf(
                WildEncounter(77, "Ponyta", sprite(77), listOf("fire"), EncounterMethod.WALK, 40, 30, 34),
                WildEncounter(58, "Growlithe", sprite(58), listOf("fire"), EncounterMethod.WALK, 30, 30, 32),
                WildEncounter(126, "Magmar", sprite(126), listOf("fire"), EncounterMethod.WALK, 15, 34, 38),
                WildEncounter(109, "Koffing", sprite(109), listOf("poison"), EncounterMethod.WALK, 15, 30, 32)
            )
        ),
        RouteLocation(
            id = "kanto_victory_road",
            name = "Calle Victoria (Moltres)",
            region = "kanto",
            typeEmoji = "🔥",
            description = "El túnel de entrenamiento definitivo que separa a los aspirantes de la Meseta Añil. Solo se permite el paso con las 8 medallas oficiales.",
            relativeX = 0.14f,
            relativeY = 0.34f,
            keyItems = listOf("MT05 Megapatada", "Restos", "Poción Máxima"),
            wildEncounters = listOf(
                WildEncounter(146, "Moltres", sprite(146), listOf("fire", "flying"), EncounterMethod.SPECIAL, 100, 50, 50),
                WildEncounter(95, "Onix", sprite(95), listOf("rock", "ground"), EncounterMethod.CAVE, 35, 36, 42),
                WildEncounter(67, "Machoke", sprite(67), listOf("fighting"), EncounterMethod.CAVE, 30, 38, 44),
                WildEncounter(75, "Graveler", sprite(75), listOf("rock", "ground"), EncounterMethod.CAVE, 25, 38, 43),
                WildEncounter(42, "Golbat", sprite(42), listOf("poison", "flying"), EncounterMethod.CAVE, 10, 40, 44)
            )
        ),
        RouteLocation(
            id = "kanto_indigo_plateau",
            name = "Meseta Añil (Liga Pokémon)",
            region = "kanto",
            typeEmoji = "🏆",
            description = "El pináculo de la gloria. Sede del Alto Mando (Lorelei, Bruno, Agatha, Lance) y del Campeón de la Liga Pokémon.",
            relativeX = 0.14f,
            relativeY = 0.22f,
            keyItems = listOf("Título de Campeón de la Liga Pokémon", "Acceso al Hall de la Fama"),
            wildEncounters = emptyList()
        )
    )
}
