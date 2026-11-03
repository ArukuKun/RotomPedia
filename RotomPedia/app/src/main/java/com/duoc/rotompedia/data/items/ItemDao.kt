package com.duoc.rotompedia.data.items

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM item_index ORDER BY itemId") fun observeItems(): Flow<List<IndexedItem>>
    @Query("SELECT * FROM item_index ORDER BY itemId") suspend fun items(): List<IndexedItem>
    @Query("SELECT * FROM item_index WHERE itemId = :id") suspend fun item(id: Int): IndexedItem?
    @Query("SELECT * FROM item_index WHERE slug = :slug ORDER BY itemId") suspend fun bySlug(slug: String): List<IndexedItem>
    @Upsert suspend fun putItems(rows: List<IndexedItem>)
    @Query("SELECT * FROM item_index_state WHERE id=1") suspend fun state(): ItemIndexState?
    @Upsert suspend fun putState(row: ItemIndexState)
    @Query("SELECT * FROM item_detail_cache WHERE itemId=:id") suspend fun cached(id: Int): CachedItemDetail?
    @Upsert suspend fun cache(row: CachedItemDetail)
}
