# Backend Architecture Overview

## Purpose

This document outlines a rough backend architecture for the Rummikub game. It describes how the Android client should communicate with the backend, where the game logic should live, and how the backend can be structured in a clean and maintainable way.

## Core Architectural Idea

The backend should be the **single source of truth** for all important gameplay state.

This includes:

- game state
- turn order
- tile distribution
- meld validation
- scoring
- win detection
- timers
- reconnect state

The Android client should mainly be responsible for:

- rendering the UI
- sending player actions and intentions
- receiving validated game state updates
- handling animations and user feedback

This keeps all players in sync, avoids rule mismatches between clients, and prevents cheating.

---

## Communication Strategy

The recommended communication model is a mix of **REST** and **WebSocket**.

### REST for non-live features

Use REST endpoints for features that are request-response based and do not require live updates.

Examples:

- authentication
- user profile
- create lobby
- join lobby
- leave lobby
- start match
- fetch match history
- fetch leaderboard

Possible example endpoints:

```text
POST /api/lobbies
POST /api/lobbies/{id}/join
POST /api/matches/{id}/start
GET /api/leaderboard
GET /api/matches/{id}
```

REST is simple, easy to test, and well suited for non-realtime interactions.

### WebSocket for live gameplay

Use WebSocket for everything that happens during a running match.

Examples:

- player connected or disconnected
- match started
- turn changed
- board updated
- tile placed
- move submitted
- move rejected
- timer updates
- game ended

Typical flow:

1. The client opens the match screen.
2. The client opens a WebSocket connection.
3. The client subscribes to the match channel.
4. The client sends player actions.
5. The server validates the actions.
6. The server broadcasts the updated state to all players.

This is much better than polling REST continuously for game updates.

### Why this split works well for Rummikub

Rummikub has both turn-based structure and complex in-match state changes.

A split between REST and WebSocket fits this well:

- REST works well for actions that create, fetch, or update stable resources such as users, lobbies, match metadata, history, and leaderboard entries
- WebSocket works well for highly interactive game actions and rapid state updates during an active match
- this keeps the API easier to understand because non-live operations and live gameplay are handled differently on purpose
- it also makes frontend development easier because the Android app can use normal HTTP for setup flows and one persistent socket connection for the match itself

This hybrid approach is a practical balance between simplicity and realtime capability.

---

## Backend as the Authority

### Why the backend should own the rules

The backend should validate all actual game rules.

This includes:

- whether a set or run is valid
- whether joker usage is valid
- whether board rearrangement is legal
- whether the first meld meets the minimum threshold
- whether the move leaves all table groups valid
- whether the player is allowed to end the turn
- whether the player must draw a tile
- whether the match is finished

The client must not be trusted to decide:

- that a meld is valid
- that a player has won
- that a score is correct
- that a turn is over

The client may perform local preview validation for better user experience, but the backend must be the final authority.

### What should remain on the client

The client should keep only lightweight logic for interaction and presentation.

Examples:

- drag and drop handling
- visual grouping of tiles
- local tentative arrangement before submit
- optimistic previews if needed
- animations and feedback

When the player submits a move, the backend decides whether it is accepted.

---

## Recommended Backend Structure

A good high-level package structure for the Spring Boot backend could look like this:

```text
apps/backend/src/main/kotlin/at/se2group/backend/
├── common/
│   ├── exception/
│   ├── util/
│   └── validation/
├── config/
├── auth/
├── user/
├── lobby/
│   ├── api/
│   ├── service/
│   ├── dto/
│   └── persistence/
├── match/
│   ├── api/
│   ├── service/
│   ├── domain/
│   ├── engine/
│   ├── dto/
│   ├── persistence/
│   └── mapper/
├── leaderboard/
│   ├── api/
│   ├── service/
│   ├── dto/
│   └── persistence/
└── websocket/
    ├── config/
    ├── handler/
    └── dto/
```

### Layer responsibilities

#### `api/`

Contains REST controllers and WebSocket entrypoints.

This layer is the outer boundary of the backend. It should deal with transport concerns, but it should not contain the actual game rules.

Typical responsibilities:

- receive HTTP requests
- receive WebSocket messages
- validate basic request structure
- read path parameters, query parameters, headers, and authentication context
- map incoming payloads into DTOs
- call application services
- return HTTP responses or publish outgoing socket events
- translate domain or service errors into transport-level error responses

Suggested classes:

