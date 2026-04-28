package at.se2group.backend.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TileRulesTest {

    @Test
    fun `tile rules expose expected numbered tile constants`() {
        assertEquals(2, TileRules.NUMBERED_TILE_COPY_COUNT)
        assertEquals(1, TileRules.MIN_TILE_NUMBER)
        assertEquals(13, TileRules.MAX_TILE_NUMBER)
    }

    @Test
    fun `tile rules expose expected joker colors`() {
        assertEquals(
            listOf(TileColor.BLACK, TileColor.RED),
            TileRules.jokerColors
        )
    }
}
