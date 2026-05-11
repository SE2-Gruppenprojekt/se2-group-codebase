# Game / Match Architecture

## TL;DR

The backend owns the **authoritative match state** and the current **live turn draft**.

### How the game works

1. When a match starts, the backend creates the initial confirmed game state and the first player's draft.
2. The frontend loads the game and subscribes to the game websocket topic.
3. During a turn, the active player rearranges tiles on the frontend.
4. The frontend sends updated draft states to the backend.
5. The backend stores the draft and broadcasts it so all players can see the move live.
6. When the player ends their turn, the backend validates the final draft.
7. If valid, the backend turns the draft into the new confirmed game state, advances the turn, creates the next draft, and broadcasts the updated state.
8. If invalid, the backend rejects the move and the player keeps or resets the draft.

### Responsibility split

- **Frontend:** UI, drag and drop, sending draft updates, rendering confirmed and live state
- **Backend:** source of truth, rule validation, turn flow, persistence, broadcasting

In short: players edit a **shared live draft**, and the backend converts it into the **real confirmed game state** only when the move is valid and the turn is submitted.

---

## 1. Scope

This document describes the architecture for the **Rummikub match/game feature only**.

This document covers:

- confirmed game state
- live turn draft state
- rule validation
- turn flow
- REST commands
- websocket events
- frontend interaction model
- persistence strategy

---

## 2. Core Architectural Principles

### 2.1 Backend as source of truth

The backend owns:

- the confirmed game state
- the current turn
- the turn timer state
- the current player's hand
- the board state
- the draw pile
- the game rules
- the current live draft state
- all validation
- all event broadcasting

The frontend must **not** independently decide whether a move is valid.

### 2.2 Two-state model

The match uses two related but different states.

#### Confirmed game state

This is the last valid, committed state of the match.

It contains:

- the official board
- official hands
- current turn player
- turn deadline / remaining turn time
- draw pile
- scores
- game status
- winner if the game has ended

#### Live turn draft

This is the in-progress temporary state during the current player's turn.

It contains:

- the player's temporary board arrangement
- the temporary hand after moving tiles
- draft metadata such as version and status

This draft is shared live with all clients so everyone can see the rearrangement.

### 2.3 Event-driven synchronization

The system should be mostly real-time.

The backend broadcasts:

- live draft updates
- committed game updates
- turn changes
- game end events

The frontend reacts to those events and updates the UI accordingly.

### 2.4 Temporary invalid draft states are allowed

During a player's turn, the live draft may be temporarily invalid.

This is required because Rummikub moves often involve:

- taking tiles out of an existing set
- leaving a set invalid for a short time
- using those tiles somewhere else
- making the full board valid again only at the end

Because of that:

- the frontend must allow temporary invalid draft states
- the backend should usually do only minimal checks on draft updates
- the backend performs full rule validation only when the turn is submitted
- the visual arrangement of board sets on the screen may differ per client
- the visual arrangement of tiles in a player's hand may also differ per client

### 2.5 Confirmed state and draft state must stay consistent

At any moment, the backend must ensure that:

- only one active draft exists for the current game
- only the active player may update that draft
- a submitted draft is validated against the confirmed game state
- stale draft updates do not overwrite newer state

---

## 3. High-Level System Design

### Frontend responsibilities

The Android app is responsible for:

- rendering the board and tile sets
- rendering the player hand
- drag and drop / rearrangement UI
- local UI interaction state
- local arrangement of board sets on the screen
- local arrangement of tiles in the player's hand
- sending updated draft state to the backend
- sending end-turn / submit-turn commands
- observing websocket updates
- keeping the UI synchronized with backend events
- giving lightweight local validation feedback without blocking editing

Small example:

```kotlin
viewModel.updateDraft(newDraft)
viewModel.endTurn()
```

### Backend responsibilities

The backend is responsible for:

- storing confirmed match state
- storing live draft state
- enforcing turn ownership
- validating all submitted moves
- updating confirmed state
- creating the next draft
- broadcasting state changes
- handling game end and scoring
- protecting against stale or conflicting draft updates

Small example:

```kotlin
val savedDraft = turnDraftService.saveDraft(updatedDraft)
gameBroadcastService.broadcastDraftUpdated(savedDraft)
```

---

## 4. Communication Model

### REST

Keep REST small and command-oriented.

Recommended REST usage:

- initial match load
- update draft
- submit / end turn
- optional reset draft
- optional draw tile

### WebSocket

Use WebSocket for all real-time shared state.

Recommended WebSocket events:

- `game.draft.updated`
- `game.updated`
- `turn.changed`
- `game.ended`

