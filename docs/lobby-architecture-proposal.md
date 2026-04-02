# Lobby Architecture and Implementation Plan

## Purpose

This document describes the full lobby architecture for the Rummikub backend and gives a practical implementation plan from start to finish. It includes:

- the role of the lobby in the overall system
- recommended backend structure
- domain model and rules
- REST endpoints
- WebSocket events for live lobby updates
- DTOs
- persistence design
- service responsibilities
- code examples for a first implementation
- the recommended next steps for turning the first version into a cleaner architecture

The goal is to keep the lobby simple, reliable, and easy to extend.

---

## Mock Design Reference

A mock design or wireframe for the lobby feature can be placed here for visual reference during implementation.

<table>
  <tr>
    <td align="center">
<img width="702" height="1616" alt="image" src="https://github.com/user-attachments/assets/c92a5e14-181d-48fe-99a4-4d3e79b91a75" />
      <sub>Lobby screen</sub>
    </td>
    <td align="center">
<img width="674" height="1546" alt="image" src="https://github.com/user-attachments/assets/7d2c5a9f-a5d0-4a97-a66e-9d5c313deb71" />
      <sub>Lobby list screen</sub>
    </td>
  </tr>
</table>




This section should help align backend and frontend implementation with the intended user flow and screen structure.

---

## What the lobby is responsible for

The lobby is the waiting room before a match starts.

It should support at least these actions:

- create a lobby
- fetch a lobby by ID
- list open lobbies
- join a lobby
- leave a lobby
- mark a player as ready
- mark a player as unready
- update lobby settings - only for host
- start a match - only for host
- broadcast lobby changes to connected clients

The lobby should **not** contain actual game logic for Rummikub turns, board validation, or scoring. Once a game starts, responsibility moves to the match or game module.

A useful rule of thumb is:

- **Lobby = waiting room**
- **Match = actual game**

_note: perhaps dont distinguish between lobby and match - use the same for everything -> lobby = match_

---

## High-level architecture

A clean first version can use these backend parts:

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

This keeps the lobby feature separated from the match feature and makes it easier to evolve later.

---

### Suggested frontend feature structure

The Android app should mirror the same separation conceptually, even if the exact package structure differs.

A clean frontend structure for the lobby feature could look like this:

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
│   ├── CreateLobbyScreen.kt
│   └── components/
└── navigation/
    └── LobbyNavigation.kt
```

### Frontend responsibilities by layer

#### `api/`

Contains the HTTP service definitions and network DTOs used to communicate with the backend.

Responsibilities:

- define Retrofit or HTTP endpoints for lobby actions
- define request and response DTOs matching the backend contract
- keep transport models separate from UI models

#### `data/`

Contains the repository and mapping logic that coordinates REST calls and WebSocket events.

Responsibilities:

- call lobby REST endpoints
- subscribe to lobby WebSocket events
- map API DTOs into frontend models
- expose a clean interface to ViewModels

#### `model/`

Contains the models that the UI actually consumes.

Responsibilities:

- represent lobby state in a frontend-friendly shape
- keep UI concerns separate from raw network payloads
- hold data used directly by screens and state containers

#### `websocket/`

Contains the socket client and event handling logic for live lobby updates.

Responsibilities:

- connect and subscribe to lobby topics
- parse lobby event payloads
- notify repository or ViewModels about updates
- handle reconnect and resubscribe behavior

#### `presentation/`

Contains ViewModels, screens, and reusable UI components.

Responsibilities:

- manage screen state
- call repository methods for user actions
- expose loading, error, and content state to the UI
- render lobby list, create lobby, and lobby detail flows

#### `navigation/`

Contains the routes and navigation wiring for the lobby flow.

Responsibilities:

- define lobby-related destinations
- pass required arguments such as `lobbyId`
- connect lobby screens to the rest of the app flow

### Frontend data model guidance

Even in a shared monorepo, the frontend should **not** directly reuse backend persistence entities.

Recommended separation:

- backend entities -> database and persistence only
- backend DTOs -> API contract
- frontend DTOs -> network transport models
- frontend UI models -> screen and ViewModel state

If duplication later becomes annoying, a small shared **API contract** module can be introduced for request and response shapes only. However, backend JPA entities and internal backend domain models should not be shared with the Android app.

This keeps the frontend decoupled from backend persistence concerns and makes both sides easier to evolve independently.

---

## Main lobby rules

The service layer should enforce these rules.

### General rules

- a lobby can only be joined when its status is `OPEN`
- a lobby cannot exceed its maximum player count
- the same player cannot join the same lobby twice
- only players inside the lobby can ready or unready themselves
- only the host can update settings
- only the host can start the match
- a match can only start if enough players joined
- a match can only start if all players are ready

### Suggested first-version rules

These are practical defaults for the first version:

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

## Domain model

The domain model should describe the lobby clearly.

### `LobbyStatus`

```kotlin
package at.se2group.backend.lobby.domain

