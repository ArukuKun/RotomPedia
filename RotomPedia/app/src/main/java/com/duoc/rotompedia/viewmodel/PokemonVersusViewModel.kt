package com.duoc.rotompedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.rotompedia.data.local.CachedPokemonDao
import com.duoc.rotompedia.data.model.PokemonData
import com.duoc.rotompedia.data.remote.PokemonApi
import com.duoc.rotompedia.util.TypeEffectiveness
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VersusMatchupAnalysis(
    val p1AgainstP2Multiplier: Float, // Daño máximo que P1 causa a P2 con sus tipos STAB
    val p2AgainstP1Multiplier: Float, // Daño máximo que P2 causa a P1 con sus tipos STAB
    val p1TotalBST: Int,
    val p2TotalBST: Int,
    val statWinsP1: Int,
    val statWinsP2: Int,
    val rotomVerdict: String
)

class PokemonVersusViewModel(
    private val cachedPokemonDao: CachedPokemonDao? = null
) : ViewModel() {

    private val _pokemon1 = MutableStateFlow<PokemonData?>(null)
    val pokemon1: StateFlow<PokemonData?> = _pokemon1

    private val _pokemon2 = MutableStateFlow<PokemonData?>(null)
    val pokemon2: StateFlow<PokemonData?> = _pokemon2

    private val _isLoadingP1 = MutableStateFlow(false)
    val isLoadingP1: StateFlow<Boolean> = _isLoadingP1

    private val _isLoadingP2 = MutableStateFlow(false)
    val isLoadingP2: StateFlow<Boolean> = _isLoadingP2

    private val _matchupAnalysis = MutableStateFlow<VersusMatchupAnalysis?>(null)
    val matchupAnalysis: StateFlow<VersusMatchupAnalysis?> = _matchupAnalysis

    init {
        // Cargar por defecto Charizard (6) y Blastoise (9) como matchup icónico
        loadPokemon1(6)
        loadPokemon2(9)
    }

    fun loadPokemon1(pokemonId: Int) {
        viewModelScope.launch {
            _isLoadingP1.value = true
            try {
                val data = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemon(pokemonId)
                }
                _pokemon1.value = data
                recalculateAnalysis()
            } catch (e: Exception) {
                // Fallback offline
                val cached = withContext(Dispatchers.IO) {
                    cachedPokemonDao?.getPokemonById(pokemonId)
                }
                if (cached != null) {
                    _pokemon1.value = mapCachedToData(cached)
                    recalculateAnalysis()
                }
            } finally {
                _isLoadingP1.value = false
            }
        }
    }

    fun loadPokemon2(pokemonId: Int) {
        viewModelScope.launch {
            _isLoadingP2.value = true
            try {
                val data = withContext(Dispatchers.IO) {
                    PokemonApi.service.getPokemon(pokemonId)
                }
                _pokemon2.value = data
                recalculateAnalysis()
            } catch (e: Exception) {
                // Fallback offline
                val cached = withContext(Dispatchers.IO) {
                    cachedPokemonDao?.getPokemonById(pokemonId)
                }
                if (cached != null) {
                    _pokemon2.value = mapCachedToData(cached)
                    recalculateAnalysis()
                }
            } finally {
                _isLoadingP2.value = false
            }
        }
    }

    private fun recalculateAnalysis() {
        val p1 = _pokemon1.value ?: return
        val p2 = _pokemon2.value ?: return

        val p1Types = p1.types.map { it.type.name }
        val p2Types = p2.types.map { it.type.name }

        // Daño de P1 hacia P2
        var maxP1Damage = 1.0f
        for (t1 in p1Types) {
            val mult = TypeEffectiveness.getDualMultiplier(t1, p2Types[0], p2Types.getOrNull(1))
            if (mult > maxP1Damage) maxP1Damage = mult
        }

        // Daño de P2 hacia P1
        var maxP2Damage = 1.0f
        for (t2 in p2Types) {
            val mult = TypeEffectiveness.getDualMultiplier(t2, p1Types[0], p1Types.getOrNull(1))
            if (mult > maxP2Damage) maxP2Damage = mult
        }

        val p1Stats = extractStatMap(p1)
        val p2Stats = extractStatMap(p2)

        val bst1 = p1Stats.values.sum()
        val bst2 = p2Stats.values.sum()

        var wins1 = 0
        var wins2 = 0
        listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed").forEach { statName ->
            val s1 = p1Stats[statName] ?: 0
            val s2 = p2Stats[statName] ?: 0
            if (s1 > s2) wins1++ else if (s2 > s1) wins2++
        }

        val p1Name = p1.name.replaceFirstChar { it.uppercase() }
        val p2Name = p2.name.replaceFirstChar { it.uppercase() }

        val verdict = when {
            maxP1Damage > maxP2Damage && bst1 >= bst2 ->
                "¡$p1Name tiene una ventaja táctica y estadística dominante sobre $p2Name!"
            maxP2Damage > maxP1Damage && bst2 >= bst1 ->
                "¡$p2Name tiene una clara ventaja elemental y de estadísticas sobre $p1Name!"
            maxP1Damage > maxP2Damage ->
                "¡$p1Name tiene ventaja elemental (${maxP1Damage}x), pero las estadísticas de $p2Name pueden equilibrar el combate!"
            maxP2Damage > maxP1Damage ->
                "¡$p2Name tiene ventaja elemental (${maxP2Damage}x), pero $p1Name puede resistir con su conjunto de estadísticas!"
            bst1 > bst2 ->
                "¡Sin ventajas elementales directas, $p1Name supera a $p2Name en potencia base total ($bst1 vs $bst2)!"
            bst2 > bst1 ->
                "¡Sin ventajas elementales directas, $p2Name supera a $p1Name en potencia base total ($bst2 vs $bst1)!"
            else ->
                "¡Ambos Pokémon se encuentran perfectamente igualados en poder!"
        }

        _matchupAnalysis.value = VersusMatchupAnalysis(
            p1AgainstP2Multiplier = maxP1Damage,
            p2AgainstP1Multiplier = maxP2Damage,
            p1TotalBST = bst1,
            p2TotalBST = bst2,
            statWinsP1 = wins1,
            statWinsP2 = wins2,
            rotomVerdict = verdict
        )
    }

    private fun extractStatMap(data: PokemonData): Map<String, Int> {
        return data.stats.associate { it.stat.name to it.baseStat }
    }

    private fun mapCachedToData(cached: com.duoc.rotompedia.data.local.CachedPokemon): PokemonData {
        return PokemonData(
            id = cached.id,
            name = cached.name,
            sprites = com.duoc.rotompedia.data.model.SpritesData(cached.imageUrl),
            types = cached.types.split(",").filter { it.isNotBlank() }.mapIndexed { idx, t ->
                com.duoc.rotompedia.data.model.TypesSlot(idx + 1, com.duoc.rotompedia.data.model.TypeData(t))
            },
            stats = listOf(
                com.duoc.rotompedia.data.model.StatInfo(cached.hp, com.duoc.rotompedia.data.model.Stat("hp")),
                com.duoc.rotompedia.data.model.StatInfo(cached.attack, com.duoc.rotompedia.data.model.Stat("attack")),
                com.duoc.rotompedia.data.model.StatInfo(cached.defense, com.duoc.rotompedia.data.model.Stat("defense")),
                com.duoc.rotompedia.data.model.StatInfo(cached.specialAttack, com.duoc.rotompedia.data.model.Stat("special-attack")),
                com.duoc.rotompedia.data.model.StatInfo(cached.specialDefense, com.duoc.rotompedia.data.model.Stat("special-defense")),
                com.duoc.rotompedia.data.model.StatInfo(cached.speed, com.duoc.rotompedia.data.model.Stat("speed"))
            ),
            cries = com.duoc.rotompedia.data.model.CriesData(latest = cached.cryUrl)
        )
    }
}
