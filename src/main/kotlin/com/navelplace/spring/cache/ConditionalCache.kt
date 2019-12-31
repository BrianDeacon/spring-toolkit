package com.navelplace.spring.cache

import org.springframework.cache.Cache

/**
 * Conditional Cache
 *
 * A ConditionalCache is a [Cache] that can determine if it should be used to cache a given
 * key-value pair.
 */
interface ConditionalCache<K, V>: Cache {
    fun accepts(key: K, value: V?): Boolean
}

/**
 * Conditional Cache Strategy
 *
 * An implementation of [CacheStrategy] to be used when all Caches are implementations of
 * [ConditionalCache]. It chooses the first member of the provided Set of caches that returns
 * true for [ConditionalCache.accepts]
 */
class ConditionalCacheStrategy<K, V>(private val defaultCache: Cache? = null) : CacheStrategy<K, V> {

    override fun chooseCache(key: K, value: V?, eligibleCaches: Set<Cache>): Cache? {
        require(eligibleCaches.all { it is ConditionalCache<*, *> }) { "Can only apply ConditionalCacheStrategy to ConditionalCaches" }
        return eligibleCaches.filterIsInstance<ConditionalCache<K, V>>()
                .firstOrNull { cache -> cache.accepts(key, value) }
                ?:eligibleCaches.first { it == defaultCache?:it }
    }

}
