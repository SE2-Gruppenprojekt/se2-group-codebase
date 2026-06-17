package at.aau.serg.android.ui.screens.game.components

import androidx.compose.ui.graphics.Color
import shared.models.game.domain.BoardSet

data class TileRowConfig(
    val selectedRow: String? = null,
    val borderColor: Color? = null,
    val rowId: String? = null,
    val boardSet: BoardSet? = null,
)