- `AuthController`
- `UserController`
- `LobbyController`
- `MatchController`
- `LeaderboardController`
- `MatchWebSocketController` or `MatchSocketHandler`

Example functionality:

- `AuthController` handles guest login, logout, and current session lookup
- `LobbyController` handles creating lobbies, joining, leaving, ready state changes, and lobby settings updates
- `MatchController` handles fetching match metadata, public state, private state, match summary, and reconnect snapshots
- `LeaderboardController` handles leaderboard and ranking endpoints
- `MatchSocketHandler` handles live match messages such as `move.submit`, `turn.draw`, and `match.subscribe`

Important rule:

This layer should stay thin. It should coordinate request and response flow, but not decide whether a Rummikub move is valid.

#### `service/`

Contains application-level orchestration and use-case logic.

This layer coordinates backend workflows. It sits between controllers and the domain or engine layer.

Typical responsibilities:

- execute use cases such as creating lobbies or starting matches
- load and save data through repositories
- call the game engine when rule validation is needed
- coordinate public and private state responses
- enforce application-level policies such as permissions or host-only actions
- publish WebSocket events after state changes
- handle reconnect and recovery flows

Suggested classes:

- `AuthService`
- `UserService`
- `LobbyService`
- `MatchService`
- `LeaderboardService`
- `MatchConnectionService`
- `MatchBroadcastService`

Example functionality:

- `AuthService` creates guest users or authenticated sessions
- `LobbyService` creates lobbies, manages ready states, validates whether a lobby can start, and starts the first match
- `MatchService` loads the current match, applies actions, coordinates validation, and persists the next state
- `MatchConnectionService` manages active socket sessions and reconnect behavior
- `MatchBroadcastService` sends public and private socket updates to the correct players after accepted actions
- `LeaderboardService` calculates or fetches ranking data

Important note:

This layer is a strong place for use-case methods such as:

- `createLobby()`
- `joinLobby()`
- `startMatch()`
- `submitMove()`
- `drawTile()`
- `endTurn()`
- `reconnectToMatch()`

#### `domain/`

Contains the core game concepts and value objects.

This layer represents the business model of the game itself. It should describe what exists in the game world and what state needs to be tracked.

Typical responsibilities:

- represent matches, players, tiles, melds, and phases
- model immutable or carefully controlled state transitions
- express important game concepts in code
- provide domain-level helper behavior when useful

Suggested classes:

- `Match`
- `Player`
- `PlayerId`
- `Tile`
- `TileColor`
- `TileNumber`
- `JokerTile`
- `Rack`
- `BoardGroup`
- `Meld`
- `TurnState`
- `GamePhase`
- `MatchStatus`
- `MoveResult`
- `MoveRejectionReason`

Example functionality:

- `Match` holds the current authoritative match state
- `Rack` models the tiles currently held by a player
- `BoardGroup` models a set or run placed on the table
- `TurnState` tracks whose turn it is, whether a draw already happened, and similar turn-specific facts
- `MatchStatus` distinguishes states such as waiting, active, finished, or cancelled

Important guideline:

The domain model should describe the game clearly. It should not contain controller annotations, database annotations unless intentionally reused, or WebSocket-specific concerns.

#### `engine/`

Contains the pure Rummikub rules engine.

This layer is one of the most important parts of the backend. It should contain the actual rule validation and state transition logic for the game.

Typical responsibilities:

- validate whether a board arrangement is legal
- validate runs and sets
- validate joker placement and reuse rules
- validate the first meld threshold
- check tile conservation between rack, board, and pool
- determine whether a player may end a turn
- determine whether a player must draw
- apply accepted moves to produce the next authoritative state
- determine victory, game over, and score results

Suggested classes:

- `RummikubRuleEngine`
- `MoveValidator`
- `BoardValidator`
- `MeldValidator`
- `JokerRuleValidator`
- `FirstMeldValidator`
- `TileConservationValidator`
- `TurnResolver`
- `ScoreCalculator`
- `WinnerDetector`
- `InitialDealFactory`

Example functionality:

- `MoveValidator` validates a submitted proposed final turn state
- `BoardValidator` ensures that all board groups remain valid after rearrangement
- `MeldValidator` checks whether a group forms a legal set or run
- `FirstMeldValidator` ensures that the opening meld meets the minimum threshold
- `TileConservationValidator` verifies that no tiles were duplicated, removed, or illegally introduced
- `TurnResolver` determines the next active player and the next turn state
- `ScoreCalculator` computes end-of-game scoring or ranking values
- `InitialDealFactory` creates the shuffled tile pool and starting racks when a match begins

