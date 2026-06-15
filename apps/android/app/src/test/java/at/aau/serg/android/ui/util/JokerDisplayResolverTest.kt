package at.aau.serg.android.ui.screens.game.util

import org.junit.Assert.assertEquals
import org.junit.Test
import shared.models.game.domain.BoardSet
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor

class JokerDisplayResolverTest {

    @Test
    fun `non joker tile returns empty label`() {
        val tile = NumberedTile("t1", TileColor.RED, 5)

        assertEquals("", jokerDisplayResolver(boardSet = null, tile = tile))
    }

    @Test
    fun `joker without board set falls back to J`() {
        val tile = JokerTile("j1", TileColor.RED)

        assertEquals("J", jokerDisplayResolver(boardSet = null, tile = tile))
    }

    @Test
    fun `joker fills gap in run`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.RUN,
            tiles = listOf(
                NumberedTile("t1", TileColor.RED, 5),
                joker,
                NumberedTile("t2", TileColor.RED, 7)
            )
        )

        assertEquals("6", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `joker extends run at upper end`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.RUN,
            tiles = listOf(
                NumberedTile("t1", TileColor.RED, 5),
                NumberedTile("t2", TileColor.RED, 6),
                joker
            )
        )

        assertEquals("7", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `joker with ambiguous gap in run falls back to J`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.RUN,
            tiles = listOf(
                NumberedTile("t1", TileColor.RED, 5),
                NumberedTile("t2", TileColor.RED, 9),
                joker
            )
        )

        assertEquals("J", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `joker alone with single numbered tile in run falls back to J`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.RUN,
            tiles = listOf(NumberedTile("t1", TileColor.RED, 5), joker)
        )

        assertEquals("J", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `joker extends run at lower end when already at upper bound`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.RUN,
            tiles = listOf(
                NumberedTile("t1", TileColor.RED, 11),
                NumberedTile("t2", TileColor.RED, 12),
                NumberedTile("t3", TileColor.RED, 13),
                joker
            )
        )

        assertEquals("10", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `joker in full-range run falls back to J`() {
        val joker = JokerTile("j1", TileColor.RED)
        val numberedTiles = (1..13).map { NumberedTile("t$it", TileColor.RED, it) }
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.RUN,
            tiles = numberedTiles + joker
        )

        assertEquals("J", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `ambiguous group with two jokers falls back to J`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.GROUP,
            tiles = listOf(
                joker,
                JokerTile("j2", TileColor.RED),
                NumberedTile("t1", TileColor.BLUE, 9)
            )
        )

        assertEquals("J", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `joker in group takes the shared number`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.GROUP,
            tiles = listOf(
                NumberedTile("t1", TileColor.BLUE, 9),
                NumberedTile("t2", TileColor.ORANGE, 9),
                joker
            )
        )

        assertEquals("9", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `unresolved set falls back to J`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.UNRESOLVED,
            tiles = listOf(joker, NumberedTile("t1", TileColor.RED, 5))
        )

        assertEquals("J", jokerDisplayResolver(boardSet, joker))
    }

    @Test
    fun `ambiguous run with two jokers falls back to J`() {
        val joker = JokerTile("j1", TileColor.RED)
        val boardSet = BoardSet(
            boardSetId = "set1",
            type = BoardSetType.RUN,
            tiles = listOf(
                joker,
                JokerTile("j2", TileColor.RED),
                NumberedTile("t1", TileColor.RED, 5)
            )
        )

        assertEquals("J", jokerDisplayResolver(boardSet, joker))
    }
}
