package at.aau.serg.android.core.network.mapper

import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile
import shared.models.game.request.BoardSetRequest
import shared.models.game.domain.BoardSet
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.GamePlayerMetrics
import shared.models.game.domain.GameStatus
import shared.models.game.domain.TileColor
import shared.models.game.request.TileRequest
import shared.models.game.response.BoardSetResponse
import shared.models.game.response.GamePlayerMetricsResponse
import shared.models.game.response.GamePlayerResponse
import shared.models.game.response.GameResponse
import shared.models.game.response.TileResponse
import java.time.Instant

fun BoardSet.toRequest(): BoardSetRequest {
    return BoardSetRequest(
        boardSetId = boardSetId,
        type = type,
        tiles = tiles.map { it.toRequest() }
    )
}

fun BoardSetResponse.toDomain(): BoardSet {
    return BoardSet(
        boardSetId = boardSetId,
        type = BoardSetType.valueOf(type),
        tiles = tiles.map { it.toDomain() }
    )
}

fun Tile.toRequest(): TileRequest {
    return when (this) {
        is NumberedTile -> TileRequest(
            tileId = tileId,
            color = color.toString(),
            number = number,
            isJoker = false
        )
        is JokerTile -> TileRequest(
            tileId = tileId,
            color = color.toString(),
            number = null,
            isJoker = true
        )
    }
}

fun TileResponse.toDomain(): Tile {
    return if (isJoker) {
        JokerTile(
            tileId = tileId,
            color = TileColor.valueOf(color),
        )
    } else {
        NumberedTile(
            tileId = tileId,
            color = TileColor.valueOf(color),
            number = number ?: 0
        )
    }
}

fun GamePlayerMetricsResponse.toDomain(): GamePlayerMetrics {
    return GamePlayerMetrics(
        turnsCompleted = turnsCompleted,
        tilesPlayed = tilesPlayed,
        meldsCreated = meldsCreated,
        pointsPlayed = pointsPlayed,
        tilesRemainingAtEnd = tilesRemainingAtEnd,
        penaltyPointsAtEnd = penaltyPointsAtEnd,
        winner = winner,
        finishPosition = finishPosition
    )
}

fun GamePlayerResponse.toDomain(): GamePlayer {
    return GamePlayer(
        userId = userId,
        displayName = displayName,
        turnOrder = turnOrder,
        rackTiles = rackTiles.map { it.toDomain() },
        hasCompletedInitialMeld = hasCompletedInitialMeld,
        score = score,
        joinedAt = Instant.parse(joinedAt),
        metrics = metrics.toDomain()
    )
}

fun GameResponse.toDomain(): ConfirmedGame {
    return ConfirmedGame(
        gameId = gameId,
        lobbyId = lobbyId,
        players = players.map { it.toDomain() },
        boardSets = board.map { it.toDomain() },
        drawPile = drawPile.map { it.toDomain() },
        currentPlayerUserId = currentPlayerUserId,
        status = GameStatus.valueOf(status),
        createdAt = Instant.parse(createdAt),
        startedAt = startedAt?.takeIf { it != "null" }?.let { Instant.parse(it) },
        finishedAt = finishedAt?.takeIf { it != "null" }?.let { Instant.parse(it) },
        totalTurnsCompleted = totalTurnsCompleted,
        winnerUserId = winnerUserId
    )
}
