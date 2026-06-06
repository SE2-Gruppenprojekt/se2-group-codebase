package at.se2group.backend.api

import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.service.DrawTileService
import at.se2group.backend.service.EndTurnService
import at.se2group.backend.service.GameService
import at.se2group.backend.service.SecurityScanFixtureService
import at.se2group.backend.service.TurnDraftService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import shared.models.game.domain.NumberedTile
import shared.models.game.request.EndTurnRequest
import shared.models.game.request.TileRequest
import shared.models.game.request.UpdateDraftRequest

@SpringBootTest(properties = ["app.scan-fixture.enabled=false"])
@Transactional
class SecurityScanFixtureServiceTest {

    @Autowired
    lateinit var securityScanFixtureService: SecurityScanFixtureService

    @Autowired
    lateinit var lobbyRepository: LobbyRepository

    @Autowired
    lateinit var gameRepository: GameRepository

    @Autowired
    lateinit var turnDraftRepository: TurnDraftRepository

    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var turnDraftService: TurnDraftService

    @Autowired
    lateinit var drawTileService: DrawTileService

    @Autowired
    lateinit var endTurnService: EndTurnService

    @Test
    fun `recreates deterministic fixture state idempotently`() {
        val first = securityScanFixtureService.recreateFixture()
        val second = securityScanFixtureService.recreateFixture()

        assertEquals(first, second)
        assertTrue(lobbyRepository.findById(first.lobbyId).isPresent)
        assertTrue(lobbyRepository.findById(SecurityScanFixtureService.SCAN_ACTIVE_LOBBY_ID).isPresent)
        assertTrue(gameRepository.findById(first.gameId).isPresent)
        assertTrue(turnDraftRepository.findById(first.gameId).isPresent)
    }

    @Test
    fun `fixture supports positive game get draft draw and end turn flow`() {
        val fixture = securityScanFixtureService.recreateFixture()

        val game = gameService.getGame(fixture.gameId)
        assertEquals(fixture.gameId, game.gameId)
        assertEquals(fixture.hostUserId, game.currentPlayerUserId)

        val updatedDraft = turnDraftService.updateDraft(
            fixture.gameId,
            fixture.draftOwnerUserId,
            UpdateDraftRequest(
                boardSets = emptyList(),
                rackTiles = SecurityScanFixtureService.preDrawRack().map { it.toRequest() }
            )
        )
        assertEquals(fixture.draftOwnerUserId, updatedDraft.playerUserId)
        assertEquals(
            SecurityScanFixtureService.preDrawRack().map { it.tileId },
            updatedDraft.rackTiles.map { it.tileId }
        )

        val drawnGame = drawTileService.drawTile(fixture.gameId, fixture.hostUserId)
        val hostAfterDraw = drawnGame.players.first { it.userId == fixture.hostUserId }
        assertEquals(
            SecurityScanFixtureService.postDrawRack().map { it.tileId },
            hostAfterDraw.rackTiles.map { it.tileId }
        )

        val endedTurnGame = endTurnService.endTurn(
            fixture.gameId,
            fixture.hostUserId,
            EndTurnRequest(
                boardSets = emptyList(),
                rackTiles = SecurityScanFixtureService.postDrawRack().map { it.toRequest() }
            )
        )
        assertEquals(fixture.guestUserId, endedTurnGame.currentPlayerUserId)

        val nextDraft = turnDraftRepository.findByGameId(fixture.gameId)
        assertNotNull(nextDraft)
        assertEquals(fixture.guestUserId, nextDraft?.playerUserId)
    }

    private fun NumberedTile.toRequest() = TileRequest(
        tileId = tileId,
        color = color.name,
        number = number,
        isJoker = false
    )
}
