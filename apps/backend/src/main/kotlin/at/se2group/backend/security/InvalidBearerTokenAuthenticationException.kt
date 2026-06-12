package at.se2group.backend.security

import org.springframework.security.core.AuthenticationException

class InvalidBearerTokenAuthenticationException :
    AuthenticationException("Missing or invalid bearer token")
