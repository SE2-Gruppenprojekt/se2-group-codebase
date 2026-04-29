package at.se2group.backend.dto

data class UpdateDraftRequest(
    val boardSets: List<BoardSetRequest>,
    val rackTiles: List<TileRequest>
)

data class BoardSetRequest(
    val tiles: List<TileRequest>
)

data class TileRequest(
    val color: String,
    val number: Int?,
    val joker: Boolean
)
