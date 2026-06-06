package at.se2group.backend.api

import at.se2group.backend.service.SecurityScanFixtureService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/internal/security")
@ConditionalOnProperty(
    prefix = "app.scan-fixture",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class SecurityScanFixtureController(
    private val securityScanFixtureService: SecurityScanFixtureService,
    @Value("\${app.scan-fixture.secret:}") private val expectedSecret: String
) {

    @PostMapping("/scan-fixture")
    fun createScanFixture(
        @RequestHeader("X-Scan-Secret", required = false) providedSecret: String?
    ): SecurityScanFixtureResponse {
        if (providedSecret.isNullOrBlank() || expectedSecret.isBlank() || providedSecret != expectedSecret) {
            throw SecurityException("Invalid scan fixture secret")
        }

        val state = securityScanFixtureService.recreateFixture()
        return SecurityScanFixtureResponse(
            lobbyId = state.lobbyId,
            hostUserId = state.hostUserId,
            guestUserId = state.guestUserId,
            gameId = state.gameId,
            draftOwnerUserId = state.draftOwnerUserId
        )
    }
}

data class SecurityScanFixtureResponse(
    val lobbyId: String,
    val hostUserId: String,
    val guestUserId: String,
    val gameId: String,
    val draftOwnerUserId: String
)
