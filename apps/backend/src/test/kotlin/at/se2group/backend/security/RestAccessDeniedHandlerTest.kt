package at.se2group.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import shared.models.api.ApiErrorResponse

class RestAccessDeniedHandlerTest {

    private val objectMapper = ObjectMapper()
    private val handler = RestAccessDeniedHandler(objectMapper)

    @Test
    fun `handle writes forbidden json response`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        handler.handle(request, response, AccessDeniedException("nope"))

        assertEquals(403, response.status)
        assertEquals("application/json", response.contentType)

        val body = objectMapper.readTree(response.contentAsString)
        assertEquals("FORBIDDEN", body["errorCode"].asText())
        assertEquals("Access denied", body["errorMessage"].asText())
        assertEquals(0, body["violations"].size())
    }
}
