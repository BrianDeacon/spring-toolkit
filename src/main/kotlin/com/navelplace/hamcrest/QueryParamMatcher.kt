package com.navelplace.hamcrest

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URL
import com.navelplace.collections.CollectionUtilsKt.containsExactlyInAnyOrder

/**
 * Matches when the query strings for two URIs contain exactly the same arguments
 * in name, number and value, ignoring the order of those arguments and
 * any portions of the host or path.
 */
class QueryParamMatcher(private val uriComponents: UriComponents): BaseMatcher<String>() {

    companion object {
        @JvmStatic fun hasSameQueryParams(uriString: String) = QueryParamMatcher(uriString)
        @JvmStatic fun hasSameQueryParams(uriComponents: UriComponents) = QueryParamMatcher(uriComponents)
        @JvmStatic fun hasSameQueryParams(uri: URI) = QueryParamMatcher(uri)
        @JvmStatic fun hasSameQueryParams(url: URL) = QueryParamMatcher(url)
    }

    constructor(uriString: String): this(UriComponentsBuilder.fromUriString(uriString).build())
    constructor(uri: URI): this(UriComponentsBuilder.fromUri(uri).build())
    constructor(url: URL): this(url.toURI())

    override fun describeTo(description: Description) {
        description.appendText("$this")
    }

    private fun MultiValueMap<String,String>.matches(other: MultiValueMap<String, String>): Boolean {
        return when {
            this.keys.containsExactlyInAnyOrder(other.keys).not() -> false
            this.any { it.value.containsExactlyInAnyOrder(other[it.key]!!).not()  } -> false
            else -> true
        }
    }

    override fun matches(item: Any?): Boolean {
        val params = UriComponentsBuilder.fromUriString(item?.toString()?:"").build().queryParams
        return params.matches(uriComponents.queryParams)
    }

    override fun toString() = "hasSameQueryParams(${uriComponents.queryParams})"
}

