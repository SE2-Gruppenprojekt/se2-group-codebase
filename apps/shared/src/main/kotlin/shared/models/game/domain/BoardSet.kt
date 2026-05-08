package shared.models.game.domain

data class BoardSet(
    val boardSetId: String,
    val type: BoardSetType = BoardSetType.UNRESOLVED,
    val tiles: List<Tile> = emptyList()
) {
    init {
        require(boardSetId.isNotBlank()) { "boardSetId must not be blank" }
        require(tiles.isNotEmpty()) { "board sets must contain at least one tile" }
    }
}
