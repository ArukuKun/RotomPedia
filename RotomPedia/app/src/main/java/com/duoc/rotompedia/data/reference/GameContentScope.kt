package com.duoc.rotompedia.data.reference

/** Client scope only: keep the verified upstream snapshot and saved identities intact. */
object GameContentScope {
    fun allowsVersion(slug: String) = slug != "colosseum" && slug != "xd"
    fun allowsMove(move: ReferenceMove) = move.type != "shadow"
    fun allowsEncounterMethod(slug: String) = slug.split('-').none { it == "colosseum" || it == "xd" }
    // Reviewed against the pinned reference; do not exclude legitimate Shadow Ball/Force/Tag.
    private val shadowSlugs = listOf("shadow-rush", "shadow-blast", "shadow-blitz", "shadow-bolt", "shadow-break",
        "shadow-chill", "shadow-end", "shadow-fire", "shadow-rave", "shadow-storm", "shadow-wave", "shadow-down",
        "shadow-half", "shadow-hold", "shadow-mist", "shadow-panic", "shadow-shed", "shadow-sky")
    fun allowsLearning(row: LearnRow) = allowsVersion(row.versionGroup) &&
        shadowSlugs.getOrNull(row.moveId - 10001) != row.slug

    fun catalogue(index: ReferenceIndex): ReferenceIndex {
        val moves = index.moves.filter(::allowsMove).map { it.copy(texts = it.texts.filter { t -> allowsVersion(t.versionGroup) }) }
        val ids = moves.map { it.id }.toSet()
        return index.copy(versions = index.versions.filter { allowsVersion(it.slug) }, moves = moves,
            abilities = index.abilities.map { it.copy(texts = it.texts.filter { t -> allowsVersion(t.versionGroup) }) },
            machines = index.machines.filter { it.moveId in ids && allowsVersion(it.versionGroup) })
    }
    fun learnset(data: Learnset) = data.copy(rows = data.rows.filter(::allowsLearning))
}
