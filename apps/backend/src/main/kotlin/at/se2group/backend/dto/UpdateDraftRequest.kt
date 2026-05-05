package at.se2group.backend.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.NotEmpty

data class UpdateDraftRequest(
    @field:NotNull
    val boardSets: List<@Valid BoardSetRequest>,

    @field:NotNull
    val rackTiles: List<@Valid TileRequest>
)

data class BoardSetRequest(
    @field:NotEmpty(message = "tiles must not be empty")
    val tiles: List<@Valid TileRequest>
)

data class TileRequest(
    @field:NotBlank(message = "tileId must not be blank")
    val tileId: String,

    @field:NotBlank(message = "color must not be blank")
    val color: String,
    val number: Int?,
    val joker: Boolean
)
