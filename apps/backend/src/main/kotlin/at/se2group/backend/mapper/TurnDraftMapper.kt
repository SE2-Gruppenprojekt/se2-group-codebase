package at.se2group.backend.mapper

import at.se2group.backend.domain.*
import at.se2group.backend.dto.*
import at.se2group.backend.persistence.TurnDraftEntity
import java.util.UUID

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
        boardSetId = UUID.randomUUID().toString(),
        type = BoardSetType.UNRESOLVED,
        tiles = tiles.map { it.toTileDomain() }
    )
}

fun TileRequest.toTileDomain(): Tile {
    return if (joker) {
        JokerTile(TileColor.valueOf(color))
    } else {
        require(number != null) { "Number required for non-joker" }
        NumberedTile(TileColor.valueOf(color), number)
    }
}

fun TurnDraft.toEntity(existing: TurnDraftEntity): TurnDraftEntity {
    // NOTE: boardSets are currently flattened into boardTiles.
    // This loses set structure, but is sufficient for the current draft persistence.
    // Proper BoardSet persistence should be handled in a later iteration.
    existing.boardTiles = boardSets
        .flatMap { it.tiles }
        .map { it.toEmbeddable() }
        .toMutableList()

    existing.rackTiles = rackTiles
        .map { it.toEmbeddable() }
        .toMutableList()

    return existing
}
fun TurnDraftEntity.toDomain(): TurnDraft {
    //map boardTiles and rackTiles back to domain representation
    return TurnDraft(
        gameId = gameId,
        playerUserId = playerUserId,
        boardSets = emptyList(),
        rackTiles = emptyList()
    )
}
