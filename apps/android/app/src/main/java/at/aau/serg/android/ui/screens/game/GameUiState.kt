package at.aau.serg.android.ui.screens.game

import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import shared.models.match.domain.BoardSet
import shared.models.match.domain.Tile


data class GameUiState(
    val loadState: LoadState = LoadState.Success,
    val user: User? = null,
    val rackTiles: List<Tile> = emptyList(),
    val boardSets: List<BoardSet> = emptyList(),
    val selectedTiles: Set<Tile> = emptySet(),
    val activeSelectionRow: String? = null
)