Important guideline:

This layer should be as pure as possible. It should be easy to unit test without Spring Boot, a database, or WebSocket infrastructure.

#### `persistence/`

Contains repositories and persistence models.

This layer handles how data is stored and loaded.

Typical responsibilities:

- persist users, lobbies, matches, and leaderboard data
- load the latest authoritative match snapshot
- save match results and optional history data
- isolate database-specific concerns from the rest of the backend

Suggested classes:

- `UserEntity`
- `LobbyEntity`
- `MatchEntity`
- `MatchSnapshotEntity`
- `MatchResultEntity`
- `LeaderboardEntryEntity`
- `UserRepository`
- `LobbyRepository`
- `MatchRepository`
- `MatchSnapshotRepository`
- `MatchResultRepository`

Example functionality:

- `LobbyRepository` loads and saves lobby state
- `MatchRepository` loads the main match metadata
- `MatchSnapshotRepository` stores the current authoritative match state, potentially as JSON
- `MatchResultRepository` stores final game outcome data

Important guideline:

Persistence models should be optimized for storage and querying. They do not need to look the same as API DTOs or domain objects.

#### `dto/`

Contains request and response shapes for REST and WebSocket communication.

This layer defines the data contract between the backend and the client.

Typical responsibilities:

- define incoming REST request payloads
- define outgoing REST response payloads
- define incoming WebSocket message payloads
- define outgoing WebSocket event payloads
- keep transport data separate from domain and persistence models

Suggested classes:

- `GuestLoginRequest`
- `UserResponse`
- `CreateLobbyRequest`
- `LobbyResponse`
- `LobbySettingsResponse`
- `MatchSummaryResponse`
- `PublicMatchStateResponse`
- `PrivateMatchStateResponse`
- `LeaderboardEntryResponse`
- `SubmitMoveMessage`
- `MoveAcceptedEvent`
- `MoveRejectedEvent`
- `TurnChangedEvent`
- `MatchEndedEvent`
- `TileDto`
- `BoardGroupDto`
- `PlayerSummaryDto`

Example functionality:

- `SubmitMoveMessage` carries the proposed final board and rack state from the client
- `PublicMatchStateResponse` contains the state visible to all players
- `PrivateMatchStateResponse` contains only the rack and private information for one player
- `MoveRejectedEvent` returns a stable error contract for invalid actions

Important guideline:

DTOs should be designed for communication clarity, not for internal convenience.

#### `common/`

Contains shared building blocks that can be reused across backend modules.

This layer is useful for cross-cutting utilities and abstractions that do not belong to a single feature area.

Typical responsibilities:

- define shared exceptions
- provide reusable validation helpers
- provide common utility functions
- define shared error models or response wrappers if needed
- hold constants or helper types that are used across modules

Suggested classes:

- `ApiException`
- `NotFoundException`
- `ForbiddenActionException`
- `ValidationException`
- `ErrorCode`
- `ApiErrorResponse`
- `ClockProvider`
- `IdGenerator`

Example functionality:

- `ApiException` and subclasses create a standard way to signal business or request errors
- `ErrorCode` gives stable identifiers for frontend error handling
- `ClockProvider` makes time-dependent behavior easier to test
- `IdGenerator` can centralize ID generation strategy for lobbies and matches

Important guideline:

Only place code here if it is truly shared. Avoid turning `common/` into a miscellaneous dump folder.

#### `config/`

Contains Spring configuration classes and technical setup.

This layer wires the backend together and defines infrastructure behavior.

Typical responsibilities:

- configure WebSocket endpoints
- configure CORS and JSON serialization
- configure security if authentication is added
- configure database, environment, and application beans
- configure message converters or interceptors

Suggested classes:

- `WebSocketConfig`
- `CorsConfig`
- `JacksonConfig`
- `SecurityConfig`
- `PersistenceConfig`
- `OpenApiConfig`

Example functionality:

- `WebSocketConfig` registers socket endpoints and message broker behavior
- `JacksonConfig` configures JSON serialization for Kotlin and domain-specific types
- `SecurityConfig` can later protect endpoints and associate authenticated users with socket sessions
- `OpenApiConfig` documents REST APIs automatically

Important guideline:

Configuration classes should focus on wiring and setup, not game rules or business workflows.

