package com.duoc.rotompedia.data

import androidx.compose.ui.graphics.Color

/**
 * Categoría de daño de un movimiento Pokémon.
 */
enum class MoveCategory(val displayName: String, val emoji: String, val color: Color) {
    PHYSICAL("Físico", "💥", Color(0xFFE53935)),
    SPECIAL("Especial", "🔮", Color(0xFF1E88E5)),
    STATUS("Estado", "🛡️", Color(0xFF757575))
}

/**
 * Modelo de datos para un movimiento de combate Pokémon.
 */
data class PokemonMove(
    val nameEn: String,
    val nameEs: String,
    val type: String,
    val category: MoveCategory,
    val power: Int? = null,
    val accuracy: Int? = null,
    val pp: Int = 15,
    val description: String = ""
)

/**
 * Catálogo maestro de movimientos competitivos canónicos (VGC, Singles y Cobblemon).
 */
object PokemonMoveData {

    val allMoves: List<PokemonMove> = listOf(
        // ==================== TIPO NORMAL ====================
        PokemonMove("Body Slam", "Golpe Cuerpo", "normal", MoveCategory.PHYSICAL, 85, 100, 15, "Puede paralizar al objetivo (30%)."),
        PokemonMove("Double-Edge", "Doble Filo", "normal", MoveCategory.PHYSICAL, 120, 100, 15, "El atacante recibe 1/3 de daño de retroceso."),
        PokemonMove("Hyper Voice", "Hipervoz", "normal", MoveCategory.SPECIAL, 90, 100, 10, "Ataque de sonido que atraviesa Sustituto."),
        PokemonMove("Fake Out", "Sorpresa", "normal", MoveCategory.PHYSICAL, 40, 100, 10, "Prioridad +3. Hace retroceder si se usa en el turno 1."),
        PokemonMove("Extreme Speed", "Velocidad Extrema", "normal", MoveCategory.PHYSICAL, 80, 100, 5, "Prioridad +2 letal para rematar enemigos."),
        PokemonMove("Protect", "Protección", "normal", MoveCategory.STATUS, null, null, 10, "Prioridad +4. Evita todo daño en ese turno."),
        PokemonMove("Substitute", "Sustituto", "normal", MoveCategory.STATUS, null, null, 10, "Crea un señuelo sacrificando 25% de PS."),
        PokemonMove("Swords Dance", "Danza Espada", "normal", MoveCategory.STATUS, null, null, 20, "Aumenta el Ataque Físico en 2 niveles (+2)."),
        PokemonMove("Facade", "Imagen", "normal", MoveCategory.PHYSICAL, 70, 100, 20, "Duplica potencia a 140 si está quemado, paralizado o envenenado."),
        PokemonMove("Recover", "Recuperación", "normal", MoveCategory.STATUS, null, null, 5, "Restaura el 50% de los PS máximos."),

        // ==================== TIPO FUEGO ====================
        PokemonMove("Flamethrower", "Lanzallamas", "fire", MoveCategory.SPECIAL, 90, 100, 15, "Consistente. 10% probabilidad de quemar."),
        PokemonMove("Fire Blast", "Llamarada", "fire", MoveCategory.SPECIAL, 110, 85, 5, "Gran potencia especial con 85% de precisión."),
        PokemonMove("Flare Blitz", "Envite Ígneo", "fire", MoveCategory.PHYSICAL, 120, 100, 15, "Potente golpe físico con 1/3 de retroceso."),
        PokemonMove("Heat Wave", "Onda Ígnea", "fire", MoveCategory.SPECIAL, 95, 90, 10, "Golpea a ambos rivales en combate doble."),
        PokemonMove("Overheat", "Sofoco", "fire", MoveCategory.SPECIAL, 130, 90, 5, "Baja el Ataque Especial del usuario en 2 niveles."),
        PokemonMove("Will-O-Wisp", "Fuego Fatuo", "fire", MoveCategory.STATUS, null, 85, 15, "Quema al objetivo reduciendo su ataque a la mitad."),

        // ==================== TIPO AGUA ====================
        PokemonMove("Scald", "Escaldar", "water", MoveCategory.SPECIAL, 80, 100, 15, "30% probabilidad de quemar al objetivo."),
        PokemonMove("Hydro Pump", "Hidrobomba", "water", MoveCategory.SPECIAL, 110, 80, 5, "Potente chorro de agua de alta potencia."),
        PokemonMove("Waterfall", "Cascada", "water", MoveCategory.PHYSICAL, 80, 100, 15, "Golpe físico con 20% probabilidad de retroceso."),
        PokemonMove("Liquidation", "Hidroariete", "water", MoveCategory.PHYSICAL, 85, 100, 10, "20% probabilidad de bajar la Defensa rival."),
        PokemonMove("Aqua Jet", "Aqua Jet", "water", MoveCategory.PHYSICAL, 40, 100, 20, "Ataque con prioridad +1."),
        PokemonMove("Surf", "Surf", "water", MoveCategory.SPECIAL, 90, 100, 15, "Golpea a todos los Pokémon en el campo."),
        PokemonMove("Flip Turn", "Viraje", "water", MoveCategory.PHYSICAL, 60, 100, 20, "Golpea y retira al usuario como pivot."),

        // ==================== TIPO PLANTA ====================
        PokemonMove("Energy Ball", "Energibola", "grass", MoveCategory.SPECIAL, 90, 100, 10, "10% probabilidad de bajar Defensa Especial."),
        PokemonMove("Leaf Storm", "Lluevehojas", "grass", MoveCategory.SPECIAL, 130, 90, 5, "Baja el Ataque Especial del usuario 2 niveles."),
        PokemonMove("Wood Hammer", "Mazazo", "grass", MoveCategory.PHYSICAL, 120, 100, 15, "Golpe brutal con 1/3 de retroceso."),
        PokemonMove("Leaf Blade", "Hoja Aguda", "grass", MoveCategory.PHYSICAL, 90, 100, 15, "Alto ratio de golpe crítico."),
        PokemonMove("Giga Drain", "Gigadrenado", "grass", MoveCategory.SPECIAL, 75, 100, 10, "Restaura la mitad del daño infligido."),
        PokemonMove("Spore", "Espora", "grass", MoveCategory.STATUS, null, 100, 15, "Duerme al objetivo con 100% de precisión."),
        PokemonMove("Leech Seed", "Drenadoras", "grass", MoveCategory.STATUS, null, 90, 10, "Drena 1/8 de PS del rival en cada turno."),

        // ==================== TIPO ELÉCTRICO ====================
        PokemonMove("Thunderbolt", "Rayo", "electric", MoveCategory.SPECIAL, 90, 100, 15, "Consistente. 10% probabilidad de paralizar."),
        PokemonMove("Thunder", "Trueno", "electric", MoveCategory.SPECIAL, 110, 70, 10, "100% de precisión bajo la Lluvia."),
        PokemonMove("Volt Switch", "Voltiocambio", "electric", MoveCategory.SPECIAL, 70, 100, 20, "Golpea y retira al Pokémon (Pivot)."),
        PokemonMove("Wild Charge", "Voltio Cruel", "electric", MoveCategory.PHYSICAL, 90, 100, 15, "Ataque físico con 1/4 de daño de retroceso."),
        PokemonMove("Thunder Wave", "Onda Trueno", "electric", MoveCategory.STATUS, null, 90, 20, "Paraliza al objetivo reduciendo su velocidad 50%."),
        PokemonMove("Thunder Punch", "Puño Trueno", "electric", MoveCategory.PHYSICAL, 75, 100, 15, "Puñetazo con 10% de parálisis."),

        // ==================== TIPO HIELO ====================
        PokemonMove("Ice Beam", "Rayo Hielo", "ice", MoveCategory.SPECIAL, 90, 100, 10, "10% probabilidad de congelar."),
        PokemonMove("Blizzard", "Ventisca", "ice", MoveCategory.SPECIAL, 110, 70, 5, "100% de precisión bajo Nieve/Granizo."),
        PokemonMove("Icicle Crash", "Chuzos", "ice", MoveCategory.PHYSICAL, 85, 90, 10, "30% probabilidad de hacer retroceder."),
        PokemonMove("Ice Shard", "Canto Helado", "ice", MoveCategory.PHYSICAL, 40, 100, 30, "Ataque con prioridad +1."),
        PokemonMove("Freeze-Dry", "Liofilización", "ice", MoveCategory.SPECIAL, 70, 100, 20, "Supereficaz también contra tipos Agua."),

        // ==================== TIPO LUCHA ====================
        PokemonMove("Close Combat", "A Bocajarro", "fighting", MoveCategory.PHYSICAL, 120, 100, 5, "Baja Defensa y Def. Especial 1 nivel."),
        PokemonMove("Drain Punch", "Puño Drenaje", "fighting", MoveCategory.PHYSICAL, 75, 100, 10, "Restaura 50% del daño infligido."),
        PokemonMove("Focus Blast", "Onda Certera", "fighting", MoveCategory.SPECIAL, 120, 70, 5, "Cañón especial de altísimo poder."),
        PokemonMove("Mach Punch", "Ultrapuño", "fighting", MoveCategory.PHYSICAL, 40, 100, 30, "Golpe de contacto con prioridad +1."),
        PokemonMove("Body Press", "Plancha Corporal", "fighting", MoveCategory.PHYSICAL, 80, 100, 10, "Calcula el daño usando la Defensa del usuario."),
        PokemonMove("Bulk Up", "Corpulencia", "fighting", MoveCategory.STATUS, null, null, 20, "Aumenta Ataque y Defensa en 1 nivel."),

        // ==================== TIPO VENENO ====================
        PokemonMove("Sludge Bomb", "Bomba Lodo", "poison", MoveCategory.SPECIAL, 90, 100, 10, "30% probabilidad de envenenar."),
        PokemonMove("Sludge Wave", "Onda Tóxica", "poison", MoveCategory.SPECIAL, 95, 100, 10, "Ataque de área con 10% de envenenar."),
        PokemonMove("Poison Jab", "Puya Nociva", "poison", MoveCategory.PHYSICAL, 80, 100, 20, "30% probabilidad de envenenar."),
        PokemonMove("Gunk Shot", "Lanza Mugre", "poison", MoveCategory.PHYSICAL, 120, 80, 5, "Gran impacto con 30% de envenenamiento."),
        PokemonMove("Toxic", "Tóxico", "poison", MoveCategory.STATUS, null, 90, 10, "Envenenamiento grave progresivo."),

        // ==================== TIPO TIERRA ====================
        PokemonMove("Earthquake", "Terremoto", "ground", MoveCategory.PHYSICAL, 100, 100, 10, "El rey del combate físico terrestre."),
        PokemonMove("Earth Power", "Tierra Viva", "ground", MoveCategory.SPECIAL, 90, 100, 10, "10% probabilidad de bajar Defensa Especial."),
        PokemonMove("Headlong Rush", "Asalto Espada", "ground", MoveCategory.PHYSICAL, 120, 100, 5, "Baja Def y Def Especial 1 nivel."),
        PokemonMove("Stealth Rock", "Trampa Rocas", "rock", MoveCategory.STATUS, null, null, 20, "Piedras trampa que dañan a cada relevo."),

        // ==================== TIPO VOLADOR ====================
        PokemonMove("Brave Bird", "Pájaro Osado", "flying", MoveCategory.PHYSICAL, 120, 100, 15, "1/3 de daño de retroceso al atacante."),
        PokemonMove("Air Slash", "Tajo Aéreo", "flying", MoveCategory.SPECIAL, 75, 95, 15, "30% probabilidad de retroceso."),
        PokemonMove("Hurricane", "Vendaval", "flying", MoveCategory.SPECIAL, 110, 70, 10, "100% de precisión bajo la Lluvia."),
        PokemonMove("Roost", "Respiro", "flying", MoveCategory.STATUS, null, null, 10, "Recupera 50% de PS y pierde tipo Volador ese turno."),
        PokemonMove("Tailwind", "Viento Afín", "flying", MoveCategory.STATUS, null, null, 15, "Duplica la Velocidad del equipo por 4 turnos."),

        // ==================== TIPO PSÍQUICO ====================
        PokemonMove("Psychic", "Psíquico", "psychic", MoveCategory.SPECIAL, 90, 100, 10, "10% probabilidad de bajar Defensa Especial."),
        PokemonMove("Psyshock", "Psicocarga", "psychic", MoveCategory.SPECIAL, 80, 100, 10, "Ataque especial que daña según la Defensa física."),
        PokemonMove("Zen Headbutt", "Cabezazo Zen", "psychic", MoveCategory.PHYSICAL, 80, 90, 15, "20% probabilidad de hacer retroceder."),
        PokemonMove("Calm Mind", "Paz Mental", "psychic", MoveCategory.STATUS, null, null, 20, "Sube Atq. Especial y Def. Especial en +1."),
        PokemonMove("Trick Room", "Espacio Raro", "psychic", MoveCategory.STATUS, null, null, 5, "Los Pokémon más lentos se mueven primero."),

        // ==================== TIPO BICHO ====================
        PokemonMove("U-turn", "Ida y Vuelta", "bug", MoveCategory.PHYSICAL, 70, 100, 20, "Ataca y permite cambiar de Pokémon inmediatamente."),
        PokemonMove("Bug Buzz", "Zumbido", "bug", MoveCategory.SPECIAL, 90, 100, 10, "Atraviesa sustitutos de los rivales."),
        PokemonMove("X-Scissor", "Tijera X", "bug", MoveCategory.PHYSICAL, 80, 100, 15, "Corte físico limpio y consistente."),
        PokemonMove("Quiver Dance", "Danza Aleteo", "bug", MoveCategory.STATUS, null, null, 20, "Sube Atq. Esp, Def. Esp y Velocidad +1."),

        // ==================== TIPO ROCA ====================
        PokemonMove("Stone Edge", "Roca Afilada", "rock", MoveCategory.PHYSICAL, 100, 80, 5, "Elevado índice de golpe crítico."),
        PokemonMove("Rock Slide", "Avalancha", "rock", MoveCategory.PHYSICAL, 75, 90, 10, "Golpea a ambos rivales con 30% de retroceso."),
        PokemonMove("Power Gem", "Joya de Luz", "rock", MoveCategory.SPECIAL, 80, 100, 20, "Roca especial precisa y consistente."),

        // ==================== TIPO FANTASMA ====================
        PokemonMove("Shadow Ball", "Bola Sombra", "ghost", MoveCategory.SPECIAL, 80, 100, 15, "20% probabilidad de bajar Defensa Especial."),
        PokemonMove("Shadow Claw", "Garra Umbría", "ghost", MoveCategory.PHYSICAL, 70, 100, 15, "Alto ratio de golpe crítico."),
        PokemonMove("Shadow Sneak", "Sombra Vil", "ghost", MoveCategory.PHYSICAL, 40, 100, 30, "Ataque fantasmal con prioridad +1."),
        PokemonMove("Poltergeist", "Poltergeist", "ghost", MoveCategory.PHYSICAL, 110, 90, 5, "Utiliza el objeto del rival para infligir gran daño."),

        // ==================== TIPO DRAGÓN ====================
        PokemonMove("Dragon Claw", "Garra Dragón", "dragon", MoveCategory.PHYSICAL, 80, 100, 15, "Zarpazo dragón muy consistente."),
        PokemonMove("Draco Meteor", "Cometa Draco", "dragon", MoveCategory.SPECIAL, 130, 90, 5, "Impacto astronómico. Baja Atq. Esp 2 niveles."),
        PokemonMove("Dragon Pulse", "Pulso Dragón", "dragon", MoveCategory.SPECIAL, 85, 100, 10, "Haz dragón puro sin efectos negativos."),
        PokemonMove("Outrage", "Enfado", "dragon", MoveCategory.PHYSICAL, 120, 100, 10, "Ataca durante 2 o 3 turnos y confunde al usuario."),
        PokemonMove("Dragon Dance", "Danza Dragón", "dragon", MoveCategory.STATUS, null, null, 20, "Aumenta Ataque Físico y Velocidad en +1."),

        // ==================== TIPO SINIESTRO ====================
        PokemonMove("Knock Off", "Desarme", "dark", MoveCategory.PHYSICAL, 65, 100, 20, "Inflige x1.5 de daño y quita el objeto del rival."),
        PokemonMove("Crunch", "Triturar", "dark", MoveCategory.PHYSICAL, 80, 100, 15, "20% probabilidad de bajar la Defensa rival."),
        PokemonMove("Dark Pulse", "Pulso Umbrío", "dark", MoveCategory.SPECIAL, 80, 100, 15, "20% probabilidad de hacer retroceder."),
        PokemonMove("Sucker Punch", "Golpe Bajo", "dark", MoveCategory.PHYSICAL, 70, 100, 5, "Prioridad +1 si el objetivo va a atacar."),
        PokemonMove("Taunt", "Mofa", "dark", MoveCategory.STATUS, null, 100, 20, "Impide al rival usar movimientos de estado por 3 turnos."),

        // ==================== TIPO ACERO ====================
        PokemonMove("Iron Head", "Cabeza de Hierro", "steel", MoveCategory.PHYSICAL, 80, 100, 15, "30% probabilidad de hacer retroceder."),
        PokemonMove("Flash Cannon", "Foco Resplandor", "steel", MoveCategory.SPECIAL, 80, 100, 10, "10% probabilidad de bajar Defensa Especial."),
        PokemonMove("Heavy Slam", "Cuerpo Pesado", "steel", MoveCategory.PHYSICAL, 100, 100, 10, "Daño basado en la diferencia de peso."),
        PokemonMove("Bullet Punch", "Puño Bala", "steel", MoveCategory.PHYSICAL, 40, 100, 30, "Puño metálico con prioridad +1."),

        // ==================== TIPO HADA ====================
        PokemonMove("Moonblast", "Fuerza Lunar", "fairy", MoveCategory.SPECIAL, 95, 100, 15, "30% probabilidad de bajar el Ataque Especial."),
        PokemonMove("Dazzling Gleam", "Brillo Mágico", "fairy", MoveCategory.SPECIAL, 80, 100, 10, "Golpea a ambos enemigos en combate doble."),
        PokemonMove("Play Rough", "Carantoña", "fairy", MoveCategory.PHYSICAL, 90, 90, 10, "10% probabilidad de bajar el Ataque rival.")
    )

    fun findMove(nameOrQuery: String): PokemonMove? {
        val q = nameOrQuery.trim().lowercase()
        return allMoves.find {
            it.nameEn.equals(q, ignoreCase = true) ||
            it.nameEs.equals(q, ignoreCase = true) ||
            it.nameEn.replace(" ", "").equals(q.replace(" ", ""), ignoreCase = true)
        }
    }
}
