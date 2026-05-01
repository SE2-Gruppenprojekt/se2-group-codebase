# Sprint 2 - Frontend tasks

## GameScreen

- Rows need to be reorganisable (sorting) - Sabine (done)
  - sort should persist turns (tileRack)
- Tracking of ownership of tiles (is user allowed to pick up tiles?)

```kotlin
turnSnapshot {
	rackTile: List<Tile>
	boardSets: List<BoardSets>
	placedCounter: Int
}
```

## Heartbeat (Health related)

Simple scheduled task that periodically queries if the server is reachable and possibly displays success/failure as an icon in the TopBar.

## Match API - Networking

Implement the api requests for match while in game.

## TopBar (Vanessa)

- remove duplicate Settings button from Home
- not used in Settings
- not used in GameScreen

## Theme (Vanessa)

- remove all hardcoded colors with a proper ThemeState (no magic color numbers!)

# Extensions

## Emotes

User can press a thumbs up button and other clients see it.
