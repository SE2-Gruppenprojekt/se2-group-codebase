# WebSocket Integration

## Event Types

The backend can broadcast the following event types.

### 1. `lobby.updated`

This event is sent whenever the current lobby state changes.

Typical triggers:

- lobby created
- player joined
- player left
- player marked ready
- player marked unready
- lobby settings changed
- lobby started

This is the main event the frontend should use to refresh the current lobby screen.

### 2. `lobby.deleted`

This event is sent when a lobby is deleted.

Typical triggers:

- host deletes lobby
- last player leaves and the lobby is removed

The frontend should react by closing the lobby screen and navigating away.

### 3. `lobby.started`

This event is sent when the lobby successfully starts a match.

Typical trigger:

- host starts the match

The frontend should react by navigating from the lobby screen to the game screen.

---

## Event Payloads

### `LobbyUpdatedEvent`

```kotlin
package at.se2group.backend.lobby.dto

data class LobbyUpdatedEvent(
    val type: String = "lobby.updated",
    val lobby: LobbyResponse
)
```

Example JSON:

```json
{
    "type": "lobby.updated",
    "lobby": {
        "lobbyId": "abc123",
        "hostUserId": "u1",
        "status": "OPEN",
        "players": [
            {
                "userId": "u1",
                "displayName": "Julian",
                "isReady": false
            },
            {
                "userId": "u2",
                "displayName": "Vanessa",
                "isReady": true
            }
        ],
        "settings": {
            "maxPlayers": 4,
            "isPrivate": false,
            "allowGuests": true
        }
    }
}
```

### `LobbyDeletedEvent`

```kotlin
package at.se2group.backend.lobby.dto

data class LobbyDeletedEvent(
    val type: String = "lobby.deleted",
    val lobbyId: String
)
```

Example JSON:

```json
{
    "type": "lobby.deleted",
    "lobbyId": "abc123"
}
```

### `LobbyStartedEvent`

```kotlin
package at.se2group.backend.lobby.dto

data class LobbyStartedEvent(
    val type: String = "lobby.started",
    val lobbyId: String,
    val matchId: String
)
```

Example JSON:

```json
{
    "type": "lobby.started",
    "lobbyId": "abc123",
    "matchId": "match-42"
}
```

## Frontend Integration Overview

The Android frontend should use WebSockets only for real-time updates.

### Recommended frontend flow

1. Use REST API to create / join / update / start / leave a lobby
2. Connect to the websocket server
3. Subscribe to `/topic/lobbies/{lobbyId}`
4. Listen for incoming events
5. Update UI state when an event arrives

This means:

- REST is used for sending actions
- WebSocket is used for receiving live shared state updates

---

## Frontend Dependencies

A common and simple Kotlin Android setup uses the STOMP client library together with OkHttp.

Example Gradle dependencies:

```kotlin
implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.moshi:moshi:1.15.1")
implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
```

If your project already uses another JSON library such as Kotlin serialization or Gson, you can keep using that instead of Moshi.

---

## Frontend Data Models

The frontend should define websocket event models matching the backend payloads.

### Example models in Kotlin

```kotlin
data class LobbyPlayerResponse(
    val userId: String,
    val displayName: String,
    val isReady: Boolean
)

data class LobbySettingsResponse(
    val maxPlayers: Int,
    val isPrivate: Boolean,
    val allowGuests: Boolean
)

data class LobbyResponse(
    val lobbyId: String,
    val hostUserId: String,
    val status: String,
    val players: List<LobbyPlayerResponse>,
    val settings: LobbySettingsResponse
)

data class LobbyUpdatedEvent(
    val type: String,
    val lobby: LobbyResponse
)

data class LobbyDeletedEvent(
    val type: String,
    val lobbyId: String
)

data class LobbyStartedEvent(
    val type: String,
    val lobbyId: String,
    val matchId: String
)
```

---

## Frontend WebSocket Service Example

A simple frontend websocket service can manage:

- connect
- disconnect
- subscribe to a lobby topic
- parse incoming events
- expose them to ViewModel or state holders

### Example `LobbyWebSocketService.kt`

```kotlin
package at.se2group.rummikub.data.websocket

import android.util.Log
import com.squareup.moshi.Moshi
import io.reactivex.disposables.Disposable
import org.java_websocket.client.WebSocketClient
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

class LobbyWebSocketService(
    private val moshi: Moshi
) {

    private var stompClient: StompClient? = null
    private var topicDisposable: Disposable? = null
    private var lifecycleDisposable: Disposable? = null

    fun connect(baseWsUrl: String) {
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "$baseWsUrl/ws"
        )

        lifecycleDisposable = stompClient
            ?.lifecycle()
            ?.subscribe(
                { event -> Log.d("LobbyWebSocket", "Lifecycle: $event") },
                { error -> Log.e("LobbyWebSocket", "Lifecycle error", error) }
            )

        stompClient?.connect()
    }

    fun disconnect() {
        topicDisposable?.dispose()
        lifecycleDisposable?.dispose()
        stompClient?.disconnect()
    }

    fun subscribeToLobby(
        lobbyId: String,
        onLobbyUpdated: (LobbyUpdatedEvent) -> Unit,
        onLobbyDeleted: (LobbyDeletedEvent) -> Unit,
        onLobbyStarted: (LobbyStartedEvent) -> Unit,
        onUnknownMessage: (String) -> Unit = {}
    ) {
        val updatedAdapter = moshi.adapter(LobbyUpdatedEvent::class.java)
        val deletedAdapter = moshi.adapter(LobbyDeletedEvent::class.java)
        val startedAdapter = moshi.adapter(LobbyStartedEvent::class.java)

        topicDisposable?.dispose()
        topicDisposable = stompClient
            ?.topic("/topic/lobbies/$lobbyId")
            ?.subscribe(
                { topicMessage ->
                    val payload = topicMessage.payload

                    when {
                        payload.contains("\"type\":\"lobby.updated\"") -> {
                            updatedAdapter.fromJson(payload)?.let(onLobbyUpdated)
                        }

                        payload.contains("\"type\":\"lobby.deleted\"") -> {
                            deletedAdapter.fromJson(payload)?.let(onLobbyDeleted)
                        }

                        payload.contains("\"type\":\"lobby.started\"") -> {
                            startedAdapter.fromJson(payload)?.let(onLobbyStarted)
                        }

                        else -> onUnknownMessage(payload)
                    }
                },
                { error ->
                    Log.e("LobbyWebSocket", "Topic error", error)
                }
            )
    }
}
```

