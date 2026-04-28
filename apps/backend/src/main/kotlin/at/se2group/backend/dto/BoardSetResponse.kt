package at.se2group.backend.dto

data class BoardSetResponse(
    val boardSetId: String,
    val type: String,
    val tiles: List<TileResponse>
)
