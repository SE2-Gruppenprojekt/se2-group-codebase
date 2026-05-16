package shared.models.game.request

import shared.models.game.domain.BoardSetType

data class UpdateDraftRequest(
    val boardSets: List<BoardSetRequest>,

    val rackTiles: List<TileRequest>
)

data class BoardSetRequest(
    val boardSetId: String,
    val type: BoardSetType,
    val tiles: List<TileRequest>
)

data class TileRequest(
    val tileId: String,

    val color: String,
    val number: Int?,
    val isJoker: Boolean
)
