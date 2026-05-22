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

    private fun attachAppender(): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(RequestResponseLoggingFilter::class.java) as Logger
        return ListAppender<ILoggingEvent>().also {
            it.start()
            logger.addAppender(it)
        }
    }
}
