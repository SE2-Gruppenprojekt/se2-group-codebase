# Lobby Architecture

## Purpose

This document defines the lobby architecture for the Rummikub system.

The lobby is the waiting room before a match starts. It owns player presence,
ready state, lobby settings, and the handoff into game initialization. It does
not own turn logic, board validation, or scoring once the game has started.

In short:

- lobby = pre-game coordination
- game = actual match state and gameplay

---

## Scope

The lobby should support:

- create a lobby
- fetch a lobby by ID
- list open lobbies
- join a lobby
- leave a lobby
- mark a player as ready
- mark a player as unready
- update lobby settings as host
- start a match as host
- broadcast lobby changes to connected clients

The lobby should not contain Rummikub gameplay rules beyond the checks needed
to decide whether a lobby may start.

---

## Main Rules

### General rules

- a lobby can only be joined when its status is `OPEN`
- a lobby cannot exceed its maximum player count
- the same player cannot join the same lobby twice
- only players inside the lobby can ready or unready themselves
- only the host can update settings
- only the host can start the match
- a match can only start if enough players joined
- a match can only start if all players are ready

### First-version defaults

- minimum players to start: `2`
- maximum players: configurable, default `4`
- lobby status values:
  - `OPEN`
  - `IN_GAME`
  - `CLOSED`
- all players must be ready before the host can start
- if the host leaves, host ownership transfers to the oldest remaining player
- if the last player leaves, the lobby is deleted or marked closed

---

## Backend Structure

```text
lobby/
├── api/
│   └── LobbyController.kt
├── service/
│   ├── LobbyService.kt
│   └── LobbyBroadcastService.kt
├── domain/
│   ├── Lobby.kt
│   ├── LobbyPlayer.kt
│   ├── LobbySettings.kt
│   └── LobbyStatus.kt
├── dto/
│   ├── CreateLobbyRequest.kt
│   ├── JoinLobbyRequest.kt
│   ├── UpdateLobbySettingsRequest.kt
│   ├── LobbyResponse.kt
│   ├── LobbyPlayerResponse.kt
│   ├── LobbyListItemResponse.kt
│   └── LobbyEvent.kt
├── persistence/
│   ├── LobbyEntity.kt
│   ├── LobbyPlayerEmbeddable.kt
│   └── LobbyRepository.kt
└── mapper/
    └── LobbyMapper.kt
```

This keeps the lobby feature isolated from the game module and gives it a
clear handoff boundary.

---

## Frontend Structure

The Android app should mirror the same separation conceptually.

```text
apps/android/.../lobby/
├── api/
│   ├── LobbyApiService.kt
│   ├── dto/
│   │   ├── CreateLobbyRequestDto.kt
│   │   ├── JoinLobbyRequestDto.kt
│   │   ├── UpdateLobbySettingsRequestDto.kt
│   │   ├── LobbyResponseDto.kt
│   │   ├── LobbyListItemResponseDto.kt
│   │   └── LobbyEventDto.kt
├── data/
│   ├── LobbyRepository.kt
│   ├── LobbyRepositoryImpl.kt
│   └── mapper/
├── model/
│   ├── LobbyUiModel.kt
│   ├── LobbyPlayerUiModel.kt
│   ├── LobbySettingsUiModel.kt
│   └── LobbyStatusUiModel.kt
├── websocket/
│   ├── LobbySocketClient.kt
│   └── LobbyEventHandler.kt
├── presentation/
│   ├── LobbyListViewModel.kt
│   ├── LobbyDetailViewModel.kt
│   ├── CreateLobbyViewModel.kt
│   ├── LobbyListScreen.kt
│   ├── LobbyDetailScreen.kt
│   └── components/
└── navigation/
    └── LobbyNavigation.kt
```

### Frontend responsibilities

- `api/`: REST endpoints and network DTOs
- `data/`: repository orchestration and mapping
- `model/`: UI-facing state
- `websocket/`: realtime lobby subscriptions
- `presentation/`: screens, state, user actions
- `navigation/`: lobby routes and argument passing

### Model separation

The frontend should not directly reuse backend entities.

Recommended split:

- backend entities: persistence only
- backend DTOs: API contract
- frontend DTOs: transport models
- frontend UI models: screen state

If needed later, a small shared API contract module can be introduced for
stable request and response shapes only.

---

## Domain Model

### `LobbyStatus`

```kotlin
enum class LobbyStatus {
    OPEN,
    IN_GAME,
    CLOSED
}
```

### `LobbyPlayer`

```kotlin
data class LobbyPlayer(
    val userId: String,
    val displayName: String,
    val isReady: Boolean = false,
    val joinedAt: Instant = Instant.now()
)
```

### `LobbySettings`

```kotlin
data class LobbySettings(
    val maxPlayers: Int = 4,
    val isPrivate: Boolean = false,
    val allowGuests: Boolean = true
)
```

### `Lobby`

