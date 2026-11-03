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
data class ItemIndexPayload(val revision: String, val items: List<IndexedItem>)

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
