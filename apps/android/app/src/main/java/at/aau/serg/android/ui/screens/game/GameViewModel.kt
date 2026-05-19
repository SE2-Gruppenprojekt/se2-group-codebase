package at.aau.serg.android.ui.screens.game

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.game.GameAPI
import at.aau.serg.android.core.network.game.GameService
import at.aau.serg.android.core.network.mapper.NetworkErrorMapper
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.core.network.mapper.toRequest
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.util.ErrorUiMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shared.models.game.domain.BoardSet
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.Tile
import shared.models.game.request.UpdateDraftRequest
import java.util.UUID


class GameViewModel(
    private val userStore: ProtoStore<User>,
    private val gameService: GameService = GameService(
        RetrofitProvider.retrofit.create(GameAPI::class.java)
    )
) : ViewModel() {
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
    internal fun applyGameState(game: ConfirmedGame) {
        val user = uiState.value.user ?: throw IllegalStateException("User must not be null when applyGameState is called.")
        val newRack = game.players
            .firstOrNull { it.userId == user.uid }
            ?.rackTiles ?: emptyList()
        val newBoard = game.boardSets

        _uiState.update { old ->
            old.copy(
                gameState = game,
                rackTiles = newRack,
                boardSets = newBoard
            )
        }
    }

    @VisibleForTesting
    internal fun onUIEvent(event: GameUIEvent) {
        val state = _uiState.value
        try {
            if (event.requiresActivePlayer && !state.isActivePlayer) {
                throw IllegalStateException("You can only perform this action during your turn.")
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
                applyGameState(gameState)
                _uiState.update {
                    it.copy(loadState = LoadState.Success)
                }
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
                activeSelectionRow = null
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
                activeSelectionRow = null
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
                state.copy(rackTiles = list)
            } else {
                val updatedSets = state.boardSets.toMutableList()
                updatedSets[boardIndex] = updatedSets[boardIndex].copy(tiles = list)
                state.copy(boardSets = updatedSets)
            }
        }
        sendTurnDraft()
    }

    @VisibleForTesting
    internal fun resetSelection() {
        val gameState = _uiState.value.gameState ?: throw IllegalStateException("GameState must not be null resetBoard is attempted.")
        applyGameState(gameState)
        _uiState.update { state ->
            state.copy(
                selectedTiles = emptySet(),
                activeSelectionRow = null
            )
        }
        sendTurnDraft()
    }

    @VisibleForTesting
    internal fun drawTile() {
        viewModelScope.launch {
            val user = uiState.value.user ?: throw IllegalStateException("User must not be null when drawTile is called.")
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }
            try {
                val gameState = gameService.drawTile(user.gameId, user.uid).toDomain()
                applyGameState(gameState)
                _uiState.update {
                    it.copy(
                        loadState = LoadState.Success,
                        gameState = gameState,
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
        viewModelScope.launch {
            val user = uiState.value.user ?: throw IllegalStateException("User must not be null when endTurn is called.")
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }

            try {
                val gameState = gameService.endTurn(
                    user.gameId,
                    user.uid,

                    ).toDomain()
                applyGameState(gameState)
                _uiState.update {
                    it.copy(
                        selectedTiles = emptySet(),
                        activeSelectionRow = null,
                        loadState = LoadState.Success
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
    internal fun sendTurnDraft() {
        viewModelScope.launch {
            val user = uiState.value.user ?: throw IllegalStateException("User must not be null when sendTurnDraft is called.")

            _uiState.update {
                it.copy(loadState = LoadState.Success
                )
            }

            try {
                val request = UpdateDraftRequest(
                    boardSets = uiState.value.boardSets.map { it.toRequest() },
                    rackTiles = uiState.value.rackTiles.map { it.toRequest() }
                )
                gameService.updateDraft(
                    gameId = user.gameId,
                    request = request
                )
            } catch (e: Throwable) {
                val appError = NetworkErrorMapper.map(e)

                _uiState.update {
                    it.copy(loadState = LoadState.Error(appError))
                }
            }
        }
    }

    @VisibleForTesting
    internal fun setUiStateForTest(state: GameUiState) {
        _uiState.value = state
    }
}
