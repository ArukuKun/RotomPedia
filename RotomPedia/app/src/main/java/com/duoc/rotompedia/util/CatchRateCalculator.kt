package com.duoc.rotompedia.util

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Motor canónico de cálculo de ratio de captura de Pokémon (Fórmulas oficiales Gen 8 / 9).
 */
object CatchRateCalculator {

    enum class PokeBall(
        val displayName: String,
        val emoji: String,
        val description: String,
        val baseMultiplier: Float
    ) {
        POKE_BALL("Poké Ball", "🔴", "Eficacia base estándar (1.0x)", 1.0f),
        GREAT_BALL("Súper Ball", "🔵", "Eficacia media mejorada (1.5x)", 1.5f),
        ULTRA_BALL("Ultra Ball", "🟡", "Alto rendimiento garantizado (2.0x)", 2.0f),
        MASTER_BALL("Master Ball", "🟣", "Captura infalible e instantánea (100%)", 255.0f),
        QUICK_BALL("Quick Ball", "⚡", "5.0x si se utiliza en el Turno 1", 5.0f),
        DUSK_BALL("Ocaso Ball", "🌙", "3.0x de noche o dentro de cuevas", 3.0f),
        NET_BALL("Malla Ball", "🕸️", "3.5x frente a tipos Agua o Bicho", 3.5f),
        DIVE_BALL("Buceo Ball", "🌊", "3.5x bajo el agua o surfeando", 3.5f),
        REPEAT_BALL("Acopio Ball", "🔄", "3.5x si la especie ya fue capturada", 3.5f),
        TIMER_BALL("Turno Ball", "⏱️", "Hasta 4.0x a medida que avanzan los turnos", 1.0f),
        FAST_BALL("Veloz Ball", "💨", "4.0x frente a especies con Velocidad Base ≥ 100", 4.0f),
        HEAVY_BALL("Peso Ball", "⚖️", "Modificador plano según el peso del objetivo", 1.0f)
    }

    enum class StatusCondition(
        val displayName: String,
        val emoji: String,
        val multiplier: Float
    ) {
        NONE("Saludable", "⚪", 1.0f),
        SLEEP("Dormido", "💤", 2.5f),
        FREEZE("Congelado", "❄️", 2.5f),
        PARALYSIS("Paralizado", "⚡", 1.5f),
        POISON("Envenenado", "☠️", 1.5f),
        BURN("Quemado", "🔥", 1.5f)
    }

    data class CatchResult(
        val modifiedCatchRate: Int,
        val singleThrowProbPercent: Float,
        val expectedThrows: Float,
        val prob3ThrowsPercent: Float,
        val prob5ThrowsPercent: Float,
        val prob10ThrowsPercent: Float,
        val shakeCheckValue: Int,
        val rotomTip: String
    )