#### `mapper/`

Contains mapping logic between persistence models, domain objects, and DTOs when that mapping becomes large enough to deserve a dedicated place.

This layer helps keep controllers and services cleaner when conversion logic grows.

Typical responsibilities:

- map entities to domain objects
- map domain objects to DTOs
- map request DTOs into command or domain structures
- centralize conversion rules so response shapes remain consistent

Suggested classes:

- `LobbyMapper`
- `MatchMapper`
- `LeaderboardMapper`
- `TileMapper`
- `BoardGroupMapper`

Example functionality:

- `MatchMapper` converts an internal `Match` domain object into `PublicMatchStateResponse` and `PrivateMatchStateResponse`
- `TileMapper` converts transport tile payloads into domain tile objects
- `LobbyMapper` converts `LobbyEntity` or domain state into API responses

Important guideline:

Mapping logic should stay mechanical and predictable. Complex rules should remain in services or the rules engine, not inside mappers.

### How these layers work together

A useful way to think about the flow is:

1. `api/` receives a request or socket message
2. `dto/` defines the payload shape used at the boundary
3. `service/` coordinates the use case
4. `persistence/` loads the current stored state
5. `domain/` and `engine/` represent and validate the game state
6. `service/` persists the new result and decides what should be broadcast
7. `mapper/` converts internal objects into response DTOs
8. `api/` sends the HTTP response or WebSocket event

### Example: move submission flow across layers

For a submitted move, the flow could look like this:

- `MatchSocketHandler` receives `SubmitMoveMessage`
- `MatchService.submitMove()` loads the current match state
- `MatchMapper` or a dedicated converter turns DTO payloads into domain structures
- `RummikubRuleEngine` validates the proposed final turn state
- if valid, `MatchService` stores the new snapshot through `MatchSnapshotRepository`
- `MatchBroadcastService` publishes `MatchStateUpdatedEvent` and any private rack updates
- if invalid, `MoveRejectedEvent` is returned with a stable rejection reason

This example shows why the backend stays easier to understand when each layer has a focused role.

## How to Model Communication

The backend communication for live gameplay should use a clear request-and-authoritative-response pattern.

For this project, the recommended approach is to use **Option B: send the proposed final turn state**.

### Chosen approach: Option B

Instead of sending many small tile movement operations one by one, the client sends the full proposed result of the turn.

That means the client submits:

- the proposed final board state after the player's rearrangement
- the proposed remaining rack for that player
- the current match or turn version for concurrency protection

The backend then:

1. loads the current authoritative match state
2. compares the authoritative state with the proposed client state
3. checks that the player only moved tiles they are allowed to use
4. validates that all resulting groups on the table are legal
5. validates turn-specific rules such as the first meld threshold
6. either accepts the move and publishes the new state, or rejects it with a reason

### Why Option B is preferred for Rummikub

Rummikub allows players to heavily rearrange tiles that are already on the board. In such a game, reconstructing dozens of single drag operations on the server is more complicated than validating the resulting board as a whole.

Option B has these advantages:

- easier rule validation
- simpler backend logic for complex rearrangements
- clearer contract between frontend and backend
- easier recovery after reconnects because the server always works from complete state snapshots

### Recommended live communication pattern

A typical move submission cycle should look like this:

1. the client renders the current authoritative match state
2. the player performs local drag-and-drop interactions only on the client
3. the client builds a proposed final board and rack state
4. the client sends a `move.submit` message through WebSocket
5. the server validates the proposal against the current authoritative state
6. the server either broadcasts the accepted updated state or sends a rejection error
7. the client updates the UI to the latest authoritative state

### Suggested payload direction

Client to server:

- match identifier
- player identifier or authenticated session context
- expected match version
- proposed board groups
- proposed remaining rack
- optional metadata such as whether the player intends to end the turn

Server to client on success:

- updated public match state
- updated private rack for the acting player if needed
- next turn information
- new state version

Server to client on failure:

- error code
- human-readable reason
- authoritative state to restore if needed

---

## Key Design Principle

Keep these three concerns clearly separated:

- game rules
- transport
- persistence

The Rummikub rules engine should not depend on:

- HTTP
- WebSocket
- Spring annotations
- database tables

Instead, it should receive a game state and an action and return either:

- a valid new game state
- or a validation error

This makes the game logic easier to test and much easier to maintain.

---

## Gameplay Flow

### 1. Lobby phase

