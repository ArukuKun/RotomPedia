package com.duoc.rotompedia.util

/**
 * Motor de comentarios tácticos, curiosidades y ocurrencias humorísticas de Rotom
 * para la ficha detallada de la Pokédex (RotomDex).
 */
object RotomCommentaryEngine {

    /**
     * Lista de IDs de Pokémon favoritos del Developer.
     * Actualmente configurado con Gardevoir (#282). En el futuro se expandirá con la lista del usuario.
     */
    val DEVELOPER_FAVORITES: Set<Int> = setOf(282)

    fun isDeveloperFavorite(pokemonId: Int): Boolean = pokemonId in DEVELOPER_FAVORITES

    /**
     * Devuelve una lista de frases temáticas de Rotom para el Pokémon consultado.
     */
    fun getComments(
        pokemonId: Int,
        pokemonName: String,
        types: List<String>,
        hp: Int = 0,
        attack: Int = 0,
        defense: Int = 0,
        specialAttack: Int = 0,
        specialDefense: Int = 0,
        speed: Int = 0,
        rotomForm: RotomForm = RotomForm.NORMAL
    ): List<String> {
        val comments = mutableListOf<String>()
        val nameCap = pokemonName.replaceFirstChar { it.uppercase() }
        val bst = hp + attack + defense + specialAttack + specialDefense + speed

        // 1. Detección especial de Favoritos del Developer (Efecto "Shiny" de probabilidad / rareza)
        if (isDeveloperFavorite(pokemonId)) {
            comments.add("✨ [DATO SHINY DEL DEVELOPER] ✨ ¡Bzzzzzt! ¡Mis sensores de plasma brillan con chispas doradas! $nameCap está marcada en el núcleo de la app como el Pokémon predilecto del Developer... ¡Trátala con máximo respeto, zzt!")
            comments.add("✨ ¡Lealtad legendaria! Gardevoir es capaz de crear un agujero negro diminuto para proteger a su entrenador. ¡Elegancia y poder místico en su estado más puro!")
            comments.add("✨ ¡Competitivamente temible! Con su combinación Psíquico / Hada y acceso a Fuerza Lunar, Paz Mental y Bola Sombra, es una auténtica amenaza en combate.")
        }

        // 2. Easter Eggs y Pokémon Específicos
        when (pokemonId) {

            // Kirlia / Ralts / Gallade
            280, 281 -> {
                comments.add("¡Bzzzzt! De la línea evolutiva predilecta del creador de la app. ¡Entrénalo con cariño y evolucionará en una magnífica Gardevoir!")
            }
            475 -> {
                comments.add("¡Gallade! La contraparte guerrera con espadas en los codos. Si le das una Piedra Alba a Kirlia macho, ¡tienes un maestro del combate cuerpo a cuerpo!")
            }

            // ⭐ PIKACHU
            25 -> {
                comments.add("¡Bzzzt! ¡Pikachu, el Pokémon ratón! Es de tipo Eléctrico... ¡como yo, colega! Aunque honestamente, mi chispa de plasma tiene un toque más moderno, zzt.")
                comments.add("¡Ojo con sus mejillas rojas! Almacenan electricidad estática y cuando se reúnen en manadas pueden provocar tormentas eléctricas.")
                comments.add("¡Un clásico mundial! Con una Bola Luminosa equipada, duplica su Ataque y Ataque Especial. ¡Poder de bolsillo!")
            }

            // ⭐ ROTOM (Él mismo)
            479 -> {
                comments.add("¡¡OYE, PERO SI ESE SOY YO!! ¡El Pokémon más multifacético, carismático y tecnológico de toda la creación, zzzzt! ⚡⚡")
                comments.add("¡Mírame qué guapo salgo en la ficha! Entro en motores de electrodomésticos y cambio de tipo al instante. ¡Soy una maravilla de la ciencia!")
                comments.add("¡Consejo de mí para ti: usa el selector de formas en el menú principal para vestirme con mis mejores galas de batalla, zzt!")
            }

            // CHARIZARD
            6 -> {
                comments.add("¡Fuego puro, zzt! ¡Asegúrate de no tocarle la cola, quema incluso más que el termostato de mi forma Rotom Calor!")
                comments.add("¡El dragón que no era dragón... hasta que consiguió sus megaevoluciones y teracristalización! Un ícono indiscutible.")
            }

            // GENGAR
            94 -> {
                comments.add("¡Bzz-¡aaah! ¡Un tipo Fantasma travieso! Aunque yo también soy mitad fantasma, ¡su sonrisa burlona en la penumbra me da escalofríos, zzt!")
                comments.add("¡Si sientes un escalofrío repentino en la habitación, es que un Gengar acaba de pasar a tu lado absorbiendo el calor!")
            }

            // MAGIKARP
            129 -> {
                comments.add("¡Plop, plop! Dicen que es el Pokémon más patético del mundo... ¡pero no te rías tanto, que cuando salte a Gyarados no habrá quien lo pare!")
                comments.add("¡Salpicadura no hace nada, zzt! Pero tener la paciencia de subirlo al nivel 20 forja el carácter de cualquier verdadero entrenador.")
            }

            // EEVEE
            133 -> {
                comments.add("¡Increíble potencial genético, zzt! ¡Tiene nada menos que 8 evoluciones posibles! ¿Cuál es tu Eeveelution favorita?")
                comments.add("¡Su ADN es tan inestable que muta con piedras, amistad, afecto o según el momento del día. ¡Una maravilla biológica!")
            }

            // MEWTWO
            150 -> {
                comments.add("¡Alerta máxima, zzzzt! Lecturas de energía psíquica fuera de escala. Nació de experimentos genéticos... ¡no querrás hacerlo enfadar!")
                comments.add("¡Uno de los seres más temibles de Kanto! Su velocidad y poder especial son leyenda viva.")
            }

            // SNORLAX
            143 -> {
                comments.add("¡Bzzzz... Zzz... Oh, perdón! Es que ver a Snorlax me dio sueño. ¡Pesa más de 460 kg y come 400 kg de comida al día, qué envidia, zzt!")
                comments.add("¡Si bloquea el camino, vas a necesitar una Poké Flauta sí o sí! Despertarlo de mal humor no es una buena idea.")
            }

            // GARCHOMP
            445 -> {
                comments.add("¡Cuidado con las turbinas dorsales! Este tiburón terrestre vuela tan rápido como un avión a reacción militar. ¡El terror de Sinnoh, zzt!")
                comments.add("¡Terremoto y Garra Dragón listos! Pregúntale a Cintia cuántos aspirantes a campeón sucumbieron ante este coloso.")
            }

            // LUCARIO
            448 -> {
                comments.add("¡Siente el aura, zzt! Puede percibir los sentimientos y pensamientos de los humanos a más de un kilómetro de distancia.")
                comments.add("¡Esfera Aural nunca falla el blanco! Su combinación Lucha/Acero le otorga una cantidad ridícula de resistencias elementales.")
            }

            // DITTO
            132 -> {
                comments.add("¡Una masa gelatinosa con una sonrisa boba capaz de transformarse en cualquier criatura! En la guardería de crianza es una leyenda viviente, zzt.")
            }

            // RAYQUAZA
            384 -> {
                comments.add("¡Vive en la capa de ozono alimentándose de meteoritos espaciales! ¡Eso sí que es una dieta rica en minerales, zzzzt!")
            }

            // GRENINJA
            658 -> {
                comments.add("¡El sigiloso ninja de Kalos! Lanza shurikens de agua a presión... ¡y esa bufanda rosa que lleva es en realidad su propia lengua, zzzzt!")
            }

            // MIMIKYU
            778 -> {
                comments.add("¡Se disfraza de Pikachu con un saco viejo porque solo quiere hacer amigos y que lo quieran! ¡A mí me parece adorable, zzt!")
            }
        }

        // 2. Sinergia elemental con la forma actual de Rotom
        types.forEach { tipo ->
            when {
                rotomForm == RotomForm.LAVADO && tipo.equals("water", ignoreCase = true) -> {
                    comments.add("¡Tipo Agua detectado! Con mi turbina hidro de Rotom Lavado haríamos una combinación acuática imparable, zzt. 💧")
                }
                rotomForm == RotomForm.CALOR && tipo.equals("fire", ignoreCase = true) -> {
                    comments.add("¡Fuego elemental! ¡Siento cómo arde el termostato de mi forma Rotom Calor en sintonía con $nameCap! 🔥")
                }
                rotomForm == RotomForm.CORTE && tipo.equals("grass", ignoreCase = true) -> {
                    comments.add("¡Poder vegetal! Con mi cortacésped de Rotom Corte nos llevaríamos de maravilla en el jardín, zzt. 🌱")
                }
                rotomForm == RotomForm.FRIO && tipo.equals("ice", ignoreCase = true) -> {
                    comments.add("¡Brrrr! ¡Temperatura bajo cero! Mi forma Rotom Frío se siente como en casa al lado de $nameCap. ❄️")
                }
                rotomForm == RotomForm.VENTILADOR && tipo.equals("flying", ignoreCase = true) -> {
                    comments.add("¡Corrientes de aire ascendentes! ¡Mi forma Rotom Ventilador está lista para planear junto a $nameCap! 🌪️")
                }
                rotomForm == RotomForm.NORMAL && tipo.equals("electric", ignoreCase = true) && pokemonId != 25 && pokemonId != 479 -> {
                    comments.add("¡Camarada de altos voltajes! El tipo Eléctrico es el más enérgico del mundo Pokémon... ¡chispas garantizadas, zzt! ⚡")
                }
            }
        }

        // 3. Comentarios por perfil estadístico competitivo
        when {
            bst >= 600 -> {
                comments.add("¡Lecturas de combate estratosféricas! Este Pokémon tiene un BST de $bst puntos... ¡un auténtico titán de nivel pseudo-legendario o superior, zzt!")
            }
            speed >= 120 -> {
                comments.add("¡Velocidad supersónica ($speed pts)! Se moverá primero en casi cualquier combate antes de que el rival pueda parpadear.")
            }
            attack >= 130 -> {
                comments.add("¡Fuerza bruta extrema! Con $attack de Ataque físico, sus golpes romperán cualquier barrera defensiva.")
            }
            specialAttack >= 130 -> {
                comments.add("¡Potencia especial desbordante ($specialAttack pts)! Sus ataques mágicos y elementales causan estragos masivos.")
            }
            defense >= 130 || specialDefense >= 130 -> {
                comments.add("¡Una fortaleza andante! Sus defensas superan los 130 puntos; vencerlo requerirá daño súper-eficaz bien calculado, zzt.")
            }
        }

        // 4. Frases generales tácticas de Rotom si la lista es corta
        if (comments.isEmpty()) {
            comments.add("¡Bzzzzt! Analizando a $nameCap... Su combinación elemental de ${types.joinToString(" y ") { traducirTipo(it) }} ofrece posibilidades tácticas muy interesantes.")
            comments.add("¡Recuerda revisar la tabla de daño dual más abajo para planificar tus resistencias y debilidades con $nameCap!")
            comments.add("¡Dato Rotom: llévalo a la Calculadora IV/EV desde el menú principal para descubrir su máximo potencial competitivo al Nivel 50!")
        }

        return comments
    }
}