enum class LobbyStatus {
    OPEN,
    IN_GAME,
    CLOSED
}
```

### `LobbyPlayer`

```kotlin
package at.se2group.backend.lobby.domain

import java.time.Instant

data class LobbyPlayer(
    val userId: String,
    val displayName: String,
    val isReady: Boolean = false,
    val joinedAt: Instant = Instant.now()
)
```

_note: only arbitrary settings for now - discuss what settings would be useful_

### `LobbySettings`

```kotlin
package at.se2group.backend.lobby.domain

data class LobbySettings(
    val maxPlayers: Int = 4,
    val isPrivate: Boolean = false,
    val allowGuests: Boolean = true
)
```

### `Lobby`

```kotlin
package at.se2group.backend.lobby.domain

import java.time.Instant

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

## DTOs

DTOs define the API contract between the backend and the client.

For this project, DTOs should still exist even though the backend and frontend live in the same monorepo. The frontend should not directly depend on backend persistence entities or backend-internal domain models. At most, only stable API contract models should ever be shared, while JPA entities and backend implementation models should remain backend-only.


### Requests

```kotlin
package at.se2group.backend.lobby.dto

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
package at.se2group.backend.lobby.dto

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

### WebSocket event payloads

```kotlin
package at.se2group.backend.lobby.dto

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

## Mapper helpers

It is useful to convert domain objects into response DTOs in one place.

```kotlin
package at.se2group.backend.lobby.mapper

import at.se2group.backend.lobby.domain.Lobby
import at.se2group.backend.lobby.dto.LobbyListItemResponse
import at.se2group.backend.lobby.dto.LobbyPlayerResponse
import at.se2group.backend.lobby.dto.LobbyResponse

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

## REST API design

The lobby should mainly use REST for lobby mutations and retrieval.

### Recommended endpoints

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

### Responsibility of each endpoint

- `POST /api/lobbies` creates a new lobby and adds the host as the first player
- `GET /api/lobbies` lists open lobbies for browse or join screens
- `GET /api/lobbies/{lobbyId}` fetches a specific lobby state
- `POST /api/lobbies/{lobbyId}/join` adds a player to the lobby
- `POST /api/lobbies/{lobbyId}/leave` removes a player from the lobby
- `POST /api/lobbies/{lobbyId}/ready` marks the calling player as ready
- `POST /api/lobbies/{lobbyId}/unready` removes ready state
- `PATCH /api/lobbies/{lobbyId}/settings` updates lobby settings, host only
- `POST /api/lobbies/{lobbyId}/start` starts the match, host only
- `DELETE /api/lobbies/{lobbyId}` closes the lobby, host only

---

## Persistence design

The first sample implementation used an in-memory `ConcurrentHashMap`. That is fine for a prototype, but the next step should be a repository-backed implementation.

### Suggested persistence approach

A practical first database design is:

- one `LobbyEntity`
- embedded or child player rows for `LobbyPlayer`
- columns for host, status, and settings

You can keep this fairly simple.

### Example JPA persistence classes

#### `LobbyPlayerEmbeddable`

```kotlin
package at.se2group.backend.lobby.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.Instant

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
package at.se2group.backend.lobby.persistence

import at.se2group.backend.lobby.domain.LobbyStatus
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "lobbies")
class LobbyEntity(
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

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @ElementCollection
    @CollectionTable(name = "lobby_players", joinColumns = [JoinColumn(name = "lobby_id")])
    var players: MutableList<LobbyPlayerEmbeddable> = mutableListOf()
)
```

#### `LobbyRepository`

```kotlin
package at.se2group.backend.lobby.persistence

