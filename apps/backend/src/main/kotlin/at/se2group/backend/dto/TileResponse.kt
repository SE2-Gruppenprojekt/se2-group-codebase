package at.se2group.backend.dto

data class TileResponse(
    val tileId: String,
    val color: String,
    val number: Int?,
    val isJoker: Boolean
)