This means:

- **REST** is used to send commands
- **WebSocket** is used to receive live shared state

### Why both are used

REST is still useful because it gives a clear request/response flow for commands such as:

- updating the draft
- ending the turn
- drawing a tile
- resetting the draft

WebSocket is used because all connected players need to see state changes immediately.

Small example:

```text
PUT  /api/games/{gameId}/draft
POST /api/games/{gameId}/end-turn
/topic/games/{gameId}
```

---

## 5. Backend Architecture

### 5.1 Package structure

```text
apps/backend/src/main/kotlin/at/se2group/backend/
├── config/
│   ├── WebSocketConfig.kt
│   └── JacksonConfig.kt
├── common/
│   ├── dto/
│   ├── exception/
│   └── util/
├── game/
│   ├── api/
│   │   └── GameController.kt
│   ├── service/
│   │   ├── GameService.kt
│   │   ├── TurnDraftService.kt
│   │   ├── GameBroadcastService.kt
│   │   ├── GameInitializationService.kt
│   │   └── ScoreService.kt
│   ├── domain/
│   │   ├── Game.kt
│   │   ├── GamePlayer.kt
│   │   ├── TurnDraft.kt
│   │   ├── Tile.kt
│   │   ├── TileColor.kt
│   │   ├── BoardSet.kt
│   │   ├── BoardSetType.kt
│   │   ├── GameStatus.kt
│   │   └── TurnDraftStatus.kt
│   ├── shared/
│   │   └── models/game/
│   │       ├── request/
│   │       │   ├── UpdateDraftRequest.kt
│   │       │   ├── EndTurnRequest.kt
│   │       │   └── DrawTileRequest.kt
│   │       ├── response/
│   │       │   ├── GameResponse.kt
│   │       │   └── TurnDraftResponse.kt
│   │       └── event/
│   │           ├── GameDraftUpdatedEvent.kt
│   │           ├── GameUpdatedEvent.kt
│   │           ├── TurnChangedEvent.kt
│   │           ├── TurnTimedOutEvent.kt
│   │           └── GameEndedEvent.kt
│   ├── mapper/
│   │   ├── GameMapper.kt
│   │   └── TurnDraftMapper.kt
│   └── persistence/
│       ├── GameEntity.kt
│       ├── TurnDraftEntity.kt
│       ├── GameRepository.kt
│       └── TurnDraftRepository.kt
├── rules/
│   ├── service/
│   │   ├── RummikubRuleService.kt
│   │   ├── BoardValidationService.kt
│   │   ├── SetValidationService.kt
│   │   ├── GroupValidationService.kt
│   │   ├── RunValidationService.kt
│   │   ├── TileConservationService.kt
│   │   └── FirstMoveValidationService.kt
│   └── model/
│       ├── ValidationResult.kt
│       └── RuleViolation.kt
└── BackendApplication.kt
```

### 5.2 Main backend layers

#### API layer

Exposes REST endpoints.

Responsibilities:

- request parsing
- validation of request shape
- calling services
- returning DTOs

No game logic should live here.

#### Service layer

Contains orchestration logic.

Responsibilities:

- loading match state
- applying draft updates
- ending turns
- committing valid moves
- drawing tiles
- computing next turn
- triggering broadcasts

#### Domain layer

Contains pure game models.

Responsibilities:

- represent board, tiles, players, game, and draft

#### Rules layer

Contains the actual Rummikub rules.

Responsibilities:

- validate groups
- validate runs
- validate joker usage
- validate full board after rearrangement
- validate first move rules
- validate final submitted move
- validate tile conservation

#### Persistence layer

Stores confirmed game and live draft.

For the MVP, storing game state and draft state as JSON blobs is acceptable and practical.

---

## 6. Backend Domain Model

### 6.1 Tile

```kotlin
enum class TileColor {
    BLACK, BLUE, ORANGE, RED
}

sealed interface Tile {
    val tileId: String
    val color: TileColor
}

data class NumberedTile(
    override val tileId: String,
    override val color: TileColor,
    val number: Int
) : Tile

data class JokerTile(
    override val tileId: String,
    override val color: TileColor
) : Tile
```

Notes:

- `tileId` is important because duplicate tiles exist in Rummikub
- both numbered tiles and jokers have a unique tile ID
- joker tiles also keep a concrete `color`
- a unique tile ID makes move validation and tile conservation much easier

### 6.2 Board set

```kotlin
enum class BoardSetType {
    RUN, GROUP, UNRESOLVED
}

data class BoardSet(
    val boardSetId: String,
    val type: BoardSetType,
    val tiles: List<Tile>
)
```

