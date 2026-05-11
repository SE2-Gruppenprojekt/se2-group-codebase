package at.se2group.backend.service

import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile
import shared.models.game.domain.TileColor
import shared.models.game.domain.GameStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock

@ExtendWith(MockitoExtension::class)
class GameInitializationServiceTest {

    @Mock
    lateinit var tilePoolGenerationService: TilePoolGenerationService

    @Test
    fun `createGameFromLobby creates confirmed game and initial draft from lobby`() {
        val gameInitializationService = GameInitializationService(
            tilePoolGenerationService = tilePoolGenerationService,
            tileShuffleService = TileShuffleService(),
            turnDraftRepository = mock()
        )

        val lobby = Lobby(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            players = listOf(
                LobbyPlayer(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = true
                ),
                LobbyPlayer(
                    userId = "user-2",
                    displayName = "Bob",
                    isReady = true
                )
            ),
            status = LobbyStatus.IN_GAME,
            settings = LobbySettings(
                maxPlayers = 4,
                isPrivate = false,
                allowGuests = true
            )
        )

        val orderedPool = createTiles(40)
        `when`(tilePoolGenerationService.createTilePool()).thenReturn(orderedPool)

        val result = gameInitializationService.createGameFromLobby(lobby)

        assertEquals("lobby-1", result.confirmedGame.lobbyId)
        assertEquals(GameStatus.ACTIVE, result.confirmedGame.status)
        assertTrue(result.confirmedGame.gameId.isNotBlank())
        assertEquals(2, result.confirmedGame.players.size)
        assertEquals(12, result.confirmedGame.drawPile.size)
        assertTrue(
            result.confirmedGame.players.any {
                it.userId == result.confirmedGame.currentPlayerUserId
            }
        )

        val firstPlayer = result.confirmedGame.players.first { it.turnOrder == 0 }
        val secondPlayer = result.confirmedGame.players.first { it.turnOrder == 1 }

        assertEquals("host-1", firstPlayer.userId)
        assertEquals("Alice", firstPlayer.displayName)
        assertEquals("user-2", secondPlayer.userId)
        assertEquals("Bob", secondPlayer.displayName)
        assertEquals(14, firstPlayer.rackTiles.size)
        assertEquals(14, secondPlayer.rackTiles.size)
        assertEquals(28, result.confirmedGame.players.sumOf { it.rackTiles.size })
        assertEquals("host-1", result.confirmedGame.currentPlayerUserId)

        val allDistributedTiles = result.confirmedGame.players.flatMap { it.rackTiles }
        val allGameTiles = allDistributedTiles + result.confirmedGame.drawPile
        assertEquals(40, allGameTiles.size)
        assertEquals(orderedPool.toSet(), allGameTiles.toSet())

        assertEquals(result.confirmedGame.gameId, result.turnDraft?.gameId)
        assertEquals(firstPlayer.userId, result.turnDraft?.playerUserId)
        assertEquals(firstPlayer.rackTiles, result.turnDraft?.rackTiles)
        assertEquals(emptyList<Nothing>(), result.turnDraft?.boardSets)
        assertEquals(0, result.turnDraft?.version)

        verify(tilePoolGenerationService).createTilePool()
    }

    private fun createTiles(count: Int): List<Tile> {
        return (0 until count).map { index ->
            NumberedTile(
                tileId = "tile-$index",
                color = TileColor.entries[index % TileColor.entries.size],
                number = (index % 13) + 1
            )
        }
    }
}
