package com.navelplace.spring.cache

import org.springframework.cache.Cache
import java.util.function.BiFunction

/**
 * The strategy for choosing a component cache
 *
 * A [FederatedCache] uses a CacheStrategy to determine which component [Cache] to use for a given key-value pair
 *
 * @param K the type of the key
 * @param V the type of the cached value
 */
interface CacheStrategy<K, V> {

    /**
     * Provided a proposed key-value pair to insert into the [FederatedCache], the strategy chooses
     * from among [eligibleCaches], or returns null to indicate that the default cache should be used.
     *
     * @param[key] The key under which the proposed item is to be cached
     * @param[value] The proposed item to cache
     * @param[eligibleCaches] The component caches of the [FederatedCache] that are available to choose from
     * @return The [Cache] from among [eligibleCaches] that [value] should be placed in, or null to indicate
     * the default cache should be used.
     */
    fun chooseCache(key: K, value: V? , eligibleCaches: Set<Cache>): Cache?

    companion object {
        /**
         * Convenience wrapper for generating a [CacheStrategy] from a lambda
         * and optional default cache.
         *
         * @param[selector] A lambda that chooses a cache from a candidate set
         * @param[defaultCache] An optional cache instance to return if the lambda returns null
         */
        fun <K, V> of(selector: (K, V?, Set<Cache>) -> Cache?,
                      defaultCache: Cache? = null): CacheStrategy<K, V> {
            return object: CacheStrategy<K, V> {
                override fun chooseCache(key: K, value: V?, eligibleCaches: Set<Cache>): Cache? {
                    return selector.invoke(key, value, eligibleCaches)?: defaultCache
                }
            }
        }

        /**
         * A java-friendly overload for [CacheStrategy.of]
         */
        @JvmStatic
        fun <K, V> of(selector: BiFunction<Pair<K, V?>, Set<Cache>, Cache?>,
                      defaultCache: Cache?): CacheStrategy<K, V> {
            val newSelector: (K, V?, Set<Cache>) -> Cache? = { k, v, c -> selector.apply(Pair(k, v), c)  }
            return of(newSelector, defaultCache)
        }

        /**
         * A java-friendly overload for [CacheStrategy.of]
         */
        @JvmStatic
        fun <K, V> of(selector: BiFunction<Pair<K, V?>, Set<Cache>, Cache?>): CacheStrategy<K, V> = of(selector, null)
    }


}
