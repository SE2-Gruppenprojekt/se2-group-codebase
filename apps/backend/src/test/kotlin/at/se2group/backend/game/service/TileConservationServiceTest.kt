package at.se2group.backend.game.service

import at.se2group.backend.domain.BoardSet
import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.domain.GamePlayer
import at.se2group.backend.domain.GameStatus
import at.se2group.backend.domain.JokerTile
import at.se2group.backend.domain.NumberedTile
import at.se2group.backend.domain.Tile
import at.se2group.backend.domain.TileColor
import at.se2group.backend.domain.TurnDraft
import at.se2group.backend.service.TileConservationService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class TileConservationServiceTest {

    private val tileConservationService = TileConservationService()


    private fun game(boardTiles: List<Tile> = emptyList(), rackTiles: List<Tile> = emptyList())=
        ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayer(userId = "user-1", displayName = "Anna", turnOrder = 0, rackTiles = rackTiles)
            ),
            boardSets = if (boardTiles.isEmpty()) emptyList() else listOf(
                BoardSet(
                    boardSetId = "set-1",
                    tiles = boardTiles
                )
            ),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = Instant.now()
        )

    private fun draft(boardTiles: List<Tile> = emptyList(), rackTiles: List<Tile> = emptyList()) =
        TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1",
            boardSets = if (boardTiles.isEmpty()) emptyList() else listOf(
                BoardSet(
                    boardSetId = "set-1",
                    tiles = boardTiles
                )
            ),
            rackTiles = rackTiles
        )

    @Test
    fun `passes when rack tiles are conserved`() {
        val tiles = listOf(NumberedTile(TileColor.RED, 1), NumberedTile(TileColor.BLUE, 2))
        assertDoesNotThrow {
            tileConservationService.validate(
                game(rackTiles = tiles),
                "user-1",
                draft(rackTiles = tiles)
            )
        }
    }

    @Test
    fun `passes when tile moved from rack to board`() {
       val tile = NumberedTile(TileColor.RED, 5)
        val extra = listOf(NumberedTile(TileColor.BLUE, 3), NumberedTile(TileColor.ORANGE, 4))
        val confirmedGame = game(rackTiles = listOf(tile)+ extra)
        val candidateDraft = draft(boardTiles = listOf(tile) + extra)
        assertDoesNotThrow { tileConservationService.validate(confirmedGame, "user-1", candidateDraft) }
    }

    @Test
    fun `passes when tile moved from board to rack`() {
        val tile = NumberedTile(TileColor.BLACK, 8)
        val extra = listOf(NumberedTile(TileColor.BLUE, 1), NumberedTile(TileColor.RED, 3))
        val confirmedGame = game(boardTiles = listOf(tile) + extra)
        val candidateDraft = draft(rackTiles = listOf(tile) + extra)
        assertDoesNotThrow { tileConservationService.validate(confirmedGame, "user-1", candidateDraft) }
    }

    @Test
    fun `rejects when rack tile is missing`() {
       val tiles = listOf(NumberedTile(TileColor.RED, 1), NumberedTile(TileColor.BLUE, 2))
        val confirmedGame = game(rackTiles = tiles)
        val candidateDraft = draft(rackTiles = listOf(NumberedTile(TileColor.RED, 2)))
        val exception = assertThrows<IllegalArgumentException> { tileConservationService.validate(confirmedGame, "user-1", candidateDraft) }

        assert(exception.message!!.contains("missing"))
    }

    @Test
    fun `rejects when board tile is missing`() {
        val boardTile = NumberedTile(TileColor.ORANGE, 2)
        val rackTile = NumberedTile(TileColor.BLACK, 7)
        val confirmedGame = game(boardTiles = listOf(boardTile), rackTiles = listOf(rackTile))
        val candidateDraft = draft(boardTiles = listOf(boardTile))
        val exception = assertThrows<IllegalArgumentException> { tileConservationService.validate(confirmedGame, "user-1", candidateDraft) }
        assert(exception.message!!.contains("missing"))
    }

    @Test
    fun `rejects when tile is duplicated in draft rack`() {
        val tile = NumberedTile(TileColor.RED, 5)
        val confirmedGame = game(rackTiles = listOf(tile))
        val candidateDraft = draft(rackTiles = listOf(tile,tile))
        val exception = assertThrows<IllegalArgumentException> { tileConservationService.validate(confirmedGame, "user-1", candidateDraft) }

        assert(exception.message!!.contains("extra"))
    }

    @Test
    fun `rejects when tile shows up on board and rack simultaneously`() {
        val tile = NumberedTile(TileColor.ORANGE, 4)
        val confirmedGame = game(rackTiles = listOf(tile))
        val candidateDraft = draft(boardTiles = listOf(tile),rackTiles = listOf(tile))
        val exception = assertThrows<IllegalArgumentException> { tileConservationService.validate(confirmedGame, "user-1", candidateDraft) }

        assert(exception.message!!.contains("extra"))

    }


    @Test
    fun `rejects when draft contains a tile that is not allowed in the game`() {
        val confirmedGame = game(rackTiles = listOf(NumberedTile(TileColor.BLACK, 5)))
        val candidateDraft = draft(rackTiles = listOf(NumberedTile(TileColor.RED, 13)))
        val exception = assertThrows<IllegalArgumentException> { tileConservationService.validate(confirmedGame, "user-1", candidateDraft) }

        assert(exception.message!!.contains("extra"))
    }

    @Test
    fun `rejects when joker is invented in the draft`() {
        val confirmedGame = game(rackTiles = listOf(NumberedTile(TileColor.BLACK, 5)))
        val candidateDraft = draft(rackTiles = listOf(JokerTile(TileColor.BLUE)))
        val exception = assertThrows<IllegalArgumentException> { tileConservationService.validate(confirmedGame, "user-1", candidateDraft) }

        assert(exception.message!!.contains("extra"))
    }

}
