package at.aau.serg.android.ui.screens.game.components

import shared.models.game.domain.BoardSet

data class TileItemConfig(
    val size: Int,
    val selected: Boolean,
    val moveHack: Boolean,
    val boardSet: BoardSet? = null,
)