---

## Simpler Frontend Alternative

If you want to keep the first version even simpler, only support `lobby.updated` at first.

### Minimal version

```kotlin
class LobbyWebSocketService(
    private val moshi: Moshi
) {

    private var stompClient: StompClient? = null
    private var topicDisposable: Disposable? = null

    fun connect(baseWsUrl: String) {
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "$baseWsUrl/ws"
        )
        stompClient?.connect()
    }

    fun subscribeToLobby(
        lobbyId: String,
        onLobbyUpdated: (LobbyUpdatedEvent) -> Unit
    ) {
        val adapter = moshi.adapter(LobbyUpdatedEvent::class.java)

        topicDisposable = stompClient
            ?.topic("/topic/lobbies/$lobbyId")
            ?.subscribe { message ->
                adapter.fromJson(message.payload)?.let(onLobbyUpdated)
            }
    }

    fun disconnect() {
        topicDisposable?.dispose()
        stompClient?.disconnect()
    }
}
```

This is a very good first frontend implementation for the team.

---

## ViewModel Usage Example

The ViewModel should connect the websocket stream to UI state.

### Example `LobbyViewModel.kt`

```kotlin
package at.se2group.rummikub.feature.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LobbyViewModel(
    private val lobbyWebSocketService: LobbyWebSocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    fun connectToLobby(baseWsUrl: String, lobbyId: String) {
        lobbyWebSocketService.connect(baseWsUrl)
        lobbyWebSocketService.subscribeToLobby(
            lobbyId = lobbyId,
            onLobbyUpdated = { event ->
                _uiState.value = _uiState.value.copy(
                    lobby = event.lobby,
                    isDeleted = false,
                    isStarted = false
                )
            },
            onLobbyDeleted = {
                _uiState.value = _uiState.value.copy(isDeleted = true)
            },
            onLobbyStarted = {
                _uiState.value = _uiState.value.copy(isStarted = true)
            }
        )
    }

    override fun onCleared() {
        lobbyWebSocketService.disconnect()
        super.onCleared()
    }
}
```

### Example `LobbyUiState.kt`

```kotlin
data class LobbyUiState(
    val lobby: LobbyResponse? = null,
    val isDeleted: Boolean = false,
    val isStarted: Boolean = false
)
```

---

## Compose UI Example

The screen can observe the ViewModel state and react to websocket changes.

```kotlin
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    onLobbyDeleted: () -> Unit,
    onLobbyStarted: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.value.isDeleted) {
        if (uiState.value.isDeleted) {
            onLobbyDeleted()
        }
    }

    LaunchedEffect(uiState.value.isStarted) {
        if (uiState.value.isStarted) {
            onLobbyStarted()
        }
    }

    val lobby = uiState.value.lobby

    if (lobby == null) {
        Text("Loading lobby...")
        return
    }

    Column {
        Text("Lobby ${lobby.lobbyId}")
        Text("Status: ${lobby.status}")
        Text("Players:")

        lobby.players.forEach { player ->
            Text("- ${player.displayName} (ready: ${player.isReady})")
        }
    }
}
```

---

## Recommended Frontend Usage Pattern

For the frontend team, this is the easiest and cleanest pattern:

### After create lobby

1. call REST `POST /api/lobbies`
2. receive the created `LobbyResponse`
3. open the lobby screen
4. connect websocket
5. subscribe to `/topic/lobbies/{lobbyId}`

### After join lobby

1. call REST `POST /api/lobbies/{lobbyId}/join`
2. receive current `LobbyResponse`
3. open the lobby screen
4. connect websocket
5. subscribe to `/topic/lobbies/{lobbyId}`

### While inside lobby screen

- keep websocket subscription active
- update UI from `lobby.updated`
- navigate away on `lobby.deleted`
- navigate to game screen on `lobby.started`

### When leaving screen

- unsubscribe / disconnect websocket

---

## Important Notes for Frontend Team

### 1. REST still stays important

Do not replace REST with WebSockets.

Use:

- REST to perform actions
- WebSocket to receive state changes

### 2. Use the full lobby from the event

For `lobby.updated`, the backend sends the latest full lobby state.

That means the frontend can usually replace the current lobby UI state directly with the new `event.lobby` object.

### 3. Keep websocket code outside UI layer

Do not put STOMP connection logic directly inside Compose screens.

Keep it inside:

- data layer service
- repository
- ViewModel

### 4. Reconnect strategy can be added later

For the first version, a simple connect / disconnect approach is enough.

Automatic reconnect can be added later if needed.