import at.se2group.backend.lobby.domain.LobbyStatus
import org.springframework.data.jpa.repository.JpaRepository

interface LobbyRepository : JpaRepository<LobbyEntity, String> {
    fun findAllByStatus(status: LobbyStatus): List<LobbyEntity>
}
```

---

## Entity-domain mapping

Because entities and domain objects serve different purposes, it is better to map between them.

```kotlin
package at.se2group.backend.lobby.mapper

import at.se2group.backend.lobby.domain.Lobby
import at.se2group.backend.lobby.domain.LobbyPlayer
import at.se2group.backend.lobby.domain.LobbySettings
import at.se2group.backend.lobby.persistence.LobbyEntity
import at.se2group.backend.lobby.persistence.LobbyPlayerEmbeddable

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
        createdAt = createdAt,
        players = players.map {
            LobbyPlayerEmbeddable(
                userId = it.userId,
                displayName = it.displayName,
                isReady = it.isReady,
                joinedAt = it.joinedAt
            )
        }.toMutableList()
    )
```

---

## Lobby service responsibilities

The `LobbyService` should contain the main business logic for the lobby.

### Service responsibilities

- create lobby
- fetch lobby by ID
- list open lobbies
- join lobby
- leave lobby
- mark ready
- mark unready
- update settings
- start match
- delete lobby
- call broadcast service when state changes

### Important checks

The service should enforce:

- status must be `OPEN` before joining or settings updates
- player cannot join twice
- lobby must not be full
- only host can update settings
- only host can start the match
- all players must be ready before start
- at least 2 players before start

---

## LobbyBroadcastService for live updates

Even if lobby actions use REST, it is useful to notify all connected lobby screens about changes through WebSocket.

### What it should broadcast

- player joined
- player left
- ready state changed
- settings changed
- lobby deleted
- lobby started

### Example implementation

```kotlin
package at.se2group.backend.lobby.service

import at.se2group.backend.lobby.dto.LobbyDeletedEvent
import at.se2group.backend.lobby.dto.LobbyStartedEvent
import at.se2group.backend.lobby.dto.LobbyUpdatedEvent
import at.se2group.backend.lobby.mapper.toResponse
import at.se2group.backend.lobby.domain.Lobby
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class LobbyBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {

    fun broadcastLobbyUpdated(lobby: Lobby) {
        messagingTemplate.convertAndSend(
            "/topic/lobbies/${lobby.lobbyId}",
            LobbyUpdatedEvent(lobby = lobby.toResponse())
        )
    }

    fun broadcastLobbyDeleted(lobbyId: String) {
        messagingTemplate.convertAndSend(
            "/topic/lobbies/$lobbyId",
            LobbyDeletedEvent(lobbyId = lobbyId)
        )
    }

    fun broadcastLobbyStarted(lobbyId: String, matchId: String) {
        messagingTemplate.convertAndSend(
            "/topic/lobbies/$lobbyId",
            LobbyStartedEvent(lobbyId = lobbyId, matchId = matchId)
        )
    }
}
```

Clients can subscribe to:

```text
/topic/lobbies/{lobbyId}
```

That allows lobby screens to update live without polling.

---

## Full example service implementation

This version replaces the original `ConcurrentHashMap` with a repository-backed service and includes:

- unready
- updateSettings
- list endpoint support
- LobbyBroadcastService integration

```kotlin
package at.se2group.backend.lobby.service

