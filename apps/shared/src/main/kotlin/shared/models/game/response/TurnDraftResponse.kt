package shared.models.game.response

data class TurnDraftResponse(
    val gameId: String,
    val playerUserId: String,
    val draftBoard: List<BoardSetResponse>,
    val draftHand: List<TileResponse>,
    val version: Long
)