### 6.2.1 How a `BoardSet` gets its type

The current architecture requires the documentation to define **where and how** a set receives its type.

A new set should **not** need to be classified immediately as `GROUP` or `RUN` when the first tile is placed.

During editing, a newly created set may still be incomplete, for example:

- one tile only
- two tiles only
- mixed temporary rearrangement
- a temporarily invalid structure while the player is still moving tiles

Because of that, the recommended model is:

- the frontend creates new sets with `type = UNRESOLVED`
- the frontend keeps the set as `UNRESOLVED` during editing
- the backend must assign or confirm the final effective type when validating the submitted turn

#### Where type assignment happens

##### Frontend

On the frontend, type assignment is mainly a **draft editing concern**.

When the player creates a new set by dropping tiles into empty board space, the client should:

1. create a new `BoardSet`
2. initialize it as `UNRESOLVED`
3. keep it as `UNRESOLVED` while the player is editing it
4. send the set to the backend as part of the draft

This allows flexible editing without forcing the client to decide the type at all.

Small example:

```kotlin
val newSet = BoardSet(
    id = "temp-set-1",
    type = BoardSetType.UNRESOLVED,
    tiles = listOf(tile)
)
```

##### Backend

On the backend, type assignment becomes a **validation concern**.

When the player submits the turn:

1. the backend receives the draft board
2. each set is inspected
3. the backend tries validating the set as a group
4. the backend tries validating the set as a run
5. if exactly one interpretation is valid, accept that set
6. if both are valid, reject the set as ambiguous
7. if neither is valid, reject the submitted draft

So the final authoritative group/run decision happens in the backend rule layer.

Small example:

```kotlin
val result = setValidationService.validate(set)
```

#### Local board-set arrangement vs authoritative board content

The architecture should distinguish between:

- the **authoritative content** of the board
- the **local visual arrangement** of board sets on each client

The backend should care about:

- which sets exist
- which tiles belong to which set
- the final validated board content

The backend does **not** need to enforce one exact visual layout for how sets are positioned on every client screen.

This means:

- each client may arrange the rendered board sets locally in whatever layout works best for that device or user
- one client may show the same sets in a slightly different visual order or spacing than another client
- this local arrangement is a frontend concern and should not affect game legality

Only the set content and tile membership matter for backend validation.

#### Why this is needed

Without `UNRESOLVED`, the system would assume that every set always already knows whether it is a run or group.
That is not realistic during live drag-and-drop editing.

A player often creates sets in steps, for example:

- drop `red 7`
- add `blue 7`
- add `black 7`

The set only clearly becomes a `GROUP` at the end.

Or:

- drop `red 4`
- add `red 5`
- add `red 6`

The set only clearly becomes a `RUN` once the sequence is visible.

#### Recommended backend approach

The backend does not need a separate resolver helper here.
The cleaner approach is for `SetValidationService` to try both legal interpretations during final validation and decide whether the set is:

- valid as a group
- valid as a run
- ambiguous because both are valid
- invalid because neither is valid

This helper should be used when validating submitted drafts, especially for sets that are still marked as `UNRESOLVED`.

A board is simply a list of `BoardSet`.

### 6.3 Game player

```kotlin
data class GamePlayer(
    val userId: String,
    val displayName: String,
    val hand: List<Tile>,
    val hasPlayedInitialMeld: Boolean,
    val score: Int
)
```

### 6.4 Confirmed game state

```kotlin
enum class GameStatus {
    ACTIVE,
    FINISHED
}

data class Game(
    val gameId: String,
    val players: List<GamePlayer>,
    val board: List<BoardSet>,
    val drawPile: List<Tile>,
    val currentTurnPlayerId: String,
    val status: GameStatus,
    val winnerUserId: String? = null
)
```

### 6.5 Live turn draft

```kotlin
enum class TurnDraftStatus {
    IN_PROGRESS,
    SUBMITTED,
    ACCEPTED,
    REJECTED
}

data class TurnDraft(
    val gameId: String,
    val playerId: String,
    val draftBoard: List<BoardSet>,
    val draftHand: List<Tile>,
    val version: Long,
    val status: TurnDraftStatus
)
```

This is the shared in-progress turn.

### 6.6 Why `version` exists on the draft

The draft version is used to:

- reject stale draft updates
- detect conflicting updates
- help frontend reconciliation after reconnects
- reduce risk of overwriting newer draft state with older client data

---

## 7. Backend Services

### 7.1 GameService

This is the main orchestration service.

Responsibilities:

- fetch confirmed game state
- update current draft
- end / submit turn
- draw tile
- reset draft
- switch turns
- manage turn timer state
- handle turn timeout behavior
- trigger broadcasts

