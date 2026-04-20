package at.se2group.backend.domain

data class Tile(
    val tileId: String,
    val color: TileColor? = null,
    val number: Int? = null,
    val isJoker: Boolean = false
) {
    init {
        require(tileId.isNotBlank()) { "tileId must not be blank" }

        if (isJoker) {
            require(color == null) { "joker tiles must not have a color" }
            require(number == null) { "joker tiles must not have a number" }
        } else {
            require(color != null) { "numbered tiles must have a color" }
            require(number != null) { "numbered tiles must have a number" }
            require(number in 1..13) { "tile number must be between 1 and 13" }
        }
    }
}
