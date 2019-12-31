package com.navelplace.spring.cache

import org.springframework.cache.Cache
import java.util.concurrent.Callable
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

open class FederatedCache<K, V>(private val componentCaches: Set<Cache>,
                                private val cacheStrategy: CacheStrategy<K, V> = ConditionalCacheStrategy(),
                                private val defaultCache: Cache = componentCaches.first(),
                                private val federatedCacheName: String? = defaultCache.name,
                                private val nativeFederatedCache: Cache? = null): Cache {
    constructor(caches: Set<ConditionalCache<K, V>>, defaultCache: Cache = caches.first()): this(caches, ConditionalCacheStrategy(), defaultCache)
    constructor(vararg caches: ConditionalCache<K, V>): this(setOf(*caches))

    private val allCaches: Set<Cache> = componentCaches + setOf(nativeFederatedCache?:defaultCache, defaultCache)

    private val _lock = ReentrantReadWriteLock()
    val lock: ReadWriteLock get() = _lock

    override fun getName() = federatedCacheName
    override fun getNativeCache() = nativeFederatedCache?:this

    override fun clear() = _lock.write {
        allCaches.forEach{ it.clear() }
    }

    private fun whichCache(key: Any, value: Any?): Cache {
        @Suppress("UNCHECKED_CAST")
        val cache = cacheStrategy.chooseCache(key as K, value as V?, componentCaches)
                ?: defaultCache

        return cache.also {
            check(allCaches.contains(it)) { "The CacheStrategy must return one of the component caches" }
        }
    }

    private fun Cache.typedGet(key: Any): Cache.ValueWrapper? {
        @Suppress("UNCHECKED_CAST")
        return this.get(key as K)
    }

    private fun Cache.typedPut(key: Any, value: Any?) {
        @Suppress("UNCHECKED_CAST")
        return this.put(key as K, value as V?)
    }

    override fun put(key: Any, value: Any?): Unit = _lock.write {
        evict(key)
        whichCache(key, value).typedPut(key, value)
    }

    override fun get(key: Any): Cache.ValueWrapper? = _lock.read {
        return allCaches.asSequence().mapNotNull { it.typedGet(key) }.firstOrNull()
    }

    override fun <T : Any> get(key: Any, type: Class<T>): T? = _lock.read {
        @Suppress("UNCHECKED_CAST")
        return get(key)?.get() as T?
    }

    override fun <T : Any?> get(key: Any, valueLoader: Callable<T>): T? = _lock.write {
        @Suppress("UNCHECKED_CAST")
        val cached = this.get(key)?.get() as T?
        return cached?: valueLoader.call()
                .also {loaded ->
                    this.put(key, loaded)
                }
    }

    override fun evict(key: Any) = _lock.write {
        allCaches.forEach{ it.evict(key) }
    }

    override fun putIfAbsent(key: Any, value: Any?): Cache.ValueWrapper? = _lock.write {
        // Don't need to evict other caches because this only adds if it wasn't there.
        return get(key) ?: whichCache(key, value).putIfAbsent(key, value)
    }
}
