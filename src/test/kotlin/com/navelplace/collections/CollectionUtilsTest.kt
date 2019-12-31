package com.navelplace.collections

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.navelplace.collections.CollectionUtilsKt.containsExactlyInAnyOrder

class CollectionUtilsTest {
    @Test
    fun `Valid matches`() {
        val matching: Set<Pair<Collection<Any?>, Collection<Any?>>> =
                setOf(setOf(1,2,3) to setOf(3,2,1),
                setOf(1) to listOf(1),
                listOf(1,2,3) to listOf(3,2,1),
                listOf(1,2,3) to setOf(1,2,3))
        matching.forEach{
            assertThat(it.first.containsExactlyInAnyOrder(it.second)).isTrue()
            assertThat(it.second.containsExactlyInAnyOrder(it.first)).isTrue()
            assertThat(it.first.containsExactlyInAnyOrder(it.first)).isTrue()
            assertThat(it.second.containsExactlyInAnyOrder(it.second)).isTrue()
        }
        assertThat(arrayOf(1,3,2).containsExactlyInAnyOrder(listOf(1,2,3))).isTrue()
        assertThat(setOf(1,3,2).containsExactlyInAnyOrder(arrayOf(1,2,3))).isTrue()
        assertThat(arrayOf(1,3,2).containsExactlyInAnyOrder(arrayOf(1,2,3))).isTrue()
    }

    @Test
    fun `Non-matches`() {
        val nonMatching: Set<Pair<Collection<Any?>, Collection<Any?>>> =
                setOf(setOf(1,2,3) to setOf(3,2,1,4),
                        emptyList<Int>() to listOf(1),
                        listOf(1,2,3) to listOf(1L,2L,3L,4))
        nonMatching.forEach{
            assertThat(it.first.containsExactlyInAnyOrder(it.second))
                    .`as`("${it.first} should not match ${it.second}")
                    .isFalse()
            assertThat(it.second.containsExactlyInAnyOrder(it.first)).isFalse()
        }
        assertThat(arrayOf(1,2,3).containsExactlyInAnyOrder(listOf(1,2,3,4))).isFalse()
        assertThat(setOf(1,2,3).containsExactlyInAnyOrder(arrayOf(1,2,3,4))).isFalse()
        assertThat(arrayOf(1,2,3).containsExactlyInAnyOrder(arrayOf(1,2,3,4))).isFalse()
    }
}

