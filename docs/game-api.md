# Game API

This document describes the **game / match API** for the Rummikub feature.

It covers:

- REST API endpoints
- request and response DTO shapes
- websocket events
- example payloads

This document is intentionally focused on the **match/game feature only** and leaves lobby functionality out.

---

## 1. API Overview

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

## 2. Common Models

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

