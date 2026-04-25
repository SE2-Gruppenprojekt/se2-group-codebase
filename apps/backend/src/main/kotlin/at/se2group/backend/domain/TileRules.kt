package at.se2group.backend.domain

object TileRules {
    const val NUMBERED_TILE_COPY_COUNT = 2
    const val MIN_TILE_NUMBER = 1
    const val MAX_TILE_NUMBER = 13

    val jokerColors = listOf(
        TileColor.BLACK,
        TileColor.RED
    )
}
