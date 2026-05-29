package at.se2group.backend.api

import at.se2group.backend.dto.CreateLobbyRequest
import jakarta.validation.Valid
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(ExceptionProbeController::class)
@Import(GlobalExceptionHandler::class)
class GlobalExceptionHandlerMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns 400 for path variable type mismatch`() {
        mockMvc.get("/api/exception-probe/type-mismatch/not-a-number")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("BAD_REQUEST") }
                jsonPath("$.errorMessage") { value("Request parameter type mismatch") }
            }
    }

    @Test
    fun `returns 400 for missing required header`() {
        mockMvc.get("/api/exception-probe/missing-header")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("BAD_REQUEST") }
                jsonPath("$.errorMessage") { value("Missing required header: X-Probe-User") }
            }
    }

    @Test
    fun `returns 400 for malformed request body`() {
        mockMvc.post("/api/exception-probe/unreadable") {
            contentType = MediaType.APPLICATION_JSON
            content = """{ "displayName": "Alice", "maxPlayers": "abc" }"""
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("BAD_REQUEST") }
                jsonPath("$.errorMessage") { value("Malformed JSON request body") }
            }
    }

    @Test
    fun `returns 400 for bean validation failure`() {
        mockMvc.post("/api/exception-probe/validation") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "displayName": "",
                    "maxPlayers": 4,
                    "isPrivate": false,
                    "allowGuests": true
                }
            """.trimIndent()
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("BAD_REQUEST") }
                jsonPath("$.errorMessage") { value("Request validation failed") }
            }
    }

    @Test
    fun `returns 500 for unexpected exception`() {
        mockMvc.get("/api/exception-probe/generic")
            .andExpect {
                status { isInternalServerError() }
                jsonPath("$.errorCode") { value("INTERNAL_SERVER_ERROR") }
                jsonPath("$.errorMessage") { value("An unexpected error occurred") }
            }
    }
}

@RestController
@RequestMapping("/api/exception-probe")
private class ExceptionProbeController {

    @GetMapping("/type-mismatch/{turnNumber}")
    fun typeMismatch(@PathVariable turnNumber: Int): Int = turnNumber

    @GetMapping("/missing-header")
    fun missingHeader(@RequestHeader("X-Probe-User") userId: String): String = userId

    @PostMapping("/unreadable")
    fun unreadable(@RequestBody request: CreateLobbyRequest): String = request.displayName

    @PostMapping("/validation")
    fun validation(@Valid @RequestBody request: CreateLobbyRequest): String = request.displayName

    @GetMapping("/generic")
    fun generic(): Nothing = throw RuntimeException("boom")
}
