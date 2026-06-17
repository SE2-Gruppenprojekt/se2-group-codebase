package at.se2group.backend.security

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.same
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint

@ExtendWith(MockitoExtension::class)
class JwtAuthenticationFilterTest {

    @Mock
    lateinit var jwtService: JwtService

    @Mock
    lateinit var authenticationEntryPoint: AuthenticationEntryPoint

    @Mock
    lateinit var filterChain: FilterChain

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `filter passes through when authorization header is missing`() {
        val filter = JwtAuthenticationFilter(jwtService, authenticationEntryPoint)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        verifyNoInteractions(jwtService, authenticationEntryPoint)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `filter rejects authorization header without bearer prefix`() {
        val filter = JwtAuthenticationFilter(jwtService, authenticationEntryPoint)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Token abc")
        }
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        verify(authenticationEntryPoint).commence(
            same(request),
            same(response),
            any()
        )
        verify(filterChain, never()).doFilter(request, response)
        verifyNoInteractions(jwtService)
    }

    @Test
    fun `filter authenticates request when bearer token is valid`() {
        val filter = JwtAuthenticationFilter(jwtService, authenticationEntryPoint)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer valid-token")
        }
        val response = MockHttpServletResponse()

        `when`(jwtService.extractUserId("valid-token")).thenReturn("user-123")

        filter.doFilter(request, response, filterChain)

        verify(jwtService).extractUserId("valid-token")
        verify(filterChain).doFilter(request, response)
        verifyNoInteractions(authenticationEntryPoint)
        assertEquals("user-123", SecurityContextHolder.getContext().authentication?.name)
    }

    @Test
    fun `filter authenticates request when bearer scheme casing varies`() {
        val filter = JwtAuthenticationFilter(jwtService, authenticationEntryPoint)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "bEaReR valid-token")
        }
        val response = MockHttpServletResponse()

        `when`(jwtService.extractUserId("valid-token")).thenReturn("user-123")

        filter.doFilter(request, response, filterChain)

        verify(jwtService).extractUserId("valid-token")
        verify(filterChain).doFilter(request, response)
        verifyNoInteractions(authenticationEntryPoint)
        assertEquals("user-123", SecurityContextHolder.getContext().authentication?.name)
    }

    @Test
    fun `filter rejects bearer scheme without token`() {
        val filter = JwtAuthenticationFilter(jwtService, authenticationEntryPoint)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer")
        }
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        verify(authenticationEntryPoint).commence(
            same(request),
            same(response),
            any()
        )
        verify(filterChain, never()).doFilter(request, response)
        verifyNoInteractions(jwtService)
    }

    @Test
    fun `filter clears security context and returns 401 when bearer token is invalid`() {
        val filter = JwtAuthenticationFilter(jwtService, authenticationEntryPoint)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer broken-token")
        }
        val response = MockHttpServletResponse()
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated("stale-user", null, emptyList())

        `when`(jwtService.extractUserId("broken-token"))
            .thenThrow(InvalidBearerTokenAuthenticationException())

        filter.doFilter(request, response, filterChain)

        verify(jwtService).extractUserId("broken-token")
        verify(authenticationEntryPoint).commence(
            same(request),
            same(response),
            any()
        )
        verify(filterChain, never()).doFilter(request, response)
        assertNull(SecurityContextHolder.getContext().authentication)
    }
}
