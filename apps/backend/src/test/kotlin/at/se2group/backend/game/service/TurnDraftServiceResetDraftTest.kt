package at.se2group.backend.game.service

import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.persistence.TurnDraftEntity
import at.se2group.backend.service.AfterCommitExecutor
import at.se2group.backend.service.GameBroadcastService
import at.se2group.backend.service.TileConservationService
import at.se2group.backend.service.TurnDraftService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import shared.models.game.domain.*
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TurnDraftServiceResetDraftTest {

    @Mock
    lateinit var gameRepository: GameRepository

    @Mock
    lateinit var turnDraftRepository: TurnDraftRepository

    @Mock
    lateinit var gameBroadcastService: GameBroadcastService

    @Mock
    lateinit var tileConservationService: TileConservationService

    private lateinit var service: TurnDraftService

    private val player = GamePlayer(
        userId = "user-1",
        displayName = "Alice",
        turnOrder = 0,
        rackTiles = emptyList(),
        joinedAt = Instant.now()
    )

    private val confirmedGame = ConfirmedGame(
        gameId = "game-1",
        lobbyId = "lobby-1",
        players = listOf(player),
        boardSets = emptyList(),
        currentPlayerUserId = "user-1",
        status = GameStatus.ACTIVE
    )

    private val draftEntity = TurnDraftEntity(
        gameId = "game-1",
        playerUserId = "user-1",
        version = 3L
    )

    @BeforeEach
    fun setup() {
        service = TurnDraftService(
            gameRepository,
            turnDraftRepository,
            tileConservationService,
            gameBroadcastService,
            AfterCommitExecutor()
        )
    }

}
