package shared.models.game.event

import shared.models.EventPayload

sealed interface GameEvent : EventPayload {

    data class DraftUpdated(val payload: GameDraftUpdatedEvent) : GameEvent {
        override val type = payload.type
    }
    data class Ended(val payload: GameEndedEvent) : GameEvent {
        override val type = payload.type
    }
    data class Updated(val payload: GameUpdatedEvent) : GameEvent {
        override val type = payload.type
    }
    data class TurnChanged(val payload: TurnChangedEvent) : GameEvent {
        override val type = payload.type
    }
    data class TurnTimedOut(val payload: TurnTimedOutEvent) : GameEvent {
        override val type = payload.type
    }
}
