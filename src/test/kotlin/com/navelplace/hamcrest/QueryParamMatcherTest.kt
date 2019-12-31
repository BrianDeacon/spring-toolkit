package com.navelplace.hamcrest

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.springframework.web.util.UriComponentsBuilder
import java.lang.IllegalArgumentException

class QueryParamMatcherTest {

    @Test
    fun `Can accept valid input`() {
        setOf("http://foo",
                "http://foo.bar",
                "http://foo.bar/",
                "http://foo.bar?",
                "https://foo.bar/?",
                "http://foo.bar/baz",
                "http://foo.bar/baz/?",
                "http://foo.bar/baz?blah=blah&blah=blah&blif=blif",
                "blah=blah",
                "blah=blah&blif=blif",
                "?blah=blah&blif=blif",
                "/blah=blah&blif=blif"
                ).forEach {
            assertThat(QueryParamMatcher(it).matches(it))
                    .`as`("Should be able to accept $it")
                    .isTrue()
        }
    }

    @Test
    fun `Notices a simple difference`() {
        mapOf("http://foo?blah=blah" to "http://foo?blah=notBlah",
                "http://foo?blif=blah" to "http://foo?blah=blah").forEach {
            assertThat(QueryParamMatcher(it.key).matches(it.value))
                    .`as`("Should notice that ${it.key} differs from ${it.value}")
                    .isFalse()
        }
    }

    @Test
    fun `Notices that one contains additional arguments`() {
        mapOf("http://foo?blah=blah" to "http://foo?blah=blah&blif=blif",
                "http://foo?" to "http://foo?blah=blah").forEach {
            assertThat(QueryParamMatcher(it.key).matches(it.value))
                    .`as`("Should notice that ${it.key} has different args from ${it.value}")
                    .isFalse()
            assertThat(QueryParamMatcher(it.value).matches(it.key))
                    .`as`("Should notice that ${it.value} has different args from ${it.key}")
                    .isFalse()
        }
    }

    @Test
    fun `Doesn't care about order`() {
        mapOf("http://foo?blah=blah&blif=blif&blah2=blah2" to "http://foo?blah2=blah2&blah=blah&blif=blif",
                "http://foo?x=y&y=x" to "http://foo?y=x&x=y",
                "http://foo/?x=y&y=x" to "http://foo?y=x&x=y").forEach {
            assertThat(QueryParamMatcher(it.key).matches(it.value))
                    .`as`("Shouldn't care that the order of ${it.key} differs from ${it.value}")
                    .isTrue()

        }
    }

    @Test
    fun `Notices that there are multiples for the same key but they differ`() {
        mapOf("http://foo?blah=blah" to "http://foo?blah=blah&blah=blah").forEach {
            assertThat(QueryParamMatcher(it.key).matches(it.value))
                    .`as`("Should notice that ${it.key} has different args from ${it.value}")
                    .isFalse()
            assertThat(QueryParamMatcher(it.value).matches(it.key))
                    .`as`("Should notice that ${it.value} has different args from ${it.key}")
                    .isFalse()
        }
    }

    @Test
    fun `Should ignore the path and host`() {
        mapOf("http://foo?blah=blah" to "http://bar?blah=blah",
                "http://foo?blah=blah" to "https://foo?blah=blah",
                "http://foo/bar/baz?blah=blah" to "http://bar?blah=blah").forEach {
            assertThat(QueryParamMatcher(it.key).matches(it.value))
                    .`as`("Shouldn't care that the host or path for ${it.key} differs from ${it.value}")
                    .isTrue()
            assertThat(QueryParamMatcher(it.value).matches(it.key))
                    .`as`("Shouldn't care that the host or path for ${it.value} differs from ${it.key}")
                    .isTrue()
        }
    }
}
