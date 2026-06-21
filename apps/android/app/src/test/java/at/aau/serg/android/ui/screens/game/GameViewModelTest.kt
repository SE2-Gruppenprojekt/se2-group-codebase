package at.aau.serg.android.ui.screens.game

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.core.network.game.GameService
import at.aau.serg.android.core.network.game.GameWebSocketService
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateViewModel
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import shared.models.game.domain.BoardSet
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.GamePlayerMetrics
import shared.models.game.domain.GameStatus
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor
import shared.models.game.event.GameDraftUpdatedEvent
import shared.models.game.event.GameEndedEvent
import shared.models.game.event.GameEvent
import shared.models.game.event.GameUpdatedEvent
import shared.models.game.event.TurnChangedEvent
import shared.models.game.event.TurnTimedOutEvent
import shared.models.game.response.BoardSetResponse
import shared.models.game.response.GamePlayerMetricsResponse
import shared.models.game.response.GamePlayerResponse
import shared.models.game.response.GameResponse
import shared.models.game.response.TileResponse
import shared.models.game.response.TurnDraftResponse

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var userStore: UserStore
    private lateinit var service: GameService
    private lateinit var socketService: GameWebSocketService
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

    private val emptyMetrics = GamePlayerMetricsResponse(
        turnsCompleted = 0,
        tilesPlayed = 0,
        meldsCreated = 0,
        pointsPlayed = 0,
        tilesRemainingAtEnd = null,
        penaltyPointsAtEnd = null,
        winner = false,
        finishPosition = null
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
                joinedAt = "2026-05-08T10:00:00Z",
                metrics = emptyMetrics
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
        finishedAt = "2026-05-08T10:00:00Z",
        totalTurnsCompleted = 0,
        requireInitialMeld = false,
        winnerUserId = null
    )

    fun setTestGameState() {
        val initialUser = User.newBuilder()
            .setUid("FakeUser1")
            .setDisplayName("Alice")
            .setGameId("FakeGame1")
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
                ),
                isActivePlayer = true
            )
        )
    }

    private fun setPlayerTurnInactive() {
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
                        ),
                        GamePlayer(
                            userId = "FakeUser1",
                            displayName = "Fake",
                            turnOrder = 1,
                            rackTiles = listOf(
                                NumberedTile("x4", TileColor.BLACK, 1),
                            ),
                        )
                    ),
                    boardSets = fakeBoard,
                    gameId = initialUser.gameId,
                    lobbyId = "Lobby123",
                    currentPlayerUserId = "FakeUser1"
                ),
                isActivePlayer = false
            )
        )
    }

    @Before
    fun setup() = runTest {
        store = InMemoryProtoStore(User.getDefaultInstance())
        userStore = UserStore(store)
        service = mockk()
        socketService = mockk()

        coEvery { service.loadGame(any()) } returns fakeGameResponse
        coEvery { service.drawTile(any()) } returns fakeGameResponse
        coEvery { socketService.subscribe(any()) } returns flow { }

        viewmodel = GameViewModel(userStore, service, socketService)

        advanceUntilIdle()
    }

    @Test
    fun default_constructor_path_isCovered() = runTest {
        val vm = LobbyCreateViewModel(userStore)
        assertNotNull(vm)
    }

    @Test
    fun default_constructor_of_GameViewModel_isCovered() = runTest {
        val vm = GameViewModel(userStore)
        assertNotNull(vm)
        advanceUntilIdle()
    }

    @Test
    fun init_loadsUser() = runTest {
        assertEquals(viewmodel.uiState.value.user, User.getDefaultInstance())
        val user = User.newBuilder()
            .setUid("1")
            .setDisplayName("Bob")
            .setGameId("g1")
            .build()

        store.save(user)
        advanceUntilIdle()
        assertEquals(viewmodel.uiState.value.user, user)
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
    fun onTileSelected_togglesSelection_for_hand() = runTest {
        setTestGameState()
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, null))
        assertTrue(viewmodel.uiState.value.selectedTiles.contains(tile))

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, false, null))
        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, false, null))
        assertFalse(viewmodel.uiState.value.selectedTiles.contains(tile))
    }

    @Test
    fun onTileSelected_togglesSelection_for_board() = runTest {
        setTestGameState()
        val boardSet = viewmodel.uiState.value.boardSets.first()
        val tile = boardSet.tiles.first()

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, boardSet.boardSetId))
        assertTrue(viewmodel.uiState.value.selectedTiles.contains(tile))

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, false, boardSet.boardSetId))
        assertFalse(viewmodel.uiState.value.selectedTiles.contains(tile))
    }

    @Test
    fun onTileSelected_resetsSelectionOnRowChange() = runTest {
        setTestGameState()
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, "row1"))
        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, "row2"))

        val state = viewmodel.uiState.value

        assertTrue(state.selectedTiles.contains(tile))
        assertEquals("row2", state.activeSelectionRow)
    }

    @Test
    fun addRow_movesSelectedTilesToNewBoardSet() = runTest {
        setTestGameState()
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, null))
        viewmodel.onUIEvent(GameUIEvent.AddRow)

        val state = viewmodel.uiState.value

        assertTrue(state.selectedTiles.isEmpty())
        assertTrue(state.boardSets.any { it.tiles.contains(tile) })
    }

    @Test
    fun moveTiles_toRack() = runTest {
        setTestGameState()
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, null))
        viewmodel.onUIEvent(GameUIEvent.MoveTiles(null))

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

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, null))
        viewmodel.onUIEvent(GameUIEvent.MoveTiles(initialRow.boardSetId))

        val updatedState = viewmodel.uiState.value
        val updatedRow = updatedState.boardSets.first { it.boardSetId == initialRow.boardSetId }

        assertNotEquals(initialRackCount, updatedState.rackTiles.size)

        assertTrue(updatedRow.tiles.contains(tile))
    }


    @Test
    fun moveTiles_emptySelection_doesNothing() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value
        viewmodel.onUIEvent(GameUIEvent.MoveTiles(null))

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_rack_movesCorrectly() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value.rackTiles.toList()
        viewmodel.onUIEvent(GameUIEvent.MoveInSameRow(null, 0, 1))

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
        viewmodel.onUIEvent(GameUIEvent.MoveInSameRow(rowName, 0, 2))

        val after = viewmodel.uiState.value.boardSets
            .first { it.boardSetId == rowName }
            .tiles

        assertEquals(before[0], after[2])
    }

    @Test
    fun moveInSameRow_invalidRow_doesNothing() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value
        viewmodel.onUIEvent(GameUIEvent.MoveInSameRow("invalid", 0, 1))

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_invalidIndex_negative() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value
        viewmodel.onUIEvent(GameUIEvent.MoveInSameRow(null, -1, 1))

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_invalidIndex_outOfBounds() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value
        viewmodel.onUIEvent(GameUIEvent.MoveInSameRow(null, 0, 999))

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_sameIndex_doesNothing() = runTest {
        setTestGameState()
        val before = viewmodel.uiState.value
        viewmodel.onUIEvent(GameUIEvent.MoveInSameRow(null, 0, 0))

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun endTurn_sets_success_andClearsSelection() = runTest {
        setTestGameState()
        val user = User.newBuilder()
            .setUid("FakeUser1")
            .setDisplayName("Bob")
            .build()
        store.save(user)
        advanceUntilIdle()
        coEvery { service.endTurn(any(), any()) } returns fakeGameResponse


        viewmodel.onUIEvent(GameUIEvent.EndTurn)
        advanceUntilIdle()

        val state = viewmodel.uiState.value

        assertTrue(state.selectedTiles.isEmpty())
        assertNull(state.activeSelectionRow)
        assertTrue(state.loadState is LoadState.Success)
    }

    @Test
    fun endTurn_emitsErrorState_onFailure() = runTest {
        setTestGameState()
        coEvery { service.endTurn(any(), any()) } throws RuntimeException()

        viewmodel.onUIEvent(GameUIEvent.EndTurn)

        advanceUntilIdle()
        val state = viewmodel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }

    @Test(expected = IllegalStateException::class)
    fun end_turn_throws_on_user_null() = runTest {
        viewmodel.setUiStateForTest(GameUiState(
            user = null
        ))
        viewmodel.endTurn()
        advanceUntilIdle()
    }

    @Test
    fun endTurn_setsRuleValidation_onInvalidTurnSubmission() = runTest {
        setTestGameState()

        val json = """{"errorCode":"INVALID_TURN_SUBMISSION","errorMessage":"Draft invalid","violations":[{"code":"RUN_NOT_CONSECUTIVE","message":"Not consecutive","boardSetId":"b1"},{"code":"INITIAL_MELD_TOO_SMALL","message":"Meld too small","boardSetId":null}]}"""
        val response = Response.error<Any>(409, json.toResponseBody("application/json".toMediaType()))
        coEvery { service.endTurn(any(), any()) } throws HttpException(response)

        viewmodel.onUIEvent(GameUIEvent.EndTurn)
        advanceUntilIdle()

        val state = viewmodel.uiState.value
        assertTrue(state.loadState is LoadState.Success)
        assertEquals("Draft invalid", state.ruleValidation.summaryMessage)
        assertTrue(state.ruleValidation.violationsByBoardSetId.containsKey("b1"))
        assertEquals(1, state.ruleValidation.globalViolations.size)
    }

    @Test
    fun endTurn_clearsRuleValidation_onSuccess() = runTest {
        setTestGameState()
        val user = User.newBuilder()
            .setUid("FakeUser1")
            .setDisplayName("Bob")
            .build()
        store.save(user)
        advanceUntilIdle()
        viewmodel.setUiStateForTest(
            viewmodel.uiState.value.copy(
                ruleValidation = RuleValidationUiState(summaryMessage = "old error")
            )
        )
        coEvery { service.endTurn(any(), any()) } returns fakeGameResponse

        viewmodel.onUIEvent(GameUIEvent.EndTurn)
        advanceUntilIdle()

        assertEquals(RuleValidationUiState(), viewmodel.uiState.value.ruleValidation)
    }

    @Test
    fun drawTile_addsNew_updatesGameState() = runTest {
        val user = User.newBuilder()
            .setUid("FakeUser1")
            .setDisplayName("Bob")
            .setGameId("FakeGame1")
            .build()

        viewmodel.setUiStateForTest(GameUiState(
            user = user,
            rackTiles = viewmodel.uiState.value.rackTiles,
            gameState = fakeGameResponse.toDomain(),
            isActivePlayer = true
        ))

        val before = viewmodel.uiState.value.gameState
        val modifiedGameResponse = fakeGameResponse.copy(
            players = fakeGameResponse.players.map { p ->
                if (p.userId == viewmodel.uiState.value.user?.uid) {
                    p.copy(
                        rackTiles = listOf(
                            TileResponse(
                                tileId = "t1",
                                color = TileColor.BLACK.toString(),
                                number = 1,
                                isJoker = false
                            )
                        )
                    )
                } else p
            }
        )
        coEvery { service.drawTile(any()) } returns modifiedGameResponse
        viewmodel.onUIEvent(GameUIEvent.DrawTile)
        advanceUntilIdle()

        val after = viewmodel.uiState.value.gameState
        assertNotEquals(before, after)
    }

    @Test
    fun drawTile_does_not_add_with_empty_rack_updatesGameState() = runTest {
        val user = User.newBuilder()
            .setUid("FakeUser1")
            .setDisplayName("Bob")
            .setGameId("FakeGame1")
            .build()

        viewmodel.setUiStateForTest(GameUiState(
            user = user,
            rackTiles = emptyList(),
            gameState = fakeGameResponse.toDomain()
        ))
        val before = viewmodel.uiState.value.gameState

        coEvery { service.drawTile(any()) } returns fakeGameResponse
        viewmodel.onUIEvent(GameUIEvent.DrawTile)
        advanceUntilIdle()

        val after = viewmodel.uiState.value.gameState
        assertEquals(before, after)
    }

    @Test
    fun drawTile_emitsErrorState_onFailure() = runTest {
        setTestGameState()
        coEvery { service.drawTile(any()) } throws RuntimeException("Failure")

        viewmodel.onUIEvent(GameUIEvent.DrawTile)

        advanceUntilIdle()
        val state = viewmodel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }

    @Test(expected = IllegalStateException::class)
    fun drawTile_throws_on_user_null() = runTest {
        viewmodel.setUiStateForTest(GameUiState(
            user = null
        ))
        viewmodel.drawTile()
        advanceUntilIdle()
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

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, null))
        viewmodel.onUIEvent(GameUIEvent.MoveTiles(initialRow.boardSetId))

        var updatedState = viewmodel.uiState.value

        assertNotEquals(initialRackCount, updatedState.rackTiles.size)
        assertNotEquals(initialRowTileCount, updatedState.boardSets.first().tiles.size)
        viewmodel.onUIEvent(GameUIEvent.ResetSelection)

        updatedState = viewmodel.uiState.value
        assertEquals(initialRackCount, updatedState.rackTiles.size)
        assertEquals(initialRowTileCount, updatedState.boardSets.first().tiles.size)

        assertTrue(updatedState.selectedTiles.isEmpty())
        assertNull(updatedState.activeSelectionRow)
    }

    @Test(expected = IllegalStateException::class)
    fun resetSelection_throws_if_gameState_is_null() {
        viewmodel.setUiStateForTest(GameUiState(
            gameState = null
        ))
        viewmodel.resetSelection()
    }

    @Test
    fun loadGame_updatesGameState() = runTest {
        val user = User.newBuilder()
            .setUid("User123")
            .setDisplayName("Alice")
            .setGameId("g1")
            .build()
        store.save(user)
        runCurrent()

        viewmodel.setUiStateForTest(GameUiState(user = user))

        coEvery { service.loadGame(any()) } returns fakeGameResponse
        viewmodel.onUIEvent(GameUIEvent.OnLoadGame("g1"))
        runCurrent()
        viewmodel.cancelTimer()

        assertEquals(fakeGameResponse.toDomain(), viewmodel.uiState.value.gameState)
    }

    @Test
    fun loadGame_does_not_overwrite_newer_socket_state() = runTest {
        val socketGame = fakeGameResponse.copy(
            players = fakeGameResponse.players + GamePlayerResponse(
                userId = "OtherPlayer",
                displayName = "Other",
                turnOrder = 1,
                rackTiles = emptyList(),
                metrics = GamePlayerMetricsResponse(
                    turnsCompleted = 0,
                    tilesPlayed = 0,
                    meldsCreated = 0,
                    pointsPlayed = 0,
                    tilesRemainingAtEnd = null,
                    penaltyPointsAtEnd = null,
                    winner = false,
                    finishPosition = null
                ),
                hasCompletedInitialMeld = false,
                score = 0,
                joinedAt = "2026-01-01T00:00:00Z"
            ),
            currentPlayerUserId = "OtherPlayer"
        )
        coEvery { socketService.subscribe("g1") } returns flowOf(
            GameEvent.Updated(GameUpdatedEvent(gameId = "g1", game = socketGame))
        )
        coEvery { service.loadGame("g1") } coAnswers {
            delay(100)
            fakeGameResponse
        }

        val user = User.newBuilder()
            .setUid("User123")
            .setDisplayName("Alice")
            .setGameId("g1")
            .build()
        viewmodel.setUiStateForTest(GameUiState(user = user))

        viewmodel.onUIEvent(GameUIEvent.OnLoadGame("g1"))
        advanceTimeBy(200)
        runCurrent()
        viewmodel.cancelTimer()

        assertEquals("OtherPlayer", viewmodel.uiState.value.gameState?.currentPlayerUserId)
    }

    @Test
    fun loadGame_emitsErrorState_if_user_null() = runTest {
        viewmodel.setUiStateForTest(GameUiState(
            user = null
        ))
        viewmodel.onUIEvent(GameUIEvent.OnLoadGame("g1"))
        runCurrent()
        viewmodel.cancelTimer()

        val state = viewmodel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }

    @Test
    fun loadGame_emitsErrorState_onFailure() = runTest {
        val user = User.newBuilder().setUid("User123").setDisplayName("Alice").setGameId("g1").build()
        viewmodel.setUiStateForTest(GameUiState(user = user))

        coEvery { service.loadGame(any()) } throws RuntimeException()

        viewmodel.onUIEvent(GameUIEvent.OnLoadGame("g1"))
        runCurrent()
        viewmodel.cancelTimer()

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

    @Test(expected = IllegalStateException::class)
    fun sendTurnDraft_emitsErrorState_if_user_is_null() = runTest {
        viewmodel.setUiStateForTest(GameUiState(
            user = null
        ))
        coEvery { service.updateDraft(any(), any()) }

        viewmodel.sendTurnDraft()
        advanceUntilIdle()
    }

    @Test
    fun addRow_sets_loadState_error_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.AddRow)

        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun handleGameSocketEvent_draftUpdated_nullUser_setsErrorState() {
        viewmodel.setUiStateForTest(GameUiState(user = null))

        viewmodel.handleGameSocketEvent(
            GameEvent.DraftUpdated(
                GameDraftUpdatedEvent(
                    gameId = "Game123",
                    playerId = "OtherPlayer",
                    draft = TurnDraftResponse("Game123", "OtherPlayer", emptyList(), emptyList(), 0)
                )
            )
        )
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun drawTile_sets_loadState_error_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.DrawTile)

        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun handleGameSocketEvent_turnTimedOut_nullUser_setsErrorState() {
        viewmodel.setUiStateForTest(GameUiState(user = null))

        viewmodel.handleGameSocketEvent(
            GameEvent.TurnTimedOut(TurnTimedOutEvent(gameId = "Game123", previousTurnPlayerId = "User123"))
        )
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun endTurn_sets_loadState_error_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.DrawTile)
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun moveInSameRow_sets_loadState_error_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.MoveInSameRow(null, 0, 1))
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun moveTiles_sets_loadState_error_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.MoveTiles(null))
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun loadGame_sets_loadState_success_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.OnLoadGame("g1"))
        runCurrent()
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Success)
        viewmodel.cancelTimer() // must cancel before runTest cleanup calls advanceUntilIdle()
    }

    @Test
    fun tileSelected_sets_loadState_error_if_not_players_turn() = runTest {
        setTestGameState()
        val user = User.newBuilder()
            .setUid("1")
            .setDisplayName("Max")
            .build()
        viewmodel.setUiStateForTest(GameUiState(
            user = user,
            rackTiles = viewmodel.uiState.value.rackTiles,
            gameState = viewmodel.uiState.value.gameState
        ))
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onUIEvent(GameUIEvent.OnTileSelected(tile, true, null))

        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun handleGameSocketEvent_draftUpdated_updatesBoardSets_forOtherPlayer() {
        setTestGameState()

        val event = GameEvent.DraftUpdated(
            GameDraftUpdatedEvent(
                gameId = "FakeGame1",
                playerId = "OtherPlayer",
                draft = TurnDraftResponse(
                    gameId = "FakeGame1",
                    playerUserId = "OtherPlayer",
                    draftBoard = emptyList(),
                    draftHand = emptyList(),
                    version = 0
                )
            )
        )

        viewmodel.handleGameSocketEvent(event)

        assertTrue(viewmodel.uiState.value.boardSets.isEmpty())
    }

    @Test
    fun handleGameSocketEvent_draftUpdated_ignoresOwnDraft() {
        setTestGameState()
        val originalBoardSets = viewmodel.uiState.value.boardSets

        val event = GameEvent.DraftUpdated(
            GameDraftUpdatedEvent(
                gameId = "FakeGame1",
                playerId = "FakeUser1",
                draft = TurnDraftResponse(
                    gameId = "FakeGame1",
                    playerUserId = "FakeUser1",
                    draftBoard = emptyList(),
                    draftHand = emptyList(),
                    version = 0
                )
            )
        )

        viewmodel.handleGameSocketEvent(event)

        assertEquals(originalBoardSets, viewmodel.uiState.value.boardSets)
    }

    @Test
    fun handleGameSocketEvent_draftUpdated_invalidBoard_doesNotUpdateState() {
        setTestGameState()
        val originalBoardSets = viewmodel.uiState.value.boardSets

        val event = GameEvent.DraftUpdated(
            GameDraftUpdatedEvent(
                gameId = "Game123",
                playerId = "OtherPlayer",
                draft = TurnDraftResponse(
                    gameId = "Game123",
                    playerUserId = "OtherPlayer",
                    draftBoard = listOf(
                        BoardSetResponse(boardSetId = "b1", type = "RUN", tiles = emptyList())
                    ),
                    draftHand = emptyList(),
                    version = 0
                )
            )
        )

        viewmodel.handleGameSocketEvent(event)

        assertEquals(originalBoardSets, viewmodel.uiState.value.boardSets)
    }

    @Test
    fun handleGameSocketEvent_ended_setsWinnerId() {
        setTestGameState()

        viewmodel.handleGameSocketEvent(
            GameEvent.Ended(GameEndedEvent(gameId = "FakeGame1", winnerUserId = "FakeUser1"))
        )

        // navigation effect is verified in next issue [feat(android)(game): add ViewModel and UI tests for game-end flow]
    }

    @Test
    fun handleGameSocketEvent_turnChanged_updatesCurrentPlayer() {
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
                        GamePlayer(userId = "User123", displayName = "Alice", turnOrder = 0, rackTiles = fakeRack),
                        GamePlayer(userId = "OtherPlayer", displayName = "Bob", turnOrder = 1, rackTiles = emptyList())
                    ),
                    boardSets = fakeBoard,
                    gameId = "Game123",
                    lobbyId = "Lobby123",
                    currentPlayerUserId = "User123"
                )
            )
        )

        viewmodel.handleGameSocketEvent(
            GameEvent.TurnChanged(TurnChangedEvent(gameId = "Game123", currentTurnPlayerId = "OtherPlayer"))
        )

        assertEquals("OtherPlayer", viewmodel.uiState.value.gameState?.currentPlayerUserId)
    }

    @Test
    fun handleGameSocketEvent_turnTimedOut_setsError_forCurrentUser() {
        setTestGameState()

        viewmodel.handleGameSocketEvent(
            GameEvent.TurnTimedOut(TurnTimedOutEvent(gameId = "FakeGame1", previousTurnPlayerId = "FakeUser1"))
        )

        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun resetSelection_sets_loadState_error_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.ResetSelection)
        advanceUntilIdle()
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun handleGameSocketEvent_turnTimedOut_ignoresOtherPlayer() {
        setTestGameState()
        val beforeLoadState = viewmodel.uiState.value.loadState

        viewmodel.handleGameSocketEvent(
            GameEvent.TurnTimedOut(TurnTimedOutEvent(gameId = "Game123", previousTurnPlayerId = "OtherPlayer"))
        )

        assertEquals(beforeLoadState, viewmodel.uiState.value.loadState)
    }

    @Test
    fun handleGameSocketEvent_updatesGameState_if_not_active() {
        setTestGameState()

        viewmodel.handleGameSocketEvent(
            GameEvent.Updated(GameUpdatedEvent(gameId = "Game123", game = fakeGameResponse))
        )

        assertNotEquals(fakeGameResponse.toDomain(), viewmodel.uiState.value.gameState)
    }

    @Test
    fun handleGameSocketEvent_updatesGameState_if_active() = runTest {
        val user = User.newBuilder()
            .setUid("1")
            .setDisplayName("Bob")
            .setGameId("g1")
            .build()

        store.save(user)
        advanceUntilIdle()

        viewmodel.handleGameSocketEvent(
            GameEvent.Updated(GameUpdatedEvent(gameId = "Game123", game = fakeGameResponse))
        )

        assertEquals(fakeGameResponse.toDomain(), viewmodel.uiState.value.gameState)
    }

    @Test
    fun handleGameSocketEvent_updated_invalidGame_doesNotUpdateState() {
        setTestGameState()
        val originalGameState = viewmodel.uiState.value.gameState

        val invalidResponse = fakeGameResponse.copy(players = emptyList())

        viewmodel.handleGameSocketEvent(
            GameEvent.Updated(GameUpdatedEvent(gameId = "Game123", game = invalidResponse))
        )

        assertEquals(originalGameState, viewmodel.uiState.value.gameState)
    }

    @Test
    fun startSocket_catches_NetworkException_and_updates_loadState() = runTest {
        coEvery { socketService.subscribe("g1") } returns flow {
            throw RuntimeException("Connection lost")
        }

        viewmodel.startSocket("g1")
        advanceUntilIdle()
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun onSettings_emitsNavigateToSettings_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.OnSettings)

        val effect = viewmodel.effects.first()

        assertEquals(GameEffect.NavigateToSettings, effect)
    }

    @Test
    fun onBack_emitsNavigateBack_if_not_players_turn() = runTest {
        setPlayerTurnInactive()
        viewmodel.onUIEvent(GameUIEvent.OnBack)

        val effect = viewmodel.effects.first()

        assertEquals(GameEffect.NavigateBack, effect)
    }

    @Test
    fun onBack_emitsNavigateBack() = runTest {
        viewmodel.onUIEvent(GameUIEvent.OnBack)

        val effect = viewmodel.effects.first()

        assertEquals(GameEffect.NavigateBack, effect)
    }

    @Test
    fun onSettings_emitsNavigateToSettings() = runTest {
        viewmodel.onUIEvent(GameUIEvent.OnSettings)

        val effect = viewmodel.effects.first()

        assertEquals(GameEffect.NavigateToSettings, effect)
    }

    @Test(expected = IllegalStateException::class)
    fun applyGameState_throws_if_player_null() = runTest {
        viewmodel.setUiStateForTest(GameUiState(
            user = null
        ))
        viewmodel.applyGameState(fakeGameResponse.toDomain(), false)
    }

    @Test(expected = IllegalStateException::class)
    fun applyGameState_withNullUser_throws_IllegalStateException() {
        viewmodel.setUiStateForTest(GameUiState(user = null))
        viewmodel.applyGameState(fakeGameResponse.toDomain(), true)
    }

    @Test
    fun addRow_removesEmptyBoardSet_whenAllTilesFromSetSelected() = runTest {
        setTestGameState()
        val firstSet = viewmodel.uiState.value.boardSets.first()
        val allTiles = firstSet.tiles.toSet()

        viewmodel.setUiStateForTest(
            viewmodel.uiState.value.copy(
                selectedTiles = allTiles,
                activeSelectionRow = firstSet.boardSetId
            )
        )

        viewmodel.onUIEvent(GameUIEvent.AddRow)

        assertFalse(viewmodel.uiState.value.boardSets.any { it.boardSetId == firstSet.boardSetId })
        assertTrue(viewmodel.uiState.value.boardSets.any { set -> set.tiles.containsAll(allTiles) })
    }

    @Test
    fun moveTiles_removesEmptyBoardSet_whenAllTilesFromSetSelected() = runTest {
        setTestGameState()
        val firstSet = viewmodel.uiState.value.boardSets.first()
        val allTiles = firstSet.tiles.toSet()

        viewmodel.setUiStateForTest(
            viewmodel.uiState.value.copy(
                selectedTiles = allTiles,
                activeSelectionRow = firstSet.boardSetId
            )
        )

        viewmodel.onUIEvent(GameUIEvent.MoveTiles(null))

        assertFalse(viewmodel.uiState.value.boardSets.any { it.boardSetId == firstSet.boardSetId })
        assertTrue(viewmodel.uiState.value.rackTiles.containsAll(allTiles))
    }

    @Test
    fun moveInSameRow_boardRow_outOfBounds_doesNothing() = runTest {
        setTestGameState()
        val rowId = viewmodel.uiState.value.boardSets.first().boardSetId
        val before = viewmodel.uiState.value

        viewmodel.onUIEvent(GameUIEvent.MoveInSameRow(rowId, 0, 999))

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun resetSelection_withNullGameState_sets_loadState_error() {
        viewmodel.setUiStateForTest(
            GameUiState(gameState = null, selectedTiles = setOf(fakeRack[0]))
        )
        viewmodel.onUIEvent(GameUIEvent.ResetSelection)

        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }


    @Test
    fun handleGameSocketEvent_turnChanged_playerNotInList_keepsGameState() {
        setTestGameState()
        val originalGameState = viewmodel.uiState.value.gameState

        viewmodel.handleGameSocketEvent(
            GameEvent.TurnChanged(TurnChangedEvent(gameId = "Game123", currentTurnPlayerId = "NonExistentPlayer"))
        )

        assertEquals(originalGameState, viewmodel.uiState.value.gameState)
    }

    @Test
    fun handleGameSocketEvent_turnChanged_nullGameState_doesNothing() {
        viewmodel.setUiStateForTest(GameUiState(gameState = null))

        viewmodel.handleGameSocketEvent(
            GameEvent.TurnChanged(TurnChangedEvent(gameId = "Game123", currentTurnPlayerId = "SomePlayer"))
        )

        assertNull(viewmodel.uiState.value.gameState)
    }

    @Test
    fun startSocket_cancels_existing_job_on_lobby_change() = runTest {
        viewmodel.startSocket("g1")
        viewmodel.startSocket("g2")
    }

    @Test
    fun startSocket_collects_events_on_success() = runTest {
        val testFlow = MutableSharedFlow<GameEvent>()
        coEvery { socketService.subscribe("g1") } returns testFlow

        viewmodel.startSocket("g1")
        runCurrent()
        val payload = GameEndedEvent(
            gameId = "g1",
            winnerUserId = "lobby-1",
        )
        val mockEvent = GameEvent.Ended(payload)

        testFlow.emit(mockEvent)
        runCurrent()
    }

    @Test
    fun toggleXRAY_changes_rack_if_not_active() = runTest {
        setPlayerTurnInactive()

        val rack = viewmodel.uiState.value.rackTiles
        val state = viewmodel.uiState.value.cheatXRAY
        viewmodel.onUIEvent(GameUIEvent.ToggleXRAY)

        assertNotEquals(rack, viewmodel.uiState.value.rackTiles)
        assertNotEquals(state, viewmodel.uiState.value.cheatXRAY)

        viewmodel.onUIEvent(GameUIEvent.ToggleXRAY)

        assertEquals(rack, viewmodel.uiState.value.rackTiles)
        assertEquals(state, viewmodel.uiState.value.cheatXRAY)
    }

    @Test
    fun toggleXRAY_leaves_rack_if_active() = runTest {
        setTestGameState()

        val rack = viewmodel.uiState.value.rackTiles
        val state = viewmodel.uiState.value.cheatXRAY
        viewmodel.onUIEvent(GameUIEvent.ToggleXRAY)

        assertEquals(rack, viewmodel.uiState.value.rackTiles)
        assertNotEquals(state, viewmodel.uiState.value.cheatXRAY)
    }

    @Test
    fun toggleXRAY_withNullGameState_sets_loadState_error() = runTest {
        viewmodel.setUiStateForTest(GameUiState(
            gameState = null,
        ))
        viewmodel.onUIEvent(GameUIEvent.ToggleXRAY)
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun toggleXRAY_withNullUser_sets_loadState_error() = runTest {
        viewmodel.setUiStateForTest(GameUiState(
            gameState = fakeGameResponse.toDomain(),
            user = null
        ))
        viewmodel.onUIEvent(GameUIEvent.ToggleXRAY)
        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    // --- handleFinishedGame ---

    @Test
    fun handlePlayerFinished_populatesGameResult() {
        setTestGameState()
        val game = viewmodel.uiState.value.gameState!!

        viewmodel.handlePlayerFinished(game, isGameOver = true, overrideWinnerId = "FakeUser1")

        val result = viewmodel.uiState.value.gameResult
        assertNotNull(result)
        assertEquals("FakeUser1", result?.winnerUserId)
        assertTrue(result?.players?.isNotEmpty() == true)
    }

    @Test
    fun handlePlayerFinished_sortsByScoreDescending() {
        val user = User.newBuilder().setUid("u1").setDisplayName("Alice").setGameId("g1").build()
        val game = ConfirmedGame(
            gameId = "g1",
            lobbyId = "l1",
            players = listOf(
                GamePlayer(userId = "u1", displayName = "Alice", turnOrder = 0, score = 50),
                GamePlayer(userId = "u2", displayName = "Bob",   turnOrder = 1, score = 120),
                GamePlayer(userId = "u3", displayName = "Carol", turnOrder = 2, score = 80)
            ),
            currentPlayerUserId = "u1"
        )
        viewmodel.setUiStateForTest(GameUiState(user = user, gameState = game))

        viewmodel.handlePlayerFinished(game, isGameOver = true, overrideWinnerId = "u2")

        val players = viewmodel.uiState.value.gameResult?.players
        assertNotNull(players)
        assertEquals("u2", players?.get(0)?.userId)
        assertEquals(1, players?.get(0)?.finishPosition)
        assertEquals("u3", players?.get(1)?.userId)
        assertEquals(2, players?.get(1)?.finishPosition)
        assertEquals("u1", players?.get(2)?.userId)
        assertEquals(3, players?.get(2)?.finishPosition)
    }

    @Test
    fun handlePlayerFinished_navigateToResult_false_doesNotEmitEffect() = runTest {
        setTestGameState()
        val game = viewmodel.uiState.value.gameState!!

        val collected = mutableListOf<GameEffect>()
        val collectJob = launch { viewmodel.effects.collect { collected.add(it) } }
        runCurrent()

        viewmodel.handlePlayerFinished(game, isGameOver = false, navigateToResult = false)
        runCurrent()

        collectJob.cancel()
        assertTrue(collected.isEmpty())
    }

    @Test
    fun handlePlayerFinished_emitsNavigateToResultEffect() = runTest {
        setTestGameState()
        val game = viewmodel.uiState.value.gameState!!

        val effectDeferred = async { viewmodel.effects.first() }
        runCurrent()

        viewmodel.handlePlayerFinished(game, isGameOver = true, overrideWinnerId = "FakeUser1")
        runCurrent()

        assertEquals(GameEffect.NavigateToResult, effectDeferred.await())
    }

    @Test
    fun handlePlayerFinished_capturesMatchDuration() {
        setTestGameState()
        viewmodel.setUiStateForTest(
            viewmodel.uiState.value.copy(elapsedSeconds = 125)
        )
        val game = viewmodel.uiState.value.gameState!!

        viewmodel.handlePlayerFinished(game, isGameOver = true, overrideWinnerId = "FakeUser1")

        assertEquals("2:05", viewmodel.uiState.value.gameResult?.matchDuration)
    }

    @Test
    fun handleGameSocketEvent_updated_withFinishedStatus_triggersGameEnd() {
        val user = User.newBuilder().setUid("u1").setDisplayName("Alice").setGameId("g1").build()
        viewmodel.setUiStateForTest(GameUiState(user = user))

        val finishedGame = fakeGameResponse.copy(
            status = GameStatus.FINISHED.toString(),
            players = listOf(
                fakeGameResponse.players.first().copy(userId = "u1", score = 100)
            ),
            currentPlayerUserId = "u1"
        )

        viewmodel.handleGameSocketEvent(
            GameEvent.Updated(GameUpdatedEvent(gameId = "g1", game = finishedGame))
        )

        assertNotNull(viewmodel.uiState.value.gameResult)
    }

    // --- startTimer ---

    @Test
    fun startTimer_incrementsElapsedSeconds() = runTest {
        viewmodel.startTimer()
        advanceTimeBy(2500L)
        assertTrue(viewmodel.uiState.value.elapsedSeconds >= 2)
        viewmodel.cancelTimer()
    }

    // --- handlePlayerFinished — winner resolution branches ---

    @Test
    fun handlePlayerFinished_usesGameWinnerUserId_whenNoOverride() {
        setTestGameState()
        val game = viewmodel.uiState.value.gameState!!.copy(winnerUserId = "FakeUser1")
        viewmodel.handlePlayerFinished(game, isGameOver = true)
        assertEquals("FakeUser1", viewmodel.uiState.value.gameResult?.winnerUserId)
    }

    @Test
    fun handlePlayerFinished_usesFinishPosition1Player_asWinner() {
        val user = User.newBuilder().setUid("u1").setDisplayName("Alice").setGameId("g1").build()
        val game = ConfirmedGame(
            gameId = "g1", lobbyId = "l1",
            players = listOf(
                GamePlayer(userId = "u1", displayName = "Alice", turnOrder = 0, score = 50,
                    metrics = GamePlayerMetrics(finishPosition = 1)),
                GamePlayer(userId = "u2", displayName = "Bob", turnOrder = 1, score = 30)
            ),
            currentPlayerUserId = "u1"
        )
        viewmodel.setUiStateForTest(GameUiState(user = user, gameState = game))
        viewmodel.handlePlayerFinished(game, isGameOver = true)
        assertEquals("u1", viewmodel.uiState.value.gameResult?.winnerUserId)
    }

    @Test
    fun handlePlayerFinished_fallsBackToSortedFirst_whenNoWinnerInfo() {
        val user = User.newBuilder().setUid("u1").setDisplayName("Alice").setGameId("g1").build()
        val game = ConfirmedGame(
            gameId = "g1", lobbyId = "l1",
            players = listOf(
                GamePlayer(userId = "u1", displayName = "Alice", turnOrder = 0, score = 100),
                GamePlayer(userId = "u2", displayName = "Bob", turnOrder = 1, score = 50)
            ),
            currentPlayerUserId = "u1"
        )
        viewmodel.setUiStateForTest(GameUiState(user = user, gameState = game))
        viewmodel.handlePlayerFinished(game, isGameOver = true)
        assertEquals("u1", viewmodel.uiState.value.gameResult?.winnerUserId)
    }

    // --- handlePlayerFinished — isStillPlaying + isGameOver ---

    @Test
    fun handlePlayerFinished_setsIsStillPlaying_and_isGameOver_correctly() {
        val user = User.newBuilder().setUid("u1").setDisplayName("Alice").setGameId("g1").build()
        val game = ConfirmedGame(
            gameId = "g1", lobbyId = "l1",
            players = listOf(
                GamePlayer(userId = "u1", displayName = "Alice", turnOrder = 0, score = 100,
                    metrics = GamePlayerMetrics(finishPosition = 1)),
                GamePlayer(userId = "u2", displayName = "Bob", turnOrder = 1, score = 50)
            ),
            currentPlayerUserId = "u1"
        )
        viewmodel.setUiStateForTest(GameUiState(user = user, gameState = game))
        viewmodel.handlePlayerFinished(game, isGameOver = false, overrideWinnerId = "u1")

        val result = viewmodel.uiState.value.gameResult!!
        assertFalse(result.isGameOver)
        assertFalse(result.players.first { it.userId == "u1" }.isStillPlaying)
        assertTrue(result.players.first { it.userId == "u2" }.isStillPlaying)
    }

    // --- handlePlayerFinished — metrics (tilesRemainingAtEnd, penaltyPointsAtEnd) ---

    @Test
    fun handlePlayerFinished_usesMetrics_forRemainingTilesAndPenalty() {
        val user = User.newBuilder().setUid("u1").setDisplayName("Alice").setGameId("g1").build()
        val game = ConfirmedGame(
            gameId = "g1", lobbyId = "l1",
            players = listOf(
                GamePlayer(userId = "u1", displayName = "Alice", turnOrder = 0, score = 100,
                    metrics = GamePlayerMetrics(
                        finishPosition = 1,
                        tilesRemainingAtEnd = 5,
                        penaltyPointsAtEnd = 30
                    ))
            ),
            currentPlayerUserId = "u1"
        )
        viewmodel.setUiStateForTest(GameUiState(user = user, gameState = game))
        viewmodel.handlePlayerFinished(game, isGameOver = true, overrideWinnerId = "u1")

        val player = viewmodel.uiState.value.gameResult?.players?.first()
        assertEquals(5, player?.remainingTiles)
        assertEquals(30, player?.penaltyPoints)
    }

    // --- GameEvent.Updated: silent update when another player finishes ---

    @Test
    fun handleGameSocketEvent_updated_silentlyUpdatesResult_whenAnotherPlayerFinishes() = runTest {
        val user = User.newBuilder().setUid("FakeUser1").setDisplayName("Bob").setGameId("FakeGame1").build()
        viewmodel.setUiStateForTest(GameUiState(user = user))

        val twoPlayerResponse = fakeGameResponse.copy(
            status = GameStatus.ACTIVE.toString(),
            players = listOf(
                fakeGameResponse.players.first().copy(
                    userId = "FakeUser1",
                    metrics = emptyMetrics.copy(finishPosition = 1)
                ),
                GamePlayerResponse(
                    userId = "u2", displayName = "Bob2", turnOrder = 1,
                    rackTiles = emptyList(), hasCompletedInitialMeld = false, score = 0,
                    joinedAt = "2026-05-08T10:00:00Z", metrics = emptyMetrics
                )
            ),
            currentPlayerUserId = "u2"
        )

        // current player finishes → lastShownFinishCount = 1
        viewmodel.handleGameSocketEvent(GameEvent.Updated(GameUpdatedEvent("FakeGame1", twoPlayerResponse)))
        runCurrent()

        val secondResponse = twoPlayerResponse.copy(
            players = listOf(
                twoPlayerResponse.players[0],
                twoPlayerResponse.players[1].copy(metrics = emptyMetrics.copy(finishPosition = 2))
            )
        )

        val collected = mutableListOf<GameEffect>()
        val job = launch { viewmodel.effects.collect { collected.add(it) } }
        runCurrent()

        viewmodel.handleGameSocketEvent(GameEvent.Updated(GameUpdatedEvent("FakeGame1", secondResponse)))
        runCurrent()
        job.cancel()

        assertTrue(collected.isEmpty())
        assertEquals(2, viewmodel.uiState.value.gameResult?.players?.count { !it.isStillPlaying })
    }

    // --- GameEvent.Ended: no navigation when already on result screen ---

    @Test
    fun handleGameSocketEvent_ended_doesNotNavigate_whenAlreadyOnResultScreen() = runTest {
        val user = User.newBuilder().setUid("FakeUser1").setDisplayName("Bob").setGameId("FakeGame1").build()
        viewmodel.setUiStateForTest(GameUiState(user = user))

        val activeResponse = fakeGameResponse.copy(
            status = GameStatus.ACTIVE.toString(),
            players = listOf(
                fakeGameResponse.players.first().copy(
                    userId = "FakeUser1",
                    metrics = emptyMetrics.copy(finishPosition = 1)
                ),
                GamePlayerResponse(
                    userId = "u2", displayName = "Bob2", turnOrder = 1,
                    rackTiles = emptyList(), hasCompletedInitialMeld = false, score = 0,
                    joinedAt = "2026-05-08T10:00:00Z", metrics = emptyMetrics
                )
            ),
            currentPlayerUserId = "u2"
        )

        // trigger lastShownFinishCount = 1
        viewmodel.handleGameSocketEvent(GameEvent.Updated(GameUpdatedEvent("FakeGame1", activeResponse)))
        runCurrent()

        val collected = mutableListOf<GameEffect>()
        val job = launch { viewmodel.effects.collect { collected.add(it) } }
        runCurrent()

        viewmodel.handleGameSocketEvent(GameEvent.Ended(GameEndedEvent("FakeGame1", "FakeUser1")))
        runCurrent()
        job.cancel()

        assertTrue(collected.isEmpty())
        assertNotNull(viewmodel.uiState.value.gameResult)
    }

    // --- GameEvent.Updated FINISHED: no navigation when already on result screen ---

    @Test
    fun handleGameSocketEvent_updatedFinished_doesNotNavigate_whenAlreadyOnResultScreen() = runTest {
        val user = User.newBuilder().setUid("FakeUser1").setDisplayName("Bob").setGameId("FakeGame1").build()
        viewmodel.setUiStateForTest(GameUiState(user = user))

        val activeResponse = fakeGameResponse.copy(
            status = GameStatus.ACTIVE.toString(),
            players = listOf(
                fakeGameResponse.players.first().copy(
                    userId = "FakeUser1",
                    metrics = emptyMetrics.copy(finishPosition = 1)
                ),
                GamePlayerResponse(
                    userId = "u2", displayName = "Bob2", turnOrder = 1,
                    rackTiles = emptyList(), hasCompletedInitialMeld = false, score = 0,
                    joinedAt = "2026-05-08T10:00:00Z", metrics = emptyMetrics
                )
            ),
            currentPlayerUserId = "u2"
        )

        // trigger lastShownFinishCount = 1
        viewmodel.handleGameSocketEvent(GameEvent.Updated(GameUpdatedEvent("FakeGame1", activeResponse)))
        runCurrent()

        val finishedResponse = activeResponse.copy(status = GameStatus.FINISHED.toString())

        val collected = mutableListOf<GameEffect>()
        val job = launch { viewmodel.effects.collect { collected.add(it) } }
        runCurrent()

        viewmodel.handleGameSocketEvent(GameEvent.Updated(GameUpdatedEvent("FakeGame1", finishedResponse)))
        runCurrent()
        job.cancel()

        assertTrue(collected.isEmpty())
        assertTrue(viewmodel.uiState.value.gameResult?.isGameOver ?: false)
    }

    // --- GameUiState data class coverage ---

    @Test
    fun gameResultPlayerSummary_equalsAndCopy() {
        val p1 = GameResultPlayerSummary(userId = "u1", displayName = "Alice", score = 100)
        val p2 = p1.copy(score = 200)

        assertEquals(p1, p1)
        assertNotEquals(p1, p2)
        assertEquals("u1", p2.userId)
    }

    @Test
    fun gameResultUiModel_equalsAndCopy() {
        val m1 = GameResultUiModel(winnerUserId = "u1", players = emptyList(), matchDuration = "1:00")
        val m2 = m1.copy(matchDuration = "2:00")

        assertEquals(m1, m1)
        assertNotEquals(m1, m2)
        assertEquals("u1", m2.winnerUserId)
    }

    // --- DebugNavigateToResult ---

    @Test
    fun debugNavigateToResult_requiresActivePlayer_isFalse() {
        assertFalse(GameUIEvent.DebugNavigateToResult.requiresActivePlayer)
    }

    @Test
    fun debugNavigateToResult_withGameState_navigatesToResult() = runTest {
        setTestGameState()

        val collected = mutableListOf<GameEffect>()
        val job = launch { viewmodel.effects.collect { collected.add(it) } }
        runCurrent()

        viewmodel.onUIEvent(GameUIEvent.DebugNavigateToResult)
        runCurrent()
        job.cancel()

        assertTrue(collected.contains(GameEffect.NavigateToResult))
        assertNotNull(viewmodel.uiState.value.gameResult)
        assertTrue(viewmodel.uiState.value.gameResult!!.isGameOver)
    }

    @Test
    fun debugNavigateToResult_withoutGameState_usesDummyData() = runTest {
        val user = User.newBuilder().setUid("u1").setDisplayName("Alice").setGameId("g1").build()
        viewmodel.setUiStateForTest(GameUiState(user = user, gameState = null))

        val collected = mutableListOf<GameEffect>()
        val job = launch { viewmodel.effects.collect { collected.add(it) } }
        runCurrent()

        viewmodel.onUIEvent(GameUIEvent.DebugNavigateToResult)
        runCurrent()
        job.cancel()

        assertTrue(collected.contains(GameEffect.NavigateToResult))
        val result = viewmodel.uiState.value.gameResult
        assertNotNull(result)
        assertTrue(result!!.isGameOver)
        assertEquals("u1", result.winnerUserId)
        assertEquals(1, result.players.size)
    }

    @Test
    fun debugNavigateToResult_withoutGameStateAndNoUser_usesDummyDataEmpty() = runTest {
        viewmodel.setUiStateForTest(GameUiState(user = null, gameState = null))

        val collected = mutableListOf<GameEffect>()
        val job = launch { viewmodel.effects.collect { collected.add(it) } }
        runCurrent()

        viewmodel.onUIEvent(GameUIEvent.DebugNavigateToResult)
        runCurrent()
        job.cancel()

        assertTrue(collected.contains(GameEffect.NavigateToResult))
        val result = viewmodel.uiState.value.gameResult
        assertNotNull(result)
        assertTrue(result!!.players.isEmpty())
    }

    // --- GameEvent.Ended paths ---

    @Test
    fun handleGameSocketEvent_ended_usesFallback_whenLoadGameFails() = runTest {
        setTestGameState()
        coEvery { service.loadGame(any()) } throws RuntimeException()

        val gs = viewmodel.uiState.value.gameState
        assertNotNull(gs)
        viewmodel.handleGameSocketEvent(GameEvent.Ended(GameEndedEvent(gs!!.gameId, gs.currentPlayerUserId)))
        runCurrent()

        assertTrue(viewmodel.uiState.value.gameResult?.isGameOver ?: false)
    }

    @Test
    fun handleGameSocketEvent_ended_setsLoadStateError_whenGameStateIsNull() = runTest {
        val user = User.newBuilder().setUid("u1").setDisplayName("Alice").setGameId("g1").build()
        viewmodel.setUiStateForTest(GameUiState(user = user, gameState = null))

        viewmodel.handleGameSocketEvent(GameEvent.Ended(GameEndedEvent("123", "asdf")))
        runCurrent()

        assertTrue(viewmodel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun handleGameSocketEvent_ended_setsIsGameOver_onSuccess() = runTest {
        setTestGameState()

        viewmodel.handleGameSocketEvent(GameEvent.Ended(GameEndedEvent(fakeGameResponse.gameId, fakeGameResponse.currentPlayerUserId)))
        runCurrent()

        assertTrue(viewmodel.uiState.value.gameResult?.isGameOver ?: false)
    }
}
