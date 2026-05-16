package at.aau.serg.android.core.network.lobby

import androidx.annotation.VisibleForTesting
import at.aau.serg.android.core.network.WebConfig
import at.aau.serg.android.core.network.WebSocketManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import shared.models.lobby.event.LobbyDeletedPayload
import shared.models.lobby.event.LobbyEvent
import shared.models.lobby.event.LobbyStartedPayload
import shared.models.lobby.event.LobbyUpdatedPayload

class LobbyWebSocketService(
    private val moshi: Moshi,
    private val ws: WebSocketManager = WebSocketManager()
) {
    private val messageTypeAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    private val updatedAdapter = moshi.adapter(LobbyUpdatedPayload::class.java)
    private val deletedAdapter = moshi.adapter(LobbyDeletedPayload::class.java)
    private val startedAdapter = moshi.adapter(LobbyStartedPayload::class.java)

    suspend fun subscribe(lobbyId: String): Flow<LobbyEvent> {
        return ws
            .subscribe(WebConfig.Topics.lobby(lobbyId))
            .mapNotNull { parseLobbyEvent(it) }
    }

    @VisibleForTesting
    internal fun parseLobbyEvent(message: String): LobbyEvent? {
        val map = messageTypeAdapter.fromJson(message) ?: return null
        val type = map["type"] as? String ?: return null

        return when (type) {
            LobbyUpdatedPayload.TYPE ->
                LobbyEvent.Updated(updatedAdapter.fromJson(message)!!)
            LobbyDeletedPayload.TYPE ->
                LobbyEvent.Deleted(deletedAdapter.fromJson(message)!!)
            LobbyStartedPayload.TYPE ->
                LobbyEvent.Started(startedAdapter.fromJson(message)!!)
            else -> null
        }
    }
}
