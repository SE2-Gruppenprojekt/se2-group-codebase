package at.se2group.backend.domain
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TurnDraft
import shared.models.game.domain.TileColor
import shared.models.game.domain.GameStatus

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GameStartResultTest {

    @Test
    fun `should store confirmed game and turn draft`() {
        val player = GamePlayer(
            userId = "user-1",
            displayName = "Player One",
            turnOrder = 0,
            rackTiles = listOf(NumberedTile(tileId = "tile-1", color = TileColor.RED, number = 3)),
            hasCompletedInitialMeld = false,
            score = 0
        )

        val confirmedGame = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(player),
            boardSets = emptyList(),
            drawPile = listOf(NumberedTile(tileId = "tile-2", color = TileColor.BLUE, number = 5)),
            currentPlayerUserId = player.userId,
            status = GameStatus.ACTIVE
        )

        val turnDraft = TurnDraft(
            gameId = confirmedGame.gameId,
            playerUserId = player.userId,
            boardSets = emptyList(),
            rackTiles = player.rackTiles
        )

        val result = GameStartResult(
            confirmedGame = confirmedGame,
            turnDraft = turnDraft
        )

        assertEquals(confirmedGame, result.confirmedGame)
        assertEquals(turnDraft, result.turnDraft)
    }

    @Test
    fun `should default turn draft to null`() {
        val player = GamePlayer(
            userId = "user-1",
            displayName = "Player One",
            turnOrder = 0,
            rackTiles = emptyList(),
            hasCompletedInitialMeld = false,
            score = 0
        )

        val confirmedGame = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(player),
            boardSets = emptyList(),
            drawPile = emptyList(),
            currentPlayerUserId = player.userId,
            status = GameStatus.ACTIVE
        )

        val result = GameStartResult(confirmedGame = confirmedGame)

        assertEquals(confirmedGame, result.confirmedGame)
        assertNull(result.turnDraft)
    }
}
