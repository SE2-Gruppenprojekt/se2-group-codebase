package at.aau.serg.android.ui.screens.game

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.core.network.game.GameService
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateViewModel
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.game.domain.BoardSet
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.GameStatus
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor
import shared.models.game.response.GamePlayerResponse
import shared.models.game.response.GameResponse
import shared.models.game.response.TurnDraftResponse

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var service: GameService
    private lateinit var viewmodel: GameViewModel

    val fakeRack = listOf(
        NumberedTile("x1", TileColor.RED, 1),
        NumberedTile("x2", TileColor.RED, 2),
        NumberedTile("x3", TileColor.RED, 3),
    )

    val fakeBoard = listOf(
        BoardSet(
            "b1",
            BoardSetType.RUN,
            tiles = listOf(
                NumberedTile("x4", TileColor.BLUE, 1),
                NumberedTile("x5", TileColor.BLUE, 2),
                NumberedTile("x6", TileColor.BLUE, 3)
            )
        ),
        BoardSet(
            "b2",
            BoardSetType.GROUP,
            tiles = listOf(
                NumberedTile("x7", TileColor.BLUE, 10),
                NumberedTile("x8", TileColor.RED, 10),
                NumberedTile("x9", TileColor.BLACK, 10)
            )
        ),
    )

    private val fakeGameResponse: GameResponse = GameResponse(
        gameId = "FakeGame1",
        lobbyId = "FakeLobby1",
        players = listOf(
            GamePlayerResponse(
                userId = "FakeUser1",
                displayName = "Bob",
                turnOrder = 0,
                rackTiles = emptyList(),
                hasCompletedInitialMeld = false,
                score = 0,
                joinedAt = "2026-05-08T10:00:00Z"
            )
        ),
        board = emptyList(),
        drawPile = emptyList(),
        drawPileCount = 0,
        currentPlayerUserId = "FakeUser1",
        currentTurnPlayerId = "FakeUser1",
        turnDeadline = "2026-05-08T10:00:00Z",
        remainingTurnSeconds = 0,
        status = GameStatus.WAITING.toString(),
        createdAt = "2026-05-08T10:00:00Z",
        startedAt = "2026-05-08T10:00:00Z",
        finishedAt = "2026-05-08T10:00:00Z"
    )

    fun setTestGameState() {
        val initialUser = User.newBuilder()
            .setUid("User123")
            .setDisplayName("Alice")
            .setGameId("Game123")
            .build()

        viewmodel.setUiStateForTest(
            GameUiState(
                user = initialUser,
                rackTiles = fakeRack,
                boardSets = fakeBoard,
                gameState = ConfirmedGame(
                    players = listOf(
                        GamePlayer(
                            userId = initialUser.uid,
                            displayName = initialUser.displayName,
                            turnOrder = 0,
                            rackTiles = fakeRack,
                        )
                    ),
                    boardSets = fakeBoard,
                    gameId = initialUser.gameId,
                    lobbyId = "Lobby123",
                    currentPlayerUserId = initialUser.uid
                )
            )
        )
    }

    @Before
    fun setup() = runTest {
        store = InMemoryProtoStore(User.getDefaultInstance())
        service = mockk()

        coEvery { service.loadGame(any()) } returns fakeGameResponse
        coEvery { service.endTurn(any(), any()) } returns fakeGameResponse
        coEvery { service.drawTile(any(), any()) } returns fakeGameResponse

        viewmodel = GameViewModel(store, service)

        advanceUntilIdle()
    }

    @Test
    fun default_constructor_path_isCovered() = runTest {
        val vm = LobbyCreateViewModel(store)
        assertNotNull(vm)
    }

    @Test
    fun init_setsRackAndBoard() = runTest {
        val state = viewmodel.uiState.value

        assertEquals(viewmodel.uiState.value.gameState, fakeGameResponse.toDomain())

        assertTrue(state.selectedTiles.isEmpty())
        assertNull(state.activeSelectionRow)
    }

    @Test
    fun userStore_updatesUiState() = runTest {
        val user = User.newBuilder()
            .setUid("1")
            .setDisplayName("Max")
            .build()

        store.save(user)
        advanceUntilIdle()

        val state = viewmodel.uiState.value

        assertEquals("1", state.user?.uid)
        assertEquals("Max", state.user?.displayName)
    }

    @Test
    fun onTileSelected_togglesSelection() = runTest {
        setTestGameState()
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true)
        assertTrue(viewmodel.uiState.value.selectedTiles.contains(tile))

        viewmodel.onTileSelected(tile, false)
        assertFalse(viewmodel.uiState.value.selectedTiles.contains(tile))
    }

    @Test
    fun onTileSelected_resetsSelectionOnRowChange() = runTest {
        setTestGameState()
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true, "row1")
        viewmodel.onTileSelected(tile, true, "row2")

        val state = viewmodel.uiState.value

        assertTrue(state.selectedTiles.contains(tile))
        assertEquals("row2", state.activeSelectionRow)
    }

    @Test
    fun addRow_movesSelectedTilesToNewBoardSet() = runTest {
        setTestGameState()
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true)
        viewmodel.addRow()

        val state = viewmodel.uiState.value

        assertTrue(state.selectedTiles.isEmpty())
        assertTrue(state.boardSets.any { it.tiles.contains(tile) })
    }

    @Test
    fun moveTiles_toRack() = runTest {
        setTestGameState()
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true)
        viewmodel.moveTiles()

        val state = viewmodel.uiState.value

        assertTrue(state.rackTiles.contains(tile))
        assertTrue(state.selectedTiles.isEmpty())
    }

    @Test
    fun moveTiles_toBoardSet() = runTest {
        setTestGameState()
        val state = viewmodel.uiState.value
        val tile = state.rackTiles.first()

        val initialRow = state.boardSets.first()
        assertNotNull(initialRow)

        val initialRackCount = state.rackTiles.size

        viewmodel.onTileSelected(tile, true)
        viewmodel.moveTiles(initialRow.boardSetId)

        val updatedState = viewmodel.uiState.value
        val updatedRow = updatedState.boardSets.first { it.boardSetId == initialRow.boardSetId }

        assertNotEquals(initialRackCount, updatedState.rackTiles.size)

        assertTrue(updatedRow.tiles.contains(tile))
    }


    @Test
    fun moveTiles_emptySelection_doesNothing() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveTiles()

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_rack_movesCorrectly() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value.rackTiles.toList()

        viewmodel.moveInSameRow(null, 0, 1)

        val after = viewmodel.uiState.value.rackTiles

        assertEquals(before[0], after[1])
        assertEquals(before[1], after[0])
    }

    @Test
    fun moveInSameRow_board_movesCorrectly() = runTest {
        setTestGameState()
        val initialRow = viewmodel.uiState.value.boardSets.first()
        val before = initialRow.tiles
        val rowName = initialRow.boardSetId

        viewmodel.moveInSameRow(rowName, 0, 2)

        val after = viewmodel.uiState.value.boardSets
            .first { it.boardSetId == rowName }
            .tiles

        assertEquals(before[0], after[2])
    }

    @Test
    fun moveInSameRow_invalidRow_doesNothing() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveInSameRow("invalid", 0, 1)

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_invalidIndex_negative() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveInSameRow(null, -1, 1)

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_invalidIndex_outOfBounds() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveInSameRow(null, 0, 999)

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_sameIndex_doesNothing() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveInSameRow(null, 0, 0)

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun endTurn_updatesGameState_andClearsSelection() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value.gameState
        val tile = viewmodel.uiState.value.rackTiles.first()
        viewmodel.onTileSelected(tile, true)

        viewmodel.endTurn()
        advanceUntilIdle()

        val after = viewmodel.uiState.value.gameState
        assertNotEquals(before, after)

        val state = viewmodel.uiState.value
        assertTrue(state.selectedTiles.isEmpty())
        assertNull(state.activeSelectionRow)
    }

    @Test
    fun endTurn_emitsErrorState_onFailure() = runTest {
        coEvery { service.endTurn(any(), any()) } throws RuntimeException()

        viewmodel.endTurn()

        advanceUntilIdle()
        val state = viewmodel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }

    @Test
    fun drawTile_addsNew_updatesGameState() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value.gameState

        viewmodel.drawTile()
        advanceUntilIdle()

        val after = viewmodel.uiState.value.gameState
        assertNotEquals(before, after)
    }

    @Test
    fun drawTile_emitsErrorState_onFailure() = runTest {
        coEvery { service.drawTile(any(), any()) } throws RuntimeException("Failure")

        viewmodel.drawTile()

        advanceUntilIdle()
        val state = viewmodel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }

    @Test
    fun resetSelection_clearsSelection_andRestoresOriginalState() {
        setTestGameState()
        val state = viewmodel.uiState.value
        val tile = state.rackTiles.first()
        val initialRackCount = state.rackTiles.size

        val initialRow = state.boardSets.first()
        val initialRowTileCount = state.boardSets.first().tiles.size
        assertNotNull(initialRow)

        viewmodel.onTileSelected(tile, true)
        viewmodel.moveTiles(initialRow.boardSetId)

        var updatedState = viewmodel.uiState.value

        assertNotEquals(initialRackCount, updatedState.rackTiles.size)
        assertNotEquals(initialRowTileCount, updatedState.boardSets.first().tiles.size)
        viewmodel.resetSelection()

        updatedState = viewmodel.uiState.value
        assertEquals(initialRackCount, updatedState.rackTiles.size)
        assertEquals(initialRowTileCount, updatedState.boardSets.first().tiles.size)

        assertTrue(updatedState.selectedTiles.isEmpty())
        assertNull(updatedState.activeSelectionRow)
    }

    @Test
    fun loadGame_updatesGameState() = runTest {
        setTestGameState()
        val state = viewmodel.uiState.value

        viewmodel.loadGame()
        advanceUntilIdle()

        assertNotEquals(state.gameState, viewmodel.uiState.value.gameState)
    }

    @Test
    fun loadGame_emitsErrorState_onFailure() = runTest {
        coEvery { service.loadGame(any()) } throws RuntimeException()

        viewmodel.loadGame()

        advanceUntilIdle()
        val state = viewmodel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }

    @Test
    fun sendTurnDraft_updateDraftWorkflowTest() = runTest {
        coEvery { service.updateDraft(any(), any()) } returns TurnDraftResponse(
            gameId = "Game1",
            playerUserId = "Player1",
            draftBoard = emptyList(),
            draftHand = emptyList(),
            version = 0
        )

        val before = viewmodel.uiState.value.gameState
        viewmodel.sendTurnDraft()

        advanceUntilIdle()
        assertEquals(before, viewmodel.uiState.value.gameState)
        assertEquals(viewmodel.uiState.value.loadState, LoadState.Success)
    }

    @Test
    fun sendTurnDraft_emitsErrorState_onFailure() = runTest {
        coEvery { service.updateDraft(any(), any()) } throws RuntimeException()

        viewmodel.sendTurnDraft()

        advanceUntilIdle()
        val state = viewmodel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }
}
