package at.aau.serg.android.core.network.lobby

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.catch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class LobbyWebSocketService(
    private val client: StompClient = StompClient(OkHttpWebSocketClient())
) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var session: StompSession? = null

    // Suspend function — runs until the coroutine is cancelled
    suspend fun connect(
        lobbyId: String,
        onConnected: () -> Unit = {},
        onLobbyUpdated: (LobbyUpdatedPayload) -> Unit,
        onLobbyDeleted: (LobbyDeletedPayload) -> Unit,
        onLobbyStarted: (LobbyStartedPayload) -> Unit
    ) {
        session = client.connect("ws://10.0.2.2:8080/ws")
        onConnected()

        session!!
            .subscribeText("/topic/lobbies/$lobbyId")
            .catch { e -> Log.e("LobbyWebSocket", "Error receiving message", e) }
            .collect { message -> dispatch(message, onLobbyUpdated, onLobbyDeleted, onLobbyStarted) }
    }

    suspend fun disconnect() {
        try {
            session?.disconnect()
        } catch (e: Exception) {
            Log.e("LobbyWebSocket", "Error during disconnect", e)
        } finally {
            session = null
        }
    }

    private val typeAdapter = moshi.adapter(LobbyEventType::class.java)

    private fun dispatch(
        message: String,
        onLobbyUpdated: (LobbyUpdatedPayload) -> Unit,
        onLobbyDeleted: (LobbyDeletedPayload) -> Unit,
        onLobbyStarted: (LobbyStartedPayload) -> Unit
    ) {
        val type = typeAdapter.fromJson(message)?.type
        when (type) {
            "lobby.updated" ->
                moshi.adapter(LobbyUpdatedPayload::class.java)
                    .fromJson(message)?.let(onLobbyUpdated)

            "lobby.deleted" ->
                moshi.adapter(LobbyDeletedPayload::class.java)
                    .fromJson(message)?.let(onLobbyDeleted)

            "lobby.started" ->
                moshi.adapter(LobbyStartedPayload::class.java)
                    .fromJson(message)?.let(onLobbyStarted)

            else -> Log.w("LobbyWebSocket", "Unknown event: $type")
        }
    }

    // Test helper only
    internal fun testDispatch(
        message: String,
        onLobbyUpdated: (LobbyUpdatedPayload) -> Unit = {},
        onLobbyDeleted: (LobbyDeletedPayload) -> Unit = {},
        onLobbyStarted: (LobbyStartedPayload) -> Unit = {}
    ) {
        dispatch(message, onLobbyUpdated, onLobbyDeleted, onLobbyStarted)
    }
}
