package at.se2group.backend.domain

sealed class Tile {
    abstract val tileId: String
    abstract val color: TileColor
}

data class NumberedTile(
    override val tileId: String,
    override val color: TileColor,
    val number: Int
) : Tile() {
    init {
        require(tileId.isNotBlank()) { "tileId must not be blank" }
        require(number in TileRules.MIN_TILE_NUMBER..TileRules.MAX_TILE_NUMBER) {
            "tile number must be between ${TileRules.MIN_TILE_NUMBER} and ${TileRules.MAX_TILE_NUMBER}"
        }
    }
}

data class JokerTile(
    override val tileId: String,
    override val color: TileColor
) : Tile() {
    init {
        require(tileId.isNotBlank()) { "tileId must not be blank" }
    }
}
