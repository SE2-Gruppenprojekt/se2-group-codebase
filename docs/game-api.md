# Game API

[![Backend CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/backend.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/backend.yml)
[![Frontend CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/android.yml)
[![Shared CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/shared.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/shared.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SE2-Gruppenprojekt_se2-group-codebase&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SE2-Gruppenprojekt_se2-group-codebase)

This document describes the **game / match API** for the Rummikub feature.

It covers:

- REST API endpoints
- request and response DTO shapes
- websocket events
- example payloads

This document is intentionally focused on the **match/game feature only** and leaves lobby functionality out.

---

## 1. Deployment

Current backend deployment:

- REST base URL: `https://se2-group-codebase.onrender.com/`
- WebSocket / STOMP endpoint: `wss://se2-group-codebase.onrender.com/ws`
- Health check: `https://se2-group-codebase.onrender.com/actuator/health`

The WebSocket endpoint uses the same deployed backend host as the REST API.

---

## 2. API Overview

The game feature uses two communication styles:

### REST

REST is used for:

- loading the confirmed game state
- loading the current draft state if needed
- sending game commands

### WebSocket

WebSocket is used for:

- live draft updates
- confirmed game updates
- turn changes
- timeout events
- game end events

### High-level flow

Typical flow:

1. frontend loads the game via REST
2. frontend subscribes to the game websocket topic
3. active player updates the draft through REST
4. backend stores live draft updates and broadcasts them over websocket
5. the active player can currently load game state, update the draft, and draw a tile
6. later turn-submission flows can build on the same REST and websocket contract

---

## 3. Common Models

The following response types are used repeatedly across the API.

### `TileResponse`

```json
{
    "tileId": "tile-001",
    "color": "RED",
    "number": 7,
    "isJoker": false
}
```

Fields:

- `tileId: String`
- `color: String`
- `number: Int | null`
- `isJoker: Boolean`

Notes:

- jokers must still have a color set
- joker tiles use `number = null`
- tile IDs must be unique even if tiles have the same color and number

---

### `BoardSetResponse`

```json
{
    "boardSetId": "set-001",
    "type": "GROUP",
    "tiles": [
        {
            "tileId": "tile-001",
            "color": "RED",
            "number": 7,
            "isJoker": false
        },
        {
            "tileId": "tile-002",
            "color": "BLUE",
            "number": 7,
            "isJoker": false
        },
        {
            "tileId": "tile-003",
            "color": "BLACK",
            "number": 7,
            "isJoker": false
        }
    ]
}
```

Fields:

- `boardSetId: String`
- `type: "RUN" | "GROUP" | "UNRESOLVED"`
- `tiles: List<TileResponse>`

Notes:

- `UNRESOLVED` is mainly used during live draft editing
- final authoritative validation happens in the backend

---

### Joker substitution semantics

Jokers are transported through the API as normal tiles:

- `isJoker = true`
- `number = null`
- `color` is still set
- `tileId` is still unique and stable

Important backend contract:

- the API does **not** send a separately persisted “joker resolves to X” field
- the client may place a joker into a board set, but the backend decides at end-turn whether a legal substitution exists
- substitution is therefore a **validation-time rule decision**, not a client-authored data field

That means the frontend should treat a joker as:

- a real tile in the rack and board
- a tile that may participate in group/run validation
- a tile whose exact interpreted value is owned by backend rule validation

In practice:

- draft payloads send joker tiles exactly like any other tile, just with `isJoker = true`
- end-turn validation decides whether the joker can legally stand in for a missing number/color position
- if no legal substitution exists, the backend rejects the turn with normal `RuleViolation` errors

Example joker tile:

```json
{
    "tileId": "tile-joker-001",
    "color": "BLACK",
    "number": null,
    "isJoker": true
}
```

---

### `GamePlayerResponse`

```json
{
    "userId": "player-1",
    "displayName": "Julian",
    "turnOrder": 0,
    "rackTiles": [
        {
            "tileId": "tile-010",
            "color": "ORANGE",
            "number": 5,
            "isJoker": false
        }
    ],
    "hasCompletedInitialMeld": false,
    "score": 0,
    "joinedAt": "2026-04-21T12:00:00Z"
}
```

Fields:

- `userId: String`
- `displayName: String`
- `turnOrder: Int`
- `rackTiles: List<TileResponse>`
- `hasCompletedInitialMeld: Boolean`
- `score: Int`
- `joinedAt: String` (ISO-8601 timestamp)

---

### `GameResponse`

This is the confirmed authoritative game state.

```json
{
    "gameId": "game-123",
    "players": [
        {
            "userId": "player-1",
            "displayName": "Julian",
            "turnOrder": 0,
            "rackTiles": [
                {
                    "tileId": "tile-010",
                    "color": "ORANGE",
                    "number": 5,
                    "isJoker": false
                }
            ],
            "hasCompletedInitialMeld": false,
            "score": 0,
            "joinedAt": "2026-04-21T12:00:00Z"
        },
        {
            "userId": "player-2",
            "displayName": "Alex",
            "turnOrder": 1,
            "rackTiles": [
                {
                    "tileId": "tile-011",
                    "color": "RED",
                    "number": 9,
                    "isJoker": false
                }
            ],
            "hasCompletedInitialMeld": false,
            "score": 0,
            "joinedAt": "2026-04-21T12:00:05Z"
        }
    ],
    "board": [
        {
            "boardSetId": "set-001",
            "type": "RUN",
            "tiles": [
                {
                    "tileId": "tile-020",
                    "color": "RED",
                    "number": 4,
                    "isJoker": false
                },
                {
                    "tileId": "tile-021",
                    "color": "RED",
                    "number": 5,
                    "isJoker": false
                },
                {
                    "tileId": "tile-022",
                    "color": "RED",
                    "number": 6,
                    "isJoker": false
                }
            ]
        }
    ],
    "lobbyId": "lobby-456",
    "drawPile": [],
    "drawPileCount": 54,
    "currentPlayerUserId": "player-1",
    "currentTurnPlayerId": "player-1",
    "turnDeadline": null,
    "remainingTurnSeconds": null,
    "status": "ACTIVE",
    "createdAt": "2026-04-21T12:00:00Z",
    "startedAt": null,
    "finishedAt": null
}
```

Fields:

- `gameId: String`
- `lobbyId: String`
- `players: List<GamePlayerResponse>`
- `board: List<BoardSetResponse>`
- `drawPile: List<TileResponse>`
- `drawPileCount: Int`
- `currentPlayerUserId: String`
- `currentTurnPlayerId: String`
- `turnDeadline: String | null` (ISO-8601 timestamp)
- `remainingTurnSeconds: Int | null`
- `status: "WAITING" | "ACTIVE" | "FINISHED" | "CANCELLED"`
- `createdAt: String` (ISO-8601 timestamp)
- `startedAt: String | null` (ISO-8601 timestamp)
- `finishedAt: String | null` (ISO-8601 timestamp)

Notes:

- the current backend returns both `drawPile` and `drawPileCount`
- `currentTurnPlayerId` currently mirrors `currentPlayerUserId`
- `turnDeadline` and `remainingTurnSeconds` are currently returned as `null`

- possible status values currently mirror the backend `GameStatus` enum: `WAITING`, `ACTIVE`, `FINISHED`, and `CANCELLED`
- `WAITING` is mainly relevant before active gameplay begins
- `CANCELLED` is reserved for games that are terminated without a normal finish

---

### `TurnDraftResponse`

This is the current live editable draft state.

```json
{
    "gameId": "game-123",
    "playerUserId": "player-1",
    "draftBoard": [
        {
            "boardSetId": "set-001",
            "type": "RUN",
            "tiles": [
                {
                    "tileId": "tile-020",
                    "color": "RED",
                    "number": 4,
                    "isJoker": false
                },
                {
                    "tileId": "tile-021",
                    "color": "RED",
                    "number": 5,
                    "isJoker": false
                }
            ]
        },
        {
            "boardSetId": "set-temp-1",
            "type": "UNRESOLVED",
            "tiles": [
                {
                    "tileId": "tile-022",
                    "color": "RED",
                    "number": 6,
                    "isJoker": false
                }
            ]
        }
    ],
    "draftHand": [
        {
            "tileId": "tile-010",
            "color": "ORANGE",
            "number": 5,
            "isJoker": false
        }
    ],
    "version": 3
}
```

Fields:

- `gameId: String`
- `playerUserId: String`
- `draftBoard: List<BoardSetResponse>`
- `draftHand: List<TileResponse>`
- `version: Long`

---

### `ApiErrorResponse`

REST endpoints currently return a small consistent backend error shape.

```json
{
    "errorCode": "BAD_REQUEST",
    "errorMessage": "Invalid request"
}
```

Fields:

- `errorCode: String`
- `errorMessage: String`

Current HTTP status mapping from `GlobalExceptionHandler`:

- `400 Bad Request` for `IllegalArgumentException`
    - `errorCode = "BAD_REQUEST"`
- `404 Not Found` for `NoSuchElementException`
    - `errorCode = "NOT_FOUND"`
- `409 Conflict` for `IllegalStateException`
    - `errorCode = "CONFLICT"`
- `403 Forbidden` for `SecurityException`
    - `errorCode = "FORBIDDEN"`
- `500 Internal Server Error` for all other exceptions
    - `errorCode = "INTERNAL_SERVER_ERROR"`

The backend currently does not include a `timestamp` field in error responses.

---

## 4. REST API

## 4.1 `GET /api/games/{gameId}`

Returns the confirmed authoritative game state.

### Purpose

Used for:

- initial screen load
- reconnect recovery
- refreshing the confirmed match state

### Response type

- `200 OK` → `GameResponse`
- `404 Not Found` → `ApiErrorResponse`

### Example response

```json
{
    "gameId": "game-123",
    "players": [
        {
            "userId": "player-1",
            "displayName": "Julian",
            "turnOrder": 0,
            "rackTiles": [],
            "hasCompletedInitialMeld": true,
            "score": 0,
            "joinedAt": "2026-04-21T12:00:00Z"
        },
        {
            "userId": "player-2",
            "displayName": "Alex",
            "turnOrder": 1,
            "rackTiles": [],
            "hasCompletedInitialMeld": false,
            "score": 0,
            "joinedAt": "2026-04-21T12:00:05Z"
        }
    ],
    "lobbyId": "lobby-456",
    "board": [],
    "drawPile": [],
    "drawPileCount": 42,
    "currentPlayerUserId": "player-1",
    "currentTurnPlayerId": "player-1",
    "turnDeadline": null,
    "remainingTurnSeconds": null,
    "status": "ACTIVE",
    "createdAt": "2026-04-21T12:00:00Z",
    "startedAt": null,
    "finishedAt": null
}
```

---

## 4.2 `GET /api/games/{gameId}/draft` (optional)

Returns the current live draft state.

### Purpose

Useful for:

- reconnect recovery
- restoring the active player's in-progress draft
- restoring spectator views of the current live draft

### Response type

- `200 OK` → `TurnDraftResponse`
- `404 Not Found` → `ApiErrorResponse`

### Example response

```json
{
    "gameId": "game-123",
    "playerUserId": "player-1",
    "draftBoard": [],
    "draftHand": [],
    "version": 2
}
```

---

## 4.3 `PUT /api/games/{gameId}/draft`

Replaces the current draft with the latest draft state from the active player.

### Purpose

Used while the active player is rearranging tiles during their turn.

### Request type

`UpdateDraftRequest`

### Example request

The current backend identifies the player through the `X-User-Id` header and
expects this request body:

```json
{
    "boardSets": [
        {
            "boardSetId": "set-temp-1",
            "type": "UNRESOLVED",
            "tiles": [
                {
                    "tileId": "tile-020",
                    "color": "RED",
                    "number": 4,
                    "isJoker": false
                }
            ]
        }
    ],
    "rackTiles": [
        {
            "tileId": "tile-010",
            "color": "ORANGE",
            "number": 5,
            "isJoker": false
        }
    ]
}
```

Fields:

- `boardSets: List<BoardSetRequest>`
- `rackTiles: List<TileRequest>`

`BoardSetRequest` fields:

- `boardSetId: String`
- `type: "GROUP" | "RUN" | "UNRESOLVED"`
- `tiles: List<TileRequest>`

`TileRequest` fields:

- `tileId: String`
- `color: String`
- `number: Int | null`
- `isJoker: Boolean`

### Response type

- `200 OK` → `TurnDraftResponse`
- `400 Bad Request` → `ApiErrorResponse`
- `404 Not Found` → `ApiErrorResponse`
- `409 Conflict` → `ApiErrorResponse`

### Example success response

```json
{
    "gameId": "game-123",
    "playerUserId": "player-1",
    "draftBoard": [
        {
            "boardSetId": "set-temp-1",
            "type": "UNRESOLVED",
            "tiles": [
                {
                    "tileId": "tile-020",
                    "color": "RED",
                    "number": 4,
                    "isJoker": false
                }
            ]
        }
    ],
    "draftHand": [
        {
            "tileId": "tile-010",
            "color": "ORANGE",
            "number": 5,
            "isJoker": false
        }
    ],
    "version": 3
}
```

### Example error response

```json
{
    "errorCode": "CONFLICT",
    "errorMessage": "User is not the current active player"
}
```

---

## 4.4 `POST /api/games/{gameId}/end-turn`

Submits the current draft for final validation and, if valid, commits it as the new confirmed game state.

### Purpose

Used when the active player wants to finish their turn.

### Request type

`EndTurnRequest`

### Example request

```json
{
    "playerId": "player-1"
}
```

Fields:

- `playerId: String`

### Response type

- `200 OK` → `GameResponse`
- `400 Bad Request` → `ApiErrorResponse`
- `403 Forbidden` → `ApiErrorResponse`
- `404 Not Found` → `ApiErrorResponse`
- `409 Conflict` → `ApiErrorResponse`

### Example success response

```json
{
    "gameId": "game-123",
    "players": [
        {
            "userId": "player-1",
            "displayName": "Julian",
            "turnOrder": 0,
            "rackTiles": [
                {
                    "tileId": "tile-010",
                    "color": "ORANGE",
                    "number": 5,
                    "isJoker": false
                }
            ],
            "hasCompletedInitialMeld": true,
            "score": 0,
            "joinedAt": "2026-04-21T12:00:00Z"
        },
        {
            "userId": "player-2",
            "displayName": "Alex",
            "turnOrder": 1,
            "rackTiles": [
                {
                    "tileId": "tile-011",
                    "color": "RED",
                    "number": 9,
                    "isJoker": false
                }
            ],
            "hasCompletedInitialMeld": false,
            "score": 0,
            "joinedAt": "2026-04-21T12:00:05Z"
        }
    ],
    "board": [
        {
            "boardSetId": "set-001",
            "type": "GROUP",
            "tiles": [
                {
                    "tileId": "tile-001",
                    "color": "RED",
                    "number": 7,
                    "isJoker": false
                },
                {
                    "tileId": "tile-002",
                    "color": "BLUE",
                    "number": 7,
                    "isJoker": false
                },
                {
                    "tileId": "tile-003",
                    "color": "BLACK",
                    "number": 7,
                    "isJoker": false
                }
            ]
        }
    ],
    "drawPileCount": 41,
    "currentTurnPlayerId": "player-2",
    "turnDeadline": "2026-04-21T12:31:30Z",
    "remainingTurnSeconds": 60,
    "status": "ACTIVE",
    "winnerUserId": null
}
```

### Example error response

```json
{
    "errorCode": "BAD_REQUEST",
    "errorMessage": "The submitted draft is not a valid Rummikub board"
}
```

---

## 4.5 `POST /api/games/{gameId}/draw`

Lets the active player draw a tile from the draw pile.

### Purpose

Used when the active player wants to draw the next tile from the confirmed draw
pile.

### Request contract

The current backend controller does **not** consume a request body for this
endpoint. The acting player is identified only through the `X-User-Id` header:

```http
POST /api/games/{gameId}/draw
X-User-Id: player-1
```

The shared module currently contains `DrawTileRequest`, but the controller does
not use it yet.

### Response type

- `200 OK` → `GameResponse`
- `404 Not Found` → `ApiErrorResponse`
- `409 Conflict` → `ApiErrorResponse`

### Example success response

```json
{
    "gameId": "game-123",
    "players": [
        {
            "userId": "player-1",
            "displayName": "Julian",
            "turnOrder": 0,
            "rackTiles": [
                {
                    "tileId": "tile-999",
                    "color": "BLACK",
                    "number": 12,
                    "isJoker": false
                }
            ],
            "hasCompletedInitialMeld": true,
            "score": 0,
            "joinedAt": "2026-04-21T12:00:00Z"
        }
    ],
    "lobbyId": "lobby-456",
    "board": [],
    "drawPile": [
        {
            "tileId": "tile-1000",
            "color": "BLUE",
            "number": 11,
            "isJoker": false
        }
    ],
    "drawPileCount": 1,
    "currentPlayerUserId": "player-2",
    "currentTurnPlayerId": "player-2",
    "turnDeadline": null,
    "remainingTurnSeconds": null,
    "status": "ACTIVE",
    "createdAt": "2026-04-21T12:00:00Z",
    "startedAt": null,
    "finishedAt": null
}
```

Current backend draw behavior:

- verifies that the game exists and is `ACTIVE`
- verifies that the requesting user is part of the game
- verifies that the requesting user is the active player
- removes the first tile from the confirmed draw pile
- adds it to the acting player's confirmed rack
- advances `currentPlayerUserId` to the next player
- returns the updated confirmed `GameResponse`

The current draw implementation does **not** broadcast websocket game events yet.

---

## 4.6 `POST /api/games/{gameId}/reset-draft`

Resets the current draft back to the current confirmed game state.

### Purpose

Useful when:

- the active player wants to undo current draft changes
- the frontend wants to resync after a rejected move

### Request type

Can be either empty or use:

```json
{
    "playerId": "player-1"
}
```

### Response type

- `200 OK` → `TurnDraftResponse`
- `400 Bad Request` → `ApiErrorResponse`
- `403 Forbidden` → `ApiErrorResponse`
- `404 Not Found` → `ApiErrorResponse`

### Example success response

```json
{
    "gameId": "game-123",
    "playerUserId": "player-1",
    "draftBoard": [],
    "draftHand": [
        {
            "tileId": "tile-010",
            "color": "ORANGE",
            "number": 5,
            "isJoker": false
        }
    ],
    "version": 4
}
```

---

## 5. WebSocket API

## 5.1 Subscription topic

Clients should subscribe to:

```text
/topic/games/{gameId}
```

Example:

```text
/topic/games/game-123
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

---

## 5.2 `game.draft.updated`

Sent whenever the current live turn draft is stored and broadcast by the backend.

Current backend call sites:

- initial game start from `LobbyService.startLobby(...)`
- live draft updates from `TurnDraftService.updateDraft(...)`

### Event type

`GameDraftUpdatedEvent`

### Example payload

```json
{
    "type": "game.draft.updated",
    "gameId": "game-123",
    "playerId": "player-1",
    "draft": {
        "gameId": "game-123",
        "playerUserId": "player-1",
        "draftBoard": [
            {
                "boardSetId": "set-temp-1",
                "type": "UNRESOLVED",
                "tiles": [
                    {
                        "tileId": "tile-020",
                        "color": "RED",
                        "number": 4,
                        "isJoker": false
                    }
                ]
            }
        ],
        "draftHand": [
            {
                "tileId": "tile-010",
                "color": "ORANGE",
                "number": 5,
                "isJoker": false
            }
        ],
        "version": 3
    }
}
```

Fields:

- `type: "game.draft.updated"`
- `gameId: String`
- `playerId: String`
- `draft: TurnDraftResponse`

---

## 5.3 `game.updated`

Sent whenever the confirmed authoritative game state is broadcast by the backend.

Current backend call sites:

- initial game start from `LobbyService.startLobby(...)`

Later game flows may use the same event for additional confirmed-state updates.

### Event type

`GameUpdatedEvent`

### Example payload

```json
{
    "type": "game.updated",
    "gameId": "game-123",
    "game": {
        "gameId": "game-123",
        "lobbyId": "lobby-456",
        "players": [],
        "board": [],
        "drawPile": [],
        "drawPileCount": 40,
        "currentPlayerUserId": "player-2",
        "currentTurnPlayerId": "player-2",
        "turnDeadline": null,
        "remainingTurnSeconds": null,
        "status": "ACTIVE",
        "createdAt": "2026-04-21T12:00:00Z",
        "startedAt": null,
        "finishedAt": null
    }
}
```

Fields:

- `type: "game.updated"`
- `gameId: String`
- `game: GameResponse`

---

## 5.4 `turn.changed`

Sent when the active turn moves to the next player.

### Event type

`TurnChangedEvent`

### Example payload

```json
{
    "type": "turn.changed",
    "gameId": "game-123",
    "currentTurnPlayerId": "player-2"
}
```

Fields:

- `type: "turn.changed"`
- `gameId: String`
- `currentTurnPlayerId: String`

---

## 5.5 `turn.timed_out` (optional)

Sent when the active player's turn ends automatically because the timer expired.

### Event type

`TurnTimedOutEvent`

### Example payload

```json
{
    "type": "turn.timed_out",
    "gameId": "game-123",
    "previousTurnPlayerId": "player-1"
}
```

Fields:

- `type: "turn.timed_out"`
- `gameId: String`
- `previousTurnPlayerId: String`

Notes:

- this event is optional but useful for frontend messaging
- the frontend should still treat the following `game.updated`, `turn.changed`, and draft reset state as authoritative

---

## 5.6 `game.ended`

Sent when the game finishes.

### Event type

`GameEndedEvent`

### Example payload

```json
{
    "type": "game.ended",
    "gameId": "game-123",
    "winnerUserId": "player-1"
}
```

Fields:

- `type: "game.ended"`
- `gameId: String`
- `winnerUserId: String`

---

## 6. Recommended frontend handling

### On initial load

1. call `GET /api/games/{gameId}`
2. optionally call `GET /api/games/{gameId}/draft`
3. subscribe to `/topic/games/{gameId}`

### During a turn

- active player sends `PUT /api/games/{gameId}/draft`
- all clients receive `game.draft.updated`

### On draw

- active player sends `POST /api/games/{gameId}/draw`
- current backend returns the updated confirmed game state in the REST response
- current backend draw flow does not yet emit websocket follow-up events

### On end turn

- active player sends `POST /api/games/{gameId}/end-turn`
- all clients receive `game.updated`
- all clients receive `turn.changed`
- all clients may receive a fresh `game.draft.updated`

### On timeout

- clients may receive `turn.timed_out`
- clients should then trust the following updated game/draft state from the backend

### On reconnect

1. reconnect websocket
2. re-subscribe to `/topic/games/{gameId}`
3. reload confirmed game
4. optionally reload current draft
5. discard stale local assumptions if backend state differs

---

## 7. Summary

The game API uses:

- REST for commands and initial loading
- WebSocket for live shared state updates

Main REST endpoints:

- `GET /api/games/{gameId}`
- `GET /api/games/{gameId}/draft` (optional)
- `PUT /api/games/{gameId}/draft`
- `POST /api/games/{gameId}/end-turn`
- `POST /api/games/{gameId}/draw`
- `POST /api/games/{gameId}/reset-draft`

Main websocket events:

- `game.draft.updated`
- `game.updated`
- `turn.changed`
- `turn.timed_out` (optional)
- `game.ended`

Currently implemented REST endpoints in the backend controller:

- `GET /api/games/{gameId}`
- `PUT /api/games/{gameId}/draft`
- `POST /api/games/{gameId}/draw`

Currently emitted websocket events in backend service flow:

- `game.updated`
- `game.draft.updated`

The backend remains authoritative for:

- confirmed game state
- draft state
- rule validation
- turn progression
- timer / timeout behavior
