package at.se2group.backend.api

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.nio.charset.StandardCharsets

class RequestResponseLoggingFilterTest {

    private val filter = RequestResponseLoggingFilter()

    @Test
    fun `logs request and response bodies and preserves response content`() {
        val request = MockHttpServletRequest("POST", "/api/test").apply {
            queryString = "mode=full"
            contentType = "application/json"
            characterEncoding = StandardCharsets.UTF_8.name()
            setContent("""{"hello":"world"}""".toByteArray(StandardCharsets.UTF_8))
        }
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { req, res ->
                req.reader.readText()
                res.contentType = "application/json"
                res.characterEncoding = StandardCharsets.UTF_8.name()
                res.writer.write("""{"ok":true}""")
                res.writer.flush()
            }
        )

        val event = appender.list.single()

        assertEquals("""{"ok":true}""", response.contentAsString)
        assertTrue(event.formattedMessage.contains("HTTP POST /api/test?mode=full -> 200"))
        assertTrue(event.formattedMessage.contains("""requestBody={"hello":"world"}"""))
        assertTrue(event.formattedMessage.contains("""responseBody={"ok":true}"""))
    }

    @Test
    fun `logs placeholder for empty payloads`() {
        val request = MockHttpServletRequest("GET", "/actuator/health")
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { _, res ->
                res as jakarta.servlet.http.HttpServletResponse
                res.status = HttpStatus.OK.value()
            }
        )

        val event = appender.list.single()

        assertTrue(event.formattedMessage.contains("HTTP GET /actuator/health -> 200"))
        assertTrue(event.formattedMessage.contains("requestBody=-"))
        assertTrue(event.formattedMessage.contains("responseBody=-"))
    }

    @Test
    fun `logs request without query string`() {
        val request = MockHttpServletRequest("POST", "/api/no-query").apply {
            contentType = MediaType.APPLICATION_JSON_VALUE
            characterEncoding = StandardCharsets.UTF_8.name()
            setContent("""{"simple":true}""".toByteArray(StandardCharsets.UTF_8))
        }
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { req, res ->
                req.reader.readText()
                res.contentType = MediaType.APPLICATION_JSON_VALUE
                res.characterEncoding = StandardCharsets.UTF_8.name()
                res.writer.write("""{"accepted":true}""")
                res.writer.flush()
            }
        )

        val event = appender.list.single()

        assertTrue(event.formattedMessage.contains("HTTP POST /api/no-query -> 200"))
        assertTrue(!event.formattedMessage.contains("/api/no-query?"))
    }

    @Test
    fun `omits binary request and response payloads from logs`() {
        val requestBytes = byteArrayOf(1, 2, 3, 4)
        val responseBytes = byteArrayOf(5, 6, 7)
        val request = MockHttpServletRequest("POST", "/api/binary").apply {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
            setContent(requestBytes)
        }
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { req, res ->
                req.inputStream.readAllBytes()
                res.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
                res.outputStream.write(responseBytes)
                res.outputStream.flush()
            }
        )

        val event = appender.list.single()

        assertEquals(responseBytes.toList(), response.contentAsByteArray.toList())
        assertTrue(event.formattedMessage.contains("requestBody=[4 bytes omitted]"))
        assertTrue(event.formattedMessage.contains("responseBody=[3 bytes omitted]"))
    }

    @Test
    fun `falls back to utf8 when request charset is invalid`() {
        val umlautJson = """{"text":"ä"}"""
        val request = MockHttpServletRequest("POST", "/api/charset").apply {
            contentType = MediaType.APPLICATION_JSON_VALUE
            characterEncoding = "definitely-not-a-charset"
            setContent(umlautJson.toByteArray(StandardCharsets.UTF_8))
        }
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { req, res ->
                req.inputStream.readAllBytes()
                res.contentType = MediaType.APPLICATION_JSON_VALUE
                res.characterEncoding = StandardCharsets.UTF_8.name()
                res.writer.write("""{"ok":true}""")
                res.writer.flush()
            }
        )

        val event = appender.list.single()

        assertTrue(event.formattedMessage.contains("""requestBody={"text":"ä"}"""))
    }

    @Test
    fun `treats invalid content type as non loggable`() {
        val request = MockHttpServletRequest("POST", "/api/invalid-content-type").apply {
            contentType = "not/a valid media type;"
            setContent("abcdef".toByteArray(StandardCharsets.UTF_8))
        }
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { req, res ->
                req.inputStream.readAllBytes()
                res.contentType = "still not valid;"
                res.outputStream.write("xyz".toByteArray(StandardCharsets.UTF_8))
                res.outputStream.flush()
            }
        )

        val event = appender.list.single()

        assertTrue(event.formattedMessage.contains("requestBody=[6 bytes omitted]"))
        assertTrue(event.formattedMessage.contains("responseBody=[3 bytes omitted]"))
    }

    @Test
    fun `logs xml request and response payloads`() {
        val request = MockHttpServletRequest("POST", "/api/xml").apply {
            contentType = MediaType.APPLICATION_XML_VALUE
            characterEncoding = StandardCharsets.UTF_8.name()
            setContent("<request>ok</request>".toByteArray(StandardCharsets.UTF_8))
        }
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { req, res ->
                req.reader.readText()
                res.contentType = MediaType.APPLICATION_XML_VALUE
                res.characterEncoding = StandardCharsets.UTF_8.name()
                res.writer.write("<response>ok</response>")
                res.writer.flush()
            }
        )

        val event = appender.list.single()

        assertTrue(event.formattedMessage.contains("requestBody=<request>ok</request>"))
        assertTrue(event.formattedMessage.contains("responseBody=<response>ok</response>"))
    }

    @Test
    fun `logs text plain request and response payloads`() {
        val request = MockHttpServletRequest("POST", "/api/plain").apply {
            contentType = MediaType.TEXT_PLAIN_VALUE
            characterEncoding = StandardCharsets.UTF_8.name()
            setContent("plain request".toByteArray(StandardCharsets.UTF_8))
        }
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { req, res ->
                req.reader.readText()
                res.contentType = MediaType.TEXT_PLAIN_VALUE
                res.characterEncoding = StandardCharsets.UTF_8.name()
                res.writer.write("plain response")
                res.writer.flush()
            }
        )

        val event = appender.list.single()

        assertTrue(event.formattedMessage.contains("requestBody=plain request"))
        assertTrue(event.formattedMessage.contains("responseBody=plain response"))
    }

    @Test
    fun `logs generic text media types`() {
        val request = MockHttpServletRequest("POST", "/api/text-html").apply {
            contentType = MediaType.TEXT_HTML_VALUE
            characterEncoding = StandardCharsets.UTF_8.name()
            setContent("<b>request</b>".toByteArray(StandardCharsets.UTF_8))
        }
        val response = MockHttpServletResponse()
        val appender = attachAppender()

        filter.doFilter(
            request,
            response,
            FilterChain { req, res ->
                req.reader.readText()
                res.contentType = MediaType.TEXT_HTML_VALUE
                res.characterEncoding = StandardCharsets.UTF_8.name()
                res.writer.write("<i>response</i>")
                res.writer.flush()
            }
        )

        val event = appender.list.single()

        assertTrue(event.formattedMessage.contains("requestBody=<b>request</b>"))
        assertTrue(event.formattedMessage.contains("responseBody=<i>response</i>"))
    }

    private fun attachAppender(): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(RequestResponseLoggingFilter::class.java) as Logger
        return ListAppender<ILoggingEvent>().also {
            it.start()
            logger.addAppender(it)
        }
    }
}
