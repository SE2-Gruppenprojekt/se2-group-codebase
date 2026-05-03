package at.se2group.backend.api

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleIllegalState should return 409`() {
        val ex = IllegalStateException("test error")

        val response = handler.handleIllegalState(ex)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("CONFLICT", response.body?.errorCode)
        assertEquals("test error", response.body?.errorMessage)
    }
}
