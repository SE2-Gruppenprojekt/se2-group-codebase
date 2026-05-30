package at.se2group.backend.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * MVC tests for [RootController].
 *
 * This class verifies the backend root endpoint at the HTTP layer with a narrow
 * `@WebMvcTest` slice.
 *
 * The root endpoint is intentionally small, but it still represents a public
 * contract: callers expect a stable JSON response that indicates which service
 * they reached and whether it is up. Testing that contract directly at the MVC
 * layer keeps the controller covered without pulling in unrelated backend
 * infrastructure such as services, persistence, or websocket configuration.
 *
 * Coverage goals in this test:
 *
 * - the endpoint is mapped at `/`
 * - the endpoint returns `200 OK`
 * - the JSON payload contains the documented service metadata
 * - the exact response shape remains stable for external callers
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
                // Lock down the exact public metadata fields so changes to the
                // root response remain intentional.
                jsonPath("$.service") { value("SE2 Rummikub Backend") }
                jsonPath("$.status") { value("running") }
            }
    }
}
