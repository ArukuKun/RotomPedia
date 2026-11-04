package com.duoc.rotompedia.data.reference

import com.duoc.rotompedia.data.*
import com.duoc.rotompedia.util.traducirTipo

object ReferencePresentation {
    val methodLabels = mapOf("level-up" to "Nivel", "machine" to "MT / MO", "egg" to "Huevo", "tutor" to "Tutor",
        "light-ball-egg" to "Huevo con Bola Luminosa", "form-change" to "Cambio de forma", "zygarde-cube" to "Arca de Zygarde")
    fun method(slug: String) = methodLabels[slug] ?: "Método: $slug"
    fun category(slug: String) = when (slug) { "physical" -> "Físico"; "special" -> "Especial"; "status" -> "Estado"; else -> "Categoría: $slug" }
    fun text(texts: List<ReferenceText>, group: String?): ReferenceText? = group?.let { g ->
        val matching = texts.filter { it.versionGroup == g }
        matching.firstOrNull { it.language == "es" } ?: matching.firstOrNull { it.language == "en" }
    }
    fun matches(query: String, vararg fields: String): Boolean {
        val haystack = PokemonFormCatalog.normalize(fields.joinToString(" "))
        return PokemonFormCatalog.normalize(query).split(Regex("\\s+")).filter(String::isNotBlank).all { it in haystack }
    }
    fun filterMoves(index: ReferenceIndex, query: String = "", type: String? = null, category: String? = null): List<ReferenceMove> =
        index.moves.filter { (type == null || it.type == type) && (category == null || it.category == category) &&
            matches(query, it.displayName, it.nameEn, it.slug, traducirTipo(it.type), it.id.toString()) }
            .sortedWith(compareBy<ReferenceMove> { PokemonFormCatalog.normalize(it.displayName) }.thenBy { it.id })
    fun learned(rows: List<LearnRow>, group: String?, method: String? = null): Map<Int, List<LearnRow>> =
        if (group == null) emptyMap() else rows.filter { it.versionGroup == group && (method == null || it.method == method) }.groupBy { it.moveId }
    fun groups(index: ReferenceIndex, rows: List<LearnRow>): List<String> {
        val available = rows.map { it.versionGroup }.toSet()
        val ordered = index.versions.sortedBy { it.order }.map { it.slug }
        return ordered.filter { it in available } + (available - ordered.toSet()).sorted()
    }
    fun legacy(move: ReferenceMove, group: String? = null): PokemonMove {
        val description = text(move.texts, group)?.let { if (it.language == "es") it.text else "Original en inglés: ${it.text}" }
            ?: "Consulta la ficha y selecciona una edición para ver su descripción."
        return PokemonMove(move.nameEn, move.displayName, move.type, when (move.category) {
            "physical" -> MoveCategory.PHYSICAL; "special" -> MoveCategory.SPECIAL; else -> MoveCategory.STATUS
        }, move.power, move.accuracy, move.pp, description, move.id, move.priority, move.slug)
    }
}