Important rules:

- only the active player may update the draft
- only the active player may submit the turn
- submitted draft is validated before commit

Small example:

```kotlin
if (game.currentTurnPlayerId != playerId) {
    throw SecurityException("Only the active player can act")
}
```

### 7.2 TurnDraftService

Handles the temporary live turn draft.

Responsibilities:

- create draft from confirmed state
- load current draft
- update draft
- reset draft
- delete / close draft
- prepare next draft after turn change

This service exists because the draft is now a real shared backend-managed state.

Small example:

```kotlin
val draft = turnDraftService.getDraft(gameId)
val saved = turnDraftService.saveDraft(draft.copy(version = draft.version + 1))
```

### 7.3 RummikubRuleService

Validates the final move.

Responsibilities:

- validate all groups
- validate all runs
- validate full board after rearrangement
- validate joker usage
- validate tile conservation
- validate initial meld rule if enabled

This is one of the most important backend services.

### 7.4 GameBroadcastService

Handles all websocket event broadcasting.

Responsibilities:

- `game.draft.updated`
- `game.updated`
- `turn.changed`
- `game.ended`

### 7.5 ScoreService

Handles end-game scoring.

Responsibilities:

- calculate winner
- calculate remaining tile penalties
- produce final result

---

## 8. Match Flow

### 8.1 Match initialization

At game start:

1. tiles are generated
2. tiles are shuffled
3. hands are distributed
4. draw pile is created
5. first turn player is selected
6. confirmed `Game` is stored
7. initial `TurnDraft` is created from confirmed state for the first player

Small example:

```kotlin
val game = gameInitializationService.createGame(playerIds)
val draft = turnDraftService.createInitialDraft(game)
```

### 8.2 Turn start

When a player's turn begins:

1. backend identifies `currentTurnPlayerId`
2. backend starts a turn timer for that player
3. backend creates or resets `TurnDraft`
4. backend broadcasts `turn.changed`
5. backend broadcasts the active draft if needed

Small example:

```kotlin
turnTimerService.startTurn(game.gameId, game.currentTurnPlayerId)
val draft = turnDraftService.createInitialDraft(game)
```

### 8.3 During turn

While the active player rearranges tiles:

1. frontend sends updated draft state to backend
2. backend verifies turn ownership
3. backend verifies draft version / conflict conditions if enabled
4. backend stores updated draft
5. backend broadcasts `game.draft.updated`
6. all clients see the change live

Small example:

```kotlin
val saved = turnDraftService.saveDraft(updatedDraft)
gameBroadcastService.broadcastDraftUpdated(saved)
```

### 8.4 End turn

When the active player ends the turn:

1. frontend sends end-turn request
2. backend loads confirmed game and current draft
3. backend validates draft using rule services
4. if valid:
    - confirmed game is updated
    - next player is chosen
    - a new turn timer is started
    - the current draft is reset / replaced by the next turn's initial draft
    - backend broadcasts `game.updated`
    - backend broadcasts `turn.changed`
    - backend may broadcast the new initial draft for the next turn
5. if invalid:
    - backend rejects request
    - confirmed game state stays unchanged
    - the current draft remains temporary and is not committed
    - the turn continues only while the timer is still running
6. if the turn timer expires:
    - the backend resets the draft back to the current confirmed state
    - the turn ends automatically
    - the next player is chosen
    - a new turn timer is started
    - backend broadcasts `turn.changed`
    - backend broadcasts the reset / next draft state if needed

### 8.4.1 Explicit turn-limit rule

The match should use an explicit turn-limit rule so the active player does not have unlimited time.

Recommended rule:

- each turn has a fixed timer
- the timer starts when the turn starts
- the player may update the live draft while the timer is active
- if the player submits a valid move before the timer expires, the move is committed normally
- if the player submits an invalid move, the move is rejected and the player may keep editing only until the timer expires
- if the timer expires before a valid move is submitted, the backend resets the draft back to the current confirmed game state and ends the turn automatically

This means the player does **not** have unlimited retry time.

A draw-from-pile penalty on timeout is optional, but it is **not** part of the current default architecture unless added as a separate rule.

Small example:

```kotlin
if (turnTimerService.hasExpired(gameId)) {
    resetDraft(gameId)
    advanceTurn(gameId)
}
```

### 8.5 Game end

When a player empties their hand:

1. backend marks game as `FINISHED`
2. backend computes winner and scores
3. backend broadcasts `game.ended`

---

## 9. REST API Design

A minimal REST API is enough.

