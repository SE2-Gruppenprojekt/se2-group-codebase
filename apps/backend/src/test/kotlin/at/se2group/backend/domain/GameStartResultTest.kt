package at.se2group.backend.domain

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.util.UUID

class GameStartResultTest {

    @Test
    fun `GameStartResult should hold the given ConfirmedGame and TurnDraft instances`() {
        val player = GamePlayer(
            userId = "user-1",
            displayName = "Player One",
            turnOrder = 0,
            rackTiles = listOf(
                NumberedTile(color = TileColor.RED, number = 3)
            ),
            hasCompletedInitialMeld = false,
            score = 0
        )

        val confirmedGame = ConfirmedGame(
            gameId = UUID.randomUUID().toString(),
            lobbyId = "lobby-123",
            players = listOf(player),
            boardSets = emptyList(),
            drawPile = listOf(
                NumberedTile(color = TileColor.BLUE, number = 5)
            ),
            currentPlayerUserId = player.userId,
            status = GameStatus.ACTIVE
        )

        val turnDraft = TurnDraft(
            gameId = confirmedGame.gameId,
            playerUserId = player.userId,
            boardSets = emptyList(),
            rackTiles = player.rackTiles
        )

        val gameStartResult = GameStartResult(
            confirmedGame = confirmedGame,
            turnDraft = turnDraft
        )

        assertSame(confirmedGame, gameStartResult.confirmedGame)
        assertSame(turnDraft, gameStartResult.turnDraft)
    }
}
