package at.se2group.backend.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
    private val restAccessDeniedHandler: RestAccessDeniedHandler
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                it.authenticationEntryPoint(restAuthenticationEntryPoint)
                it.accessDeniedHandler(restAccessDeniedHandler)
            }
            .authorizeHttpRequests {
                it.requestMatchers("/", "/robots.txt", "/sitemap.xml").permitAll()
                it.requestMatchers("/actuator/health").permitAll()
                it.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                it.requestMatchers("/internal/security/**").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/lobbies").permitAll()
                it.requestMatchers(HttpMethod.POST, "/api/lobbies").permitAll()
                it.requestMatchers(HttpMethod.POST, "/api/lobbies/*/join").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
