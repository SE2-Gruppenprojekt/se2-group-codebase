package at.se2group.backend.domain

sealed interface Tile {
    val tileId: String
    val color: TileColor
}

data class NumberedTile(
    override val tileId: String,
    override val color: TileColor,
    val number: Int
) : Tile {
    init {
        require(tileId.isNotBlank()) { "tileId must not be blank" }
        require(number in 1..13) { "tile number must be between 1 and 13" }
    }
}

data class JokerTile(
    override val tileId: String,
    override val color: TileColor
) : Tile {
    init {
        require(tileId.isNotBlank()) { "tileId must not be blank" }
    }
}
