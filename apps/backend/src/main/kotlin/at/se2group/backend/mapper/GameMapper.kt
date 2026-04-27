package at.se2group.backend.mapper

import at.se2group.backend.domain.BoardSet
import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.domain.GamePlayer
import at.se2group.backend.domain.JokerTile
import at.se2group.backend.domain.NumberedTile
import at.se2group.backend.domain.Tile
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
        JokerTile(color)
    } else {
        NumberedTile(color, number ?: throw IllegalStateException("Numbered tile must have a number"))
    }

fun Tile.toEmbeddable(): TileEmbeddable =
    when (this) {
        is JokerTile -> TileEmbeddable(
            color = color,
            number = null,
            joker = true
        )
        is NumberedTile -> TileEmbeddable(
            color = color,
            number = number,
            joker = false
        )
    }
