package com.duoc.rotompedia.data.items

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.duoc.rotompedia.data.model.NamedApiResource
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "item_index", indices = [Index(value = ["slug"])])
@JsonClass(generateAdapter = true)
data class IndexedItem(@PrimaryKey val itemId: Int, val slug: String, val name: String,
    val nameLanguage: String, val category: String, val attributes: String) {
    val attributeList get() = attributes.split('|').filter(String::isNotBlank)
}
@Entity(tableName = "item_detail_cache")
data class CachedItemDetail(@PrimaryKey val itemId: Int, val json: String, val fetchedAt: Long)
@Entity(tableName = "item_index_state")
data class ItemIndexState(@PrimaryKey val id: Int = 1, val sha256: String)
@JsonClass(generateAdapter = true)
data class ItemIndexEnvelope(val formatVersion: Int, val sha256: String, val payloadJson: String)
@JsonClass(generateAdapter = true)
data class ItemIndexPayload(val revision: String, val items: List<IndexedItem>,
    val pockets: List<ItemPocket> = emptyList(), val categoryPockets: List<ItemCategoryPocket> = emptyList(),
    val pocketsComplete: Boolean = false)

@JsonClass(generateAdapter = true)
data class ItemPocket(val id: Int, val slug: String, val name: String, val nameLanguage: String) {
    val label get() = ItemLabels.pocket(slug) ?: name
}
@JsonClass(generateAdapter = true)
data class ItemCategoryPocket(val categoryId: Int, val category: String, val pocketId: Int)

data class ItemPocketIndex(val pockets: List<ItemPocket> = emptyList(),
    val categories: List<ItemCategoryPocket> = emptyList(), val complete: Boolean = false) {
    private val pocketById = pockets.associateBy { it.id }
    private val pocketByCategory = categories.associate { it.category to pocketById[it.pocketId] }
    fun pocket(category: String): ItemPocket? = pocketByCategory[category]

    companion object {
        const val UNKNOWN = -1
        fun validated(payload: ItemIndexPayload): ItemPocketIndex {
            val pockets = payload.pockets
            val categories = payload.categoryPockets
            if (pockets.isEmpty() && categories.isEmpty() && !payload.pocketsComplete) return ItemPocketIndex()
            require(pockets.isNotEmpty() && categories.isNotEmpty())
            require(pockets.all { it.id > 0 && it.slug.isNotBlank() && it.name.isNotBlank() && it.nameLanguage.isNotBlank() })
            require(pockets.distinctBy { it.id }.size == pockets.size && pockets.distinctBy { it.slug }.size == pockets.size)
            require(categories.distinctBy { it.categoryId }.size == categories.size && categories.distinctBy { it.category }.size == categories.size)
            require(categories.all { it.categoryId > 0 && it.category.isNotBlank() && it.pocketId in pockets.map(ItemPocket::id) })
            require(!payload.pocketsComplete || payload.items.all { item -> categories.any { it.category == item.category } })
            return ItemPocketIndex(pockets, categories, payload.pocketsComplete)
        }
    }
}

@JsonClass(generateAdapter = true)
data class ItemDetail(val id: Int, val name: String, val category: NamedApiResource,
    val attributes: List<NamedApiResource> = emptyList(), val sprites: ItemSprites? = null,
    @Json(name = "effect_entries") val effects: List<ItemEffect> = emptyList(),
    @Json(name = "flavor_text_entries") val descriptions: List<ItemFlavor> = emptyList(),
    val prices: List<ItemPrice> = emptyList(),
    @Json(name = "held_by_pokemon") val holders: List<ItemHolder> = emptyList())
@JsonClass(generateAdapter = true)
data class ItemSprites(val default: String? = null)
@JsonClass(generateAdapter = true)
data class ItemEffect(val effect: String = "", @Json(name = "short_effect") val shortEffect: String = "", val language: NamedApiResource)
@JsonClass(generateAdapter = true)
data class ItemFlavor(val text: String, val language: NamedApiResource,
    @Json(name = "version_group") val versionGroup: NamedApiResource)
@JsonClass(generateAdapter = true)
data class ItemPrice(@Json(name = "purchase_price") val purchasePrice: Int? = null,
    @Json(name = "sell_price") val sellPrice: Int? = null, val currency: NamedApiResource,
    @Json(name = "version_group") val versionGroup: NamedApiResource)
@JsonClass(generateAdapter = true)
data class ItemHolder(val pokemon: NamedApiResource,
    @Json(name = "version_details") val versions: List<ItemHeldVersion> = emptyList())
@JsonClass(generateAdapter = true)
data class ItemHeldVersion(val rarity: Int, val version: NamedApiResource)

data class ItemDetailResult(val detail: ItemDetail, val offline: Boolean, val fetchedAt: Long)
