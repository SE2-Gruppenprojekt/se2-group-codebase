# Game API

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
4. backend broadcasts draft and game updates over websocket
5. active player submits the turn through REST
6. backend validates, commits, and broadcasts the new state

---

## 3. Common Models

The following response types are used repeatedly across the API.

### `TileResponse`

```json
{
    "id": "tile-001",
    "color": "RED",
    "number": 7,
    "isJoker": false
}
```

Fields:

- `id: String`
- `color: String | null`
- `number: Int | null`
- `isJoker: Boolean`

Notes:

- jokers may use `color = null` and `number = null`
- tile IDs must be unique even if tiles have the same color and number

---

### `BoardSetResponse`

```json
{
    "id": "set-001",
    "type": "GROUP",
    "tiles": [
        {
            "id": "tile-001",
            "color": "RED",
            "number": 7,
            "isJoker": false
        },
        {
            "id": "tile-002",
            "color": "BLUE",
            "number": 7,
            "isJoker": false
        },
        {
            "id": "tile-003",
            "color": "BLACK",
            "number": 7,
            "isJoker": false
        }
    ]
}
```

Fields:

- `id: String`
- `type: "RUN" | "GROUP" | "UNRESOLVED"`
- `tiles: List<TileResponse>`

Notes:

- `UNRESOLVED` is mainly used during live draft editing
- final authoritative validation happens in the backend

---

### `GamePlayerResponse`

```json
{
    "userId": "player-1",
    "displayName": "Julian",
    "hand": [
        {
            "id": "tile-010",
            "color": "YELLOW",
            "number": 5,
            "isJoker": false
        }
    ],
    "hasPlayedInitialMeld": false,
    "score": 0
}
```

Fields:

