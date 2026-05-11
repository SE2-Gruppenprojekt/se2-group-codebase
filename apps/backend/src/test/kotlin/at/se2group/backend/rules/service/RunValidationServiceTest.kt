package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import shared.models.game.domain.BoardSet
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor


class RunValidationServiceTest {
    private val service = RunValidationService()

    @Test
    fun `throws not implemented exception`() {
        val exception = assertThrows(UnsupportedOperationException::class.java) {
            service.validate(
                BoardSet(
                    boardSetId = "set-1",
                    tiles = listOf(
                        NumberedTile("tile-1", TileColor.RED, 5),
                        NumberedTile("tile-2", TileColor.BLUE, 6),
                        NumberedTile("tile-3", TileColor.RED, 7 )
                    )
                )
            )
        }
        assertEquals("Run validation is not implemented yet.", exception.message)
    }
}
