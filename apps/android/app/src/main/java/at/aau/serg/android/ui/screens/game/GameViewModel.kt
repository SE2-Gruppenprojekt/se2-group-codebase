package at.aau.serg.android.ui.screens.game

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shared.models.match.domain.BoardSet
import shared.models.match.domain.BoardSetType
import shared.models.match.domain.NumberedTile
import shared.models.match.domain.Tile
import shared.models.match.domain.TileColor
import java.util.UUID


class GameViewModel(
    private val userStore: ProtoStore<User>
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    init {
        // Fill board with 2 rows
        val boardSets = listOf(
            BoardSet(
                boardSetId = "row1",
                tiles = listOf(
                    NumberedTile(TileColor.RED, 3),
                    NumberedTile(TileColor.RED, 4),
                    NumberedTile(TileColor.RED, 5)
                )
            ),
            BoardSet(
                boardSetId = "row2",
                tiles = listOf(
                    NumberedTile(TileColor.BLACK, 10),
                    NumberedTile(TileColor.RED, 10),
                    NumberedTile(TileColor.BLUE, 10)
                )
            )
        )

        // Create random rack tiles
        val rackTiles = List(14) {
            NumberedTile(
                color = TileColor.entries.toTypedArray().random(),
                number = (1..13).random()
            )
        }

        _uiState.value = GameUiState(
            rackTiles = rackTiles,
            boardSets = boardSets
        )

        viewModelScope.launch {
            userStore.data.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun onTileSelected(tile: Tile, selected: Boolean, rowId: String? = null) {
        _uiState.update { state ->
            val currentRow = state.activeSelectionRow
            val selectedTiles = state.selectedTiles.toMutableSet()

            val newSelectedTiles =
                if (selected && currentRow != null && currentRow != rowId) {
                    mutableSetOf(tile)
                } else {
                    if (selected) selectedTiles.add(tile)
                    else selectedTiles.remove(tile)
                    selectedTiles
                }

            state.copy(
                selectedTiles = newSelectedTiles,
                activeSelectionRow = if (newSelectedTiles.isEmpty()) null else rowId
            )
        }
    }

    fun addRow() {
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
                selectedTiles = emptySet()
            )
        }
    }

    fun moveTiles(boardSetId: String? = null) {
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
                    // Move tiles into the rack
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
    }
    fun moveTileInRack(from: Int, to: Int) {
        _uiState.update { state ->

            val list = state.rackTiles.toMutableList()

            if (from !in list.indices || to !in list.indices) return@update state

            val item = list.removeAt(from)
            list.add(to, item)

            state.copy(rackTiles = list)
        }
    }
}
