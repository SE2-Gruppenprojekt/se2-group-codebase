package at.se2group.backend.domain

sealed interface Tile {
    abstract val color: TileColor
}

data class NumberedTile(
    override val color: TileColor,
    val number: Int
) : Tile {
    init {
        require(number in 1..13) { "tile number must be between 1 and 13" }
    }
}

data class JokerTile(
    override val color: TileColor
) : Tile