Players create or join a lobby.

The backend stores:

- lobby ID
- player list
- ready states
- host
- settings

### 2. Match start

When the host starts the match:

- the backend creates a shuffled tile pool
- tiles are distributed to players
- the first player is selected
- the initial match state is created
- each player receives their private rack state
- all players receive the public match state

### 3. During a turn

A player may:

- drag tiles locally
- rearrange the board locally
- prepare a move in the client

When the player submits the move, the client sends the turn result to the backend.

The backend then validates and applies it.

### 4. On success

If the move is valid, the backend:

- updates the authoritative state
- persists the new state or event
- broadcasts the updated public state
- sends updated private rack data to the relevant player
- advances the turn if needed

### 5. On failure

If the move is invalid, the backend:

- rejects the action
- returns the reason
- allows the client to restore the authoritative state

---

## How Moves Should Be Submitted

There are two common strategies for submitting live game actions, but for this project the chosen approach is **Option B**.

### Option A: low-level tile operations

Examples:

- move tile X from rack position 3 to board group 2 position 1
- move tile Y from board group 1 to board group 4

Advantages:

- smaller payloads
- detailed replay possibilities

Disadvantages:

- harder validation
- the server must reconstruct the full player intent step by step
- not ideal for a game with heavy board rearrangement like Rummikub

### Option B: submit the proposed final turn state

The client sends:

- the proposed final board
- the proposed remaining rack
- the expected match version

Advantages:

- much easier to validate for Rummikub
- well suited for complex rearrangement of existing tiles
- simpler server-side rule checking
- easier reconnect handling because complete turn snapshots are exchanged

Disadvantages:

- larger payloads

### Final decision

For Rummikub, **Option B** is the recommended and chosen approach.

Rummikub allows complex manipulation of already placed tiles. In that case, validating the final board and rack state is usually easier and cleaner than replaying many small drag-and-drop operations one by one.

The backend should therefore:

1. compare the original state and the proposed state
2. verify that the player only used allowed tiles
3. validate that all resulting groups are legal
4. validate special turn rules such as the first meld threshold
5. accept or reject the move

This keeps the communication model easier to reason about for both frontend and backend development.

---

## Public and Private Game State

The backend should clearly separate public and private match information.

### Public match state

Visible to all players:

- board groups
- current turn
- player names
- number of tiles in each opponent rack
- timer
- match status
- winner, if the game is finished

### Private player state

Visible only to the specific player:

- exact tiles in that player's rack
- possible personal action hints later if needed

The backend must never broadcast all players' rack contents to everyone.

---

## Persistence Strategy

### First version

Keep persistence simple in the first version.

Store at least:

- users
- lobbies
- matches
- final match results
- current match snapshot if needed

For a student project, it is usually enough to store the current match state as JSON instead of fully normalizing every tile group into many relational tables.

### Later extensions

Later, the backend can be extended with:

- move history
- reconnect snapshots
- analytics
- replay support

A practical approach would be:

- PostgreSQL as the database
- standard tables for users, lobbies, and matches
- JSON snapshot storage for current match state
- optional match event tables later

---

## Concurrency and State Consistency

One common problem in multiplayer games is inconsistent state when actions arrive close together.

Each match should therefore be processed sequentially.

For a given match:

- only one move should be applied at a time
- updates should be locked by match ID
- stale actions should be rejected

A simple approach is to store a `version` field in the match state.

The client sends actions together with the expected version.
The backend rejects the action if the state has already changed.

This also helps with reconnects and duplicate requests.

---

## Suggested Backend Modules

### Auth and User

Handles:

- registration and login if needed
- guest accounts if that is simpler
- player profile
- display name

### Lobby

Handles:

- create lobby
- join lobby
- leave lobby
- ready state
- game settings
- match start

### Match and Game

Handles:

- gameplay state
- turn flow
- rule validation
- win conditions
- timers

### Leaderboard

Handles:

- ranking
- statistics
- win/loss tracking

### WebSocket Gateway

Handles:

- live subscriptions
- pushing updates
- connection lifecycle

---

## DTO Strategy

API payloads should be separated from internal domain models.

Examples:

- `SubmitMoveRequest`
- `MatchStateResponse`
- `PlayerPrivateStateResponse`
- `LobbyResponse`

Internal engine classes should not be exposed directly through the API. This keeps the transport format stable even when internal implementation changes.

---

## Suggested REST Endpoints

