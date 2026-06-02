package at.se2group.backend.game.service

import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.persistence.TurnDraftEntity
import at.se2group.backend.service.AfterCommitExecutor
import at.se2group.backend.service.GameBroadcastService
import at.se2group.backend.service.TileConservationService
import at.se2group.backend.service.TurnDraftService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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

    private val rackTile = NumberedTile("rack-1", TileColor.RED, 5)
    private val boardTile = NumberedTile("board-1", TileColor.BLUE, 7)


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
        boardSets = listOf(
            BoardSet(
                boardSetId = "set-1",
                tiles = listOf(boardTile)
            )
        ),
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
    private fun givenGameAndDraft() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(confirmedGame.toEntity()))
        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn((draftEntity))
        whenever(turnDraftRepository.save(any()))
                .thenAnswer { it.arguments[0] }
    }

    @Test
    fun `resets board from confirmed game`() {
        givenGameAndDraft()
        val result = service.resetDraft("game-1", "user-1")
        assertEquals(confirmedGame.boardSets, result.boardSets)
    }

    @Test
    fun `resets rack from confirmed player rack`() {
        givenGameAndDraft()
        val result = service.resetDraft("game-1", "user-1")
        assertEquals(player.rackTiles, result.rackTiles)
    }

    @Test
    fun `increments draft version`() {
        givenGameAndDraft()
        val result = service.resetDraft("game-1", "user-1")
        assertEquals(4, result.version)
    }

    @Test
    fun `clears drawnTile`() {
        givenGameAndDraft()
        val result = service.resetDraft("game-1", "user-1")
        assertNull(result.drawnTile)
    }

    @Test
    fun `persists the reset draft`() {
        givenGameAndDraft()
        service.resetDraft("game-1", "user-1")
        verify(turnDraftRepository).save(any())
    }

    @Test
    fun `rejects when game does not exist`() {
        whenever(gameRepository.findById("missing")).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> { service.resetDraft("missing", "user-1") }
    }

    @Test
    fun `rejects when draft does not exist`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(confirmedGame.toEntity()))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(null)
        assertThrows<NoSuchElementException> { service.resetDraft("game-1", "user-1") }
    }

    @Test
    fun `rejects when user is not the current active player`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(confirmedGame.toEntity()))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(draftEntity)


        val exception = assertThrows<IllegalStateException> { service.resetDraft("game-1", "user-2") }
        assertEquals("User is not the current active player", exception.message)
    }

    @Test
    fun `rejects when draft belongs to a different user`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(confirmedGame.toEntity()))
        val otherDraft = TurnDraftEntity(gameId = "game-1", playerUserId = "user-2", version = 1)
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(otherDraft)

        val exception = assertThrows<IllegalStateException> { service.resetDraft("game-1", "user-1") }
        assertEquals("Draft belongs to a different user", exception.message)
    }

}
