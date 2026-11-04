package com.duoc.rotompedia.data

import androidx.compose.ui.graphics.Color
import java.util.concurrent.ConcurrentHashMap

enum class MoveCategory(val displayName: String, val emoji: String, val color: Color) {
    PHYSICAL("Físico", "💥", Color(0xFFE53935)), SPECIAL("Especial", "🔮", Color(0xFF1E88E5)), STATUS("Estado", "🛡️", Color(0xFF757575))
}
data class PokemonMove(val nameEn: String, val nameEs: String, val type: String, val category: MoveCategory,
    val power: Int? = null, val accuracy: Int? = null, val pp: Int? = null, val description: String = "",
    val moveId: Int? = null, val priority: Int = 0, val slug: String? = null)

/** Compatibility lookup for text movesets. Populated ONLY from the shared reference.
 * Unknown or ambiguous saved names stay visible as text; never rewrite personal data.
 */
object PokemonMoveData {
    private val changes = kotlinx.coroutines.flow.MutableStateFlow(0)
    val revision: kotlinx.coroutines.flow.StateFlow<Int> = changes
    private val aliases = ConcurrentHashMap<String, Set<PokemonMove>>()
    private fun key(text: String) = PokemonFormCatalog.normalize(text).replace(" ", "")
    fun registerMoves(moves: List<PokemonMove>) {
        moves.forEach { move ->
            listOfNotNull(move.nameEn, move.nameEs, move.slug).forEach { alias ->
                aliases.compute(key(alias)) { _, previous -> (previous.orEmpty().filterNot { it.moveId == move.moveId } + move).toSet() }
            }
        }
        changes.value++
    }
    fun findMove(name: String): PokemonMove? = aliases[key(name)]?.singleOrNull()
}