The backend should expose REST endpoints mainly for non-live features such as authentication, player profiles, lobby management, match discovery, match history, leaderboard data, and reconnect support.

The endpoints below are split into two groups:

- **initial endpoints** that are realistic and useful for the first version of the project
- **later optional endpoints** that can be added if the project grows in scope

### Initial REST endpoints

These are the endpoints that are most relevant for the first working version of the backend.

#### Authentication and session

If the project starts with guest access, this area can stay very small at first.

```text
POST   /api/auth/guest
GET    /api/auth/me
POST   /api/auth/logout
```

Purpose:

- create a guest session for quick game access
- fetch the currently authenticated user
- end the current session

#### User and profile

```text
GET    /api/users/{userId}
PATCH  /api/users/{userId}
GET    /api/users/{userId}/stats
GET    /api/users/{userId}/matches
```

Purpose:

- fetch a user profile
- update display name or simple profile fields
- fetch player statistics
- fetch a player's played matches

#### Lobby management

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

Purpose:

- create a lobby
- list open or joinable lobbies
- fetch the current lobby state
- join a lobby
- leave a lobby
- mark a player as ready
- remove ready status
- change lobby settings such as player limit or match options
- start the match from the lobby
- delete or close a lobby, usually by the host

#### Match metadata, state, and recovery

Even though live gameplay uses WebSocket, REST should still provide reliable state fetch and reconnect support.

```text
GET    /api/matches/{matchId}
GET    /api/matches/{matchId}/summary
GET    /api/matches/{matchId}/players
GET    /api/matches/{matchId}/state
GET    /api/matches/{matchId}/private-state
GET    /api/matches/{matchId}/result
GET    /api/matches/{matchId}/reconnect
```

Purpose:

- fetch general match information
- fetch a summary suitable for match overview screens
- fetch player participation details
- fetch the current authoritative public match state
- fetch the current private state for the requesting player
- fetch the final result of a finished match
- recover the latest match snapshot for reconnect scenarios

#### Leaderboard

```text
GET    /api/leaderboard
GET    /api/leaderboard/{userId}/position
```

Purpose:

- fetch the default leaderboard
- fetch the ranking position for a specific player

#### Development and health

```text
GET    /api/health
GET    /api/info
POST   /api/dev/seed
POST   /api/dev/reset
```

Purpose:

- basic backend health check
- application info endpoint if needed
- seed development data for easier testing
- reset development data in non-production environments

### Later optional REST endpoints

These endpoints are not required for the first version, but they are useful if the backend grows in functionality.

#### Full authentication

```text
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
```

#### Additional profile and progression features

```text
GET    /api/users/{userId}/achievements
GET    /api/users/{userId}/friends
```

#### Invitations and social features

```text
POST   /api/lobbies/{lobbyId}/invites
GET    /api/invites
POST   /api/invites/{inviteId}/accept
POST   /api/invites/{inviteId}/decline
GET    /api/friends
POST   /api/friends/{userId}
DELETE /api/friends/{userId}
```

#### Extended match history and replay support

```text
GET    /api/matches/{matchId}/history
GET    /api/matches/{matchId}/events
POST   /api/matches/{matchId}/connect
POST   /api/matches/{matchId}/disconnect
```

#### Alternative leaderboard views

```text
GET    /api/leaderboard/global
GET    /api/leaderboard/seasonal
GET    /api/leaderboard/friends
```

### Core product flow covered by the initial REST endpoints

The initial REST endpoints together cover this rough product flow:

1. enter the app using a guest session
2. fetch the current user profile
3. browse or create a lobby
4. join or leave a lobby
5. mark ready and change lobby settings
6. start a match
7. fetch match metadata and recover state if reconnect is needed
8. fetch leaderboard and profile statistics

This is enough to support the non-live parts of the first playable version of the game.

## Suggested WebSocket Messages

WebSocket communication should cover the full live match experience.

The messages below are also split into two groups:

- **initial messages** for the first working realtime version
- **later optional messages** for richer features

### Initial WebSocket messages

These are the most important messages for a first playable multiplayer version.

#### Client to server

```text
match.connect
match.disconnect
match.subscribe
match.unsubscribe
move.submit
turn.draw
turn.end
ping
```

Purpose:

- connect the player to a specific running match
- disconnect cleanly if needed
- subscribe to live match updates
- unsubscribe when leaving the match screen
- submit the proposed final state of a turn
- draw a tile when no valid move is played or when required
- explicitly end a turn if that is modeled separately
- keep the socket alive or measure latency

