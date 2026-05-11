package at.se2group.backend.mapper

import shared.models.game.domain.BoardSet
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile
import shared.models.game.response.BoardSetResponse
import shared.models.game.response.GamePlayerResponse
import shared.models.game.response.GameResponse
import shared.models.game.response.TileResponse
import at.se2group.backend.persistence.BoardSetEntity
import at.se2group.backend.persistence.GameEntity
import at.se2group.backend.persistence.GamePlayerEntity
import at.se2group.backend.persistence.TileEmbeddable

fun GameEntity.toDomain(): ConfirmedGame =
    ConfirmedGame(
        gameId = gameId,
        lobbyId = lobbyId,
        players = players.map { it.toDomain() },
        boardSets = boardSets.map { it.toDomain() },
        drawPile = drawPile.map { it.toDomain() },
        currentPlayerUserId = currentPlayerUserId,
        status = status,
        createdAt = createdAt,
        startedAt = startedAt,
        finishedAt = finishedAt
    )

fun ConfirmedGame.toEntity(): GameEntity {
    val gameEntity = GameEntity(
        gameId = gameId,
        lobbyId = lobbyId,
        currentPlayerUserId = currentPlayerUserId,
        status = status,
        createdAt = createdAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
        drawPile = drawPile.map { it.toEmbeddable() }.toMutableList()
    )

    gameEntity.players = players.map { it.toEntity(gameEntity) }.toMutableList()
    gameEntity.boardSets = boardSets.map { it.toEntity(gameEntity) }.toMutableList()

    return gameEntity
}

fun GamePlayerEntity.toDomain(): GamePlayer =
    GamePlayer(
        userId = userId,
        displayName = displayName,
        turnOrder = turnOrder,
        rackTiles = rackTiles.map { it.toDomain() },
        hasCompletedInitialMeld = hasCompletedInitialMeld,
        score = score,
        joinedAt = joinedAt
    )

fun GamePlayer.toEntity(game: GameEntity): GamePlayerEntity =
    GamePlayerEntity(
        game = game,
        userId = userId,
        displayName = displayName,
        turnOrder = turnOrder,
        rackTiles = rackTiles.map { it.toEmbeddable() }.toMutableList(),
        hasCompletedInitialMeld = hasCompletedInitialMeld,
        score = score,
        joinedAt = joinedAt
    )

fun BoardSetEntity.toDomain(): BoardSet =
    BoardSet(
        boardSetId = boardSetId,
        type = type,
        tiles = tiles.map { it.toDomain() }
    )

fun BoardSet.toEntity(game: GameEntity): BoardSetEntity =
    BoardSetEntity(
        game = game,
        boardSetId = boardSetId,
        type = type,
        tiles = tiles.map { it.toEmbeddable() }.toMutableList()
    )

fun TileEmbeddable.toDomain(): Tile =
    if (joker) {
        JokerTile(tileId, color)
    } else {
        NumberedTile(
            tileId,
            color,
            number ?: throw IllegalStateException("Numbered tile must have a number")
        )
    }

fun Tile.toEmbeddable(): TileEmbeddable =
    when (this) {
        is JokerTile -> TileEmbeddable(
            tileId = tileId,
            color = color,
            number = null,
            joker = true
        )
        is NumberedTile -> TileEmbeddable(
            tileId = tileId,
            color = color,
            number = number,
            joker = false
        )
    }

fun ConfirmedGame.toResponse(): GameResponse =
    GameResponse(
        gameId = gameId,
        lobbyId = lobbyId,
        players = players.map { it.toResponse() },
        board = boardSets.map { it.toResponse() },
        drawPile = drawPile.map { it.toResponse() },
        drawPileCount = drawPile.size,
        currentPlayerUserId = currentPlayerUserId,
        currentTurnPlayerId = currentPlayerUserId,
        turnDeadline = null,
        remainingTurnSeconds = null,
        status = status.name,
        createdAt = createdAt,
        startedAt = startedAt,
        finishedAt = finishedAt
    )

fun GamePlayer.toResponse(): GamePlayerResponse =
    GamePlayerResponse(
        userId = userId,
        displayName = displayName,
        turnOrder = turnOrder,
        rackTiles = rackTiles.map { it.toResponse() },
        hasCompletedInitialMeld = hasCompletedInitialMeld,
        score = score,
        joinedAt = joinedAt
    )

fun BoardSet.toResponse(): BoardSetResponse =
    BoardSetResponse(
        boardSetId = boardSetId,
        type = type.name,
        tiles = tiles.map { it.toResponse() }
    )

fun Tile.toResponse(): TileResponse =
    when (this) {
        is JokerTile -> TileResponse(
            tileId = tileId,
            color = color.name,
            number = null,
            isJoker = true
        )
        is NumberedTile -> TileResponse(
            tileId = tileId,
            color = color.name,
            number = number,
            isJoker = false
        )
    }
