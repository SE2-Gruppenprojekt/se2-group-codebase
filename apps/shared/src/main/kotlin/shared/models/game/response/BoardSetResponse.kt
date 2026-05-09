package shared.models.game.response

data class BoardSetResponse(
    val boardSetId: String,
    val type: String,
    val tiles: List<TileResponse>
)