    fun calculate(
        baseCatchRate: Int,
        maxHp: Int = 100,
        currentHp: Int = 100,
        ball: PokeBall = PokeBall.POKE_BALL,
        status: StatusCondition = StatusCondition.NONE,
        isFirstTurn: Boolean = false,
        isNightOrCave: Boolean = false,
        isWaterOrBug: Boolean = false,
        isAlreadyCaught: Boolean = false,
        turnsPassed: Int = 1,
        targetBaseSpeed: Int = 60
    ): CatchResult {
        if (ball == PokeBall.MASTER_BALL) {
            return CatchResult(
                modifiedCatchRate = 255,
                singleThrowProbPercent = 100f,
                expectedThrows = 1f,
                prob3ThrowsPercent = 100f,
                prob5ThrowsPercent = 100f,
                prob10ThrowsPercent = 100f,
                shakeCheckValue = 65536,
                rotomTip = "¡Zzzzt! ¡La Master Ball nunca falla! Captura 100% garantizada."
            )
        }

        val ballMultiplier: Float = when (ball) {
            PokeBall.QUICK_BALL -> if (isFirstTurn) 5.0f else 1.0f
            PokeBall.DUSK_BALL -> if (isNightOrCave) 3.0f else 1.0f
            PokeBall.NET_BALL -> if (isWaterOrBug) 3.5f else 1.0f
            PokeBall.DIVE_BALL -> 3.5f
            PokeBall.REPEAT_BALL -> if (isAlreadyCaught) 3.5f else 1.0f
            PokeBall.TIMER_BALL -> min(4.0f, 1.0f + (turnsPassed * 0.3f))
            PokeBall.FAST_BALL -> if (targetBaseSpeed >= 100) 4.0f else 1.0f
            else -> ball.baseMultiplier
        }

        val safeCurrentHp = max(1, min(currentHp, maxHp))
        val safeMaxHp = max(1, maxHp)
        val rate = max(1, min(255, baseCatchRate))

        val hpFactor = (3.0 * safeMaxHp - 2.0 * safeCurrentHp) / (3.0 * safeMaxHp)
        val rawA = hpFactor * rate * ballMultiplier * status.multiplier
        val a = max(1, min(255, floor(rawA).toInt()))

        if (a >= 255) {
            return CatchResult(
                modifiedCatchRate = 255,
                singleThrowProbPercent = 100f,
                expectedThrows = 1f,
                prob3ThrowsPercent = 100f,
                prob5ThrowsPercent = 100f,
                prob10ThrowsPercent = 100f,
                shakeCheckValue = 65536,
                rotomTip = "¡El ratio modificado alcanzó el tope (255)! Captura asegurada en este turno."
            )
        }

        val b = floor(65536.0 * (a.toDouble() / 255.0).pow(0.75)).toInt()
        val singleShakeProb = (b.toDouble() / 65536.0)
        val p = singleShakeProb.pow(4.0).toFloat().coerceIn(0.0001f, 1.0f)

        val probPercent = (p * 100f).coerceIn(0.1f, 100f)
        val expectedThrows = if (p > 0f) (1.0f / p).coerceIn(1f, 99f) else 99f

        val prob3Throws = ((1.0 - (1.0 - p).pow(3.0)) * 100.0).toFloat().coerceIn(0f, 100f)
        val prob5Throws = ((1.0 - (1.0 - p).pow(5.0)) * 100.0).toFloat().coerceIn(0f, 100f)
        val prob10Throws = ((1.0 - (1.0 - p).pow(10.0)) * 100.0).toFloat().coerceIn(0f, 100f)

        val tip = when {
            probPercent >= 80f -> "¡Señales excelentes, zzt! Tienes una probabilidad altísima de atraparlo ya."
            probPercent >= 50f -> "¡Buenas probabilidades! En promedio necesitarás entre 1 y 2 lanzamientos."
            status == StatusCondition.NONE && safeCurrentHp > 30 ->
                "¡Alerta Rotom! Reduce sus PS al área roja o induce Sueño/Parálisis para triplicar tus chances."
            status == StatusCondition.NONE ->
                "¡El Pokémon está debilitado! Si lo pones a Dormir 💤 tus opciones aumentarán 2.5 veces."
            ball == PokeBall.POKE_BALL && baseCatchRate <= 45 ->
                "¡Este Pokémon es esquivo (ratio $baseCatchRate)! Cambia a Ultra Ball o Ocaso Ball para mayor éxito."
            else ->
                "¡Persistencia, entrenador! Mantén la presión, las probabilidades acumuladas suben con cada intento."
        }

        return CatchResult(
            modifiedCatchRate = a,
            singleThrowProbPercent = probPercent,
            expectedThrows = expectedThrows,
            prob3ThrowsPercent = prob3Throws,
            prob5ThrowsPercent = prob5Throws,
            prob10ThrowsPercent = prob10Throws,
            shakeCheckValue = b,
            rotomTip = tip
        )
    }

    fun getDefaultCatchRate(pokemonId: Int): Int {
        return when (pokemonId) {
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            152, 155, 158, 252, 255, 258,
            387, 390, 393, 495, 498, 501,
            650, 653, 656, 722, 725, 728,
            810, 813, 816, 906, 909, 912 -> 45

            144, 145, 146, 150, 151,
            243, 244, 245, 249, 250, 251,
            377, 378, 379, 380, 381, 382, 383, 384, 385, 386,
            480, 481, 482, 483, 484, 485, 486, 487, 488, 491, 492, 493,
            643, 644, 646, 716, 717, 718, 785, 786, 787, 788,
            789, 790, 791, 792, 800, 888, 889, 890, 1007, 1008 -> 3

            10, 11, 13, 14, 16, 19, 21, 23, 27, 29, 32, 41, 43, 69, 74,
            161, 163, 165, 167, 263, 265, 399, 401, 504, 506, 659, 731, 819, 915 -> 255

            25, 26, 35, 37, 39, 52, 54, 58, 60, 63, 66, 72, 77, 79, 81, 92, 98, 100, 129 -> 190
            133, 147, 148, 149, 246, 371, 443, 633, 704, 782, 885, 996 -> 45
            143, 131, 113 -> 30
            else -> 60
        }
    }
}
