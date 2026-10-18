package com.duoc.rotompedia.util

import com.duoc.rotompedia.data.local.MiembroEquipo

object TypeEffectiveness {

    val ALL_TYPES = listOf(
        "normal", "fire", "water", "electric", "grass", "ice",
        "fighting", "poison", "ground", "flying", "psychic", "bug",
        "rock", "ghost", "dragon", "dark", "steel", "fairy"
    )

    // Matriz de efectividad ofensiva: attacking -> (defending -> multiplier)
    private val matrix: Map<String, Map<String, Float>> = mapOf(
        "normal" to mapOf(
            "rock" to 0.5f, "ghost" to 0.0f, "steel" to 0.5f
        ),
        "fire" to mapOf(
            "fire" to 0.5f, "water" to 0.5f, "grass" to 2.0f, "ice" to 2.0f,
            "bug" to 2.0f, "rock" to 0.5f, "dragon" to 0.5f, "steel" to 2.0f
        ),
        "water" to mapOf(
            "fire" to 2.0f, "water" to 0.5f, "grass" to 0.5f, "ground" to 2.0f,
            "rock" to 2.0f, "dragon" to 0.5f
        ),
        "electric" to mapOf(
            "water" to 2.0f, "electric" to 0.5f, "grass" to 0.5f, "ground" to 0.0f,
            "flying" to 2.0f, "dragon" to 0.5f
        ),
        "grass" to mapOf(
            "fire" to 0.5f, "water" to 2.0f, "grass" to 0.5f, "poison" to 0.5f,
            "ground" to 2.0f, "flying" to 0.5f, "bug" to 0.5f, "rock" to 2.0f,
            "dragon" to 0.5f, "steel" to 0.5f
        ),
        "ice" to mapOf(
            "fire" to 0.5f, "water" to 0.5f, "grass" to 2.0f, "ice" to 0.5f,
            "ground" to 2.0f, "flying" to 2.0f, "dragon" to 2.0f, "steel" to 0.5f
        ),
        "fighting" to mapOf(
            "normal" to 2.0f, "ice" to 2.0f, "poison" to 0.5f, "flying" to 0.5f,
            "psychic" to 0.5f, "bug" to 0.5f, "rock" to 2.0f, "ghost" to 0.0f,
            "dark" to 2.0f, "steel" to 2.0f, "fairy" to 0.5f
        ),
        "poison" to mapOf(
            "grass" to 2.0f, "poison" to 0.5f, "ground" to 0.5f, "rock" to 0.5f,
            "ghost" to 0.5f, "steel" to 0.0f, "fairy" to 2.0f
        ),
        "ground" to mapOf(
            "fire" to 2.0f, "electric" to 2.0f, "grass" to 0.5f, "poison" to 2.0f,
            "flying" to 0.0f, "bug" to 0.5f, "rock" to 2.0f, "steel" to 2.0f
        ),
        "flying" to mapOf(
            "electric" to 0.5f, "grass" to 2.0f, "fighting" to 2.0f, "bug" to 2.0f,
            "rock" to 0.5f, "steel" to 0.5f
        ),
        "psychic" to mapOf(
            "fighting" to 2.0f, "poison" to 2.0f, "psychic" to 0.5f, "dark" to 0.0f,
            "steel" to 0.5f
        ),
        "bug" to mapOf(
            "fire" to 0.5f, "grass" to 2.0f, "fighting" to 0.5f, "poison" to 0.5f,
            "flying" to 0.5f, "psychic" to 2.0f, "ghost" to 0.5f, "dark" to 2.0f,
            "steel" to 0.5f, "fairy" to 0.5f
        ),
        "rock" to mapOf(
            "fire" to 2.0f, "ice" to 2.0f, "fighting" to 0.5f, "ground" to 0.5f,
            "flying" to 2.0f, "bug" to 2.0f, "steel" to 0.5f
        ),
        "ghost" to mapOf(
            "normal" to 0.0f, "psychic" to 2.0f, "ghost" to 2.0f, "dark" to 0.5f
        ),
        "dragon" to mapOf(
            "dragon" to 2.0f, "steel" to 0.5f, "fairy" to 0.0f
        ),
        "dark" to mapOf(
            "fighting" to 0.5f, "psychic" to 2.0f, "ghost" to 2.0f, "dark" to 0.5f,
            "fairy" to 0.5f
        ),
        "steel" to mapOf(
            "fire" to 0.5f, "water" to 0.5f, "electric" to 0.5f, "ice" to 2.0f,
            "rock" to 2.0f, "steel" to 0.5f, "fairy" to 2.0f
        ),
        "fairy" to mapOf(
            "fire" to 0.5f, "fighting" to 2.0f, "poison" to 0.5f, "dragon" to 2.0f,
            "dark" to 2.0f, "steel" to 0.5f
        )
    )

