package com.navelplace.spring.cache

import org.springframework.cache.Cache

/**
 * The strategy for choosing a component cache
 *
 * A [FederatedCache] uses a CacheStrategy to determine which component [Cache] to use for a given key-value pair
 *
 * @param K the type of the key
 * @param V the type of the cached value
 */
interface CacheStrategy<K, V> {
    companion object {
        fun <K, V> of(block: (K, V?, Set<Cache>) -> Cache?,
                      defaultCache: Cache? = null): CacheStrategy<K, V> {
            return object: CacheStrategy<K, V> {
                override fun chooseCache(key: K, value: V?, eligibleCaches: Set<Cache>): Cache? {
                    return block.invoke(key, value, eligibleCaches)?: defaultCache
                }
            }
        }
    }

    fun chooseCache(key: K, value: V? , eligibleCaches: Set<Cache>): Cache?
}
