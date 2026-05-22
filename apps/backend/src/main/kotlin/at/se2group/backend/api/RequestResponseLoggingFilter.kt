package at.se2group.backend.api

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Logs every HTTP request/response exchange that passes through the backend servlet chain.
 *
 * The filter exists to make transport-level backend behavior observable without pushing logging
 * responsibilities into individual controllers. It records:
 *
 * - HTTP method
 * - request URI and optional query string
 * - response status
 * - request duration
 * - request payload for loggable text-based content types
 * - response payload for loggable text-based content types
 *
 * Binary or otherwise non-text payloads are not expanded into logs. Instead, the filter logs a
 * short placeholder containing only the payload size. This keeps the logs readable and avoids
 * polluting them with opaque byte output.
 *
 * The implementation uses [ContentCachingRequestWrapper] and [ContentCachingResponseWrapper] so
 * that payloads can be inspected after downstream processing has run. The response wrapper must be
 * copied back into the real response before the request completes, otherwise the client would
 * receive an empty body.
 *
 * This filter is intentionally generic and backend-wide. It should stay at the HTTP boundary and
 * should not take on controller-specific or domain-specific logging concerns.
 */
@Component
class RequestResponseLoggingFilter : OncePerRequestFilter() {

    private val requestLogger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startNanos = System.nanoTime()
        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            logExchange(wrappedRequest, wrappedResponse, startNanos)
            // ContentCachingResponseWrapper buffers the body, so it must be copied back explicitly.
            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun logExchange(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        startNanos: Long
    ) {
        val durationMs = (System.nanoTime() - startNanos) / 1_000_000
        // Keep the path formatting stable whether a query string is present or not.
        val querySuffix = request.queryString?.let { "?$it" } ?: ""

        requestLogger.info(
            "HTTP {} {}{} -> {} in {} ms | requestBody={} | responseBody={}",
            request.method,
            request.requestURI,
            querySuffix,
            response.status,
            durationMs,
            extractPayload(request.contentAsByteArray, request.characterEncoding, request.contentType),
            extractPayload(response.contentAsByteArray, response.characterEncoding, response.contentType)
        )
    }

    private fun extractPayload(
        content: ByteArray,
        characterEncoding: String?,
        contentType: String?
    ): String {
        if (content.isEmpty()) {
            return "-"
        }

        if (!isLoggableTextContent(contentType)) {
            // For non-text payloads, log only the size instead of raw binary content.
            return "[${content.size} bytes omitted]"
        }

        val charset = characterEncoding
            ?.let { runCatching { Charset.forName(it) }.getOrNull() }
            ?: StandardCharsets.UTF_8

        val payload = String(content, charset).replace("\n", "\\n")
        return payload.take(MAX_PAYLOAD_LENGTH)
    }

    private fun isLoggableTextContent(contentType: String?): Boolean {
        val mediaType = contentType
            ?.let { runCatching { MediaType.parseMediaType(it) }.getOrNull() }
            ?: return false

        // Allow common structured and plain-text media types to appear directly in the log output.
        return mediaType.includes(MediaType.APPLICATION_JSON) ||
            mediaType.includes(MediaType.APPLICATION_XML) ||
            mediaType.includes(MediaType.TEXT_PLAIN) ||
            mediaType.type == "text"
    }

    private companion object {
        const val MAX_PAYLOAD_LENGTH = 2_000
    }
}
