package at.se2group.backend.rules.service


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import shared.models.game.domain.*
import shared.models.game.domain.TileColor
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

    @Test
    fun `returns invalid when newly committed score is lower than 30`() {
        val rackTile = tile("tile-1", TileColor.RED, 5)
        val player = player().copy(rackTiles = listOf(rackTile))
        val game = game(player)
        val draft = draft(
            boardSets = listOf(
                BoardSet(boardSetId = "set-1", tiles = listOf(rackTile)))
        )

        val result = service.validate(game, player, draft)

        assertFalse(result.isValid)
        assertEquals("INITIAL_MELD_TOO_LOW", result.violations.single().code)
        assertEquals("Initial meld must score at least 30 points", result.violations.single().message)
    }


}
