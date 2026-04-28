package at.se2group.backend.persistence

import at.se2group.backend.domain.TileColor
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class TileEmbeddable(
    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false)
    var color: TileColor = TileColor.BLACK,

    @Column(name = "number")
    var number: Int? = null,

    @Column(name = "joker", nullable = false)
    var joker: Boolean = false
)
