package com.duoc.rotompedia.data.reference

data class AbilityNameRevision(val id: Int, val slug: String, val current: String, val previous: String,
    val source: String)

/** Small, sourced nomenclature revisions; identities and source snapshot remain unchanged. */
object AbilityNames {
    val revisions = listOf(
        AbilityNameRevision(54, "truant", "Pereza", "Ausente", "https://www.wikidex.net/wiki/Pereza"),
        AbilityNameRevision(64, "liquid-ooze", "Viscosecreción", "Lodo Líquido", "https://www.wikidex.net/wiki/Viscosecreci%C3%B3n"),
        AbilityNameRevision(76, "air-lock", "Esclusa de Aire", "Bucle Aire", "https://www.wikidex.net/wiki/Esclusa_de_Aire")
    )
    fun revision(ability: ReferenceAbility): AbilityNameRevision? = revisions.firstOrNull {
        it.id == ability.id && it.slug == ability.slug && ability.nameEs in listOf(it.previous, it.current)
    }
    fun displayName(ability: ReferenceAbility): String = revision(ability)?.current ?: ability.nameEs ?: ability.nameEn
    fun matches(ability: ReferenceAbility, query: String): Boolean = ReferencePresentation.matches(query,
        displayName(ability), ability.nameEs.orEmpty(), ability.nameEn, ability.slug, ability.id.toString(),
        revision(ability)?.previous.orEmpty())
}

data class AbilityHolder(val pokemonId: Int, val normalSlots: List<Int>, val hiddenSlots: List<Int>) {
    val firstSlot get() = (normalSlots + hiddenSlots).minOrNull() ?: 0
    private fun slots(values: List<Int>) = if (values.size == 1) "slot ${values.single()}" else "slots ${values.joinToString(", ")}"
    val roleLabel get() = listOfNotNull(
        normalSlots.takeIf { it.isNotEmpty() }?.let { "Normal · ${slots(it)}" },
        hiddenSlots.takeIf { it.isNotEmpty() }?.let { "Oculta · ${slots(it)}" }
    ).joinToString(" · ")
}

data class AbilityHolderSummary(val holders: List<AbilityHolder>) {
    val total get() = holders.size
    val normal get() = holders.count { it.normalSlots.isNotEmpty() }
    val hidden get() = holders.count { it.hiddenSlots.isNotEmpty() }
    val both get() = holders.count { it.normalSlots.isNotEmpty() && it.hiddenSlots.isNotEmpty() }
}

object AbilityPresentation {
    fun holders(index: ReferenceIndex, abilityId: Int): AbilityHolderSummary = AbilityHolderSummary(
        index.pokemonAbilities.filter { it.abilityId == abilityId }.groupBy { it.pokemonId }
            .toSortedMap().map { (id, links) ->
                AbilityHolder(id, links.filterNot { it.hidden }.map { it.slot }.distinct().sorted(),
                    links.filter { it.hidden }.map { it.slot }.distinct().sorted())
            })
}
