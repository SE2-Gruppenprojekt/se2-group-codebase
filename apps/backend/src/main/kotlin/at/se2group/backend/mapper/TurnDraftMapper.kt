package at.se2group.backend.mapper

import shared.models.game.domain.*
import shared.models.game.request.BoardSetRequest
import shared.models.game.request.TileRequest
import shared.models.game.request.UpdateDraftRequest
import shared.models.game.response.TurnDraftResponse
import at.se2group.backend.persistence.TurnDraftEntity
import at.se2group.backend.persistence.TurnDraftBoardSetEntity

fun UpdateDraftRequest.toDomain(gameId: String, userId: String): TurnDraft {
    return TurnDraft(
        gameId = gameId,
        playerUserId = userId,
        boardSets = boardSets.map { it.toBoardSetDomain() },
        rackTiles = rackTiles.map { it.toTileDomain() }
    )
}

fun BoardSetRequest.toBoardSetDomain(): BoardSet {
    return BoardSet(
        boardSetId = boardSetId,
        type = type,
        tiles = tiles.map { it.toTileDomain() }
    )
}

fun TileRequest.toTileDomain(): Tile {
    return if (isJoker) {
        JokerTile(tileId, TileColor.valueOf(color))
    } else {
        val tileNumber = number ?: throw IllegalArgumentException("Number required for non-joker")
        NumberedTile(tileId, TileColor.valueOf(color), tileNumber)
    }
}

fun TurnDraft.toEntity(existing: TurnDraftEntity): TurnDraftEntity {
    existing.boardSets.clear()
    existing.version = version

    val newBoardSets = boardSets.map { set ->
        TurnDraftBoardSetEntity(
            draft = existing,
            boardSetId = set.boardSetId,
            type = set.type,
            tiles = set.tiles
                .map { it.toEmbeddable() }
                .toMutableList()
        )
    }

    existing.boardSets.addAll(newBoardSets)

    existing.rackTiles = rackTiles
        .map { it.toEmbeddable() }
        .toMutableList()

    return existing
}
fun TurnDraftEntity.toDomain(): TurnDraft {
    return TurnDraft(
        gameId = gameId,
        playerUserId = playerUserId,
        boardSets = boardSets.map { set ->
            BoardSet(
                boardSetId = set.boardSetId,
                type = set.type,
                tiles = set.tiles.map { it.toDomain() }
            )
        },
        rackTiles = rackTiles.map { it.toDomain() },
        version = version
    )
}


fun TurnDraft.toResponse(): TurnDraftResponse {
    return TurnDraftResponse(
        gameId = gameId,
        playerUserId = playerUserId,
        draftBoard = boardSets.map { it.toResponse() },
        draftHand = rackTiles.map { it.toResponse() },
        version = version
    )
}
