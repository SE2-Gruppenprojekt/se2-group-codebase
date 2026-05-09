package shared.models.game.request

data class UpdateDraftRequest(
    val boardSets: List<BoardSetRequest>,

    val rackTiles: List<TileRequest>
)

data class BoardSetRequest(
    val tiles: List<TileRequest>
)

data class TileRequest(
    val tileId: String,

    val color: String,
    val number: Int?,
    val joker: Boolean
)
