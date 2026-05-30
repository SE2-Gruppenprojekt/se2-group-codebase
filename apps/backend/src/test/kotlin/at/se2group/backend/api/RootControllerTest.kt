package at.se2group.backend.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * MVC tests for [RootController].
 *
 * This test verifies the public root endpoint contract directly at the web
 * layer so the controller stays covered independently from unrelated backend
 * infrastructure.
 */
@WebMvcTest(RootController::class)
class RootControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `root returns running service metadata`() {
        mockMvc.get("/")
            .andExpect {
                status { isOk() }
                jsonPath("$.service") { value("SE2 Rummikub Backend") }
                jsonPath("$.status") { value("running") }
            }
    }
}
