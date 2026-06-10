package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
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

    private fun tile(id: String, color: TileColor, number: Int) = NumberedTile(
        tileId = id,
        color = color,
        number = number)

    @Test
    fun `returns valid when player already completed initial meld`() {
       val result = service.validate(
           confirmedGame = game(player(hasCompletedInitialMeld = true)),
           actingPlayer = player(hasCompletedInitialMeld = true),
           submittedDraft = draft()
       )
        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }
}
