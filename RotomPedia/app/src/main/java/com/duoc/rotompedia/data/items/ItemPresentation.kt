package com.duoc.rotompedia.data.items

import com.duoc.rotompedia.data.PokemonFormCatalog

/** UX translations only. Official category/attribute identities come from the index/API. */
object ItemLabels {
    val categories = mapOf(
        "stat-boosts" to "Mejoras de estadísticas", "effort-drop" to "Reducción de esfuerzo", "medicine" to "Bayas medicinales",
        "other" to "Otras bayas", "in-a-pinch" to "Bayas en apuros", "picky-healing" to "Bayas de recuperación selectiva",
        "type-protection" to "Protección por tipo", "baking-only" to "Bayas de cocina", "collectibles" to "Coleccionables",
        "evolution" to "Evolución", "spelunking" to "Exploración", "held-items" to "Objetos llevados", "choice" to "Elección",
        "effort-training" to "Entrenamiento de esfuerzo", "bad-held-items" to "Objetos llevados perjudiciales", "training" to "Entrenamiento",
        "plates" to "Tablas", "species-specific" to "Específicos de especie", "type-enhancement" to "Potenciadores de tipo",
        "event-items" to "Eventos", "gameplay" to "Herramientas de juego", "plot-advancement" to "Historia", "unused" to "Sin uso",
        "loot" to "Tesoros", "all-mail" to "Correo", "vitamins" to "Vitaminas", "healing" to "Curación", "pp-recovery" to "Recuperación de PP",
        "revival" to "Reanimación", "status-cures" to "Curación de estados", "mulch" to "Abonos", "special-balls" to "Poké Balls especiales",
        "standard-balls" to "Poké Balls estándar", "dex-completion" to "Completar Pokédex", "scarves" to "Pañuelos", "all-machines" to "MT / MO",
        "flutes" to "Flautas", "apricorn-balls" to "Poké Balls de bonguri", "apricorn-box" to "Caja de bonguris", "data-cards" to "Tarjetas de datos",
        "jewels" to "Gemas", "miracle-shooter" to "Lanzador", "mega-stones" to "Megapiedras", "memories" to "Discos",
        "z-crystals" to "Cristales Z", "species-candies" to "Caramelos de especie", "catching-bonus" to "Bonificación de captura",
        "dynamax-crystals" to "Cristales Dinamax", "nature-mints" to "Mentas", "curry-ingredients" to "Ingredientes de curri",
        "tera-shard" to "Teralitos", "sandwich-ingredients" to "Ingredientes de bocadillos", "tm-materials" to "Materiales de MT", "picnic" to "Pícnic"
    )
    val attributes = mapOf("countable" to "Apilable", "consumable" to "Consumible", "usable-overworld" to "Uso fuera de combate",
        "usable-in-battle" to "Uso en combate", "holdable" to "Se puede llevar", "holdable-passive" to "Efecto pasivo al llevarlo",
        "holdable-active" to "Se activa al llevarlo", "underground" to "Subsuelo")
    private val versions = mapOf("red-blue" to "Rojo / Azul", "yellow" to "Amarillo", "gold-silver" to "Oro / Plata",
        "crystal" to "Cristal", "ruby-sapphire" to "Rubí / Zafiro", "emerald" to "Esmeralda", "firered-leafgreen" to "Rojo Fuego / Verde Hoja",
        "diamond-pearl" to "Diamante / Perla", "platinum" to "Platino", "heartgold-soulsilver" to "HeartGold / SoulSilver",
        "black-white" to "Negro / Blanco", "black-2-white-2" to "Negro 2 / Blanco 2", "x-y" to "X / Y",
        "omega-ruby-alpha-sapphire" to "Rubí Omega / Zafiro Alfa", "sun-moon" to "Sol / Luna",
        "ultra-sun-ultra-moon" to "Ultrasol / Ultraluna", "lets-go-pikachu-lets-go-eevee" to "Let's Go Pikachu / Eevee",
        "sword-shield" to "Espada / Escudo", "brilliant-diamond-shining-pearl" to "Diamante Brillante / Perla Reluciente",
        "legends-arceus" to "Leyendas Arceus", "scarlet-violet" to "Escarlata / Púrpura")
    fun category(slug: String) = categories[slug] ?: "Categoría sin traducir: $slug"
    fun attribute(slug: String) = attributes[slug] ?: "Atributo sin traducir: $slug"
    fun version(slug: String) = versions[slug] ?: "Grupo: $slug"
}