The turn timer itself should be managed by the backend. The frontend only displays the timer information and reacts to timeout-related websocket / state updates.

Recommended endpoints:

```text
GET  /api/games/{gameId}
PUT  /api/games/{gameId}/draft
POST /api/games/{gameId}/end-turn
POST /api/games/{gameId}/draw
POST /api/games/{gameId}/reset-draft
```

If reconnecting clients also need to load the current draft directly, add this optional endpoint:

```text
GET  /api/games/{gameId}/draft
```

This is useful for:

- reconnect recovery
- restoring the active player's in-progress turn draft
- restoring spectator views of the current live draft

Small example:

```kotlin
@PutMapping("/api/games/{gameId}/draft")
fun updateDraft(@PathVariable gameId: String, @RequestBody request: UpdateDraftRequest) =
    gameService.updateDraft(gameId, request)
```

### `GET /api/games/{gameId}`

Returns the confirmed game state.

Used when:

- opening the game screen
- reconnecting after disconnect
- restoring app state

### `PUT /api/games/{gameId}/draft`

Replaces the current turn draft with the latest draft from the active player.

This is the simplest MVP design.

The player sends:

- full draft board
- full draft hand
- current version

### `POST /api/games/{gameId}/end-turn`

Attempts to commit the draft.

Backend:

- validates the move
- updates confirmed game state
- advances turn
- broadcasts new state

### `POST /api/games/{gameId}/draw`

Lets the active player draw a tile.

Backend:

- removes one tile from draw pile
- adds it to the player hand
- updates game state
- creates next draft or keeps same draft depending on rule flow

### `POST /api/games/{gameId}/reset-draft`

Resets the draft to the current confirmed game state.

Useful if:

- player wants to undo their in-progress changes
- backend rejects submit and frontend wants to resync

### Error response shape

REST endpoints should return a small, consistent error shape for invalid input, invalid state, and missing resources.

Recommended fields:

- `code`
- `message`
- `timestamp`

Small example:

```json
{
    "code": "INVALID_MOVE",
    "message": "The submitted draft is not a valid Rummikub board",
    "timestamp": "2026-04-17T12:00:00Z"
}
```

---

## 10. WebSocket Architecture

### 10.1 Topic

```text
/topic/games/{gameId}
```

Example:

```text
/topic/games/game-42
```

### 10.2 Events

#### `game.draft.updated`

Sent whenever the active player changes the draft.

```kotlin
data class GameDraftUpdatedEvent(
    val type: String = "game.draft.updated",
    val gameId: String,
    val playerId: String,
    val draft: TurnDraftResponse
)
```

#### `game.updated`

Sent whenever a valid move has been committed.

```kotlin
data class GameUpdatedEvent(
    val type: String = "game.updated",
    val game: GameResponse
)
```

#### `turn.changed`

Sent when the active turn moves to the next player.

```kotlin
data class TurnChangedEvent(
    val type: String = "turn.changed",
    val gameId: String,
    val currentTurnPlayerId: String
)
```

#### `turn.timed_out` (optional)

This event can be added if you want an explicit websocket signal for timeout handling.

It can be useful for:

- showing a timeout message in the frontend
- distinguishing a normal turn end from an automatic timeout end

Example payload:

```kotlin
data class TurnTimedOutEvent(
    val type: String = "turn.timed_out",
    val gameId: String,
    val previousTurnPlayerId: String
)
```

#### `game.ended`

Sent when the match ends.

```kotlin
data class GameEndedEvent(
    val type: String = "game.ended",
    val gameId: String,
    val winnerUserId: String
)
```

### Event contract recommendation

All websocket events should follow a small consistent structure.

Recommended common fields:

- `type`
- `gameId`
- event-specific payload fields

Optional future fields:

- `version`
- `timestamp`
- `sequenceNumber`

This makes frontend parsing simpler and helps with reconnect and stale-event handling.

### 10.3 Optional future metadata

Later, you may also add:

- event timestamp
- event version
- sequence number

These can help with:

- stale event handling
- reconnect resync
- debugging event order problems

---

## 11. Example Backend Implementations

### 11.1 WebSocket configuration

```kotlin
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app")
    }
}
```

### 11.2 Broadcast service

