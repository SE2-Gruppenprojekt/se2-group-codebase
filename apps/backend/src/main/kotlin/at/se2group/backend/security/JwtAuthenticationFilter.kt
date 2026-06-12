package at.se2group.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val authenticationEntryPoint: AuthenticationEntryPoint
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorization = request.getHeader("Authorization")

        if (authorization.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val parts = authorization.trim().split(Regex("\\s+"), limit = 2)
        val scheme = parts.firstOrNull()
        val token = parts.getOrNull(1)?.trim()

        if (!scheme.equals("Bearer", ignoreCase = true) || token.isNullOrBlank()) {
            authenticationEntryPoint.commence(
                request,
                response,
                InvalidBearerTokenAuthenticationException()
            )
            return
        }

        try {
            val userId = jwtService.extractUserId(token)
            val authentication = UsernamePasswordAuthenticationToken.authenticated(
                userId,
                null,
                emptyList()
            )
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (ex: InvalidBearerTokenAuthenticationException) {
            SecurityContextHolder.clearContext()
            authenticationEntryPoint.commence(request, response, ex)
        }
    }
}