```kotlin
data class Lobby(
    val lobbyId: String,
    val hostUserId: String,
    val players: List<LobbyPlayer>,
    val status: LobbyStatus = LobbyStatus.OPEN,
    val settings: LobbySettings = LobbySettings(),
    val createdAt: Instant = Instant.now()
)
```

---

## API Contract

DTOs should exist even in a monorepo. The backend contract should stay stable
without exposing backend persistence models directly.

### Requests

```kotlin
data class CreateLobbyRequest(
    val displayName: String,
    val maxPlayers: Int = 4,
    val isPrivate: Boolean = false,
    val allowGuests: Boolean = true
)

data class JoinLobbyRequest(
    val userId: String,
    val displayName: String
)

data class UpdateLobbySettingsRequest(
    val maxPlayers: Int,
    val isPrivate: Boolean,
    val allowGuests: Boolean
)
```

### Responses

```kotlin
data class LobbyPlayerResponse(
    val userId: String,
    val displayName: String,
    val isReady: Boolean
)

data class LobbyResponse(
    val lobbyId: String,
    val hostUserId: String,
    val status: String,
    val maxPlayers: Int,
    val isPrivate: Boolean,
    val allowGuests: Boolean,
    val players: List<LobbyPlayerResponse>
)

data class LobbyListItemResponse(
    val lobbyId: String,
    val hostUserId: String,
    val status: String,
    val currentPlayerCount: Int,
    val maxPlayers: Int,
    val isPrivate: Boolean
)
```

### WebSocket events

```kotlin
data class LobbyUpdatedEvent(
    val type: String = "lobby.updated",
    val lobby: LobbyResponse
)

data class LobbyDeletedEvent(
    val type: String = "lobby.deleted",
    val lobbyId: String
)

data class LobbyStartedEvent(
    val type: String = "lobby.started",
    val lobbyId: String,
    val matchId: String
)
```

---

## Mapper Helpers

```kotlin
fun Lobby.toResponse(): LobbyResponse =
    LobbyResponse(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status.name,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate,
        allowGuests = settings.allowGuests,
        players = players.map {
            LobbyPlayerResponse(
                userId = it.userId,
                displayName = it.displayName,
                isReady = it.isReady
            )
        }
    )

fun Lobby.toListItemResponse(): LobbyListItemResponse =
    LobbyListItemResponse(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status.name,
        currentPlayerCount = players.size,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate
    )
```

---

## REST API

### Endpoints

```text
POST   /api/lobbies
GET    /api/lobbies
GET    /api/lobbies/{lobbyId}
POST   /api/lobbies/{lobbyId}/join
POST   /api/lobbies/{lobbyId}/leave
POST   /api/lobbies/{lobbyId}/ready
POST   /api/lobbies/{lobbyId}/unready
PATCH  /api/lobbies/{lobbyId}/settings
POST   /api/lobbies/{lobbyId}/start
DELETE /api/lobbies/{lobbyId}
```

### Endpoint responsibilities

- `POST /api/lobbies`: create lobby and add host as first player
- `GET /api/lobbies`: list open lobbies
- `GET /api/lobbies/{lobbyId}`: fetch lobby state
- `POST /api/lobbies/{lobbyId}/join`: add player
- `POST /api/lobbies/{lobbyId}/leave`: remove player
- `POST /api/lobbies/{lobbyId}/ready`: mark player ready
- `POST /api/lobbies/{lobbyId}/unready`: remove ready state
- `PATCH /api/lobbies/{lobbyId}/settings`: update settings as host
- `POST /api/lobbies/{lobbyId}/start`: start match as host
- `DELETE /api/lobbies/{lobbyId}`: close or delete lobby as host

---

## Persistence Design

The lobby can start with a repository-backed design instead of an in-memory
prototype.

### Suggested approach

- one `LobbyEntity`
- embedded or child player rows for `LobbyPlayer`
- columns for host, status, and settings

### Example JPA classes

#### `LobbyPlayerEmbeddable`

```kotlin
@Embeddable
data class LobbyPlayerEmbeddable(
    @Column(name = "user_id")
    var userId: String = "",

    @Column(name = "display_name")
    var displayName: String = "",

    @Column(name = "is_ready")
    var isReady: Boolean = false,

    @Column(name = "joined_at")
    var joinedAt: Instant = Instant.now()
)
```

#### `LobbyEntity`

```kotlin
@Entity
@Table(name = "lobbies")
data class LobbyEntity(
    @Id
    @Column(name = "lobby_id")
    var lobbyId: String = "",

    @Column(name = "host_user_id", nullable = false)
    var hostUserId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: LobbyStatus = LobbyStatus.OPEN,

    @Column(name = "max_players", nullable = false)
    var maxPlayers: Int = 4,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @Column(name = "allow_guests", nullable = false)
    var allowGuests: Boolean = true,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "lobby_players",
        joinColumns = [JoinColumn(name = "lobby_id")]
    )
    var players: MutableList<LobbyPlayerEmbeddable> = mutableListOf(),

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
```