```kotlin
@Service
class GameBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {

    fun broadcastDraftUpdated(draft: TurnDraft) {
        messagingTemplate.convertAndSend(
            "/topic/games/${draft.gameId}",
            GameDraftUpdatedEvent(
                gameId = draft.gameId,
                playerId = draft.playerId,
                draft = draft.toResponse()
            )
        )
    }

    fun broadcastGameUpdated(game: Game) {
        messagingTemplate.convertAndSend(
            "/topic/games/${game.gameId}",
            GameUpdatedEvent(game = game.toResponse())
        )
    }

    fun broadcastTurnChanged(gameId: String, currentTurnPlayerId: String) {
        messagingTemplate.convertAndSend(
            "/topic/games/$gameId",
            TurnChangedEvent(
                gameId = gameId,
                currentTurnPlayerId = currentTurnPlayerId
            )
        )
    }

    fun broadcastGameEnded(gameId: String, winnerUserId: String) {
        messagingTemplate.convertAndSend(
            "/topic/games/$gameId",
            GameEndedEvent(
                gameId = gameId,
                winnerUserId = winnerUserId
            )
        )
    }
}
```

### 11.3 Draft update flow

```kotlin
@Transactional
fun updateDraft(gameId: String, request: UpdateDraftRequest): TurnDraft {
    val game = getGame(gameId)
    val currentDraft = turnDraftService.getDraft(gameId)

    if (game.currentTurnPlayerId != request.playerId) {
        throw SecurityException("Only the active player can update the draft")
    }

    if (currentDraft.playerId != request.playerId) {
        throw IllegalStateException("Draft does not belong to this player")
    }

    val updatedDraft = currentDraft.copy(
        draftBoard = request.draftBoard.map { it.toDomain() },
        draftHand = request.draftHand.map { it.toDomain() },
        version = currentDraft.version + 1
    )

    val saved = turnDraftService.saveDraft(updatedDraft)
    gameBroadcastService.broadcastDraftUpdated(saved)
    return saved
}
```

### 11.4 End-turn flow

```kotlin
@Transactional
fun endTurn(gameId: String, playerId: String): Game {
    val game = getGame(gameId)
    val draft = turnDraftService.getDraft(gameId)

    if (game.currentTurnPlayerId != playerId) {
        throw SecurityException("Only the active player can end the turn")
    }

    rummikubRuleService.validateSubmittedDraft(game, draft)

    val updatedPlayers = game.players.map {
        if (it.userId == playerId) it.copy(hand = draft.draftHand) else it
    }

    val nextTurnPlayerId = determineNextPlayerId(game, playerId)

    val updatedGame = game.copy(
        board = draft.draftBoard,
        players = updatedPlayers,
        currentTurnPlayerId = nextTurnPlayerId
    )

    val savedGame = gameRepository.save(updatedGame.toEntity()).toDomain()

    turnDraftRepository.deleteById(gameId)
    val nextDraft = turnDraftService.createInitialDraft(savedGame)

    gameBroadcastService.broadcastGameUpdated(savedGame)
    gameBroadcastService.broadcastTurnChanged(savedGame.gameId, savedGame.currentTurnPlayerId)
    gameBroadcastService.broadcastDraftUpdated(nextDraft)

    return savedGame
}
```

---

## 12. Rules Architecture

Detailed backend rule validation is documented separately in [rule-validation.md](./rule-validation.md).

In short:

- draft updates stay lightweight and permissive
- full rule validation happens only when the turn is submitted
- the backend decides whether final sets are legal as groups or runs

## 13. Persistence Strategy

For MVP, store:

- confirmed game state
- current draft state

A practical approach is to store both as JSON-backed persistence entities.

Benefits:

- easy to implement
- easy to evolve while the model is changing
- good enough for a student MVP

Trade-offs:

- less queryable than fully normalized tables
- schema changes must be handled carefully later

Recommended persisted concepts:

- `GameEntity`
- `TurnDraftEntity`

### Persistence recommendation for local-only layout state

Do **not** persist client-only presentation layout as authoritative match state.

This includes things such as:

- exact on-screen set positions
- per-client local board ordering
- per-client hand ordering
- temporary UI-only drag positions

These belong to the frontend only and may differ between clients.
Only authoritative board content, hand content, turn state, and draft content should be persisted by the backend.

---

## 14. Frontend Architecture

### 14.1 Package structure

```text
apps/android/app/src/main/java/at/se2group/rummikub/
├── core/
│   ├── network/
│   ├── websocket/
│   ├── model/
│   └── util/
├── feature/
│   ├── game/
│   │   ├── data/
│   │   ├── ui/
│   │   ├── viewmodel/
│   │   └── mapper/
│   └── result/
├── ui/
│   ├── components/
│   └── theme/
├── navigation/
└── MainActivity.kt
```

### 14.2 Frontend game flow

#### How users create new groups or runs

**Drag tile(s) into empty board space**

The simplest UX is:

1. user drags a tile from their hand
2. user drops it into an empty board area
3. client creates a new draft set with that tile

Example:

