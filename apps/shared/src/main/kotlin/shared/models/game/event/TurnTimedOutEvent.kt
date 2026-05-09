package shared.models.game.event

data class TurnTimedOutEvent(
    val gameId: String,
    val previousTurnPlayerId: String
) : GameEvent {
    override val type = TYPE

    companion object {
        const val TYPE = "turn.timed_out"
    }
}
