package at.se2group.backend.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * End-to-end MVC coverage for the generated OpenAPI document exposed by Springdoc.
 *
 * The upcoming ZAP API coverage expansion depends on a stable machine-readable
 * contract endpoint. This test verifies that the backend actually publishes
 * `/v3/api-docs` and that the response looks like an OpenAPI document rather
 * than just any JSON payload.
 *
 * Coverage goals in this test:
 *
 * - the OpenAPI endpoint is reachable at `/v3/api-docs`
 * - the endpoint returns `200 OK`
 * - the response is JSON
 * - the payload contains the core top-level OpenAPI markers ZAP will rely on
 * - the global response hardening still applies to the generated document
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `openapi docs endpoint exposes generated api contract`() {
        mockMvc.get("/v3/api-docs")
            .andExpect {
                status { isOk() }
                // Lock down the structural markers of an OpenAPI document so the
                // ZAP AF import step can depend on this endpoint confidently.
                jsonPath("$.openapi") { exists() }
                jsonPath("$.info") { exists() }
                jsonPath("$.paths") { exists() }
                header { string("Content-Type", org.hamcrest.Matchers.containsString("application/json")) }
                header { string("X-Content-Type-Options", "nosniff") }
                header { string("Cross-Origin-Resource-Policy", "same-origin") }
                header { string("Cross-Origin-Embedder-Policy", "require-corp") }
                header { string("Cross-Origin-Opener-Policy", "same-origin") }
                header { string("Strict-Transport-Security", "max-age=31536000; includeSubDomains") }
                header { string("Cache-Control", "no-cache, no-store, must-revalidate, private") }
                header { string("Pragma", "no-cache") }
                header { string("Expires", "0") }
            }
    }
}
