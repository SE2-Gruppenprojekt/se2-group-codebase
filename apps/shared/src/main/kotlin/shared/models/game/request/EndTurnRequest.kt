package shared.models.game.request

data class EndTurnRequest(
    val boardSets: List<BoardSetRequest>,
    val rackTiles: List<TileRequest>
)