data class ItemFilter(val query: String = "", val category: String? = null, val attribute: String? = null, val editorialOnly: Boolean = false)
data class ItemText(val text: String, val language: String)
object ItemPresentation {
    fun filter(rows: List<IndexedItem>, filter: ItemFilter): List<IndexedItem> {
        val words = PokemonFormCatalog.normalize(filter.query).split(Regex("\\s+")).filter(String::isNotBlank)
        return rows.filter { row ->
            val text = PokemonFormCatalog.normalize("${row.name} ${row.slug} ${row.itemId} ${ItemLabels.category(row.category)} " + row.attributeList.joinToString(" ") { ItemLabels.attribute(it) })
            words.all { it in text } && (filter.category == null || row.category == filter.category) &&
                (filter.attribute == null || filter.attribute in row.attributeList) && (!filter.editorialOnly || row.slug in ItemEditorial.notes)
        }.sortedWith(compareBy<IndexedItem> { PokemonFormCatalog.normalize(it.name) }.thenBy { it.itemId })
    }
    fun description(detail: ItemDetail, group: String?): ItemText? {
        if (group == null || (detail.name == "shuca-berry" && group == "x-y")) return null
        val texts = detail.descriptions.filter { it.versionGroup.name == group && it.text.isNotBlank() }
        val selected = texts.firstOrNull { it.language.name == "es" } ?: texts.firstOrNull { it.language.name == "en" } ?: return null
        return ItemText(selected.text.replace(Regex("\\s+"), " ").trim(), selected.language.name)
    }
    fun effect(detail: ItemDetail): ItemText? {
        val texts = detail.effects.filter { it.shortEffect.isNotBlank() || it.effect.isNotBlank() }
        val selected = texts.firstOrNull { it.language.name == "es" } ?: texts.firstOrNull { it.language.name == "en" } ?: return null
        return ItemText(selected.shortEffect.ifBlank { selected.effect }, selected.language.name)
    }
    fun sprite(slug: String, declared: String? = null): String? {
        if (!slug.matches(Regex("[a-z0-9]+(?:-[a-z0-9]+)*"))) return null
        val expected = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/$slug.png"
        // Validated item responses can explicitly share an icon (e.g. tm-normal.png).
        // Accept only the official item sprite namespace, never a Pokemon image.
        return declared?.takeIf { it.matches(Regex("https://raw\\.githubusercontent\\.com/PokeAPI/sprites/master/sprites/items/[a-z0-9-]+\\.png")) } ?: expected
    }
}

/** Reviewed editorial overlay, NOT a second factual item catalogue. */
object ItemEditorial {
    val notes = mapOf(
        "choice-band" to "Puede encajar en atacantes físicos. Valora la limitación de movimiento antes de elegirla; no garantiza resultados de combate.",
        "choice-specs" to "Puede encajar en atacantes especiales. Considera cómo cambiar de movimiento en el formato que juegues.",
        "leftovers" to "Puede favorecer estrategias de permanencia en campo; su utilidad depende del equipo y del formato.",
        "shuca-berry" to "Considera esta baya si necesitas mitigar un ataque supereficaz de Tierra; no sustituye comprobar las reglas de tu juego."
    )
    const val shucaEffect = "Al llevarla, se consume para reducir a la mitad el daño de un ataque supereficaz de tipo Tierra."
    const val shucaSource = "Traducción revisada de item/shuca-berry, efecto inglés; contraste con texto español de Sol/Luna. No es texto oficial español del efecto."
}
