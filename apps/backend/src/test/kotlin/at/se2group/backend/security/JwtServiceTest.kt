package at.se2group.backend.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JwtServiceTest {

    private val jwtService = JwtService(
        JwtProperties(
            issuer = "test-backend",
            secret = "test-secret-test-secret-test-secret",
            accessTokenTtlSeconds = 3600
        )
    )

    @Test
    fun `issueAccessToken and extractUserId round trip user identity`() {
        val token = jwtService.issueAccessToken("user-123")
        assertEquals("user-123", jwtService.extractUserId(token))
    }

    @Test
    fun `extractUserId rejects invalid token`() {
        assertThrows<InvalidBearerTokenAuthenticationException> {
            jwtService.extractUserId("not-a-valid-token")
        }
    }
}