- user drops red 7
- client creates a new set: `[red 7]`

At this moment the set is invalid, but that is allowed during the turn.

The frontend does not need to decide immediately whether the set is a group or run. It can simply create a new editable set and let the type emerge from the tile content.

In the recommended architecture, the frontend should create these new draft sets with `type = UNRESOLVED` and leave final type resolution completely to the backend.

#### How users update existing groups or runs

A user should be able to:

- drag a tile from hand into an existing set
- drag a tile out of an existing set
- move a tile from one set to another
- reorder tiles inside a set if needed
- split one set into two
- merge tiles into another set

#### Local validation on the client

The client should do only lightweight local validation.

This is useful for:

- visual feedback
- helping the player
- showing obviously invalid sets

But it should not block editing.

**Recommended rule**

- allow all edits
- show whether a set is currently valid or invalid
- do not prevent the user from creating temporary invalid sets

**Example local checks**

The client can locally detect:

- set has fewer than 3 tiles
- group has duplicate colors
- group has mixed numbers
- run has mixed colors
- run has duplicate numbers
- run is not consecutive

This local validation is only for UI hints.

The backend remains the final authority.
The client should not try to assign the final `GROUP` or `RUN` type for authoritative game logic.

### 14.3 Recommended UX behavior

**Creating a new set**

When a tile is dropped into empty board space:

- create a new draft set
- mark it invalid until enough tiles are added

**Adding to an existing set**

When a tile is dropped onto a set:

- insert it into the set
- re-evaluate local validity
- keep the change even if invalid

**Removing from a set**

When a tile is dragged out:

- remove it from the set
- re-evaluate local validity
- if the set becomes empty, delete it

**Moving between sets**

When a tile is moved from set A to set B:

- remove from A
- add to B
- validate both sets locally
- keep both changes even if one or both sets become invalid temporarily

### 14.4 Recommended UI feedback

To help the player, the client should visually show:

- valid sets with normal styling
- invalid sets with warning styling
- the active player's current editing clearly
- optional hint labels such as:
    - `invalid set`
    - `needs at least 3 tiles`
    - `run is not consecutive`
    - `group contains duplicate color`

This makes editing easier without blocking the player.

### 14.5 End-turn behavior

When the user presses **End Turn**:

1. the client sends the current draft to the backend for final validation
2. the backend validates the full draft
3. if valid:
    - backend commits the draft to confirmed game state
    - backend broadcasts updated game state
4. if invalid:
    - backend rejects the move
    - client keeps the current draft and shows the error

So invalid draft states are normal during editing, but not allowed at submission.

### 14.6 Frontend responsibilities

The frontend should:

- fetch initial confirmed game state
- subscribe to the game topic
- render confirmed state
- render live draft state
- arrange board sets locally on the screen in a client-specific way
- arrange tiles in the player's hand locally in a client-specific way
- let only the active player modify the draft
- send updated drafts
- send end-turn command
- react to committed updates and turn changes
- display the remaining turn time
- react to automatic draft reset and turn timeout behavior
- keep client-only layout state separate from authoritative game and draft state

### 14.7 Frontend state model

The frontend should keep these state objects separate.

**Confirmed game state**

The last committed game state from backend.

**Shared live draft**

The current backend-managed draft for the active turn.

**Local interaction state**

Only UI-specific details:

- selected tile
- drag target
- highlighted set
- temporary animation state

Do not treat the local drag state as the authoritative draft.

The frontend may also keep purely local layout state for presentation purposes, for example:

- where board sets are positioned on screen
- in which order sets are rendered locally
- how tiles are visually ordered in the player's hand

This local layout state is not authoritative game state.
It may differ between clients without causing any game inconsistency.

#### Example UI state

```kotlin
data class GameUiState(
    val confirmedGame: GameResponse? = null,
    val liveDraft: TurnDraftResponse? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isSubmittingTurn: Boolean = false,
    val errorMessage: String? = null
)
```

---

## 15. Frontend Sync Flow

### Opening the game screen

1. frontend loads confirmed game via REST
2. frontend opens websocket connection
3. frontend subscribes to `/topic/games/{gameId}`
4. frontend starts rendering live updates

### Local per-client arrangement behavior

After loading the confirmed game and live draft state:

1. each client may arrange the board sets visually in its own local layout
2. each client may arrange the tiles in the player's hand visually in its own local order
3. these local layout choices do not change the authoritative backend state by themselves
4. only actual draft-changing actions, such as moving a tile between sets or between hand and board, should be sent to the backend

So the same match state may be rendered slightly differently on different clients while still representing the exact same legal board content.

### What must be synchronized vs what may stay local

