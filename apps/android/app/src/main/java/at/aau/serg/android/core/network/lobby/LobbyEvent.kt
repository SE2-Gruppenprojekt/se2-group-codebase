package at.aau.serg.android.core.network.lobby

sealed interface LobbyEvent {
    data class Updated(val payload: LobbyUpdatedPayload) : LobbyEvent
    data class Deleted(val payload: LobbyDeletedPayload) : LobbyEvent
    data class Started(val payload: LobbyStartedPayload) : LobbyEvent
}
