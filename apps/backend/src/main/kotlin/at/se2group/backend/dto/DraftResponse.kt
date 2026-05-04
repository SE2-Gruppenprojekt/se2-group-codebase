package at.se2group.backend.dto

data class DraftResponse(
    val gameId: String,
    val playerUserId: String,
    val boardSets: List<BoardSetResponse>,
    val rackTiles: List<TileResponse>
)
