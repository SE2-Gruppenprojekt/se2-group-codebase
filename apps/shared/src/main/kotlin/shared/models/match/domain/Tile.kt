package shared.models.match.domain

import java.util.concurrent.atomic.AtomicLong

private val idGenerator = AtomicLong(0)

sealed interface Tile {
    val id: Long
    val color: TileColor
}

data class NumberedTile(
    override val color: TileColor,
    val number: Int,
    override val id: Long = idGenerator.incrementAndGet()
) : Tile {
    init {
        require(number in 1..13) { "tile number must be between 1 and 13" }
    }
}

data class JokerTile(
    override val color: TileColor,
    override val id: Long = idGenerator.incrementAndGet()
) : Tile
