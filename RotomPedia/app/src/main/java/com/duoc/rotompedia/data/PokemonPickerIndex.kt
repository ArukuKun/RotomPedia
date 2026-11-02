package com.duoc.rotompedia.data

import com.duoc.rotompedia.data.model.PokemonListItem

data class PickerFormOption(val metadata: PokemonFormMetadata, val apiId: Int?) {
    val nameEn get() = metadata.nameEn
    val displayName get() = metadata.displayName
    val formTag get() = metadata.formTag
    val spriteUrl get() = apiId?.let { "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$it.png" }.orEmpty()
}

/** Une el índice ligero de API con etiquetas locales, sin descargar cientos de fichas. */
fun buildPickerForms(index: List<PokemonListItem>): List<PickerFormOption> {
    val byName = index.associateBy { it.name }
    val curated = PokemonFormCatalog.allForms.map { metadata ->
        PickerFormOption(metadata, byName[metadata.nameEn]?.url?.trimEnd('/')?.substringAfterLast('/')?.toIntOrNull())
    }
    val extras = index.mapNotNull { item ->
        val id = item.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return@mapNotNull null
        if (id <= 10000 || PokemonFormCatalog.find(item.name) != null) return@mapNotNull null
        val tag = when {
            "mega" in item.name -> "Mega"
            "alola" in item.name -> "Alola"
            "galar" in item.name -> "Galar"
            "hisui" in item.name -> "Hisui"
            "paldea" in item.name -> "Paldea"
            else -> "Variante"
        }
        PickerFormOption(PokemonFormMetadata(item.name, 0, PokemonFormCatalog.displayName(item.name), tag), id)
    }
    return (curated + extras).distinctBy { it.nameEn }
}
