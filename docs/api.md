# API

## Overview

This document provides a high-level overview of the backend API currently available for frontend integration.

## Current Status

The backend API is still in an early stage.  
Some endpoints may return mocked or hardcoded sample data until real business logic is implemented.

## Base Path

Current API endpoints are expected under a base path similar to:

```text
/api
```

# API

## Overview

This document provides a high-level overview of the backend API currently available for frontend integration.

## Current Status

The backend API is still in an early stage.  
Some endpoints may return mocked or hardcoded sample data until real business logic is implemented.

## Base Path

Current API endpoints are expected under a base path similar to:

```text
/api
```

## Leaderboard Endpoint

### Purpose

The leaderboard endpoint provides sample ranking data for the game backend. It can be used by the Android frontend to test API integration, JSON parsing, list rendering, and leaderboard UI screens before real persistence and gameplay logic are implemented.

### Endpoint

```text
GET /api/leaderboard
```

### Description

Returns a list of mocked leaderboard entries sorted by rank in ascending order. Each entry represents one player and their current leaderboard statistics.

### Response Format

The endpoint returns a JSON array of leaderboard entry objects.

Each object contains the following fields:

| Field         | Type    | Description                               |
| ------------- | ------- | ----------------------------------------- |
| `rank`        | integer | The player's current leaderboard position |
| `playerName`  | string  | The display name of the player            |
| `score`       | integer | The player's total score                  |
| `gamesPlayed` | integer | The total number of games played          |
| `wins`        | integer | The total number of wins                  |

### Example Response

```json
[
    {
        "rank": 1,
        "playerName": "Julian",
        "score": 1840,
        "gamesPlayed": 42,
        "wins": 28
    },
    {
        "rank": 2,
        "playerName": "Erik",
        "score": 1765,
        "gamesPlayed": 40,
        "wins": 24
    },
    {
        "rank": 3,
        "playerName": "Vanessa",
        "score": 1690,
        "gamesPlayed": 38,
        "wins": 22
    },
    {
        "rank": 4,
        "playerName": "Stefan",
        "score": 1615,
        "gamesPlayed": 36,
        "wins": 20
    },
    {
        "rank": 5,
        "playerName": "Katrin",
        "score": 1580,
        "gamesPlayed": 35,
        "wins": 18
    },
    {
        "rank": 6,
        "playerName": "Miriam",
        "score": 1495,
        "gamesPlayed": 33,
        "wins": 16
    },
    {
        "rank": 7,
        "playerName": "Sabine",
        "score": 1430,
        "gamesPlayed": 31,
        "wins": 14
    },
    {
        "rank": 8,
        "playerName": "Alex",
        "score": 1375,
        "gamesPlayed": 29,
        "wins": 12
    },
    {
        "rank": 9,
        "playerName": "Nina",
        "score": 1310,
        "gamesPlayed": 27,
        "wins": 11
    },
    {
        "rank": 10,
        "playerName": "Lukas",
        "score": 1260,
        "gamesPlayed": 25,
        "wins": 9
    }
]
```

### Success Response

| Status Code | Meaning                                        |
| ----------- | ---------------------------------------------- |
| `200 OK`    | The leaderboard data was returned successfully |

### Notes

- The returned data is currently mocked sample data
- The endpoint is intended for initial frontend-backend integration
- Future versions may connect this endpoint to persistent storage and real game statistics
- The response structure may be extended later with additional fields such as player ID, avatar, win rate, or last active timestamp


# APP Update

## Shared Directory

**Path:**  
`/apps/shared/src/main/kotlin/shared/models`

A new directory has been introduced to hold shared code between the backend and frontend. This helps avoid code duplication and improves maintainability.

### Example

```kotlin
data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Int,
    val gamesPlayed: Int,
    val wins: Int
)
```

This class is required by both the frontend and backend. By keeping the structure consistent, JSON parsing via Jackson becomes automatic.

---

## Manifest Updates

### Required Permissions

These permissions are necessary to allow network communication:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Network Configuration

Additional configuration via `network_security_config.xml` enables extended network options (e.g., allowing cleartext traffic for local Spring Boot testing):

```xml
android:usesCleartextTraffic="true"
android:networkSecurityConfig="@xml/network_security_config"
```

---

## Dependencies

- **krossbow** → WebSockets & STOMP support
- **ktor** → HTTP client for RESTful API calls
