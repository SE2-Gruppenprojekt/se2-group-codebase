package at.se2group.backend.api

import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.service.SecurityScanFixtureService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import shared.models.game.domain.GameStatus
import shared.models.lobby.domain.LobbyStatus

/**
 * Integration coverage for the startup security-scan fixtures.
 *
 * The ZAP API coverage workflow depends on a small deterministic backend state
 * for a handful of valid Layer 3 requests. This test enables the fixture
 * bootstrap explicitly and verifies that the known lobby, game, and draft ids
 * are present after the application context starts.
 */
@SpringBootTest(properties = ["app.scan-fixture.enabled=true", "app.scan-fixture.secret=test-secret"])
@Transactional
class SecurityScanFixtureBootstrapTest {

    @Autowired
    lateinit var lobbyRepository: LobbyRepository

    @Autowired
    lateinit var gameRepository: GameRepository

    @Autowired
    lateinit var turnDraftRepository: TurnDraftRepository

    @Test
    fun `security scan fixtures are seeded deterministically`() {
        val openLobby = lobbyRepository.findById(SecurityScanFixtureService.SCAN_OPEN_LOBBY_ID).orElse(null)
        val activeLobby = lobbyRepository.findById(SecurityScanFixtureService.SCAN_ACTIVE_LOBBY_ID).orElse(null)
        val game = gameRepository.findById(SecurityScanFixtureService.SCAN_GAME_ID).orElse(null)
        val draft = turnDraftRepository.findByGameId(SecurityScanFixtureService.SCAN_GAME_ID)

        assertNotNull(openLobby)
        assertNotNull(activeLobby)
        assertNotNull(game)
        assertNotNull(draft)
        assertEquals(SecurityScanFixtureService.SCAN_HOST_USER_ID, openLobby?.hostUserId)
        assertEquals(LobbyStatus.OPEN, openLobby?.status)
        assertEquals(SecurityScanFixtureService.SCAN_HOST_USER_ID, activeLobby?.hostUserId)
        assertEquals(LobbyStatus.IN_GAME, activeLobby?.status)
        assertEquals(SecurityScanFixtureService.SCAN_ACTIVE_LOBBY_ID, game?.lobbyId)
        assertEquals(SecurityScanFixtureService.SCAN_HOST_USER_ID, game?.currentPlayerUserId)
        assertEquals(GameStatus.ACTIVE, game?.status)
        assertEquals(SecurityScanFixtureService.SCAN_HOST_USER_ID, draft?.playerUserId)
        assertEquals(0L, draft?.version)
    }
}
