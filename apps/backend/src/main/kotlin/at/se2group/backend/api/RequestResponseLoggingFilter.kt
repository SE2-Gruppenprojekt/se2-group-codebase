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

        return mediaType.includes(MediaType.APPLICATION_JSON) ||
            mediaType.includes(MediaType.APPLICATION_XML) ||
            mediaType.includes(MediaType.TEXT_PLAIN) ||
            mediaType.type == "text"
    }

    private companion object {
        const val MAX_PAYLOAD_LENGTH = 2_000
    }
}
