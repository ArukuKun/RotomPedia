package com.duoc.rotompedia.data

/**
 * Categorías tácticas de objetos para combate competitivo (VGC y Singles).
 */
enum class ItemCategory(val displayName: String, val emoji: String) {
    ALL("Todos", "✨"),
    CHOICE("Elección", "🎯"),
    DEFENSIVE("Defensivos", "🛡️"),
    OFFENSIVE("Ofensivos", "⚔️"),
    UTILITY("Utilidad", "🎒"),
    BERRY("Bayas Competitivas", "🍒"),
    WEATHER("Clima y Terrenos", "⛅")
}

/**
 * Modelo de datos para un objeto del circuito competitivo Pokémon.
 */
data class CompetitiveItem(
    val id: String,
    val nameEs: String,
    val nameEn: String,
    val emoji: String,
    val category: ItemCategory,
    val effectDescription: String,
    val competitiveUsage: String,
    val generation: Int
)

/**
 * Catálogo maestro de objetos competitivos con notas de meta y estrategias oficiales.
 */
object CompetitiveItemData {

    val items: List<CompetitiveItem> = listOf(
        // ==================== OBJETOS ELECCIÓN (CHOICE ITEMS) ====================
        CompetitiveItem(
            id = "choice_band",
            nameEs = "Cinta Elección",
            nameEn = "Choice Band",
            emoji = "🎗️",
            category = ItemCategory.CHOICE,
            effectDescription = "Aumenta el Ataque Físico del portador un 50% (x1.5), pero solo permite utilizar el primer movimiento seleccionado hasta ser retirado.",
            competitiveUsage = "Esencial en Wallbreakers físicos inmediatos como Dragonite, Great Tusk, Urshifu o Rillaboom.",
            generation = 3
        ),
        CompetitiveItem(
            id = "choice_specs",
            nameEs = "Gafas Elección",
            nameEn = "Choice Specs",
            emoji = "👓",
            category = ItemCategory.CHOICE,
            effectDescription = "Aumenta el Ataque Especial del portador un 50% (x1.5), pero solo permite utilizar el primer movimiento seleccionado hasta ser retirado.",
            competitiveUsage = "Pilar fundamental de cañones especiales como Flutter Mane, Chi-Yu, Gholdengo o Walking Wake.",
            generation = 4
        ),
        CompetitiveItem(
            id = "choice_scarf",
            nameEs = "Pañuelo Elección",
            nameEn = "Choice Scarf",
            emoji = "🧣",
            category = ItemCategory.CHOICE,
            effectDescription = "Aumenta la Velocidad del portador un 50% (x1.5), pero solo permite utilizar el primer movimiento seleccionado hasta ser retirado.",
            competitiveUsage = "La herramienta de 'Speed Control' y venganza (Revenge Killer) por excelencia frente a sweepers rápidos.",
            generation = 4
        ),

        // ==================== OBJETOS DEFENSIVOS Y BULK ====================
        CompetitiveItem(
            id = "eviolite",
            nameEs = "Mineral Evolutivo",
            nameEn = "Eviolite",
            emoji = "💎",
            category = ItemCategory.DEFENSIVE,
            effectDescription = "Aumenta la Defensa y la Defensa Especial en un 50% (x1.5) si el portador es un Pokémon que aún puede evolucionar.",
            competitiveUsage = "Vuelve murallas infranqueables a especies como Chansey, Dusclops, Porygon2, Clefairy o Duraludon.",
            generation = 5
        ),
        CompetitiveItem(
            id = "assault_vest",
            nameEs = "Chaleco Asalto",
            nameEn = "Assault Vest",
            emoji = "🦺",
            category = ItemCategory.DEFENSIVE,
            effectDescription = "Aumenta la Defensa Especial un 50% (x1.5), pero impide por completo el uso de movimientos de estado o categoría Estado.",
            competitiveUsage = "Clave para tanques ofensivos y pivots que solo llevan 4 ataques (Tornadus, Rillaboom, Ursaluna Luna Carmesí, Ting-Lu).",
            generation = 6
        ),
        CompetitiveItem(
            id = "leftovers",
            nameEs = "Restos",
            nameEn = "Leftovers",
            emoji = "🍎",
            category = ItemCategory.DEFENSIVE,
            effectDescription = "Restaura 1/16 (6.25%) de los PS máximos del portador al final de cada turno de combate de forma pasiva.",
            competitiveUsage = "El objeto de recuperación pasiva estándar para walls y tanques de posicionamiento prolongado (Dondozo, Ting-Lu, Clodsire).",
            generation = 2
        ),
        CompetitiveItem(
            id = "black_sludge",
            nameEs = "Lodo Negro",
            nameEn = "Black Sludge",
            emoji = "🧪",
            category = ItemCategory.DEFENSIVE,
            effectDescription = "Restaura 1/16 de los PS máximos a Pokémon de tipo Veneno. Resta 1/8 de los PS a cualquier Pokémon de otro tipo si se lo transfieren con Truco.",
            competitiveUsage = "Excelente seguro de protección frente a 'Trick' o 'Switcheroo' para Toxapex, Amoonguss y Gengar.",
            generation = 4
        ),
        CompetitiveItem(
            id = "rocky_helmet",
            nameEs = "Casco Dentado",
            nameEn = "Rocky Helmet",
            emoji = "🪖",
            category = ItemCategory.DEFENSIVE,
            effectDescription = "Inflige un daño equivalente a 1/6 de los PS máximos al atacante cada vez que el portador recibe un ataque de contacto físico.",
            competitiveUsage = "Castiga severamente a usuarios de Ida y Vuelta, Sorpresa y ataques multiturno como Golpes Furia de Maushold o Urshifu.",
            generation = 5
        ),
        CompetitiveItem(
            id = "heavy_duty_boots",
            nameEs = "Botas Gruesas",
            nameEn = "Heavy-Duty Boots",
            emoji = "🥾",
            category = ItemCategory.DEFENSIVE,
            effectDescription = "Otorga inmunidad total a los efectos perjudiciales de las trampas de entrada (Trampa Rocas, Púas, Púas Tóxicas y Red Viscosa).",
            competitiveUsage = "Revolucionó el formato Individual 6v6. Esencial para Volcarona, Dragonite, Zapdos y Talonflame.",
            generation = 8
        ),

        // ==================== OBJETOS OFENSIVOS Y MULTIPLICADORES ====================
        CompetitiveItem(
            id = "life_orb",
            nameEs = "Vidasfera",
            nameEn = "Life Orb",
            emoji = "🔮",
            category = ItemCategory.OFFENSIVE,
            effectDescription = "Potencia todos los movimientos de ataque en un 30% (x1.3) a costa de perder un 10% de los PS máximos tras cada golpe acertado.",
            competitiveUsage = "La mejor alternativa a los objetos Elección cuando se necesita la libertad de cambiar de movimiento (Nidoking, Cinderace).",
            generation = 4
        ),
        CompetitiveItem(
            id = "focus_sash",
            nameEs = "Banda Focus",
            nameEn = "Focus Sash",
            emoji = "🥋",
            category = ItemCategory.OFFENSIVE,
            effectDescription = "Permite sobrevivir con exactamente 1 PS si el portador tenía los PS al 100% y recibe un golpe que normalmente lo debilitaría.",
            competitiveUsage = "Garantiza al menos un movimiento a atacantes frágiles o colocadores de Viento Afín / Espacio Raro (Chien-Pao, Whimsicott).",
            generation = 4
        ),
        CompetitiveItem(
            id = "weakness_policy",
            nameEs = "Seguro Debilidad",
            nameEn = "Weakness Policy",
            emoji = "🎯",
            category = ItemCategory.OFFENSIVE,
            effectDescription = "Aumenta el Ataque y el Ataque Especial en 2 niveles (+2) cuando el portador sobrevive a un ataque supereficaz.",
            competitiveUsage = "Devastador en Pokémon naturalmente resistentes o con dinamax/teracristalización (Metagross, Dragonite, Armarouge).",
            generation = 6
        ),
        CompetitiveItem(
            id = "loaded_dice",
            nameEs = "Dado Trucado",
            nameEn = "Loaded Dice",
            emoji = "🎲",
            category = ItemCategory.OFFENSIVE,
            effectDescription = "Garantiza que los movimientos de impacto múltiple (2 a 5 golpes) golpeen siempre 4 o 5 veces.",
            competitiveUsage = "Convierte movimientos como Escala Dragón, Carámbano, Semilladora o Población en bombas letales de 100-125 de potencia base.",
            generation = 9
        ),
        CompetitiveItem(
            id = "booster_energy",
            nameEs = "Energía Potenciadora",
            nameEn = "Booster Energy",
            emoji = "🔋",
            category = ItemCategory.OFFENSIVE,
            effectDescription = "Activa inmediatamente las habilidades Paleosíntesis o Carga Cuark sin depender del sol ni del campo eléctrico, aumentando la stat más alta.",
            competitiveUsage = "Piedra angular en el meta de Scarlet/Violet para Flutter Mane, Iron Bundle, Roaring Moon e Iron Hands.",
            generation = 9
        ),

        // ==================== UTILIDAD Y CONTROL TÁCTICO ====================
        CompetitiveItem(
            id = "covert_cloak",
            nameEs = "Capa Furtiva",
            nameEn = "Covert Cloak",
            emoji = "🧥",
            category = ItemCategory.UTILITY,
            effectDescription = "Protege al portador contra todos los efectos secundarios adicionales de los ataques rivales (incluyendo el retroceso de Sorpresa o bajadas de stat).",
            competitiveUsage = "Inmuniza a colocadores de Viento Afín o Espacio Raro contra el molesto 'Fake Out' de Incineroar o Rillaboom.",
            generation = 9
        ),
        CompetitiveItem(
            id = "clear_amulet",
            nameEs = "Amuleto Puro",
            nameEn = "Clear Amulet",
            emoji = "🧿",
            category = ItemCategory.UTILITY,
            effectDescription = "Inmuniza al portador contra cualquier bajada de estadísticas provocada por habilidades (Intimidación) o movimientos del rival.",
            competitiveUsage = "La respuesta definitiva en VGC frente a la saturación de Incineroar y Landorus-T con Intimidación.",
            generation = 9
        ),
        CompetitiveItem(
            id = "air_balloon",
            nameEs = "Globo Helio",
            nameEn = "Air Balloon",
            emoji = "🎈",
            category = ItemCategory.UTILITY,
            effectDescription = "Otorga inmunidad completa a movimientos de tipo Tierra (Terremoto, Tierra Viva) hasta que el portador recibe un ataque directo.",
            competitiveUsage = "Otorga turnos de ventaja a Pokémon con debilidad x4 a Tierra como Heatran o Gholdengo.",
            generation = 5
        ),
        CompetitiveItem(
            id = "safety_goggles",
            nameEs = "Gafas Protectoras",
            nameEn = "Safety Goggles",
            emoji = "🥽",
            category = ItemCategory.UTILITY,
            effectDescription = "Protege al portador del daño residual provocado por el Clima (Granizo, Tormenta de Arena) y del efecto de movimientos basados en polvo o esporas.",
            competitiveUsage = "Contramedida letal contra Espora de Amoonguss o Somnífero de Vivillon.",
            generation = 6
        ),
        CompetitiveItem(
            id = "white_herb",
            nameEs = "Hierba Blanca",
            nameEn = "White Herb",
            emoji = "🌿",
            category = ItemCategory.UTILITY,
            effectDescription = "Restaura automáticamente todas las bajadas de estadísticas que sufra el portador de vuelta a 0 (de un solo uso).",
            competitiveUsage = "Excelente tras usar Rompecoraza, Sofoco o A Bocajarro para conservar la defensa intacta.",
            generation = 3
        ),
        CompetitiveItem(
            id = "power_herb",
            nameEs = "Hierba Única",
            nameEn = "Power Herb",
            emoji = "🌾",
            category = ItemCategory.UTILITY,
            effectDescription = "Permite ejecutar en el mismo turno y de manera instantánea cualquier movimiento de carga de dos turnos (de un solo uso).",
            competitiveUsage = "Permite lanzar Rayo Meteórico o Geocontrol sin conceder un turno libre al rival.",
            generation = 4
        ),

        // ==================== BAYAS COMPETITIVAS ====================
        CompetitiveItem(
            id = "sitrus_berry",
            nameEs = "Baya Zidra",
            nameEn = "Sitrus Berry",
            emoji = "🍋",
            category = ItemCategory.BERRY,
            effectDescription = "Recupera un 25% de los PS máximos en cuanto la salud del portador cae por debajo del 50%.",
            competitiveUsage = "El objeto consumible más consistente y versátil en torneos oficiales de VGC (Incineroar, Urshifu, Annihilape).",
            generation = 3
        ),
        CompetitiveItem(
            id = "lum_berry",
            nameEs = "Baya Meloc",
            nameEn = "Lum Berry",
            emoji = "🫐",
            category = ItemCategory.BERRY,
            effectDescription = "Cura instantáneamente cualquier problema de estado primario (Quemado, Paralizado, Dormido, Envenenado, Congelado) o Confusión.",
            competitiveUsage = "Protege a atacantes físicos de ser inutilizados por Fuego Fatuo o Trueno Onda en el turno decisivo.",
            generation = 3
        ),
        CompetitiveItem(
            id = "chople_berry",
            nameEs = "Baya Ripano",
            nameEn = "Chople Berry",
            emoji = "🍒",
            category = ItemCategory.BERRY,
            effectDescription = "Reduce a la mitad (50%) el daño recibido de un ataque supereficaz de tipo Lucha (de un solo uso).",
            competitiveUsage = "Permite a Tyranitar, Kingambit o Blissey sobrevivir a potentes A Bocajarro rivales.",
            generation = 4
        ),
        CompetitiveItem(
            id = "occa_berry",
            nameEs = "Baya Caoba",
            nameEn = "Occa Berry",
            emoji = "🍓",
            category = ItemCategory.BERRY,
            effectDescription = "Reduce a la mitad (50%) el daño recibido de un ataque supereficaz de tipo Fuego (de un solo uso).",
            competitiveUsage = "Imprescindible en Pokémon de tipo Acero o Bicho como Scizor o Ferrothorn.",
            generation = 4
        ),
        CompetitiveItem(
            id = "yache_berry",
            nameEs = "Baya Payapa",
            nameEn = "Baya Yache",
            emoji = "🍇",
            category = ItemCategory.BERRY,
            effectDescription = "Reduce a la mitad (50%) el daño recibido de un ataque supereficaz de tipo Hielo (de un solo uso).",
            competitiveUsage = "Permite a dragones con debilidad x4 (Garchomp, Landorus, Dragonite) aguantar un Rayo Hielo.",
            generation = 4
        ),
        CompetitiveItem(
            id = "shuca_berry",
            nameEs = "Baya Pasio",
            nameEn = "Baya Pasio",
            emoji = "🍈",
            category = ItemCategory.BERRY,
            effectDescription = "Reduce a la mitad (50%) el daño recibido de un ataque supereficaz de tipo Agua (de un solo uso).",
            competitiveUsage = "Asegura a atacantes de Fuego o Tierra frente a Surf, Hidrobomba o Hidroariete.",
            generation = 4
        ),

        // ==================== CLIMA Y TERRENOS ====================
        CompetitiveItem(
            id = "damp_rock",
            nameEs = "Roca Lluvia",
            nameEn = "Damp Rock",
            emoji = "🌧️",
            category = ItemCategory.WEATHER,
            effectDescription = "Extiende la duración del clima de Lluvia provocado por Danza Lluvia o Llovizna de 5 a 8 turnos.",
            competitiveUsage = "Staple indiscutible en Pelipper o Politoed para arquetipos 'Rain Team'.",
            generation = 4
        ),
        CompetitiveItem(
            id = "heat_rock",
            nameEs = "Roca Calor",
            nameEn = "Heat Rock",
            emoji = "☀️",
            category = ItemCategory.WEATHER,
            effectDescription = "Extiende la duración del clima de Día Soleado provocado por Día Soleado o Sequía de 5 a 8 turnos.",
            competitiveUsage = "Imprescindible en Torkoal o Ninetales para equipos de Clorofila y Sol abrasador.",
            generation = 4
        ),
        CompetitiveItem(
            id = "terrain_extender",
            nameEs = "Cubresuelos",
            nameEn = "Terrain Extender",
            emoji = "🌾",
            category = ItemCategory.WEATHER,
            effectDescription = "Extiende la duración del Terreno (Eléctrico, Hierba, Psíquico, Niebla) de 5 a 8 turnos.",
            competitiveUsage = "Maximiza las ventajas de campo de Rillaboom (Terreno Hierba) o Pincurchin/Miraidon.",
            generation = 6
        )
    )
}
