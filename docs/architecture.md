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
├── config/
├── auth/
├── user/
├── lobby/
├── match/
│   ├── api/
│   ├── service/
│   ├── domain/
│   ├── engine/
│   ├── dto/
│   └── persistence/
├── leaderboard/
└── websocket/
```

### Layer responsibilities

#### `api/`

Contains REST controllers and WebSocket message handlers.

Responsibilities:

- receive requests
- validate basic input shape
- map request and response DTOs
- call services
- return responses or push events

#### `service/`

Contains application-level orchestration.

Responsibilities:

- start a match
- join a match
- submit a move
- end a turn
- draw a tile
- handle reconnects

#### `domain/`

Contains the core game entities and value objects.

Examples:

- `Match`
- `Player`
- `Tile`
- `TileColor`
- `Meld`
- `BoardGroup`
- `TurnState`
- `Rack`
- `GamePhase`

#### `engine/`

Contains the pure game rules engine.

This is one of the most important parts of the backend.

Responsibilities:

- validate runs and sets
- validate the first move threshold
- apply rearrangements
- calculate scores
- determine the next player
- detect victory and game over

This layer should be kept as isolated and pure as possible so it is easy to unit test.

#### `persistence/`

Contains repositories and persistence models.

#### `dto/`

Contains API request and response models, as well as WebSocket payloads.

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

There are two common strategies for submitting live game actions.

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

### Option B: submit the proposed final turn state

The client sends:

- the proposed final board
- the proposed remaining rack

Advantages:

- much easier to validate for Rummikub
- well suited for complex rearrangement of existing tiles
- simpler server-side rule checking

Disadvantages:

- larger payloads

### Recommendation

For Rummikub, **Option B** is recommended.

Rummikub allows complex manipulation of already placed tiles. In that case, validating the final board and rack state is usually easier and cleaner than replaying many small drag-and-drop operations one by one.

The backend can then:

1. compare the original state and the proposed state
2. verify that the player only used allowed tiles
3. validate that all resulting groups are legal
4. validate special turn rules such as the first meld threshold
5. accept or reject the move

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

## Suggested Initial REST Endpoints

```text
POST /api/lobbies
POST /api/lobbies/{lobbyId}/join
POST /api/lobbies/{lobbyId}/ready
POST /api/lobbies/{lobbyId}/start
GET /api/matches/{matchId}
GET /api/leaderboard
```

## Suggested Initial WebSocket Messages

### Client to server

- `match.join`
- `move.submit`
- `turn.draw`
- `turn.end`
- `ping`

### Server to client

- `match.state.updated`
- `match.player.private`
- `match.error`
- `match.turn.changed`
- `match.ended`
- `player.connected`
- `player.disconnected`

---

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

---

## Simple Rule of Thumb

Use this split throughout the project:

- the client decides what the player wants to do
- the server decides whether it is allowed
- the server publishes the new truth
- the client renders that truth
