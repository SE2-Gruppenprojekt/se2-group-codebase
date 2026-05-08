package shared.models.game.event

data class TurnChangedEvent(
    val gameId: String,
    val currentTurnPlayerId: String
) : GameEvent {
    override val type = TYPE

    companion object {
        const val TYPE = "turn.changed"
    }
}
