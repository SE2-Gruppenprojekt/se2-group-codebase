package at.se2group.backend.rules.service


import org.junit.jupiter.api.Assertions.assertEquals
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

    @Test
    fun `returns valid when newly committed score is 30`(){
        val tiles = listOf(
            tile("tile-1", TileColor.RED, 10),
            tile("tile-2", TileColor.RED, 10),
            tile("tile-3", TileColor.RED, 10),
        )
        val player = player().copy(rackTiles = tiles)
        val game = game(player)
        val draft = draft(
            boardSets = listOf(
                BoardSet(boardSetId = "set-1", tiles = tiles)))

        val result = service.validate(game, player, draft)

        assertTrue(result.isValid)
    }

    @Test
    fun `returns valid when newly committed is higher than 30`() {
        val tiles = listOf(
            tile("tile-1", TileColor.RED, 13),
            tile("tile-2", TileColor.RED, 13),
            tile("tile-3", TileColor.RED, 13)

        )
        val player = player().copy(rackTiles = tiles)
        val game = game(player)
        val draft = draft(
            boardSets = listOf(
                BoardSet(boardSetId = "set-1", tiles = tiles)
            )
        )

        val result = service.validate(game, player, draft)

        assertTrue(result.isValid)
    }

    @Test
    fun `does not count existing board tiles toward initial meld score`() {
        val existingBoardTile = tile("existing-1", TileColor.BLUE, 13)
        val rackTile = tile("rack-1", TileColor.RED, 5)

        val player = player().copy(rackTiles = listOf(rackTile))
        val game = game(player).copy(
            boardSets = listOf(BoardSet(boardSetId = "existing-1", tiles = listOf(existingBoardTile)))
        )
        val draft = draft(
            boardSets = listOf(
                BoardSet(boardSetId = "existing-1", tiles = listOf(existingBoardTile)),
                BoardSet(boardSetId = "new-set", tiles = listOf(rackTile))
            )
        )
        val result = service.validate(game, player, draft)

        assertFalse(result.isValid)
        assertEquals("INITIAL_MELD_TOO_LOW", result.violations.single().code)

    }


}
