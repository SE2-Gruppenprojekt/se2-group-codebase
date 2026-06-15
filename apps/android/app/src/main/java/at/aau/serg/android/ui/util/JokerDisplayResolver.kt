package at.aau.serg.android.ui.util

import shared.models.game.domain.BoardSet
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile

private const val MIN_TILE_NUMBER = 1
private const val MAX_TILE_NUMBER = 13

fun jokerDisplayResolver(boardSet: BoardSet?, tile: Tile): String {
    if (tile !is JokerTile) return ""
    if (boardSet == null) return "J"

    return when (boardSet.type) {
        BoardSetType.RUN -> inferRunJokerValue(boardSet)
        BoardSetType.GROUP -> inferGroupJokerValue(boardSet)
        BoardSetType.UNRESOLVED -> null
    }?.toString() ?: "J"
}

private fun inferRunJokerValue(boardSet: BoardSet): Int? {
    if (boardSet.tiles.count { it is JokerTile } != 1) return null

    val numbers = boardSet.tiles.mapNotNull { (it as? NumberedTile)?.number }.sorted()
    if (numbers.size < 2) return null

    for (i in 0 until numbers.size - 1) {
        if (numbers[i + 1] - numbers[i] == 2) return numbers[i] + 1
    }

    val min = numbers.first()
    val max = numbers.last()

    if (numbers.size == max - min + 1) {
        return when {
            max < MAX_TILE_NUMBER -> max + 1
            min > MIN_TILE_NUMBER -> min - 1
            else -> null
        }
    }

    return null
}

private fun inferGroupJokerValue(boardSet: BoardSet): Int? {
    if (boardSet.tiles.count { it is JokerTile } != 1) return null

    return boardSet.tiles.mapNotNull { (it as? NumberedTile)?.number }.distinct().singleOrNull()
}
