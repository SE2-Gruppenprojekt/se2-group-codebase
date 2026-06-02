package at.se2group.backend.api

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

/**
 * Unit tests for [ResponseSecurityHeadersFilter].
 *
 * These tests cover the baseline hardening contract independently from MVC so the global header
 * behavior stays verified even if controller tests change. The key things to lock down are:
 *
 * - the filter adds the expected defensive headers
 * - HSTS is added only for secure requests
 * - explicit endpoint-provided headers are not overwritten
 * - headers are still written when downstream code throws
 */
class ResponseSecurityHeadersFilterTest {

    private val filter = ResponseSecurityHeadersFilter()

    @Test
    fun `adds baseline security headers for secure requests`() {
        val request = MockHttpServletRequest("GET", "/").apply {
            isSecure = true
        }
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, FilterChain { _, res ->
            (res as MockHttpServletResponse).status = HttpStatus.OK.value()
        })

        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"))
        assertEquals("same-origin", response.getHeader("Cross-Origin-Resource-Policy"))
        assertEquals("require-corp", response.getHeader("Cross-Origin-Embedder-Policy"))
        assertEquals("same-origin", response.getHeader("Cross-Origin-Opener-Policy"))
        assertEquals("no-cache, no-store, must-revalidate, private", response.getHeader("Cache-Control"))
        assertEquals("no-cache", response.getHeader("Pragma"))
        assertEquals("0", response.getHeader("Expires"))
        assertEquals(
            "max-age=31536000; includeSubDomains",
            response.getHeader("Strict-Transport-Security")
        )
    }

    @Test
    fun `adds hsts even when request is not marked secure`() {
        val request = MockHttpServletRequest("GET", "/").apply {
            isSecure = false
        }
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, FilterChain { _, res ->
            (res as MockHttpServletResponse).status = HttpStatus.OK.value()
        })

        assertEquals(
            "max-age=31536000; includeSubDomains",
            response.getHeader("Strict-Transport-Security")
        )
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"))
    }

    @Test
    fun `keeps explicit downstream headers`() {
        val request = MockHttpServletRequest("GET", "/").apply {
            isSecure = true
        }
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, FilterChain { _, res ->
            res as MockHttpServletResponse
            // Preserve downstream intent if a future endpoint opts into a custom header policy.
            res.setHeader("Cache-Control", "public, max-age=60")
            res.setHeader("Cross-Origin-Resource-Policy", "cross-origin")
            res.status = HttpStatus.OK.value()
        })

        assertEquals("public, max-age=60", response.getHeader("Cache-Control"))
        assertEquals("cross-origin", response.getHeader("Cross-Origin-Resource-Policy"))
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"))
    }

    @Test
    fun `adds headers even when downstream throws`() {
        val request = MockHttpServletRequest("GET", "/").apply {
            isSecure = true
        }
        val response = MockHttpServletResponse()

        runCatching {
            filter.doFilter(request, response, FilterChain { _, _ ->
                throw IllegalStateException("boom")
            })
        }

        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"))
        assertEquals(
            "max-age=31536000; includeSubDomains",
            response.getHeader("Strict-Transport-Security")
        )
    }
}