import at.se2group.backend.lobby.domain.Lobby
import at.se2group.backend.lobby.domain.LobbyPlayer
import at.se2group.backend.lobby.domain.LobbySettings
import at.se2group.backend.lobby.domain.LobbyStatus
import at.se2group.backend.lobby.dto.CreateLobbyRequest
import at.se2group.backend.lobby.dto.JoinLobbyRequest
import at.se2group.backend.lobby.dto.UpdateLobbySettingsRequest
import at.se2group.backend.lobby.mapper.toDomain
import at.se2group.backend.lobby.mapper.toEntity
import at.se2group.backend.lobby.persistence.LobbyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class LobbyService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyBroadcastService: LobbyBroadcastService
) {

    fun createLobby(hostUserId: String, request: CreateLobbyRequest): Lobby {
        // evtl. falsch
        //require(request.maxPlayers >= 2) { "maxPlayers must be at least 2" }

        val lobby = Lobby(
            lobbyId = UUID.randomUUID().toString(),
            hostUserId = hostUserId,
            players = listOf(
                LobbyPlayer(
                    userId = hostUserId,
                    displayName = request.displayName,
                    isReady = false,
                    joinedAt = Instant.now()
                )
            ),
            settings = LobbySettings(
                maxPlayers = request.maxPlayers,
                isPrivate = request.isPrivate,
                allowGuests = request.allowGuests
            )
        )

        val saved = lobbyRepository.save(lobby.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    @Transactional(readOnly = true)
    fun getLobby(lobbyId: String): Lobby {
        return lobbyRepository.findById(lobbyId)
            .orElseThrow { IllegalArgumentException("Lobby not found") }
            .toDomain()
    }

    @Transactional(readOnly = true)
    fun listOpenLobbies(): List<Lobby> {
        return lobbyRepository.findAllByStatus(LobbyStatus.OPEN)
            .map { it.toDomain() }
    }

    fun joinLobby(lobbyId: String, request: JoinLobbyRequest): Lobby {
        val lobby = getLobby(lobbyId)

        require(lobby.status == LobbyStatus.OPEN) { "Lobby is not open" }
        require(lobby.players.none { it.userId == request.userId }) { "Player already in lobby" }
        require(lobby.players.size < lobby.settings.maxPlayers) { "Lobby is full" }

        val updated = lobby.copy(
            players = lobby.players + LobbyPlayer(
                userId = request.userId,
                displayName = request.displayName,
                isReady = false,
                joinedAt = Instant.now()
            )
        )

        val saved = lobbyRepository.save(updated.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    fun leaveLobby(lobbyId: String, userId: String): Lobby? {
        val lobby = getLobby(lobbyId)
        val remainingPlayers = lobby.players.filterNot { it.userId == userId }

        if (remainingPlayers.size == lobby.players.size) {
            throw IllegalArgumentException("Player is not in lobby")
        }

        if (remainingPlayers.isEmpty()) {
            lobbyRepository.deleteById(lobbyId)
            lobbyBroadcastService.broadcastLobbyDeleted(lobbyId)
            return null
        }

        val newHostUserId = if (lobby.hostUserId == userId) {
            remainingPlayers.minBy { it.joinedAt }.userId
        } else {
            lobby.hostUserId
        }

        val updated = lobby.copy(
            hostUserId = newHostUserId,
            players = remainingPlayers
        )

        val saved = lobbyRepository.save(updated.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    fun markReady(lobbyId: String, userId: String): Lobby {
        val lobby = getLobby(lobbyId)
        val updated = lobby.copy(
            players = lobby.players.map {
                if (it.userId == userId) it.copy(isReady = true) else it
            }
        )

        val saved = lobbyRepository.save(updated.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    fun markUnready(lobbyId: String, userId: String): Lobby {
        val lobby = getLobby(lobbyId)
        val updated = lobby.copy(
            players = lobby.players.map {
                if (it.userId == userId) it.copy(isReady = false) else it
            }
        )

        val saved = lobbyRepository.save(updated.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    fun updateSettings(lobbyId: String, userId: String, request: UpdateLobbySettingsRequest): Lobby {
        val lobby = getLobby(lobbyId)

        require(lobby.hostUserId == userId) { "Only host can update settings" }
        require(lobby.status == LobbyStatus.OPEN) { "Lobby is not open" }
        require(request.maxPlayers >= lobby.players.size) { "maxPlayers cannot be smaller than current player count" }
        require(request.maxPlayers >= 2) { "maxPlayers must be at least 2" }

        val updated = lobby.copy(
            settings = LobbySettings(
                maxPlayers = request.maxPlayers,
                isPrivate = request.isPrivate,
                allowGuests = request.allowGuests
            )
        )

        val saved = lobbyRepository.save(updated.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    fun startMatch(lobbyId: String, userId: String): Lobby {
        val lobby = getLobby(lobbyId)

        require(lobby.hostUserId == userId) { "Only host can start" }
        require(lobby.players.size >= 2) { "At least 2 players required" }
        require(lobby.players.all { it.isReady }) { "All players must be ready" }
        require(lobby.status == LobbyStatus.OPEN) { "Lobby is not open" }

        val updated = lobby.copy(status = LobbyStatus.IN_GAME)
        val saved = lobbyRepository.save(updated.toEntity()).toDomain()

        val matchId = UUID.randomUUID().toString()
        lobbyBroadcastService.broadcastLobbyStarted(lobbyId, matchId)
        lobbyBroadcastService.broadcastLobbyUpdated(saved)

        return saved
    }

    fun deleteLobby(lobbyId: String, userId: String) {
        val lobby = getLobby(lobbyId)
        require(lobby.hostUserId == userId) { "Only host can delete lobby" }

        lobbyRepository.deleteById(lobbyId)
        lobbyBroadcastService.broadcastLobbyDeleted(lobbyId)
    }
}
```

---

## Controller implementation

This controller exposes all core lobby endpoints.

```kotlin
package at.se2group.backend.lobby.api

import at.se2group.backend.lobby.dto.CreateLobbyRequest
import at.se2group.backend.lobby.dto.JoinLobbyRequest
import at.se2group.backend.lobby.dto.LobbyListItemResponse
import at.se2group.backend.lobby.dto.LobbyResponse
import at.se2group.backend.lobby.dto.UpdateLobbySettingsRequest
import at.se2group.backend.lobby.mapper.toListItemResponse
import at.se2group.backend.lobby.mapper.toResponse
import at.se2group.backend.lobby.service.LobbyService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/lobbies")
class LobbyController(
    private val lobbyService: LobbyService
) {

    @PostMapping
    fun createLobby(
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: CreateLobbyRequest
    ): LobbyResponse {
        return lobbyService.createLobby(userId, request).toResponse()
    }

    @GetMapping
    fun listLobbies(): List<LobbyListItemResponse> {
        return lobbyService.listOpenLobbies().map { it.toListItemResponse() }
    }

    @GetMapping("/{lobbyId}")
    fun getLobby(@PathVariable lobbyId: String): LobbyResponse {
        return lobbyService.getLobby(lobbyId).toResponse()
    }

    @PostMapping("/{lobbyId}/join")
    fun joinLobby(
        @PathVariable lobbyId: String,
        @RequestBody request: JoinLobbyRequest
    ): LobbyResponse {
        return lobbyService.joinLobby(lobbyId, request).toResponse()
    }

    @PostMapping("/{lobbyId}/leave")
    fun leaveLobby(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Any> {
        val lobby = lobbyService.leaveLobby(lobbyId, userId)
        return if (lobby == null) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(lobby.toResponse())
        }
    }

    @PostMapping("/{lobbyId}/ready")
    fun markReady(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String
    ): LobbyResponse {
        return lobbyService.markReady(lobbyId, userId).toResponse()
    }

    @PostMapping("/{lobbyId}/unready")
    fun markUnready(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String
    ): LobbyResponse {
        return lobbyService.markUnready(lobbyId, userId).toResponse()
    }

    @PatchMapping("/{lobbyId}/settings")
    fun updateSettings(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: UpdateLobbySettingsRequest
    ): LobbyResponse {
        return lobbyService.updateSettings(lobbyId, userId, request).toResponse()
    }

    @PostMapping("/{lobbyId}/start")
    fun startMatch(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String
    ): LobbyResponse {
        return lobbyService.startMatch(lobbyId, userId).toResponse()
    }

    @DeleteMapping("/{lobbyId}")
    fun deleteLobby(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        lobbyService.deleteLobby(lobbyId, userId)
        return ResponseEntity.noContent().build()
    }
}
```

---

## Live lobby update flow

The REST API performs the mutation, and the broadcast service pushes the change.

### Example flow: player joins lobby

1. client calls `POST /api/lobbies/{lobbyId}/join`
2. `LobbyController` forwards request to `LobbyService`
3. `LobbyService` validates lobby state and adds the player
4. updated lobby is saved through `LobbyRepository`
5. `LobbyBroadcastService` publishes `lobby.updated`
6. all clients subscribed to `/topic/lobbies/{lobbyId}` receive the new state

### Example flow: host starts match

1. client calls `POST /api/lobbies/{lobbyId}/start`
2. `LobbyService` checks host ownership, readiness, and minimum players
3. lobby status becomes `IN_GAME`
4. match creation is triggered or delegated to the match module
5. `LobbyBroadcastService` publishes `lobby.started`
6. clients navigate from lobby screen to match screen

---

## Suggested WebSocket subscription pattern

For live lobby updates, clients can subscribe to:

```text
/topic/lobbies/{lobbyId}
```

Possible event types:

- `lobby.updated`
- `lobby.deleted`
- `lobby.started`

This is enough for a first version. You do not need many more event types unless the feature becomes more advanced.

### Disconnect and reconnect behavior

If a client disconnects from the lobby WebSocket subscription, this should **not** automatically remove the player from the lobby.

For the first version, lobby membership should only change through explicit REST endpoints such as:

- `POST /api/lobbies/{lobbyId}/leave`
- `DELETE /api/lobbies/{lobbyId}`

A temporary socket disconnect should be treated as a lost live connection, not as a lobby leave.

When the client reconnects, it should:

1. reconnect the WebSocket connection
2. resubscribe to `/topic/lobbies/{lobbyId}`
3. refetch `GET /api/lobbies/{lobbyId}` once to restore the latest authoritative state

This keeps the first-version lobby behavior simple and avoids incorrectly removing players because of short network interruptions.

---

## Error handling recommendations

The lobby service should throw stable exceptions or use a common error model.

Example failure cases:

- lobby not found
- lobby full
- lobby not open
- player already joined
- only host can update settings
- only host can start match
- all players must be ready
- minimum player count not met

A shared exception structure can later map these into clean API responses such as:

```json
{
    "code": "LOBBY_FULL",
    "message": "Lobby is full"
}
```

---

## Recommended implementation order

This is a practical step-by-step implementation plan.

### Phase 1: domain and DTOs

Implement:

- `LobbyStatus`
- `LobbyPlayer`
- `LobbySettings`
- `Lobby`
- request and response DTOs
- mapper helpers

### Phase 2: persistence

Implement:

- `LobbyEntity`
- `LobbyPlayerEmbeddable`
- `LobbyRepository`
- entity-domain mapping

### Phase 3: service logic

Implement:

- `createLobby`
- `getLobby`
- `listOpenLobbies`
- `joinLobby`
- `leaveLobby`
- `markReady`
- `markUnready`
- `updateSettings`
- `startMatch`
- `deleteLobby`

### Phase 4: REST controller

Implement all endpoints in `LobbyController`.

### Phase 5: live updates

Implement:

- `LobbyBroadcastService`
- topic naming convention
- event DTOs
- frontend subscription handling

### Phase 6: match handoff

When start is called:

- create a match from the lobby players and settings
- return or publish the new `matchId`
- move users from lobby screen to match screen

---

## Good next steps after the first complete version

Once the above is working, good follow-up improvements are:

- add validation annotations on request DTOs
- replace header-based identity with the project-wide guest-user identity mechanism
- add dedicated error codes and exception handlers
- add host transfer tests
- add service unit tests for lobby rules
- add integration tests for REST endpoints
- add pagination or filters for lobby listing if needed
- optionally add invite codes for private lobbies later

---

## Minimum complete first version checklist

A good complete first version of lobby functionality should include all of these:

- [ ] create lobby
- [ ] fetch lobby by ID
- [ ] list open lobbies
- [ ] join lobby
- [ ] leave lobby
- [ ] ready
- [ ] unready
- [ ] update settings
- [ ] host transfer on leave
- [ ] start match
- [ ] delete lobby
- [ ] repository-backed persistence
- [ ] DTO mapping
- [ ] live lobby updates via `LobbyBroadcastService`
- [ ] basic error handling

---

## Recommended Backend Lobby Issue Order

The backend lobby feature should be implemented in a layered order so that planning and core models come first, service logic is built on top of stable persistence and DTO structures, and live updates, testing, and documentation are added afterward.

### Best implementation order

#### Phase 0: Planning (Can be split up and assigned to other phases when actually needed)

1. `docs(backend)(lobby): plan lobby architecture and implementation`

This issue defines the technical structure, rules, responsibilities, persistence approach, API design, and overall implementation strategy for the backend lobby feature.

#### Phase 1: Core Models and Contracts

2. `feat(backend)(lobby): add lobby domain models and status enums`
3. `feat(backend)(lobby): add lobby request and response DTOs`
4. `feat(backend)(lobby): add lobby domain and response mappers`

These issues define the core domain layer, transport contracts, and mapping layer that the rest of the backend depends on.

#### Phase 2: Persistence Layer

5. `feat(backend)(lobby): add lobby persistence entities`
6. `feat(backend)(lobby): add lobby repository for database access`

These issues add the storage model and repository access needed by the service layer.

#### Phase 3: Core Service Logic

7. `feat(backend)(lobby): implement create lobby service logic`
8. `feat(backend)(lobby): implement get lobby by id use case`
9. `feat(backend)(lobby): implement open lobby listing`
10. `feat(backend)(lobby): implement join lobby use case`
11. `feat(backend)(lobby): implement leave lobby and host transfer logic`
12. `feat(backend)(lobby): implement mark player as ready`
13. `feat(backend)(lobby): implement mark player as unready`
14. `feat(backend)(lobby): implement lobby settings update for host`
15. `feat(backend)(lobby): implement lobby start match validation and state transition`
16. `feat(backend)(lobby): implement delete lobby use case`

This phase builds the complete backend lobby behavior in the service layer, including all main rules and state transitions.

#### Phase 4: REST API

17. `feat(backend)(lobby): add lobby REST controller endpoints`

This issue exposes the completed lobby service logic through HTTP endpoints for create, list, fetch, join, leave, ready, unready, settings update, start, and delete operations.

#### Phase 5: Live Lobby Updates

18. `feat(backend)(lobby): add websocket lobby broadcast service`
19. `feat(backend)(lobby): add lobby websocket event payloads`

These issues add live update support so connected clients can receive lobby changes without polling.

#### Phase 6: Error Handling and Match Handoff

20. `feat(backend)(lobby): add lobby-specific error handling and validation responses`
21. `feat(backend)(lobby): add handoff from lobby start to match creation`

These issues improve backend robustness and connect the lobby flow to the actual match lifecycle.

#### Phase 7: Testing

22. `test(backend)(lobby): add unit tests for lobby service rules`
23. `test(backend)(lobby): add integration tests for lobby REST endpoints`
24. `test(backend)(lobby): add tests for lobby websocket broadcast behavior`

These issues verify service rules, REST integration, and live update behavior.

#### Phase 8: Documentation

25. `docs(backend)(lobby): document lobby architecture and implementation flow`

This issue should be completed last so the documentation reflects the final implementation and not just the planned structure.

---

### Simple dependency chain

```text
Planning
-> Domain / DTOs / Mappers
-> Persistence
-> Service Logic
-> REST Controller
-> WebSocket Updates
-> Error Handling + Match Handoff
-> Tests
-> Documentation
```

This backend order should be followed based on implementation dependency, not raw GitHub issue number order.

## Recommended Frontend Lobby Issue Order

The Android lobby feature should be implemented in a layered order so that the technical foundations come first, the core REST-based flow works next, and live updates, polish, testing, and documentation are added afterward.

### Best implementation order

#### Phase 1: Foundation

1. `feat(android)(lobby): add lobby domain and UI models`
2. `feat(android)(lobby): add lobby API DTOs and mapping`
3. `feat(android)(lobby): add lobby API service definitions`
4. `feat(android)(lobby): add lobby websocket event models`
5. `feat(android)(lobby): add lobby repository`

These issues define the core data models, networking contracts, and repository layer that the rest of the frontend depends on.

#### Phase 2: Navigation and State Management

6. `feat(android)(lobby): add lobby navigation routes`
7. `feat(android)(lobby): add lobby viewmodel for list screen`
8. `feat(android)(lobby): add lobby viewmodel for detail screen`

These issues prepare the screen flow and state handling for the actual lobby UI.

#### Phase 3: Core UI

9. `feat(android)(lobby): implement lobby list screen UI`
10. `feat(android)(lobby): implement create lobby screen UI`
11. `feat(android)(lobby): implement lobby detail screen UI`
12. `feat(android)(lobby): implement lobby player list component`
13. `feat(android)(lobby): implement lobby settings section UI`

These issues build the visible interface on top of the prepared models and ViewModels.

#### Phase 4: Core Lobby Flows

14. `feat(android)(lobby): load open lobbies from backend`
15. `feat(android)(lobby): implement fetch lobby by id flow`
16. `feat(android)(lobby): implement create lobby action flow`
17. `feat(android)(lobby): implement join lobby action flow`
18. `feat(android)(lobby): implement leave lobby action flow`
19. `feat(android)(lobby): implement mark ready action flow`
20. `feat(android)(lobby): implement mark unready action flow`
21. `feat(android)(lobby): implement host-only settings update flow`
22. `feat(android)(lobby): implement host-only start match flow`
23. `feat(android)(lobby): implement delete lobby flow`

This phase completes the main REST-based lobby feature from browsing through match start or deletion.

#### Phase 5: Realtime Updates

24. `feat(android)(lobby): add websocket subscription for live lobby updates`
25. `feat(android)(lobby): handle lobby started event and navigate to match`
26. `feat(android)(lobby): handle lobby deleted event in UI`

These issues add live synchronization and event-driven transitions.

#### Phase 6: UX Polish

27. `feat(android)(lobby): add loading and error states for lobby screens`
28. `feat(android)(lobby): add host and permission-based UI states`

These issues improve usability and make the lobby behavior clearer for players and hosts.

#### Phase 7: Testing

29. `test(android)(lobby): add unit tests for lobby viewmodels`
30. `test(android)(lobby): add ui tests for lobby screens`
31. `test(android)(lobby): add integration tests for lobby repository`

These issues verify ViewModel behavior, screen flows, and backend integration logic.

#### Phase 8: Documentation

32. `docs(android)(lobby): document lobby frontend architecture and flow` `#103`

This issue should be completed after the main implementation is stable so the documentation reflects the actual frontend structure and flow.

### Why this order is recommended

This order minimizes rework and follows the real dependency structure of the Android app:

- models, DTOs, and API service definitions should exist before repository work
- repository and navigation should exist before complete ViewModels and screen flows
- screens should exist before wiring all user actions
- the REST-based flow should work before adding WebSocket updates
- testing and documentation are most useful once the feature is largely stable

### Simple dependency chain

```text
Models / DTOs / Events
-> API Service
-> Repository
-> Navigation + ViewModels
-> Screens
-> Action Flows
-> WebSocket Updates
-> UX Polish
-> Tests
-> Documentation
```

This frontend order should be followed based on implementation dependency, not raw GitHub issue number order.

## Summary

The recommended lobby implementation is:

- **REST for lobby actions**
- **WebSocket for live lobby updates**
- **repository-backed persistence instead of `ConcurrentHashMap`**
- **clear domain model for lobby, players, settings, and status**
- **service layer for lobby rules**
- **broadcast service for realtime UI updates**

This gives you a clean and realistic first version that is simple enough for a student project but structured enough to scale into the full multiplayer flow.

---

## Frontend and Backend Alignment

The backend and frontend lobby issue orders should be treated as two connected implementation tracks rather than two completely separate efforts.

A good coordination rule is:

- the backend should implement and stabilize the domain model, DTOs, REST endpoints, and basic lobby service behavior first
- the frontend should build its models, DTO mapping, repository, navigation, and UI against those stable backend contracts
- once the REST-based lobby flow works end to end, both sides can implement WebSocket-based live lobby updates
- testing and final documentation on both sides should happen after the main feature flow is stable

### Recommended cross-team implementation sequence

1. backend planning, models, DTOs, persistence, and service logic
2. backend REST controller endpoints
3. frontend models, API DTO mapping, API service definitions, and repository
4. frontend lobby screens and action flows against the finished REST endpoints
5. backend lobby broadcast service and websocket event payloads
6. frontend websocket subscription and live event handling
7. backend and frontend testing
8. backend and frontend documentation cleanup and final alignment

### Practical takeaway

The safest implementation path is:

- **backend first for contracts and core behavior**
- **frontend next for screen flow and REST integration**
- **then realtime updates on both sides**

This reduces blocking, avoids repeated contract changes, and makes it easier for both parts of the project to progress in parallel with clear handoff points.
