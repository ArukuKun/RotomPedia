package com.duoc.rotompedia.util

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Motor oficial de cálculo de estadísticas Pokémon, naturalezas, IVs y EVs.
 * Fórmulas canónicas de la franquicia principal (VGC y Competitivo Estándar).
 */
object StatCalculator {

    data class Nature(
        val nameEs: String,
        val nameEn: String,
        val increasedStat: StatType?,
        val decreasedStat: StatType?
    ) {
        val displayName: String
            get() = if (increasedStat == null || decreasedStat == null) {
                "$nameEs (Neutra)"
            } else {
                "$nameEs (+${increasedStat.shortName}, -${decreasedStat.shortName})"
            }

        fun getMultiplier(statType: StatType): Float {
            return when {
                statType == StatType.HP -> 1.0f
                statType == increasedStat && statType == decreasedStat -> 1.0f
                statType == increasedStat -> 1.1f
                statType == decreasedStat -> 0.9f
                else -> 1.0f
            }
        }
    }

    enum class StatType(val shortName: String, val fullNameEs: String) {
        HP("PS", "Puntos de Salud"),
        ATTACK("Atq", "Ataque"),
        DEFENSE("Def", "Defensa"),
        SPECIAL_ATTACK("At. Esp", "Ataque Especial"),
        SPECIAL_DEFENSE("Def. Esp", "Defensa Especial"),
        SPEED("Vel", "Velocidad")
    }

    val ALL_NATURES: List<Nature> = listOf(
        // Neutras
        Nature("Fuerte", "Hardy", null, null),
        Nature("Dócil", "Docile", null, null),
        Nature("Tímida", "Bashful", null, null),
        Nature("Seria", "Quirky", null, null),
        Nature("Rara", "Serious", null, null),

        // +Ataque
        Nature("Firme", "Adamant", StatType.ATTACK, StatType.SPECIAL_ATTACK),
        Nature("Pícara", "Naughty", StatType.ATTACK, StatType.SPECIAL_DEFENSE),
        Nature("Audaz", "Brave", StatType.ATTACK, StatType.SPEED),
        Nature("Huraña", "Lonely", StatType.ATTACK, StatType.DEFENSE),

        // +Defensa
        Nature("Osada", "Bold", StatType.DEFENSE, StatType.ATTACK),
        Nature("Agitada", "Impish", StatType.DEFENSE, StatType.SPECIAL_ATTACK),
        Nature("Floja", "Lax", StatType.DEFENSE, StatType.SPECIAL_DEFENSE),
        Nature("Plácida", "Relaxed", StatType.DEFENSE, StatType.SPEED),

        // +Ataque Especial
        Nature("Modesta", "Modest", StatType.SPECIAL_ATTACK, StatType.ATTACK),
        Nature("Afable", "Mild", StatType.SPECIAL_ATTACK, StatType.DEFENSE),
        Nature("Alocada", "Rash", StatType.SPECIAL_ATTACK, StatType.SPECIAL_DEFENSE),
        Nature("Mansa", "Quiet", StatType.SPECIAL_ATTACK, StatType.SPEED),

        // +Defensa Especial
        Nature("Serena", "Calm", StatType.SPECIAL_DEFENSE, StatType.ATTACK),
        Nature("Amable", "Gentle", StatType.SPECIAL_DEFENSE, StatType.DEFENSE),
        Nature("Cauta", "Careful", StatType.SPECIAL_DEFENSE, StatType.SPECIAL_ATTACK),
        Nature("Grosera", "Sassy", StatType.SPECIAL_DEFENSE, StatType.SPEED),

        // +Velocidad
        Nature("Miedosa", "Timid", StatType.SPEED, StatType.ATTACK),
        Nature("Alegre", "Jolly", StatType.SPEED, StatType.SPECIAL_ATTACK),
        Nature("Activa", "Hasty", StatType.SPEED, StatType.DEFENSE),
        Nature("Ingenua", "Naive", StatType.SPEED, StatType.SPECIAL_DEFENSE)
    )

    fun getNatureByName(name: String): Nature {
        return ALL_NATURES.find {
            it.nameEs.equals(name, ignoreCase = true) || it.nameEn.equals(name, ignoreCase = true)
        } ?: ALL_NATURES.first()
    }

    data class StatSpread(
        val hp: Int = 0,
        val atk: Int = 0,
        val def: Int = 0,
        val spa: Int = 0,
        val spd: Int = 0,
        val spe: Int = 0
    ) {
        val total: Int get() = hp + atk + def + spa + spd + spe
    }

    data class CalculatedStats(
        val hp: Int,
        val atk: Int,
        val def: Int,
        val spa: Int,
        val spd: Int,
        val spe: Int
    ) {
        val totalBST: Int get() = hp + atk + def + spa + spd + spe
    }

