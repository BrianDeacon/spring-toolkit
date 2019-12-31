package com.navelplace.collections
import com.navelplace.collections.CollectionUtilsKt.containsExactlyInAnyOrder

object CollectionUtils {
    /**
     * @param[source] Source collection
     * @param[target] Target collection
     * @return `true` if both collections are non-null, of the same size, and each
     * [java.util.Collection.containsAll] the other
     */
    @JvmStatic
    fun <T> containsExactlyInAnyOrder(source: Collection<T>, target: Collection<T>?): Boolean {
        return source.containsExactlyInAnyOrder(target)
    }

    /**
     * Compares the contents of two arrays
     *
     * This overload is the equivalent of:
     * containsExactlyInAnyOrder(Arrays.asList(source), Arrays.asList(target))
     *
     * @param[source] Source array
     * @param[target] Target array
     * @return `true` if both arrays are non-null, of the same size, and each
     * contains all elements of the other.
     *
     */
    @JvmStatic
    fun <T> containsExactlyInAnyOrder(source: Array<T>, target: Array<T>?): Boolean {
        return source.containsExactlyInAnyOrder(target)
    }

    /**
     * Compares the contents of an array and any `iterable`.
     * @param[source] Source iterable
     * @param[target] Target array
     * @return `true` if both the iterable and the array are non-null, of the same size,
     * and each contains all elements of the other.
     */
    @JvmStatic
    fun <T> containsExactlyInAnyOrder(source: Iterable<T>, target: Array<T>?): Boolean {
        return source.containsExactlyInAnyOrder(target)
    }

    /**
     * @param[source] Source array
     * @param[target] Target iterable
     * @return `true` if both the iterable and the array are non-null, of the same size,
     * and each contains all elements of the other.
     */
    @JvmStatic
    fun <T> containsExactlyInAnyOrder(source: Array<T>, target: Iterable<T>?): Boolean {
        return source.containsExactlyInAnyOrder(target)
    }
}

object CollectionUtilsKt {

    fun <T> Collection<T>?.containsExactlyInAnyOrder(other: Collection<T>?): Boolean {
        return when {
            this == null -> false
            other == null -> false
            this.size != other.size -> false
            this.containsAll(other).not() -> false
            other.containsAll(this).not() -> false
            else -> true
        }
    }

    fun <T> Iterable<T>?.containsExactlyInAnyOrder(other: Iterable<T>?): Boolean {
        return this?.toList().containsExactlyInAnyOrder(other?.toList())
    }

    fun <T> Iterable<T>?.containsExactlyInAnyOrder(other: Array<T>?): Boolean {
        return this.containsExactlyInAnyOrder(other?.asList())
    }

    fun <T> Array<T>?.containsExactlyInAnyOrder(other: Iterable<T>?): Boolean {
        return this?.asList().containsExactlyInAnyOrder(other)
    }

    fun <T> Array<T>?.containsExactlyInAnyOrder(other: Array<T>?): Boolean {
        return this?.asList().containsExactlyInAnyOrder(other)
    }
}
