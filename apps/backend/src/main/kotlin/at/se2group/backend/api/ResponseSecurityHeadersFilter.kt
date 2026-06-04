package at.se2group.backend.api

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Applies a small baseline set of defensive HTTP response headers to every backend response.
 *
 * The backend does not serve a large browser application, but it still exposes public HTTP
 * endpoints such as `/`, `robots.txt`, `sitemap.xml`, actuator endpoints, and REST APIs.
 * Those responses benefit from a consistent baseline hardening layer so transport-level security
 * headers do not need to be repeated in individual controllers.
 *
 * This filter currently adds:
 *
 * - `X-Content-Type-Options: nosniff`
 * - `Cross-Origin-Resource-Policy: same-origin`
 * - `Cross-Origin-Embedder-Policy: require-corp`
 * - `Cross-Origin-Opener-Policy: same-origin`
 * - `Cache-Control: no-cache, no-store, must-revalidate, private`
 * - `Pragma: no-cache`
 * - `Expires: 0`
 * - `Strict-Transport-Security`
 *
 * Two implementation details matter:
 *
 * 1. Headers are applied in a `finally` block so they also appear on error responses produced by
 *    Spring MVC exception handling.
 * 2. Existing explicit headers are preserved. If a future endpoint needs a different caching
 *    policy or resource-sharing policy, that endpoint can set the header itself and this filter
 *    will not overwrite it.
 */
@Component
class ResponseSecurityHeadersFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } finally {
            applyHeaderIfMissing(response, "X-Content-Type-Options", "nosniff")
            applyHeaderIfMissing(response, "Cross-Origin-Resource-Policy", "same-origin")
            applyHeaderIfMissing(response, "Cross-Origin-Embedder-Policy", "require-corp")
            applyHeaderIfMissing(response, "Cross-Origin-Opener-Policy", "same-origin")

            // Conservative cache directives are appropriate for API and metadata responses because
            // they prevent intermediate caches from storing stale or sensitive payloads by default.
            applyHeaderIfMissing(response, "Cache-Control", "no-cache, no-store, must-revalidate, private")
            applyHeaderIfMissing(response, "Pragma", "no-cache")
            applyHeaderIfMissing(response, "Expires", "0")

            // The deployed backend is exposed over HTTPS, and setting HSTS here avoids depending
            // on proxy-specific forwarded-header handling before the header reaches the client.
            applyHeaderIfMissing(
                response,
                "Strict-Transport-Security",
                "max-age=31536000; includeSubDomains"
            )
        }
    }

    private fun applyHeaderIfMissing(
        response: HttpServletResponse,
        name: String,
        value: String
    ) {
        if (!response.containsHeader(name)) {
            response.setHeader(name, value)
        }
    }
}
