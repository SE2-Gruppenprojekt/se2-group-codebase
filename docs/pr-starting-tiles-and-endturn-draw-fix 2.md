![Scope](https://img.shields.io/badge/Scope-Backend%20%26%20Android-475569)
![Domain](https://img.shields.io/badge/Domain-Lobby%20Setup%20%26%20Turn%20Flow-64748b)
![Feature](https://img.shields.io/badge/Feature-Starting%20Tiles%20Config-6b7280)
![Fix](https://img.shields.io/badge/Fix-End%20Turn%20Auto%20Draw-52525b)
![API](https://img.shields.io/badge/API-Create%20Lobby%20Request-71717a)
![Gameplay](https://img.shields.io/badge/Gameplay-Rack%20Initialization%20%26%20Pass%20Turns-737373)
![Status](https://img.shields.io/badge/PR-Ready%20for%20Review-4b5563)

# PR Description: Starting Tiles Configuration and End-Turn Auto-Draw Fix

## Title

`feat(gameplay): add configurable starting tiles and fix auto-draw on pass end turns`

## Summary

This PR combines two closely related gameplay changes:

- it makes the number of starting tiles configurable during lobby creation
- it fixes the backend turn-finalization flow so a player who ends a turn
  without drawing a required tile receives that tile automatically

Together, these changes tighten the setup-to-gameplay contract: lobby settings
now define how many tiles each player starts with, and the backend now enforces
the expected draw behavior when a player passes without having drawn first.

## Why this PR exists

Two issues were present in the current flow:

1. the starting hand size was effectively fixed in backend game initialization
2. a player could end a pass turn without having drawn a tile first, and the
   backend did not always repair that state automatically

That led to two gaps:

- lobby setup could not control the initial rack size
- end-turn handling could produce an invalid gameplay outcome for pass turns

This PR closes both gaps.

## What changed

### 1. Configurable starting tiles

The create-lobby request now includes a `startingTiles` field. That value is
carried through shared models, Android request creation, backend lobby
creation, persistence, lobby responses, and finally game initialization.

The new request shape is:

```json
{
  "hostDisplayName": "Alice",
  "maxPlayers": 4,
  "isPrivate": false,
  "allowGuests": true,
  "requireInitialMeld": false,
  "startingTiles": 14
}
```

The backend then uses that configured value when distributing opening hands
instead of relying on a hardcoded tile count.

Conceptually, the initialization flow changed from:

```text
lobby created -> game starts -> every player receives 14 tiles
```

to:

```text
lobby created with startingTiles -> game starts -> every player receives that configured amount
```

### 2. End-turn pass auto-draw fix

The backend end-turn flow now detects the case where:

- the acting player did not already draw a tile
- the submitted turn is effectively a pass turn
- the draw pile still contains tiles

In that case, the backend automatically draws the next tile and adds it to the
player's rack before committing the confirmed game state.

That means a pass-turn flow now behaves like this:

```text
player submits unchanged board + unchanged rack
-> backend recognizes pass without prior draw
-> backend draws top tile
-> tile is added to the player's rack
-> tile is removed from draw pile
-> turn is committed
```

## Backend impact

### Lobby setup

The backend now:

- accepts `startingTiles` on create-lobby requests
- validates the value with a lower bound
- persists it on the lobby entity
- exposes it again in lobby responses
- uses it when generating initial player hands

This keeps the lobby configuration and the actual started game in sync.

### Turn handling

The backend end-turn flow now repairs pass turns that would otherwise skip the
required draw behavior.

The important behavioral rule is:

```text
if the player passes and has not drawn yet, the backend draws for them automatically
```

That is intentionally narrow. It does not redraw in arbitrary cases. It only
applies to pass-turn handling where the submitted draft leaves the board and the
acting player's rack effectively unchanged before the automatic draw.

## Android / shared impact

The Android create-lobby flow now sends `startingTiles` explicitly instead of
depending on backend-only defaults.

Shared request and response models were updated so both sides agree on the
contract.

That means:

- Android can choose the configured starting hand size
- backend receives and stores it
- lobby responses expose the configured value consistently

## Validation and safety

This PR is low-risk in shape but important in behavior:

- the starting-tile change extends an existing request/response contract in a
  straightforward way
- the auto-draw fix is scoped to pass-turn finalization and avoids changing
  unrelated turn-submission behavior

The backend auto-draw logic is also careful to update both:

- the acting player's rack
- the draw pile

so the confirmed game state remains internally consistent after the turn ends.

## Result

After this PR:

- lobby creation can define how many tiles players start with
- game initialization respects that configured number
- pass turns can no longer complete without the required draw being applied by
  the backend

This makes the game setup more flexible and the turn resolution logic more
robust.
