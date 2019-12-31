package com.navelplace.spring.cache

import com.github.javafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.cache.Cache
import org.springframework.cache.concurrent.ConcurrentMapCache
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

val faker = Faker()

class EvenNumberCache(delegate: Cache = ConcurrentMapCache(faker.hobbit().character())): Cache by delegate, ConditionalCache<Int, ArbitraryThing> {
    override fun accepts(key: Int, value: ArbitraryThing?): Boolean {
        return key % 2 == 0
    }
}

class OddNumberCache(delegate: Cache = ConcurrentMapCache(faker.hobbit().character())): Cache by delegate, ConditionalCache<Int, ArbitraryThing> {
    override fun accepts(key: Int, value: ArbitraryThing?): Boolean {
        return key % 2 == 1
    }
}

data class ArbitraryThing(val value: Int)

open class FederatedCacheTest {

    val oddDelegate = delegateCache()
    val evenDelegate = delegateCache()
    val evenCache = EvenNumberCache(evenDelegate)
    val oddCache = OddNumberCache(oddDelegate)
    lateinit var federatedCache: FederatedCache<Int, ArbitraryThing>
    lateinit var evenThing: ArbitraryThing
    lateinit var oddThing: ArbitraryThing

    private fun delegateCache(name: String = UUID.randomUUID().toString()): Cache {
        return ConcurrentMapCache(name)
    }

    private fun Cache.cacheThing(thing: ArbitraryThing) {
        this.put(thing.value, thing)
    }

    @Before
    fun before() {
        evenThing = ArbitraryThing((Random().nextInt() / 2) * 2)
        oddThing = ArbitraryThing(evenThing.value + 1)
        evenDelegate.clear()
        oddDelegate.clear()
        federatedCache = FederatedCache(evenCache, oddCache)
    }

    @Test
    fun testVanilla() {
        val thing1 = ArbitraryThing(1)
        val thing2 = ArbitraryThing(2)
        federatedCache.cacheThing(thing1)
        federatedCache.cacheThing(thing2)

        assertThat(oddDelegate.get(thing1.value)?.get()).isSameAs(thing1)
        assertThat(evenDelegate.get(thing2.value)?.get()).isSameAs(thing2)
        assertThat(federatedCache.get(thing1.value)?.get()).isSameAs(thing1)
        assertThat(federatedCache.get(thing2.value)?.get()).isSameAs(thing2)
        assertThat(federatedCache.get(-1)).isNull()
    }

    @Test
    fun testEvict() {
        federatedCache.cacheThing(evenThing)
        federatedCache.cacheThing(oddThing)
        assertThat(evenDelegate.get(evenThing.value)?.get()).isNotNull
        federatedCache.evict(evenThing.value)
        assertThat(evenDelegate.get(evenThing.value)).isNull()
        assertThat(federatedCache.get(evenThing.value)).isNull()
        assertThat(federatedCache.get(oddThing.value)?.get()).isSameAs(oddThing)
    }

    @Test
    fun testClear() {
        federatedCache.cacheThing(evenThing)
        assertThat(evenDelegate.get(evenThing.value)?.get()).isSameAs(evenThing)
        assertThat(federatedCache.get(evenThing.value)?.get()).isSameAs(evenThing)
        federatedCache.clear()
        assertThat(federatedCache.get(evenThing.value)).isNull()
        assertThat(evenDelegate.get(evenThing.value)).isNull()
    }

    @Test
    fun testName() {
        val name = faker.harryPotter().character()
        val cache = FederatedCache<Int, ArbitraryThing>(componentCaches = setOf(evenCache, oddCache), federatedCacheName = name)
        assertThat(cache.name).isEqualTo(name)
    }

    @Test
    fun testDefaultName() {
        val name = faker.harryPotter().character()
        val defaultCache = delegateCache(name)
        val cache = FederatedCache<Int, ArbitraryThing>(componentCaches = setOf(evenCache, oddCache), defaultCache = defaultCache)
        assertThat(cache.name).isEqualTo(name)
    }

    @Test
    fun testNativeCache() {
        FederatedCache(setOf(evenCache, oddCache)).also {
            assertThat(it.nativeCache).isSameAs(it)
        }
    }

    @Test
    fun testDefaultNativeCache() {
        val nativeCache = delegateCache()
        FederatedCache<Int, ArbitraryThing>(componentCaches = setOf(evenCache, oddCache),
                nativeFederatedCache = nativeCache).also {
            assertThat(it.nativeCache).isSameAs(nativeCache)
        }
    }

    @Test
    fun getWithLoader() {
        val key = faker.number().randomDigit()
        val loader = { evenThing }
        federatedCache.get(key, loader)
        assertThat(federatedCache.get(UUID.randomUUID().toString())).isNull()
        assertThat(federatedCache.get(key)?.get()).isSameAs(evenThing)
    }

    @Test
    fun putIfAbsent() {
        val key = faker.number().randomDigit()
        federatedCache.putIfAbsent(key, evenThing)
        federatedCache.putIfAbsent(key, oddThing)
        assertThat(federatedCache.get(key)?.get()).isSameAs(evenThing)
    }

    @Test
    fun bunchaTimes() {
        repeat(20) { testLock() }
    }


    @Test
    fun testLock() {
        assertThat(federatedCache.get(evenThing.value)).isNull()
        val exceptions: MutableList<Throwable> = mutableListOf()


        val thread3 = thread(start = false) {
            federatedCache.get(evenThing.value)?.get().let {
                runCatching {  assertThat(it)
                        .`as`("We shouldn't be able to read from it until the writeLock is released")
                        .isSameAs(evenThing) }
                        .exceptionOrNull()?.let { exceptions.add(it) }
            }
            federatedCache.evict(evenThing.value)
        }

        val thread2 = thread(start = false) {
            federatedCache.lock.writeLock().withLock {
                thread3.start()
                federatedCache.put(evenThing.value, evenThing)
            }
        }


        val thread1 = thread {
            federatedCache.lock.readLock().withLock {
                thread2.start()
                federatedCache.get(evenThing.value).let {
                    runCatching {  assertThat(it)
                            .`as`("It shouldn't be able to add to the cache while we hold the read lock")
                            .isNull()
                    }.exceptionOrNull()?.let { exceptions.add(it) }
                }
            }


        }



        thread1.join()
        thread2.join()

        thread3.join()
        exceptions.firstOrNull()?.let { throw it }
    }
}