    fun getMultiplier(attackingType: String, defendingType: String): Float {
        val atk = attackingType.lowercase()
        val def = defendingType.lowercase()
        return matrix[atk]?.get(def) ?: 1.0f
    }

    fun getDualMultiplier(attackingType: String, defType1: String, defType2: String?): Float {
        val m1 = getMultiplier(attackingType, defType1)
        val m2 = if (!defType2.isNullOrBlank()) getMultiplier(attackingType, defType2) else 1.0f
        return m1 * m2
    }

    data class TeamTypeDefense(
        val type: String,
        val weakCount: Int,
        val superWeakCount: Int,
        val resistCount: Int,
        val immuneCount: Int
    )

    data class TeamAnalysisResult(
        val defenseSummary: List<TeamTypeDefense>,
        val criticalWeaknesses: List<String>, // Tipos donde 3 o más miembros son débiles
        val offensiveCoverage: Set<String>,   // Tipos a los que el equipo golpea supereficaz (2x)
        val uncoveredOffense: List<String>    // Tipos donde nadie del equipo golpea supereficaz
    )

    fun analyzeTeam(members: List<MiembroEquipo>): TeamAnalysisResult {
        if (members.isEmpty()) {
            return TeamAnalysisResult(
                defenseSummary = emptyList(),
                criticalWeaknesses = emptyList(),
                offensiveCoverage = emptySet(),
                uncoveredOffense = ALL_TYPES
            )
        }

        val defenseList = mutableListOf<TeamTypeDefense>()
        val criticalWeaknesses = mutableListOf<String>()

        for (atkType in ALL_TYPES) {
            var weak = 0
            var superWeak = 0
            var resist = 0
            var immune = 0

            for (m in members) {
                val mult = getDualMultiplier(atkType, m.tipo1, m.tipo2)
                when {
                    mult >= 4.0f -> {
                        superWeak++
                        weak++
                    }
                    mult in 1.9f..3.9f -> weak++
                    mult == 0.0f -> immune++
                    mult in 0.01f..0.6f -> resist++
                }
            }

            if (weak >= 3 && resist == 0 && immune == 0) {
                criticalWeaknesses.add(atkType)
            }

            defenseList.add(
                TeamTypeDefense(
                    type = atkType,
                    weakCount = weak,
                    superWeakCount = superWeak,
                    resistCount = resist,
                    immuneCount = immune
                )
            )
        }

        val coveredTypes = mutableSetOf<String>()
        val teamTypes = members.flatMap { listOfNotNull(it.tipo1, it.tipo2) }.map { it.lowercase() }.distinct()

        for (t in teamTypes) {
            val targets = matrix[t] ?: emptyMap()
            targets.forEach { (defending, mult) ->
                if (mult >= 2.0f) {
                    coveredTypes.add(defending)
                }
            }
        }

        val uncovered = ALL_TYPES.filter { !coveredTypes.contains(it) }

        return TeamAnalysisResult(
            defenseSummary = defenseList,
            criticalWeaknesses = criticalWeaknesses,
            offensiveCoverage = coveredTypes,
            uncoveredOffense = uncovered
        )
    }
}