#### `LobbyRepository`

```kotlin
interface LobbyRepository : JpaRepository<LobbyEntity, String> {
    fun findAllByStatus(status: LobbyStatus): List<LobbyEntity>
}
```

### Entity-domain mapping

```kotlin
fun LobbyEntity.toDomain(): Lobby =
    Lobby(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        players = players.map {
            LobbyPlayer(
                userId = it.userId,
                displayName = it.displayName,
                isReady = it.isReady,
                joinedAt = it.joinedAt
            )
        },
        status = status,
        settings = LobbySettings(
            maxPlayers = maxPlayers,
            isPrivate = isPrivate,
            allowGuests = allowGuests
        ),
        createdAt = createdAt
    )

fun Lobby.toEntity(): LobbyEntity =
    LobbyEntity(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate,
        allowGuests = settings.allowGuests,
        players = players.map {
            LobbyPlayerEmbeddable(
                userId = it.userId,
                displayName = it.displayName,
                isReady = it.isReady,
                joinedAt = it.joinedAt
            )
        }.toMutableList(),
        createdAt = createdAt
    )
```

---

## Service Responsibilities

### `LobbyService`

Owns lobby state and business rules.

Responsibilities:

- create lobby
- fetch lobby
- list open lobbies
- join lobby
- leave lobby
- ready and unready players
- update settings
- start lobby
- enforce all lobby rules
- persist changes

Important checks:

- lobby exists
- lobby is `OPEN` where required
- player is or is not already present as needed
- `maxPlayers` is respected
- caller is host where required
- enough players are present to start
- all players are ready to start

### `LobbyBroadcastService`

Owns realtime event emission only.

It should broadcast:

- lobby updated
- lobby deleted
- lobby started

It should not contain lobby rule logic.

---

## Controller Responsibilities

`LobbyController` should stay thin.

Responsibilities:

- receive HTTP requests
- validate request shape
- delegate to `LobbyService`
- map results into response DTOs
- return HTTP responses

It should not contain core lobby business rules.

---

## Live Update Design

Lobby mutations should primarily use REST, while live state updates should be
broadcast through WebSocket events.

### Typical event flow: player joins

1. client calls `POST /api/lobbies/{lobbyId}/join`
2. `LobbyService` updates and persists the lobby
3. `LobbyBroadcastService` emits `lobby.updated`
4. subscribed clients refresh local lobby state

### Typical event flow: host starts match

1. client calls `POST /api/lobbies/{lobbyId}/start`
2. `LobbyService` validates start conditions
3. game initialization is triggered
4. lobby status changes to `IN_GAME`
5. `LobbyBroadcastService` emits `lobby.started`
6. clients navigate from lobby to game

### Subscription pattern

Suggested topics:

- `/topic/lobbies` for list-level updates if needed
- `/topic/lobbies/{lobbyId}` for detailed lobby updates

### Reconnect behavior

On reconnect, clients should:

- resubscribe to the lobby topic
- refetch lobby state through REST
- treat WebSocket events as incremental updates, not the sole source of truth

---

## Error Handling

Use clear, rule-aligned failures.

Typical cases:

- lobby not found
- lobby is not open
- lobby is full
- player already in lobby
- player not in lobby
- caller is not host
- not enough players to start
- not all players are ready
- invalid settings update

The API should return predictable status codes and stable error messages for
these cases.

---

## Handoff Into Game Start

Starting a lobby is the boundary between the lobby module and the game module.

The lobby module should:

- validate that the lobby may start
- mark the lobby as `IN_GAME`
- trigger game initialization
- broadcast the resulting transition

The game module should:

- create confirmed game state
- create the initial turn draft
- own all game logic after start

---

## Recommended Implementation Order

### Backend

1. domain models and DTOs
2. persistence layer
3. `LobbyService`
4. `LobbyController`
5. `LobbyBroadcastService`
6. game-start handoff
7. tests
8. documentation cleanup

### Frontend

1. API and DTO layer
2. navigation and state setup
3. lobby list and detail UI
4. create, join, leave, ready, unready, settings flows
5. realtime updates
6. tests and polish

---

## Minimum Complete First Version

A good first complete version should include:

- lobby domain model
- persistence-backed lobby storage
- create, get, list, join, leave, ready, unready, settings, start endpoints
- lobby rule enforcement in `LobbyService`
- WebSocket broadcasts for updates and start
- controller and service tests
- clean handoff into game initialization

---

## Summary

The lobby architecture should stay narrow and explicit:

- `LobbyService` owns lobby rules and persistence coordination
- `LobbyBroadcastService` owns realtime updates
- `LobbyController` stays thin
- the lobby ends once a valid game start handoff happens

If this boundary stays clean, the lobby remains easy to implement, test, and
extend without mixing waiting-room logic with actual gameplay.
