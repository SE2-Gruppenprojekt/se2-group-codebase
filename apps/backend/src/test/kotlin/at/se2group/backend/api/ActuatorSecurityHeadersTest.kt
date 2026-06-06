package at.se2group.backend.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * End-to-end MVC coverage for the deployed health endpoint contract targeted by the ZAP scan.
 *
 * The security scan explicitly reported findings on `/actuator/health`, so this test verifies
 * that the global response-header hardening also reaches actuator responses, not only normal
 * application controllers and MVC error payloads.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ActuatorSecurityHeadersTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `health endpoint returns hardened security headers`() {
        mockMvc.get("/actuator/health")
            .andExpect {
                status { isOk() }
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
