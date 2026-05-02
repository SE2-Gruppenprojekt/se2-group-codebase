package at.aau.serg.android.core.network.lobby

import at.aau.serg.android.core.network.WebConfig
import at.aau.serg.android.core.network.WebSocketManager
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class LobbyWebSocketService(
    private val moshi: Moshi,
    private val ws: WebSocketManager = WebSocketManager()
) {
    private val typeAdapter = moshi.adapter(LobbyEventType::class.java)

    fun subscribe(lobbyId: String): Flow<LobbyEvent> {
        return ws
            .subscribe(WebConfig.Topics.lobby(lobbyId))
            .mapNotNull { parseLobbyEvent(it) }
    }

    private fun parseLobbyEvent(message: String): LobbyEvent? {
        return when (typeAdapter.fromJson(message)?.type) {

            "lobby.updated" ->
                moshi.adapter(LobbyUpdatedPayload::class.java)
                    .fromJson(message)
                    ?.let { LobbyEvent.Updated(it) }

            "lobby.deleted" ->
                moshi.adapter(LobbyDeletedPayload::class.java)
                    .fromJson(message)
                    ?.let { LobbyEvent.Deleted(it) }

            "lobby.started" ->
                moshi.adapter(LobbyStartedPayload::class.java)
                    .fromJson(message)
                    ?.let { LobbyEvent.Started(it) }

            else -> null
        }
    }

    internal fun dispatch(message: String): LobbyEvent? {
        return parseLobbyEvent(message)
    }
}
