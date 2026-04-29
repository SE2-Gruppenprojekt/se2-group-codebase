package at.se2group.backend.dto

data class UpdateDraftRequest(
    val boardSets: List<Any> = emptyList(),
    val rackTiles: List<Any> = emptyList()
)
