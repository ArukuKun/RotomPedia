package com.duoc.rotompedia.data.items

import androidx.room.withTransaction
import com.duoc.rotompedia.data.index.IndexValidator
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.remote.PokeApiService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ItemRepository {
    fun observeItems(): Flow<List<IndexedItem>>
    suspend fun ensureIndex()
    suspend fun pocketIndex(): ItemPocketIndex
    suspend fun resolve(slug: String): IndexedItem?
    suspend fun detail(id: Int, refresh: Boolean = false): ItemDetailResult
}

class DefaultItemRepository(private val db: AppDatabase, private val api: PokeApiService,
    moshi: Moshi, private val bundled: suspend () -> String) : ItemRepository {
    private val dao get() = db.itemDao()
    private val indexLock = Mutex()
    private val locks = java.util.concurrent.ConcurrentHashMap<Int, Mutex>()
    private val envelopeAdapter = moshi.adapter(ItemIndexEnvelope::class.java)
    private val payloadAdapter = moshi.adapter(ItemIndexPayload::class.java)
    private val detailAdapter = moshi.adapter(ItemDetail::class.java)
    @Volatile private var pocketMetadata = ItemPocketIndex()
    override suspend fun pocketIndex(): ItemPocketIndex { ensureIndex(); return pocketMetadata }
    override fun observeItems() = dao.observeItems()
    override suspend fun ensureIndex() = withContext(Dispatchers.IO) {
        indexLock.withLock {
            val envelope = requireNotNull(envelopeAdapter.fromJson(bundled()))
            require(envelope.formatVersion == 1 && IndexValidator.sha256(envelope.payloadJson) == envelope.sha256)
            val payload = requireNotNull(payloadAdapter.fromJson(envelope.payloadJson))
            require(payload.revision.matches(Regex("[a-f0-9]{40}")) && payload.items.isNotEmpty())
            require(payload.items.distinctBy { it.itemId }.size == payload.items.size)
            require(payload.items.all { it.itemId > 0 && it.slug.isNotBlank() && it.name.isNotBlank() && it.category.isNotBlank() })
            val pockets = ItemPocketIndex.validated(payload)
            if (dao.state()?.sha256 == envelope.sha256) {
                pocketMetadata = pockets
                return@withLock
            }
            db.withTransaction {
                val current = payload.items.associateBy { it.itemId }
                require(dao.items().all { current[it.itemId]?.slug == it.slug }) { "Identidad de objeto reasignada" }
                dao.putItems(payload.items)
                dao.putState(ItemIndexState(sha256 = envelope.sha256))
            }
            pocketMetadata = pockets
        }
    }
    override suspend fun resolve(slug: String): IndexedItem? {
        ensureIndex()
        val candidates = dao.bySlug(slug)
        if (candidates.size < 2) return candidates.singleOrNull()
        // Upstream can contain multiple IDs with the same slug (e.g. Roseli Berry).
        // Resolve a slug via its canonical API resource, never guess/reassign an ID.
        return try {
            val canonical = api.getItem(slug)
            candidates.singleOrNull { it.itemId == canonical.id && it.slug == canonical.name }
        } catch (e: Exception) { if (e is CancellationException) throw e; null }
    }
    override suspend fun detail(id: Int, refresh: Boolean): ItemDetailResult = withContext(Dispatchers.IO) {
        require(id > 0)
        ensureIndex()
        locks.getOrPut(id) { Mutex() }.withLock {
            val item = requireNotNull(dao.item(id))
            val cached = dao.cached(id)
            val saved = cached?.let { runCatching { detailAdapter.fromJson(it.json) }.getOrNull() }
                ?.takeIf { it.id == id && it.name == item.slug }
            if (!refresh && saved != null && System.currentTimeMillis() - cached.fetchedAt < 24 * 60 * 60_000)
                return@withLock ItemDetailResult(saved, false, cached.fetchedAt)
            try {
                val data = api.getItem(id.toString())
                require(data.id == id && data.name == item.slug && data.category.name == item.category) { "Identidad de objeto incompatible" }
                val timestamp = System.currentTimeMillis()
                dao.cache(CachedItemDetail(id, detailAdapter.toJson(data), timestamp))
                ItemDetailResult(data, false, timestamp)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (saved == null) throw e
                ItemDetailResult(saved, true, cached.fetchedAt)
            }
        }
    }
}