The frontend should synchronize with the backend for:

- confirmed board content
- draft board content
- hand content
- current turn player
- timer / timeout state
- committed game updates

The frontend may keep these things local only:

- exact set placement on screen
- exact local hand tile ordering
- drag hover state
- selection / highlight state
- temporary animations

A good rule is:

- if it changes gameplay legality or shared match state, synchronize it
- if it only changes local presentation, keep it local

### During a turn

1. active player drags or rearranges tiles
2. frontend builds updated draft
3. frontend sends draft update to backend
4. backend stores draft and broadcasts `game.draft.updated`
5. all clients render the same shared draft

### Turn timer behavior in the frontend

While a turn is active:

1. the frontend shows the remaining turn time for the active player
2. the active player may keep editing only while the timer is active
3. if the player submits a valid move, normal end-turn flow happens
4. if the timer expires first, the frontend should expect the backend to reset the draft and advance the turn
5. the frontend must treat the backend's timeout result as authoritative

### Ending the turn

1. active player taps `End Turn`
2. frontend sends REST command
3. backend validates and commits
4. backend broadcasts:
    - `game.updated`
    - `turn.changed`
    - next `game.draft.updated` if a new draft is created immediately

---

## 16. Reconnect and Recovery Strategy

The architecture should support reconnects cleanly.

### On frontend reconnect

The frontend should:

1. reconnect websocket
2. re-subscribe to `/topic/games/{gameId}`
3. reload confirmed game via `GET /api/games/{gameId}`
4. optionally reload current draft if a dedicated draft load endpoint exists or if the backend includes draft state in reconnect flow

### Why this matters

Reconnect handling is important when:

- the app goes to background
- network is lost temporarily
- websocket reconnects after failure

The frontend should treat backend state as authoritative after reconnect.

This also applies to the turn timer. After reconnect, the frontend should use the backend-provided turn timing information and must not continue an old local timer independently.

### Reconnect recovery recommendation

After reconnect, the safest recovery order is:

1. reconnect websocket
2. re-subscribe to the game topic
3. reload confirmed game state
4. reload current draft state if the backend exposes it separately
5. discard stale local-only assumptions that conflict with backend state

This avoids keeping outdated local draft or timer state after a reconnect.

---

## 17. Minimal Recommended Implementation Order

### Backend

1. create game domain models
2. add game persistence
3. add websocket config
4. add `GameBroadcastService`
5. add `TurnDraft`
6. add backend-managed turn timer handling
7. implement `PUT /api/games/{gameId}/draft`
8. broadcast `game.draft.updated`
9. implement `POST /api/games/{gameId}/end-turn`
10. add rule validation
11. add game end logic

### Frontend

1. build game screen layout
2. fetch initial game state
3. subscribe to game websocket topic
4. render live draft
5. implement drag-and-drop for active player
6. display the backend-managed turn timer
7. send draft updates
8. react to `game.updated`, `turn.changed`, and timeout-related state changes
9. implement result screen

---

## 18. Open Questions / Decisions to Finalize

These points should still be decided explicitly if not already fixed:

- exact first meld rule
- exact turn timer duration
- whether timeout should also force a draw-tile penalty or only reset the draft and advance the turn
- strict joker behavior and edge cases
- whether `draw` automatically ends the turn or not
- whether draft reset is always available or only for active player
- whether draft updates should be throttled / debounced on the frontend
- whether reconnect should reload both confirmed game and current draft separately
- whether websocket events should include version / timestamp metadata
- whether reconnect uses only `GET /api/games/{gameId}` or also a dedicated `GET /api/games/{gameId}/draft`
- exact REST error response contract
- whether websocket events should always include a common envelope with `type`, `gameId`, and metadata fields

---

## 19. Summary

The Rummikub match architecture is built around two states:

- a **confirmed game state**
- a **live shared turn draft**

The backend is the single source of truth for both.
The backend is also the source of truth for the turn timer and timeout handling.
The frontend renders those states, lets the active player edit the draft, and sends commands back to the backend.

Clients may still arrange board sets and hand tiles locally for presentation without changing the authoritative match state.

REST is used for:

- initial loading
- draft updates
- ending turns
- optional draw and reset actions

WebSocket is used for:

- `game.draft.updated`
- `game.updated`
- `turn.changed`
- `game.ended`

This architecture ensures that:

- all players can see the active player's rearrangement live
- the backend remains authoritative
- client-only layout state can differ safely between players because it is not part of the authoritative match state
- active players do not have unlimited time because turn duration is limited by a backend-managed timer
- rule validation stays secure and consistent
- the frontend stays synchronized without polling
