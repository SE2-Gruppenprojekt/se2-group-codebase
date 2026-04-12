package at.aau.serg.android.network.lobby

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class LobbyWebSocketService {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var session: StompSession? = null

    // Suspend-Funktion — läuft bis die Coroutine gecancelled wird
    suspend fun connect(
        lobbyId: String,
        onLobbyUpdated: (LobbyUpdatedPayload) -> Unit,
        onLobbyDeleted: (LobbyDeletedPayload) -> Unit,
        onLobbyStarted: (LobbyStartedPayload) -> Unit
    ) {
        val client = StompClient(OkHttpWebSocketClient())
        session = client.connect("ws://10.0.2.2:8080/ws")

        session!!
            .subscribeText("/topic/lobbies/$lobbyId")
            .catch { e -> Log.e("LobbyWebSocket", "Fehler beim Empfangen", e) }
            .collect { message -> dispatch(message, onLobbyUpdated, onLobbyDeleted, onLobbyStarted) }
    }

    suspend fun disconnect() {
        try {
            session?.disconnect()
        } catch (e: Exception) {
            Log.e("LobbyWebSocket", "Fehler beim Disconnect", e)
        } finally {
            session = null
        }
    }

    private fun dispatch(
        message: String,
        onLobbyUpdated: (LobbyUpdatedPayload) -> Unit,
        onLobbyDeleted: (LobbyDeletedPayload) -> Unit,
        onLobbyStarted: (LobbyStartedPayload) -> Unit
    ) {
        when {
            message.contains("\"type\":\"lobby.updated\"") ->
                moshi.adapter(LobbyUpdatedPayload::class.java)
                    .fromJson(message)?.let(onLobbyUpdated)

            message.contains("\"type\":\"lobby.deleted\"") ->
                moshi.adapter(LobbyDeletedPayload::class.java)
                    .fromJson(message)?.let(onLobbyDeleted)

            message.contains("\"type\":\"lobby.started\"") ->
                moshi.adapter(LobbyStartedPayload::class.java)
                    .fromJson(message)?.let(onLobbyStarted)

            else -> Log.w("LobbyWebSocket", "Unbekanntes Event: $message")
        }
    }
}
