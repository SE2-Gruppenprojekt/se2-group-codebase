package shared.models.lobby.event

sealed interface LobbyEvent {
    data class Updated(val payload: LobbyUpdatedPayload) : LobbyEvent
    data class Deleted(val payload: LobbyDeletedPayload) : LobbyEvent
    data class Started(val payload: LobbyStartedPayload) : LobbyEvent
}
