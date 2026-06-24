package at.aau.serg.android.ui.screens.game

import shared.models.game.domain.Tile

sealed interface GameUIEvent {
    val requiresActivePlayer: Boolean get() = true

    object AddRow : GameUIEvent
    object DrawTile : GameUIEvent
    object ResetSelection : GameUIEvent
    object ToggleXRAY : GameUIEvent {
        override val requiresActivePlayer = false
    }
    data class MoveTiles(val rowId: String?) : GameUIEvent
    data class MoveInSameRow(val rowId: String?, val from: Int, val to: Int) : GameUIEvent
    data class OnTileSelected(val tile: Tile, val selected: Boolean, val rowId: String?) : GameUIEvent
    object EndTurn : GameUIEvent
    data class OnLoadGame(val gameId: String) : GameUIEvent {
        override val requiresActivePlayer = false
    }
    data object OnSettings : GameUIEvent {
        override val requiresActivePlayer = false
    }
    data object OnBack : GameUIEvent {
        override val requiresActivePlayer = false
    }
    data object DebugNavigateToResult : GameUIEvent {
        override val requiresActivePlayer = false
    }
}
