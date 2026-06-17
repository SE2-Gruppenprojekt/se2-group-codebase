package at.se2group.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

@Service
class JwtService(
    private val jwtProperties: JwtProperties
) {
    private val algorithm = Algorithm.HMAC256(jwtProperties.secret)
    private val verifier = JWT.require(algorithm)
        .withIssuer(jwtProperties.issuer)
        .build()

    fun issueAccessToken(userId: String): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(jwtProperties.accessTokenTtlSeconds)
        return JWT.create()
            .withIssuer(jwtProperties.issuer)
            .withSubject(userId)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
    }

    fun extractUserId(token: String): String {
        return try {
            verifier.verify(token).subject
                ?: throw InvalidBearerTokenAuthenticationException()
        } catch (_: Exception) {
            throw InvalidBearerTokenAuthenticationException()
        }
    }
}