    /**
     * Calcula la estadística de Puntos de Salud (HP).
     * Fórmula oficial: floor(((2 * Base + IV + floor(EV / 4)) * Level) / 100) + Level + 10
     * Caso especial: Shedinja (id 292 o baseHp == 1) siempre tiene 1 HP.
     */
    fun calculateHp(base: Int, iv: Int, ev: Int, level: Int, isShedinja: Boolean = false): Int {
        if (isShedinja || base == 1) return 1
        val safeIv = iv.coerceIn(0, 31)
        val safeEv = ev.coerceIn(0, 252)
        val safeLevel = level.coerceIn(1, 100)

        val raw = ((2 * base + safeIv + (safeEv / 4)) * safeLevel) / 100
        return raw + safeLevel + 10
    }

    /**
     * Calcula cualquier estadística diferente a HP (Ataque, Defensa, Atq Esp, Def Esp, Velocidad).
     * Fórmula oficial: floor((floor(((2 * Base + IV + floor(EV / 4)) * Level) / 100) + 5) * NatureMultiplier)
     */
    fun calculateOtherStat(
        base: Int,
        iv: Int,
        ev: Int,
        level: Int,
        natureMultiplier: Float
    ): Int {
        val safeIv = iv.coerceIn(0, 31)
        val safeEv = ev.coerceIn(0, 252)
        val safeLevel = level.coerceIn(1, 100)

        val raw = ((2 * base + safeIv + (safeEv / 4)) * safeLevel) / 100 + 5
        return floor(raw * natureMultiplier).toInt()
    }

    /**
     * Calcula las 6 estadísticas completas simultáneamente.
     */
    fun calculateAll(
        baseStats: StatSpread,
        ivs: StatSpread = StatSpread(31, 31, 31, 31, 31, 31),
        evs: StatSpread = StatSpread(0, 0, 0, 0, 0, 0),
        level: Int = 50,
        nature: Nature = ALL_NATURES.first(),
        isShedinja: Boolean = false
    ): CalculatedStats {
        return CalculatedStats(
            hp = calculateHp(baseStats.hp, ivs.hp, evs.hp, level, isShedinja),
            atk = calculateOtherStat(baseStats.atk, ivs.atk, evs.atk, level, nature.getMultiplier(StatType.ATTACK)),
            def = calculateOtherStat(baseStats.def, ivs.def, evs.def, level, nature.getMultiplier(StatType.DEFENSE)),
            spa = calculateOtherStat(baseStats.spa, ivs.spa, evs.spa, level, nature.getMultiplier(StatType.SPECIAL_ATTACK)),
            spd = calculateOtherStat(baseStats.spd, ivs.spd, evs.spd, level, nature.getMultiplier(StatType.SPECIAL_DEFENSE)),
            spe = calculateOtherStat(baseStats.spe, ivs.spe, evs.spe, level, nature.getMultiplier(StatType.SPEED))
        )
    }

    /**
     * Genera la cadena de texto compatible al 100% con Pokémon Showdown.
     */
    fun toShowdownFormat(
        speciesName: String,
        item: String? = null,
        ability: String? = null,
        level: Int = 50,
        nature: Nature,
        evs: StatSpread,
        ivs: StatSpread
    ): String {
        val sb = StringBuilder()
        val header = if (!item.isNullOrBlank()) "$speciesName @ $item" else speciesName
        sb.appendLine(header)
        if (!ability.isNullOrBlank()) {
            sb.appendLine("Ability: $ability")
        }
        sb.appendLine("Level: $level")

        // EVs (solo los que sean > 0)
        val evList = mutableListOf<String>()
        if (evs.hp > 0) evList.add("${evs.hp} HP")
        if (evs.atk > 0) evList.add("${evs.atk} Atk")
        if (evs.def > 0) evList.add("${evs.def} Def")
        if (evs.spa > 0) evList.add("${evs.spa} SpA")
        if (evs.spd > 0) evList.add("${evs.spd} SpD")
        if (evs.spe > 0) evList.add("${evs.spe} Spe")
        if (evList.isNotEmpty()) {
            sb.appendLine("EVs: ${evList.joinToString(" / ")}")
        }

        sb.appendLine("${nature.nameEn} Nature")

        // IVs (solo si alguno es < 31)
        val ivList = mutableListOf<String>()
        if (ivs.hp < 31) ivList.add("${ivs.hp} HP")
        if (ivs.atk < 31) ivList.add("${ivs.atk} Atk")
        if (ivs.def < 31) ivList.add("${ivs.def} Def")
        if (ivs.spa < 31) ivList.add("${ivs.spa} SpA")
        if (ivs.spd < 31) ivList.add("${ivs.spd} SpD")
        if (ivs.spe < 31) ivList.add("${ivs.spe} Spe")
        if (ivList.isNotEmpty()) {
            sb.appendLine("IVs: ${ivList.joinToString(" / ")}")
        }

        return sb.toString().trimEnd()
    }
}
