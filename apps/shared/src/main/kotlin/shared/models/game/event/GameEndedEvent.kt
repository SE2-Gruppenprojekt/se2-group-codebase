package shared.models.game.event

data class GameEndedEvent(
    val gameId: String,
    val winnerUserId: String
) : GameEvent {
    override val type = TYPE

    companion object {
        const val TYPE = "game.ended"
    }
}
