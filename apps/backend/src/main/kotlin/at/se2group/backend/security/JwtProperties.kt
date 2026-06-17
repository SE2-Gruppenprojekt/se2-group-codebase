package at.se2group.backend.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth.jwt")
data class JwtProperties(
    val issuer: String,
    val secret: String,
    val accessTokenTtlSeconds: Long
)
