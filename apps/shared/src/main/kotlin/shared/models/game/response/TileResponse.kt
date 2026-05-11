package shared.models.game.response

data class TileResponse(
    val tileId: String,
    val color: String,
    val number: Int?,
    val isJoker: Boolean
)