- `userId: String`
- `displayName: String`
- `hand: List<TileResponse>`
- `hasPlayedInitialMeld: Boolean`
- `score: Int`

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
            "hand": [
                {
                    "id": "tile-010",
                    "color": "YELLOW",
                    "number": 5,
                    "isJoker": false
                }
            ],
            "hasPlayedInitialMeld": false,
            "score": 0
        },
        {
            "userId": "player-2",
            "displayName": "Alex",
            "hand": [
                {
                    "id": "tile-011",
                    "color": "RED",
                    "number": 9,
                    "isJoker": false
                }
            ],
            "hasPlayedInitialMeld": false,
            "score": 0
        }
    ],
    "board": [
        {
            "id": "set-001",
            "type": "RUN",
            "tiles": [
                {
                    "id": "tile-020",
                    "color": "RED",
                    "number": 4,
                    "isJoker": false
                },
                {
                    "id": "tile-021",
                    "color": "RED",
                    "number": 5,
                    "isJoker": false
                },
                {
                    "id": "tile-022",
                    "color": "RED",
                    "number": 6,
                    "isJoker": false
                }
            ]
        }
    ],
    "drawPileCount": 54,
    "currentTurnPlayerId": "player-1",
    "turnDeadline": "2026-04-21T12:30:00Z",
    "remainingTurnSeconds": 42,
    "status": "ACTIVE",
    "winnerUserId": null
}
```

Fields:

- `gameId: String`
- `players: List<GamePlayerResponse>`
- `board: List<BoardSetResponse>`
- `drawPileCount: Int`
- `currentTurnPlayerId: String`
- `turnDeadline: String | null` (ISO-8601 timestamp)
- `remainingTurnSeconds: Int | null`
- `status: "ACTIVE" | "FINISHED"`
- `winnerUserId: String | null`

Notes:

- using `drawPileCount` instead of returning the full pile is usually enough for the frontend
- `remainingTurnSeconds` is optional but useful for reconnect recovery and timer display

---

### `TurnDraftResponse`

This is the current live editable draft state.

```json
{
    "gameId": "game-123",
    "playerId": "player-1",
    "draftBoard": [
        {
            "id": "set-001",
            "type": "RUN",
            "tiles": [
                {
                    "id": "tile-020",
                    "color": "RED",
                    "number": 4,
                    "isJoker": false
                },
                {
                    "id": "tile-021",
                    "color": "RED",
                    "number": 5,
                    "isJoker": false
                }
            ]
        },
        {
            "id": "set-temp-1",
            "type": "UNRESOLVED",
            "tiles": [
                {
                    "id": "tile-022",
                    "color": "RED",
                    "number": 6,
                    "isJoker": false
                }
            ]
        }
    ],
    "draftHand": [
        {
            "id": "tile-010",
            "color": "YELLOW",
            "number": 5,
            "isJoker": false
        }
    ],
    "version": 3,
    "status": "ACTIVE"
}
```

Fields:

- `gameId: String`
- `playerId: String`
- `draftBoard: List<BoardSetResponse>`
- `draftHand: List<TileResponse>`
- `version: Long`
- `status: "ACTIVE" | "SUBMITTED" | "CANCELLED"`

---

### `ErrorResponse`

REST endpoints should return a small consistent error shape.

```json
{
    "code": "INVALID_MOVE",
    "message": "The submitted draft is not a valid Rummikub board",
    "timestamp": "2026-04-21T12:31:00Z"
}
```

Fields:

- `code: String`
- `message: String`
- `timestamp: String`

Common example error codes:

- `INVALID_MOVE`
- `GAME_NOT_FOUND`
- `DRAFT_NOT_FOUND`
- `TURN_EXPIRED`
- `FORBIDDEN_ACTION`
- `INVALID_STATE`

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
- `404 Not Found` → `ErrorResponse`

### Example response

```json
{
    "gameId": "game-123",
    "players": [
        {
            "userId": "player-1",
            "displayName": "Julian",
            "hand": [],
            "hasPlayedInitialMeld": true,
            "score": 0
        },
        {
            "userId": "player-2",
            "displayName": "Alex",
            "hand": [],
            "hasPlayedInitialMeld": false,
            "score": 0
        }
    ],
    "board": [],
    "drawPileCount": 42,
    "currentTurnPlayerId": "player-1",
    "turnDeadline": "2026-04-21T12:30:00Z",
    "remainingTurnSeconds": 50,
    "status": "ACTIVE",
    "winnerUserId": null
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
- `404 Not Found` → `ErrorResponse`

### Example response

```json
{
    "gameId": "game-123",
    "playerId": "player-1",
    "draftBoard": [],
    "draftHand": [],
    "version": 2,
    "status": "ACTIVE"
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

```json
{
    "playerId": "player-1",
    "draftBoard": [
        {
            "id": "set-temp-1",
            "type": "UNRESOLVED",
            "tiles": [
                {
                    "id": "tile-020",
                    "color": "RED",
                    "number": 4,
                    "isJoker": false
                }
            ]
        }
    ],
    "draftHand": [
        {
            "id": "tile-010",
            "color": "YELLOW",
            "number": 5,
            "isJoker": false
        }
    ],
    "version": 2
}
```

Fields:

- `playerId: String`
- `draftBoard: List<BoardSetResponse>`
- `draftHand: List<TileResponse>`
- `version: Long`

### Response type

- `200 OK` → `TurnDraftResponse`
- `400 Bad Request` → `ErrorResponse`
- `403 Forbidden` → `ErrorResponse`
- `404 Not Found` → `ErrorResponse`
- `409 Conflict` → `ErrorResponse`

### Example success response

```json
{
    "gameId": "game-123",
    "playerId": "player-1",
    "draftBoard": [
        {
            "id": "set-temp-1",
            "type": "UNRESOLVED",
            "tiles": [
                {
                    "id": "tile-020",
                    "color": "RED",
                    "number": 4,
                    "isJoker": false
                }
            ]
        }
    ],
    "draftHand": [
        {
            "id": "tile-010",
            "color": "YELLOW",
            "number": 5,
            "isJoker": false
        }
    ],
    "version": 3,
    "status": "ACTIVE"
}
```

### Example error response

```json
{
    "code": "FORBIDDEN_ACTION",
    "message": "Only the active player can update the draft",
    "timestamp": "2026-04-21T12:31:00Z"
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
- `400 Bad Request` → `ErrorResponse`
- `403 Forbidden` → `ErrorResponse`
- `404 Not Found` → `ErrorResponse`
- `409 Conflict` → `ErrorResponse`

### Example success response

```json
{
    "gameId": "game-123",
    "players": [
        {
            "userId": "player-1",
            "displayName": "Julian",
            "hand": [
                {
                    "id": "tile-010",
                    "color": "YELLOW",
                    "number": 5,
                    "isJoker": false
                }
            ],
            "hasPlayedInitialMeld": true,
            "score": 0
        },
        {
            "userId": "player-2",
            "displayName": "Alex",
            "hand": [
                {
                    "id": "tile-011",
                    "color": "RED",
                    "number": 9,
                    "isJoker": false
                }
            ],
            "hasPlayedInitialMeld": false,
            "score": 0
        }
    ],
    "board": [
        {
            "id": "set-001",
            "type": "GROUP",
            "tiles": [
                {
                    "id": "tile-001",
                    "color": "RED",
                    "number": 7,
                    "isJoker": false
                },
                {
                    "id": "tile-002",
                    "color": "BLUE",
                    "number": 7,
                    "isJoker": false
                },
                {
                    "id": "tile-003",
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
    "code": "INVALID_MOVE",
    "message": "The submitted draft is not a valid Rummikub board",
    "timestamp": "2026-04-21T12:31:00Z"
}
```

---

## 4.5 `POST /api/games/{gameId}/draw`

Lets the active player draw a tile from the draw pile.

### Purpose

Used when the player cannot or chooses not to play tiles.

### Request type

Can be either empty or use a small request DTO like:

```json
{
    "playerId": "player-1"
}
```

### Response type

- `200 OK` → `GameResponse`
- `400 Bad Request` → `ErrorResponse`
- `403 Forbidden` → `ErrorResponse`
- `404 Not Found` → `ErrorResponse`
- `409 Conflict` → `ErrorResponse`

### Example success response

```json
{
    "gameId": "game-123",
    "players": [
        {
            "userId": "player-1",
            "displayName": "Julian",
            "hand": [
                {
                    "id": "tile-999",
                    "color": "BLACK",
                    "number": 12,
                    "isJoker": false
                }
            ],
            "hasPlayedInitialMeld": true,
            "score": 0
        }
    ],
    "board": [],
    "drawPileCount": 40,
    "currentTurnPlayerId": "player-2",
    "turnDeadline": "2026-04-21T12:31:30Z",
    "remainingTurnSeconds": 60,
    "status": "ACTIVE",
    "winnerUserId": null
}
```

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
- `400 Bad Request` → `ErrorResponse`
- `403 Forbidden` → `ErrorResponse`
- `404 Not Found` → `ErrorResponse`

### Example success response

```json
{
    "gameId": "game-123",
    "playerId": "player-1",
    "draftBoard": [],
    "draftHand": [
        {
            "id": "tile-010",
            "color": "YELLOW",
            "number": 5,
            "isJoker": false
        }
    ],
    "version": 4,
    "status": "ACTIVE"
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

Sent whenever the active player updates the draft.

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
        "playerId": "player-1",
        "draftBoard": [
            {
                "id": "set-temp-1",
                "type": "UNRESOLVED",
                "tiles": [
                    {
                        "id": "tile-020",
                        "color": "RED",
                        "number": 4,
                        "isJoker": false
                    }
                ]
            }
        ],
        "draftHand": [
            {
                "id": "tile-010",
                "color": "YELLOW",
                "number": 5,
                "isJoker": false
            }
        ],
        "version": 3,
        "status": "ACTIVE"
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

Sent whenever the confirmed authoritative game state changes.

Typical causes:

- valid turn commit
- draw action if confirmed state changes
- finished game state

### Event type

`GameUpdatedEvent`

### Example payload

```json
{
    "type": "game.updated",
    "gameId": "game-123",
    "game": {
        "gameId": "game-123",
        "players": [],
        "board": [],
        "drawPileCount": 40,
        "currentTurnPlayerId": "player-2",
        "turnDeadline": "2026-04-21T12:31:30Z",
        "remainingTurnSeconds": 60,
        "status": "ACTIVE",
        "winnerUserId": null
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

The backend remains authoritative for:

- confirmed game state
- draft state
- rule validation
- turn progression
- timer / timeout behavior
