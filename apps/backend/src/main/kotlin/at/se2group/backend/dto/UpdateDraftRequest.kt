package at.se2group.backend.dto

import at.se2group.backend.domain.BoardSet
import at.se2group.backend.domain.Tile

data class UpdateDraftRequest(
    val boardSets: List<BoardSet> = emptyList(),
    val rackTiles: List<Tile> = emptyList()
)
