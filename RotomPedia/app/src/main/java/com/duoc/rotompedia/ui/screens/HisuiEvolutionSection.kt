package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.data.HisuiEvolutions
import com.duoc.rotompedia.data.HisuiEvolutionGame
import com.duoc.rotompedia.data.explorer.ExplorerPokemon
import com.duoc.rotompedia.viewmodel.EvolutionStage
import com.duoc.rotompedia.viewmodel.EvolutionState

@Composable
internal fun PokemonEvolutionSection(slug: String, speciesId: Int, entries: List<ExplorerPokemon>,
    state: EvolutionState, onPokemonClick: (String) -> Unit, onItemClick: (String) -> Unit) {
    val contextual = HisuiEvolutions.forVariety(slug).isNotEmpty() || HisuiEvolutions.notice(slug) != null
    HisuiEvolutionSection(slug, entries, onPokemonClick, onItemClick)
    var showSpecies by rememberSaveable(slug) { mutableStateOf(false) }
    if (contextual) TextButton({ showSpecies = !showSpecies }) {
        Text(if (showSpecies) "Ocultar referencia de especie" else "Ver referencia general de la especie")
    }
    if (contextual && showSpecies) Text("Referencia de especie · no filtrada por edición",
        style = MaterialTheme.typography.labelSmall)
    if (!contextual || showSpecies) when (state) {
        is EvolutionState.Loading -> CircularProgressIndicator(Modifier.size(24.dp))
        is EvolutionState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
        is EvolutionState.Success -> EvolutionChainColumn(
            chain = speciesEvolutionReference(state.evolutionChain, slug), currentPokemonId = speciesId.toString(),
            onPokemonClick = onPokemonClick, onItemClick = onItemClick)
    }
}

/** The same contextual section is used by production and tests, even if the remote chain failed. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HisuiEvolutionSection(slug: String, entries: List<ExplorerPokemon>,
    onPokemonClick: (String) -> Unit, onItemClick: (String) -> Unit) {
    val rules = remember(slug) { HisuiEvolutions.forVariety(slug) }
    HisuiEvolutions.notice(slug)?.let { Text(it) }
    if (rules.isEmpty()) return
    var game by rememberSaveable(slug) { mutableStateOf(HisuiEvolutionGame.ARCEUS) }
    Text("Evoluciones de Hisui", style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HisuiEvolutionGame.entries.forEach { edition ->
            FilterChip(selected = game == edition, onClick = { game = edition }, label = { Text(edition.label) })
        }
    }
    val bySlug = remember(entries) { entries.associateBy { it.slug } }
    rules.forEach { rule ->
        val from = bySlug[rule.from]
        val to = bySlug[rule.to]
        if (from == null || to == null) {
            Text("Cargando variedades…")
        } else {
            val method = rule.method(game)
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("evolution_${rule.to}")) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton({ onPokemonClick(from.pokemonId.toString()) }) {
                        PokemonImage(from.pokemonId, null, Modifier.size(40.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Desde ${from.displayName}", modifier = Modifier.weight(1f))
                    }
                    TextButton({ onPokemonClick(to.pokemonId.toString()) }) {
                        PokemonImage(to.pokemonId, null, Modifier.size(40.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("${to.numberLabel} · ${to.displayName}", fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f))
                    }
                    Text(method.text)
                    if (method.canEvolve && game == HisuiEvolutionGame.ARCEUS && "elegir Evolucionar" !in method.text) {
                        Text("Al cumplirlo, elige Evolucionar en el menú.", style = MaterialTheme.typography.labelSmall)
                    }
                    method.items.forEach { item ->
                        TextButton({ onItemClick(item) }) { Text("Consultar objeto") }
                    }
                }
            }
        }
    }
    Text("Fuentes: Serebii · Pokémon Database · Bulbapedia", style = MaterialTheme.typography.labelSmall)
    Text("Cobertura: Arceus y Escarlata/Púrpura.", style = MaterialTheme.typography.labelSmall)
}

internal fun speciesEvolutionReference(chain: List<EvolutionStage>, slug: String): List<EvolutionStage> =
    chain.filterNot { it.id in HisuiEvolutions.excludedTargets(slug) }
