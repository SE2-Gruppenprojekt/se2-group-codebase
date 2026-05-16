package shared.models.game.event

sealed interface GameEvent {
    data class DraftUpdated(val payload: GameDraftUpdatedEvent) : GameEvent
    data class Ended(val payload: GameEndedEvent) : GameEvent
    data class Updated(val payload: GameUpdatedEvent) : GameEvent
    data class TurnChanged(val payload: TurnChangedEvent) : GameEvent
    data class TurnTimedOut(val payload: TurnTimedOutEvent) : GameEvent
}