#### Server to client

```text
match.connected
match.disconnected
match.subscribed
match.unsubscribed
match.state.updated
match.player.private
match.turn.changed
match.move.accepted
match.move.rejected
match.tile.drawn
match.ended
player.connected
player.disconnected
error
pong
```

Purpose:

- confirm that a match connection was accepted
- confirm that the player disconnected
- confirm that the client subscribed to match updates
- confirm that the client unsubscribed
- publish the latest authoritative public match state
- send updated private rack or private player data
- notify clients that the active turn changed
- confirm that a submitted move was accepted
- reject a move and explain why
- notify the acting player that a draw was performed
- notify all players that the match ended
- notify that another player connected
- notify that another player disconnected
- deliver generic socket-level or domain-level errors
- respond to client ping messages

### Recommended initial live flow

A typical realtime match flow should look like this:

1. the client opens the match screen
2. the client sends `match.connect`
3. the client sends `match.subscribe`
4. the server responds with `match.connected`, `match.subscribed`, and the latest state
5. the player performs local actions on the client
6. the client sends `move.submit` with the proposed final turn state
7. the server validates the move
8. on success, the server sends `match.move.accepted`, `match.state.updated`, and any needed private updates
9. on failure, the server sends `match.move.rejected` and, if needed, authoritative state to restore
10. when the turn changes, the server sends `match.turn.changed`
11. when the match ends, the server sends `match.ended`

### Example responsibility split for a submitted move

When a player submits a move:

- the client is responsible for sending the proposed final state of the turn
- the server is responsible for validating whether the move is legal
- the server is responsible for publishing the accepted new authoritative state
- the client is responsible for rendering that authoritative state exactly as received

### Later optional WebSocket messages

These messages can be added if the project later supports richer interaction.

#### Client to server

```text
match.reconnect
match.request.state
chat.send
emote.send
presence.update
```

#### Server to client

```text
match.reconnected
match.state.snapshot
chat.received
emote.received
presence.updated
timer.updated
```

Possible later use cases:

- explicit reconnect commands
- on-demand state snapshots
- match chat or reactions
- richer presence updates
- separate timer push events

### Important guideline

WebSocket messages should be represented with dedicated DTOs just like REST requests and responses.

That means:

- incoming client messages should map to request-style DTOs
- outgoing server events should map to event or response DTOs
- domain objects should not be sent directly over the socket

This keeps the live protocol stable and easier to evolve.

## Validation Strategy

Validation should happen in multiple layers.

### Input validation

Checks whether the incoming request is structurally correct.

Examples:

- required fields are present
- IDs are valid
- the player belongs to the match

### State validation

Checks whether the action is allowed in the current match context.

Examples:

- it is the player's turn
- the match is still active
- the request references the correct state version

### Rule validation

Checks whether the move is valid according to Rummikub rules.

Examples:

- all board groups are valid
- the first meld threshold is satisfied
- no tile duplication exists
- rack and board conservation holds

This separation keeps the implementation easier to understand and maintain.

---

## Things to Avoid

The following design choices should be avoided:

- putting all logic into controllers
- trusting the client for game rule validation
- using only REST polling for live gameplay
- duplicating full rule logic independently on frontend and backend
- overengineering the project as multiple microservices

For this project, a **modular monolith** is the recommended approach: one Spring Boot backend with clearly separated packages and responsibilities.

---

## Recommended Practical Setup for This Project

A practical architecture for this monorepo would be:

### Android app

Responsible for:

- UI rendering
- local drag-and-drop interaction
- REST client
- WebSocket client
- lightweight view models and UI state handling

### Spring Boot backend

Responsible for:

- REST controllers for lobby, profile, history, and leaderboard
- WebSocket communication for live gameplay
- service layer orchestration
- pure Rummikub rules engine
- persistence with PostgreSQL
- JSON snapshots for current match state

This setup offers:

- maintainability
- testability
- fair multiplayer synchronization
- clean frontend-backend integration

The most important implementation guideline is that the client should never be treated as the final authority for rules or state transitions. The client proposes, but the backend decides.

---

## Simple Rule of Thumb

Use this split throughout the project:

- the client decides what the player wants to do
- the client sends a proposed final turn state
- the server decides whether it is allowed
- the server publishes the new authoritative truth
- the client renders that truth
