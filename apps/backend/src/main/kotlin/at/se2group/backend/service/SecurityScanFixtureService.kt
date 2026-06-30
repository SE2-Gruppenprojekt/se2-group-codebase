package at.se2group.backend.service

import at.se2group.backend.mapper.toEmbeddable
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.mapper.toEntity as toLobbyEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.persistence.TurnDraftEntity
import at.se2group.backend.persistence.TurnDraftRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.GameStatus
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor
import shared.models.game.domain.TurnDraft
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus
import java.time.Instant

@Service
class SecurityScanFixtureService(
    private val lobbyRepository: LobbyRepository,
    private val gameRepository: GameRepository,
    private val turnDraftRepository: TurnDraftRepository
) {

    @Transactional
    fun recreateFixture(): SecurityScanFixtureState {
        deleteIfPresent()
        seedOpenLobby()
        seedActiveGame()

        return SecurityScanFixtureState(
            lobbyId = SCAN_OPEN_LOBBY_ID,
            hostUserId = SCAN_HOST_USER_ID,
            guestUserId = SCAN_GUEST_USER_ID,
            gameId = SCAN_GAME_ID,
            draftOwnerUserId = SCAN_HOST_USER_ID
        )
    }

    @Transactional
    fun deleteFixture() {
        deleteIfPresent()
    }

    private fun deleteIfPresent() {
        if (turnDraftRepository.existsById(SCAN_GAME_ID)) {
            turnDraftRepository.deleteById(SCAN_GAME_ID)
        }
        if (gameRepository.existsById(SCAN_GAME_ID)) {
            gameRepository.deleteById(SCAN_GAME_ID)
        }
        if (lobbyRepository.existsById(SCAN_ACTIVE_LOBBY_ID)) {
            lobbyRepository.deleteById(SCAN_ACTIVE_LOBBY_ID)
        }
        if (lobbyRepository.existsById(SCAN_OPEN_LOBBY_ID)) {
            lobbyRepository.deleteById(SCAN_OPEN_LOBBY_ID)
        }
    }

    private fun seedOpenLobby() {
        val openLobby = Lobby(
            lobbyId = SCAN_OPEN_LOBBY_ID,
            hostUserId = SCAN_HOST_USER_ID,
            players = listOf(
                LobbyPlayer(
                    userId = SCAN_HOST_USER_ID,
                    displayName = "Scan Host",
                    isReady = false
                ),
                LobbyPlayer(
                    userId = SCAN_GUEST_USER_ID,
                    displayName = "Scan Guest",
                    isReady = false
                )
            ),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = 4,
                isPrivate = false,
                allowGuests = true
            )
        )

        lobbyRepository.save(openLobby.toLobbyEntity())
    }

    private fun seedActiveGame() {
        val activeLobby = Lobby(
            lobbyId = SCAN_ACTIVE_LOBBY_ID,
            hostUserId = SCAN_HOST_USER_ID,
            players = listOf(
                LobbyPlayer(
                    userId = SCAN_HOST_USER_ID,
                    displayName = "Scan Host",
                    isReady = true
                ),
                LobbyPlayer(
                    userId = SCAN_GUEST_USER_ID,
                    displayName = "Scan Guest",
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

        lobbyRepository.save(activeLobby.toLobbyEntity())

        val hostRack = preDrawRack()
        val guestRack = listOf(
            NumberedTile(
                tileId = "scan-guest-rack-black-3",
                color = TileColor.BLACK,
                number = 3
            ),
            NumberedTile(
                tileId = "scan-guest-rack-orange-9",
                color = TileColor.ORANGE,
                number = 9
            )
        )

        val confirmedGame = ConfirmedGame(
            gameId = SCAN_GAME_ID,
            lobbyId = SCAN_ACTIVE_LOBBY_ID,
            players = listOf(
                GamePlayer(
                    userId = SCAN_HOST_USER_ID,
                    displayName = "Scan Host",
                    turnOrder = 0,
                    rackTiles = hostRack,
                    hasCompletedInitialMeld = true
                ),
                GamePlayer(
                    userId = SCAN_GUEST_USER_ID,
                    displayName = "Scan Guest",
                    turnOrder = 1,
                    rackTiles = guestRack
                )
            ),
            boardSets = emptyList(),
            drawPile = listOf(
                NumberedTile(
                    tileId = SCAN_DRAWN_TILE_ID,
                    color = TileColor.RED,
                    number = 1
                ),
                NumberedTile(
                    tileId = "scan-draw-blue-2",
                    color = TileColor.BLUE,
                    number = 2
                ),
                NumberedTile(
                    tileId = "scan-draw-black-4",
                    color = TileColor.BLACK,
                    number = 4
                )
            ),
            currentPlayerUserId = SCAN_HOST_USER_ID,
            status = GameStatus.ACTIVE,
            createdAt = FIXTURE_TIME,
            startedAt = FIXTURE_TIME
        )

        gameRepository.save(confirmedGame.toEntity())

        val draft = TurnDraft(
            gameId = SCAN_GAME_ID,
            playerUserId = SCAN_HOST_USER_ID,
            boardSets = emptyList(),
            rackTiles = hostRack,
            version = 0
        )

        turnDraftRepository.save(
            TurnDraftEntity(
                gameId = draft.gameId,
                playerUserId = draft.playerUserId,
                version = draft.version,
                boardSets = mutableListOf(),
                rackTiles = draft.rackTiles.map { it.toEmbeddable() }.toMutableList()
            )
        )
    }

    data class SecurityScanFixtureState(
        val lobbyId: String,
        val hostUserId: String,
        val guestUserId: String,
        val gameId: String,
        val draftOwnerUserId: String
    )

    companion object {
        const val SCAN_OPEN_LOBBY_ID = "scan-open-lobby"
        const val SCAN_ACTIVE_LOBBY_ID = "scan-active-lobby"
        const val SCAN_GAME_ID = "scan-game-1"
        const val SCAN_HOST_USER_ID = "scan-host-user"
        const val SCAN_GUEST_USER_ID = "scan-guest-user"
        const val SCAN_DRAWN_TILE_ID = "scan-draw-red-1"
        val FIXTURE_TIME: Instant = Instant.parse("2026-01-01T00:00:00Z")

        fun preDrawRack() = listOf(
            NumberedTile(
                tileId = "scan-host-rack-red-5",
                color = TileColor.RED,
                number = 5
            ),
            NumberedTile(
                tileId = "scan-host-rack-blue-7",
                color = TileColor.BLUE,
                number = 7
            )
        )

        fun postDrawRack() = preDrawRack() + NumberedTile(
            tileId = SCAN_DRAWN_TILE_ID,
            color = TileColor.RED,
            number = 1
        )
    }
}
