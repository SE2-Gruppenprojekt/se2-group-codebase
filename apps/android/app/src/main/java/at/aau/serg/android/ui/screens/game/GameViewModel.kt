package at.aau.serg.android.ui.screens.game

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.errors.AppError
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.ServiceLocator
import at.aau.serg.android.core.network.game.GameAPI
import at.aau.serg.android.core.network.game.GameService
import at.aau.serg.android.core.network.game.GameWebSocketService
import at.aau.serg.android.core.network.mapper.NetworkErrorMapper
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.core.network.mapper.toRequest
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.util.ErrorUiMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shared.models.game.domain.BoardSet
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GameStatus
import shared.models.game.domain.Tile
import shared.models.game.event.GameEvent
import shared.models.game.request.EndTurnRequest
import shared.models.game.request.UpdateDraftRequest
import java.util.UUID


class GameViewModel(
    private val userStore: ProtoStore<User>,
    private val gameService: GameService = GameService(
        RetrofitProvider.retrofit.create(GameAPI::class.java)
    ),
    private val socket: GameWebSocketService = ServiceLocator.gameWebSocketService
) : ViewModel() {

    private var socketJob: Job? = null
    private var hasEmittedGameResultNavigation = false
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private val _effect = MutableSharedFlow<GameEffect>()
    val effects: SharedFlow<GameEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            userStore.data.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    @VisibleForTesting
    internal fun startSocket(gameId: String) {
        socketJob?.cancel()

        socketJob = viewModelScope.launch {
            socket.subscribe(gameId)
                .catch { e ->
                    val appError = NetworkErrorMapper.map(e)
                    _uiState.update { it.copy(loadState = LoadState.Error(appError)) }
                }
                .collect { handleGameSocketEvent(it) }
        }
    }

    @VisibleForTesting
    internal fun applyGameState(game: ConfirmedGame, refreshView: Boolean) {
        val user = _uiState.value.user
            ?: throw IllegalStateException("User must not be null when applyGameState is called.")

        val newRack = game.players
            .firstOrNull { it.userId == user.uid }
            ?.rackTiles ?: emptyList()

        val oldRack = _uiState.value.rackTiles
        val mergedRack = mergeRackPreservingOrder(oldRack, newRack)

        _uiState.update { old ->
            old.copy(
                gameState = game,
                rackTiles = if (refreshView) mergedRack else old.rackTiles,
                boardSets = if (refreshView) game.boardSets else old.boardSets,
                isActivePlayer = game.currentPlayerUserId == user.uid
            )
        }

        if (game.status == GameStatus.FINISHED) {
            val winner = game.players.maxByOrNull { it.score }
            if (winner != null) handleFinishedGame(winner.userId)
        }
    }

    private fun mergeRackPreservingOrder(old: List<Tile>, new: List<Tile>): List<Tile> {
        val oldIds = old.map { it.tileId }
        val newIds = new.map { it.tileId }

        val tilesRemoved = oldIds.any { it !in newIds }

        if (!tilesRemoved) {
            val newById = new.associateBy { it.tileId }
            val preserved = oldIds.mapNotNull { newById[it] }
            val addedTiles = new.filter { it.tileId !in oldIds }

            return preserved + addedTiles
        }

        return new
    }

    @VisibleForTesting
    internal fun onUIEvent(event: GameUIEvent) {
        val state = _uiState.value
        try {
            check(!(event.requiresActivePlayer && !state.isActivePlayer)) {
                "You can only perform this action during your turn."
            }
            when (event) {
                GameUIEvent.AddRow ->
                    addRow()
                GameUIEvent.DrawTile ->
                    drawTile()
                GameUIEvent.EndTurn ->
                    endTurn()
                is GameUIEvent.MoveInSameRow -> {
                    moveInSameRow(
                        srcRowId = event.rowId,
                        from = event.from,
                        to = event.to
                    )
                }
                is GameUIEvent.MoveTiles -> moveTiles(event.rowId)
                is GameUIEvent.OnLoadGame -> {
                    loadGame(event.gameId)
                }
                is GameUIEvent.OnTileSelected -> {
                    onTileSelected(
                        tile = event.tile,
                        selected = event.selected,
                        rowId = event.rowId
                    )
                }
                GameUIEvent.ResetSelection -> resetSelection()
                GameUIEvent.OnSettings -> {
                    viewModelScope.launch {
                        _effect.emit(GameEffect.NavigateToSettings)
                    }
                }
                GameUIEvent.OnBack -> {
                    viewModelScope.launch {
                        _effect.emit(GameEffect.NavigateBack)
                    }
                }
            }
        } catch (e : Exception) {
            val appError = ErrorUiMapper.map(e)

            _uiState.update {
                it.copy(loadState = LoadState.Error(appError))
            }
        }
    }

    private fun loadGame(gameId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }
            try {
                val gameState = gameService.loadGame(gameId).toDomain()
                applyGameState(gameState, true)
                _uiState.update {
                    it.copy(loadState = LoadState.Success)
                }
                startSocket(gameId)
            } catch (e: Throwable) {
                val appError = NetworkErrorMapper.map(e)

                _uiState.update {
                    it.copy(loadState = LoadState.Error(appError))
                }
            }
        }
    }

    private fun onTileSelected(tile: Tile, selected: Boolean, rowId: String?) {
        _uiState.update { state ->
            val currentRow = state.activeSelectionRow
            val selectedTiles = state.selectedTiles.toMutableSet()

            val newSelectedTiles =
                if (selected && currentRow != null && currentRow != rowId) {
                    mutableSetOf(tile)
                } else {
                    if (selected) selectedTiles.add(tile)
                    else selectedTiles.remove(tile)
                    selectedTiles.toSet()
                }

            state.copy(
                selectedTiles = newSelectedTiles,
                activeSelectionRow = if (newSelectedTiles.isEmpty()) null else rowId
            )
        }
    }

    private fun addRow() {
        _uiState.update { state ->
            val selected = state.selectedTiles

            val newRack = state.rackTiles.filter { it !in selected }
            val cleanedBoardSets = state.boardSets
                .mapNotNull { set ->
                    val cleanedTiles = set.tiles.filter { it !in selected }
                    if (cleanedTiles.isEmpty()) null
                    else set.copy(tiles = cleanedTiles)
                }

            val newSet = BoardSet(
                boardSetId = UUID.randomUUID().toString(),
                type = BoardSetType.UNRESOLVED,
                tiles = selected.toList()
            )
            val updatedBoardSets = cleanedBoardSets + newSet
            state.copy(
                rackTiles = newRack,
                boardSets = updatedBoardSets,
                selectedTiles = emptySet(),
                activeSelectionRow = null,
                ruleValidation = RuleValidationUiState()
            )
        }
        sendTurnDraft()
    }

    private fun moveTiles(boardSetId: String?) {
        _uiState.update { state ->
            val selected = state.selectedTiles
            if (selected.isEmpty()) return@update state

            val newRack = state.rackTiles.filter { it !in selected }
            val cleanedBoardSets = state.boardSets
                .mapNotNull { set ->
                    val cleanedTiles = set.tiles.filter { it !in selected }
                    if (cleanedTiles.isEmpty()) null
                    else set.copy(tiles = cleanedTiles)
                }

            val updatedBoardSets =
                if (boardSetId == null) {
                    cleanedBoardSets
                } else {
                    cleanedBoardSets.map { set ->
                        if (set.boardSetId == boardSetId) {
                            set.copy(tiles = set.tiles + selected)
                        } else set
                    }
                }

            val finalRack =
                if (boardSetId == null) newRack + selected
                else newRack

            state.copy(
                rackTiles = finalRack,
                boardSets = updatedBoardSets,
                selectedTiles = emptySet(),
                activeSelectionRow = null,
                ruleValidation = RuleValidationUiState()
            )
        }
        sendTurnDraft()
    }

    private fun moveInSameRow(srcRowId: String?, from: Int, to: Int) {
        _uiState.update { state ->
            val (list, boardIndex) = if (srcRowId == null) {
                state.rackTiles.toMutableList() to null
            } else {
                val idx = state.boardSets.indexOfFirst { it.boardSetId == srcRowId }
                if (idx == -1) return@update state
                state.boardSets[idx].tiles.toMutableList() to idx
            }

            if (from !in list.indices || to !in list.indices) return@update state

            val item = list.removeAt(from)
            list.add(to, item)

            if (boardIndex == null) {
                state.copy(rackTiles = list, ruleValidation = RuleValidationUiState())
            } else {
                val updatedSets = state.boardSets.toMutableList()
                updatedSets[boardIndex] = updatedSets[boardIndex].copy(tiles = list)
                state.copy(boardSets = updatedSets, ruleValidation = RuleValidationUiState())
            }
        }
        sendTurnDraft()
    }

    @VisibleForTesting
    internal fun resetSelection() {
        val gameState = _uiState.value.gameState ?: throw IllegalStateException("GameState must not be null resetBoard is attempted.")
        applyGameState(gameState, true)
        _uiState.update { state ->
            state.copy(
                selectedTiles = emptySet(),
                activeSelectionRow = null,
                ruleValidation = RuleValidationUiState()
            )
        }
        sendTurnDraft()
    }

    @VisibleForTesting
    internal fun drawTile() {
        val user = uiState.value.user ?: throw IllegalStateException("User must not be null when drawTile is called.")
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }
            try {
                val gameState = gameService.drawTile(user.gameId, user.uid).toDomain()
                val player = gameState.players.firstOrNull { it.userId == user.uid }
                val newTile = player?.rackTiles?.lastOrNull()
                applyGameState(gameState, false)
                _uiState.update { currentState ->
                    currentState.copy(
                        loadState = LoadState.Success,
                        rackTiles = if (newTile != null) currentState.rackTiles + newTile else currentState.rackTiles
                    )
                }
            } catch (e: Throwable) {
                val appError = NetworkErrorMapper.map(e)

                _uiState.update {
                    it.copy(loadState = LoadState.Error(appError))
                }
            }
        }
    }

    @VisibleForTesting
    internal fun endTurn() {
        val user = uiState.value.user ?: throw IllegalStateException("User must not be null when endTurn is called.")
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }
            try {
                val request = EndTurnRequest(
                    boardSets = uiState.value.boardSets.map { it.toRequest() },
                    rackTiles = uiState.value.rackTiles.map { it.toRequest() }
                )
                gameService.endTurn(
                    gameId = user.gameId,
                    playerId = user.uid,
                    request = request
                )
                _uiState.update {
                    it.copy(
                        selectedTiles = emptySet(),
                        activeSelectionRow = null,
                        loadState = LoadState.Success,
                        ruleValidation = RuleValidationUiState()
                    )
                }
            } catch (e: Throwable) {
                /*
                Rule-validation failures are recoverable: the player stays on the board
                and corrects the move. All other errors (network, server, etc.) are fatal
                and surface through LoadState.Error as usual.
                */
                when (val appError = NetworkErrorMapper.map(e)) {
                    is AppError.Rest.RuleValidation -> {
                        val setViolations = appError.violations.filter { it.boardSetId != null }
                        val globalViolations = appError.violations.filter { it.boardSetId == null }
                        _uiState.update {
                            it.copy(
                                loadState = LoadState.Success,
                                ruleValidation = RuleValidationUiState(
                                    violationsByBoardSetId = setViolations.groupBy { v -> v.boardSetId!! },
                                    globalViolations = globalViolations,
                                    summaryMessage = appError.message
                                )
                            )
                        }
                    }
                    else -> {
                        _uiState.update {
                            it.copy(loadState = LoadState.Error(appError))
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    internal fun sendTurnDraft() {
        val user = uiState.value.user ?: throw IllegalStateException("User must not be null when sendTurnDraft is called.")
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading
                )
            }

            try {
                val request = UpdateDraftRequest(
                    boardSets = uiState.value.boardSets.map { it.toRequest() },
                    rackTiles = uiState.value.rackTiles.map { it.toRequest() }
                )
                gameService.updateDraft(
                    gameId = user.gameId,
                    playerId = user.uid,
                    request = request
                )
                _uiState.update { it.copy(loadState = LoadState.Success) }
            } catch (e: Throwable) {
                val appError = NetworkErrorMapper.map(e)

                _uiState.update {
                    it.copy(loadState = LoadState.Error(appError))
                }
            }
        }
    }

    internal fun handleGameSocketEvent(event: GameEvent) {
        try {
            when (event) {
                is GameEvent.DraftUpdated -> {
                    val user = _uiState.value.user
                        ?: throw IllegalStateException("User must not be null when DraftUpdated received.")
                    if (user.uid == event.payload.playerId) return

                    _uiState.update {
                        it.copy(boardSets = event.payload.draft.draftBoard.map { it.toDomain() })
                    }
                }

                is GameEvent.Ended -> {
                    handleFinishedGame(event.payload.winnerUserId)
                }

                is GameEvent.TurnChanged -> {
                    _uiState.value.gameState?.let { game ->
                        if (game.players.any { it.userId == event.payload.currentTurnPlayerId }) {
                            val updated = game.copy(currentPlayerUserId = event.payload.currentTurnPlayerId)
                            applyGameState(updated, false)
                        }
                    }
                    _uiState.value
                }

                is GameEvent.TurnTimedOut -> {
                    val user = _uiState.value.user
                        ?: throw IllegalStateException("User must not be null when TurnTimedOut received.")
                    if (user.uid == event.payload.previousTurnPlayerId) {
                        _uiState.update {
                            it.copy(loadState = LoadState.Error(AppError.Game.TurnTimedOut))
                        }
                    }
                }

                is GameEvent.Updated -> {
                    val game = event.payload.game.toDomain()
                    if (game.status == GameStatus.FINISHED) {
                        applyGameState(game, true)
                        val winner = game.players.maxByOrNull { it.score }
                        if (winner != null) handleFinishedGame(winner.userId)
                    } else if (!_uiState.value.isActivePlayer) {
                        applyGameState(game, true)
                    }
                }

            }
        } catch (e: Exception) {
            val appError = ErrorUiMapper.map(e)
            _uiState.update { it.copy(loadState = LoadState.Error(appError)) }
        }
    }

    @VisibleForTesting
    internal fun handleFinishedGame(winnerUserId: String) {
        if (hasEmittedGameResultNavigation) return
        hasEmittedGameResultNavigation = true

        val players = _uiState.value.gameState?.players.orEmpty()
            .sortedByDescending { it.score }
            .mapIndexed { index, it ->
                GameResultPlayerSummary(
                    userId = it.userId,
                    displayName = it.displayName,
                    score = it.score,
                    finishPosition = index + 1,
                    remainingTiles = it.rackTiles.size,
                    // TODO: map from backend response when available
                    tilesPlayed = 0,
                    meldsCreated = 0,
                    turnsCompleted = 0,
                    pointsFromTiles = it.score,
                    penaltyPoints = 0,
                    isStillPlaying = it.rackTiles.isNotEmpty() && it.userId != winnerUserId
                )
            }

        _uiState.update {
            it.copy(gameResult = GameResultUiModel(winnerUserId = winnerUserId, players = players))
        }

        viewModelScope.launch {
            _effect.emit(GameEffect.NavigateToResult(winnerUserId))
        }
    }

    @VisibleForTesting
    internal fun setUiStateForTest(state: GameUiState) {
        _uiState.value = state
    }
}
