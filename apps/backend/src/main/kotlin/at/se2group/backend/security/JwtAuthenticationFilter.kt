package at.se2group.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
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

        if (!authorization.startsWith("Bearer ")) {
            authenticationEntryPoint.commence(
                request,
                response,
                InvalidBearerTokenAuthenticationException()
            )
            return
        }

        val token = authorization.removePrefix("Bearer ").trim()

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
