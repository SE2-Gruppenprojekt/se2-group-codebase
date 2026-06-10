package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import shared.models.game.domain.*
import java.time.Instant

class FirstMoveValidationServiceTest {

    private val service = FirstMoveValidationService()

    private fun player(hasCompletedInitialMeld: Boolean = false) = GamePlayer(
        userId = "user-1",
        displayName = "Alice",
        turnOrder = 1,
        hasCompletedInitialMeld = hasCompletedInitialMeld,
        joinedAt = Instant.now()
    )

    private fun game(player: GamePlayer) = ConfirmedGame(
        gameId = "game-1",
        lobbyId = "lobby-1",
        players = listOf(player),
        currentPlayerUserId = "user-1",
        status = GameStatus.ACTIVE
    )

    private fun draft(boardSets: List<BoardSet> = emptyList()) = TurnDraft(
        gameId = "game-1",
        playerUserId = "user-1",
        boardSets = boardSets,
        rackTiles = emptyList()
    )

    @Test
    fun `validate throws not implemented yet`() {
        val exception = assertThrows(UnsupportedOperationException::class.java) {
            service.validate(game(player()), player(), draft())
        }
        assertEquals("Not implemented yet", exception.message)
    }
}
