# Backend Rule Validation

## TL;DR

The backend must validate **submitted turns**, not every temporary drag-and-drop state.

During a player's turn, the frontend is allowed to create **temporary invalid draft states** while rearranging tiles. The backend should therefore split validation into two levels:

1. **Draft update validation**
    - lightweight
    - protects integrity and ownership
    - allows temporary invalid intermediate structures
2. **Turn submission validation**
    - full Rummikub validation
    - resolves each final set as **group** or **run** inside backend set validation
    - checks whether the final submitted draft can legally become the next confirmed game state

In short:

- `PUT /api/games/{gameId}/draft` stores work in progress
- `POST /api/games/{gameId}/end-turn` validates and commits the final move

The most important design consequence in this document is:

> **The backend, not the frontend, decides whether a final set is a group or a run.**

That decision should live inside backend set validation rather than in a separate standalone classification service.

---

# 1. Recommended Layered Validation Architecture

A clean validation design has multiple levels.
Each level answers a different question.

This layered architecture also defines **when** validation runs. Draft updates
should stay narrow and storage-safe, while full Rummikub legality belongs to
final turn submission.

The backend also owns final set resolution. Because the frontend does **not**
send a trustworthy final `BoardSetType`, submitted sets must be resolved from
their tiles during final validation, not during draft updates.

## Level 1: Ownership and lifecycle validation

Question:

> Is this user allowed to update or submit this draft right now?

Examples:

- game exists
- draft exists
- game is active
- draft is active/open
- caller is the active player
- caller matches `draft.playerUserId`
- request targets the correct game/turn

This can live in `TurnDraftService` or in small helper validators.

### Example

```kotlin
fun validateUserMayUpdateDraft(game: Game, draft: TurnDraft, userId: String) {
    require(game.status == GameStatus.ACTIVE) {
        "Game is not active"
    }
    require(draft.status == TurnDraftStatus.ACTIVE) {
        "Draft is not active"
    }
    require(game.currentPlayerUserId == userId) {
        "Only the active player may update the draft"
    }
    require(draft.playerUserId == userId) {
        "Draft does not belong to the acting player"
    }
}
```

This still is not Rummikub rule validation. It is lifecycle and ownership validation.

---

## Level 2: Tile integrity / conservation validation

Question:

> Does the submitted draft still contain exactly the same tile universe that the player is allowed to rearrange?

This is one of the most important layers.
Without it, a client could:

- drop tiles
- duplicate tiles
- invent tiles
- steal tiles from nowhere

### Core idea

Compare:

**Allowed tiles during this turn**

- confirmed board tiles
- active player's confirmed rack tiles

against:

**Submitted draft tiles**

- candidate draft board tiles
- candidate draft rack tiles

Those two multisets must match exactly.

### Responsibilities of `TileConservationService`

It should detect:

- missing tiles
- duplicated tiles
- extra tiles
- tiles with changed identity

### Example

```kotlin
class TileConservationService {

    fun validate(
        confirmedGame: Game,
        actingPlayerId: String,
        candidateDraft: TurnDraft
    ): ValidationResult {
        // The active player may rearrange the current confirmed board plus their own confirmed rack.
        val allowedTileIds = (
            confirmedGame.boardSets.flatMap { it.tiles } +
            confirmedGame.players.first { it.userId == actingPlayerId }.rackTiles
        ).groupingBy { it.tileId }
            .eachCount()

        // After editing, the draft must still contain exactly that same tile universe.
        val submittedTileIds = (
            candidateDraft.boardSets.flatMap { it.tiles } +
            candidateDraft.rackTiles
        ).groupingBy { it.tileId }
            .eachCount()

        // Conservation is an exact multiset equality check, not a subset check.
        return if (allowedTileIds == submittedTileIds) {
            valid()
        } else {
            invalid("Submitted draft does not preserve the allowed tile set")
        }
    }
}
```

### Important note

This validator does **not** check whether the board is a legal Rummikub board.
It only checks whether the player rearranged the correct tiles.

---

## Set resolution inside set validation

Question:

> If this set is part of a final submitted board, is it legal as a group, legal as a run, ambiguous, or invalid?

This is the key new layer.

Because the frontend does not send a trustworthy final type, the backend must
attempt both legal interpretations inside `SetValidationService`. This
resolution happens only during final submitted-turn validation. Draft updates
remain permissive and do not require sets to be fully resolved already.

The service should answer:

- is this set valid as a group?
- is this set valid as a run?
- is it invalid as both?
- is it valid as both and therefore ambiguous?

A good practical strategy is:

- let `GroupValidationService` validate the set
- let `RunValidationService` validate the set
- let `SetValidationService` decide based on those two results

This avoids a separate classification pass that duplicates much of the same reasoning.

---

# `GroupValidationService`

A group usually means:

- same number
- different colors
- valid size
- joker handling if supported

#### Typical checks

- minimum size
- maximum size
- all non-joker tiles have the same number
- non-joker colors are unique
- joker substitution is allowed only under your defined rules
- all-joker groups are rejected if your rules do not allow them

### Example pseudo-code

```kotlin
fun validateGroup(set: BoardSet): ValidationResult {
    if (set.tiles.size < 3) {
        return invalid("Group must contain at least 3 tiles")
    }

    val nonJokers = set.tiles.filterNot { it.isJoker }
    val numbers = nonJokers.map { it.number }.distinct()
    val colors = nonJokers.map { it.color }

    if (numbers.size > 1) {
        return invalid("Group tiles must have the same number")
    }

    if (colors.size != colors.distinct().size) {
        return invalid("Group tiles must have unique colors")
    }

    return valid()
}
```

---

### `RunValidationService`

A run usually means:

- same color
- ascending numbers
- valid size
- joker handling if supported

#### Typical checks

- minimum size
- all non-jokers share the same color
- numbers are strictly ascending
- no duplicates
- jokers can only fill legal gaps
- all-joker runs are rejected if your rules do not allow them

### Example pseudo-code

```kotlin
fun validateRun(set: BoardSet): ValidationResult {
    if (set.tiles.size < 3) {
        return invalid("Run must contain at least 3 tiles")
    }

    val nonJokers = set.tiles.filterNot { it.isJoker }
    val colors = nonJokers.map { it.color }.distinct()

    if (colors.size > 1) {
        return invalid("Run tiles must have the same color")
    }

    val sortedNumbers = nonJokers.map { it.number }.sorted()
    if (sortedNumbers != sortedNumbers.distinct()) {
        return invalid("Run must not contain duplicate numbers")
    }

    // Additional gap/joker handling would go here

    return valid()
}
```

---

### `SetValidationService`

This service should not trust a pre-existing `BoardSetType` from the client.
Instead, it should validate the set as both a group and a run and then decide the outcome.

### Example

```kotlin
class SetValidationService(
    private val groupValidationService: GroupValidationService,
    private val runValidationService: RunValidationService
) {
    fun validate(set: BoardSet): ValidationResult {
        val groupResult = groupValidationService.validate(set)
        val runResult = runValidationService.validate(set)

        return when {
            groupResult.isValid && !runResult.isValid -> valid()
            runResult.isValid && !groupResult.isValid -> valid()
            groupResult.isValid && runResult.isValid -> invalid(
                "Set is ambiguous because it is valid as both a group and a run"
            )
            else -> invalid(
                violations = groupResult.violations + runResult.violations
            )
        }
    }
}
```

This is the core change from the earlier architecture.

---

## Board-level validation

Question:

> Is the entire board valid as a final board arrangement?

This layer validates all sets together.
It usually does not add many new rules itself; it mainly aggregates set-level validators.

### Responsibilities of `BoardValidationService`

- iterate through all board sets
- call `SetValidationService` for each set
- aggregate violations
- return a combined `ValidationResult`

### Example

```kotlin
class BoardValidationService(
    private val setValidationService: SetValidationService
) {
    fun validate(boardSets: List<BoardSet>): ValidationResult {
        val violations = mutableListOf<RuleViolation>()

        boardSets.forEach { set ->
            val result = setValidationService.validate(set)
            violations += result.violations
        }

        return if (violations.isEmpty()) valid() else invalid(violations)
    }
}
```

This layer is mainly used during **end-turn**, not during every draft update.

---

## Turn-level / game-context validation

Question:

> Is this submitted draft a legal completed move in the context of this game and this player's history?

At this level, rules depend on the whole game context.

### Example responsibilities

- initial meld requirements
- first move scoring threshold
- restrictions on whether board tiles may be reused on first move
- active player ownership
- turn timing state if relevant
- draw/end-turn rules if applicable

---

### 1.6.1 `FirstMoveValidationService`

This validator handles rules that only apply if the player has not yet completed the initial meld.

Typical responsibilities:

- require minimum total meld score on first completed turn
- optionally enforce that only rack tiles may be used on first move
- skip this validation for players who have already completed the initial meld

### Example pseudo-code

```kotlin
fun validateFirstMove(game: Game, draft: TurnDraft, player: GamePlayer): ValidationResult {
    if (player.hasCompletedInitialMeld) {
        return valid()
    }

    val points = calculateNewlyPlacedPoints(game, draft, player.userId)
    if (points < 30) {
        return invalid("Initial meld must score at least 30 points")
    }

    return valid()
}
```

---

## Top-level rule orchestration

Question:

> Can this submitted draft legally become the next confirmed game state?

This is the top-level orchestration layer.

### `RummikubRuleService`

This service should:

- coordinate lower-level validators
- aggregate their violations
- return a final `ValidationResult`
- stay thin and orchestration-focused

### Example

```kotlin
class RummikubRuleService(
    private val tileConservationService: TileConservationService,
    private val boardValidationService: BoardValidationService,
    private val firstMoveValidationService: FirstMoveValidationService
) {

    fun validateSubmittedDraft(
        confirmedGame: Game,
        draft: TurnDraft,
        actingPlayer: GamePlayer
    ): ValidationResult {
        val violations = mutableListOf<RuleViolation>()

        violations += tileConservationService
            .validate(confirmedGame, actingPlayer.userId, draft)
            .violations

        violations += boardValidationService
            .validate(draft.boardSets)
            .violations

        violations += firstMoveValidationService
            .validate(confirmedGame, draft, actingPlayer)
            .violations

        return if (violations.isEmpty()) valid() else invalid(violations)
    }
}
```

This service is the final gate before state commitment.

---

# 2. Recommended Responsibility Split Across Services

A clean backend design should separate the following concerns.

## 2.1 `GameService`

Owns confirmed game state.

Responsibilities:

- load confirmed game
- save confirmed game
- perform confirmed-state transitions
- expose simple game-level queries

Should **not** own live draft mutation logic.

---

## 2.2 `TurnDraftService`

Owns live draft state.

Responsibilities:

- load active draft
- update active draft
- reset draft from confirmed state
- create initial draft
- create next-turn draft
- save / close / replace draft
- validate caller ownership for draft operations

This is the main service for the draft update endpoint.

---

## 2.3 `GameBroadcastService`

Owns websocket event emission only.

Responsibilities:

- broadcast draft updates
- broadcast committed game updates
- broadcast turn changes
- broadcast game ended events

Should not contain game rule logic.

---

## 2.4 Rule validation services

Own rule validation only.

Responsibilities:

- set resolution inside set validation
- set legality
- board legality
- tile conservation
- first move checks
- orchestration of submitted draft validation

Should not own persistence or websocket broadcasting.

---

# 3. Full Validation Flow From Start to Finish

This section describes the full validation flow through the backend.

The split is simple:

- `PUT /draft` stores work in progress and runs only lightweight safety checks
- `POST /end-turn` runs full rule validation before committing state

---

## 3.1 Draft update flow (`PUT /api/games/{gameId}/draft`)

Purpose:

> Store work in progress safely.

### Step-by-step flow

1. Controller receives `UpdateDraftRequest`
2. DTO validation checks transport-level structure
3. `TurnDraftService.updateDraft(...)` is called
4. Service loads confirmed game
5. Service loads current draft
6. Service validates game/draft ownership and lifecycle
7. Request is mapped to a candidate draft state
8. `TileConservationService` validates tile integrity
9. Candidate draft is persisted
10. `GameBroadcastService.broadcastDraftUpdated(...)` is called
11. Controller returns updated `TurnDraftResponse`

### Important property of this flow

This flow does **not** require the board to already be fully legal.
It only ensures that the draft is safe to store.

### Example pseudo-code

```kotlin
fun updateDraft(gameId: String, userId: String, request: UpdateDraftRequest): TurnDraft {
    val game = gameService.getGame(gameId)
    val draft = getDraft(gameId)

    validateUserMayUpdateDraft(game, draft, userId)

    val candidateDraft = draftMapper.applyUpdate(
        existingDraft = draft,
        request = request
    )

    val integrity = tileConservationService.validate(
        confirmedGame = game,
        actingPlayerId = userId,
        candidateDraft = candidateDraft
    )
    if (!integrity.isValid) {
        throw InvalidDraftStateException(integrity.violations)
    }

    val savedDraft = draftRepository.save(candidateDraft)
    gameBroadcastService.broadcastDraftUpdated(savedDraft)
    return savedDraft
}
```

---

## 3.2 Turn submission flow (`POST /api/games/{gameId}/end-turn`)

Purpose:

> Validate and commit the final move.

### Step-by-step flow

1. Controller receives end-turn request
2. `GameService` loads confirmed game
3. `TurnDraftService` loads active draft
4. Backend validates ownership and lifecycle again
5. `RummikubRuleService.validateSubmittedDraft(...)` is called
6. The orchestrator invokes layered validators:
    - tile conservation
    - board validation
    - set resolution and set validation inside board validation
    - first move validation
    - any later turn-level validators
7. If validation fails:
    - move is rejected
    - draft remains or can be reset
    - controller returns error response
8. If validation succeeds:
    - confirmed game is updated from draft
    - scores / game end state are updated if needed
    - next player is calculated
    - next draft is created
    - updated confirmed game is persisted
    - next draft is persisted
    - websocket events are broadcast
9. Controller returns success response

### Example pseudo-code

```kotlin
fun endTurn(gameId: String, userId: String): Game {
    val game = gameService.getGame(gameId)
    val draft = turnDraftService.getDraft(gameId)

    validateUserMaySubmitDraft(game, draft, userId)

    val actingPlayer = game.players.first { it.userId == userId }

    val validation = rummikubRuleService.validateSubmittedDraft(
        confirmedGame = game,
        draft = draft,
        actingPlayer = actingPlayer
    )

    if (!validation.isValid) {
        throw InvalidMoveException(validation.violations)
    }

    val updatedGame = commitDraftToConfirmedGame(game, draft)
    val nextDraft = turnDraftService.createNextDraft(updatedGame)

    gameService.save(updatedGame)
    turnDraftService.replaceWithNextDraft(updatedGame.gameId, nextDraft)

    gameBroadcastService.broadcastGameUpdated(updatedGame)
    gameBroadcastService.broadcastTurnChanged(updatedGame, nextDraft)

    return updatedGame
}
```

---

## 3.3 `commitDraftToConfirmedGame(...)`

Once end-turn validation succeeds, the backend still needs one final step:

> convert the validated draft into the next confirmed game state

This step should stay separate from rule validation itself.

Validation answers:

- is this draft legal to commit?

`commitDraftToConfirmedGame(...)` answers:

- what does the next confirmed game state look like now that the draft is accepted?

Typical responsibilities:

- replace the confirmed board with the submitted draft board
- replace the acting player's confirmed rack with the submitted draft rack
- preserve the other players unchanged
- keep the same game identity and lobby identity
- keep or update status fields as needed
- leave turn advancement and next-draft creation to the surrounding orchestration layer

### Example pseudo-code

```kotlin
fun commitDraftToConfirmedGame(
    confirmedGame: Game,
    draft: TurnDraft
): Game {
    val updatedPlayers = confirmedGame.players.map { player ->
        if (player.userId == draft.playerUserId) {
            player.copy(rackTiles = draft.rackTiles)
        } else {
            player
        }
    }

    return confirmedGame.copy(
        players = updatedPlayers,
        boardSets = draft.boardSets
    )
}
```

### Important boundary

This function should assume the draft has already passed full validation.
It should not redo group/run validation, board validation, or first-move validation.

Its job is state transition, not rule checking.

---

# 4. Validation Result Models

## 4.1 `ValidationResult`

A rule validation should return a structured object, not just a boolean.

### Example

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val violations: List<RuleViolation> = emptyList()
)
```

This lets the backend:

- aggregate multiple errors
- return detailed UI feedback
- keep validation composable

---

## 4.2 `RuleViolation`

A `RuleViolation` should be the standard structured result object for **game-rule validation failures**.

It should **not** be used for every possible backend failure.
For example:

- missing game
- missing draft
- unauthorized player
- stale draft version
- broken internal persistence state

are usually better represented as exceptions.

`RuleViolation` is best used for **collectable validation problems** where the backend wants to continue validating and report multiple issues together, especially during **end-turn validation**.

### Purpose

A `RuleViolation` should:

- identify what rule failed
- provide a human-readable explanation
- be stable enough for frontend handling and debugging

### Recommended class shape

```kotlin
data class RuleViolation(
    val code: String,
    val message: String,
    val setIndex: Int? = null,
    val tileIds: List<String> = emptyList()
)
```

### Field meaning

#### `code`

A machine-readable stable identifier for the violation.

Examples:

- `SET_NOT_CLASSIFIABLE`
- `SET_CLASSIFICATION_AMBIGUOUS`
- `GROUP_MIN_SIZE`
- `GROUP_DUPLICATE_COLOR`
- `RUN_COLOR_MISMATCH`
- `RUN_DUPLICATE_NUMBER`
- `RUN_NOT_ASCENDING`
- `INITIAL_MELD_TOO_SMALL`
- `TILE_DUPLICATED`
- `TILE_MISSING`

The `code` should be stable and predictable so the frontend can react to it if needed.

#### `message`

A human-readable explanation of the problem.

Examples:

- `"Run tiles must have the same color"`
- `"Initial meld must score at least 30 points"`
- `"Submitted draft does not preserve the allowed tile set"`

This is what you would usually log or return directly in an API error body.

#### `setIndex`

An optional numeric shortcut for the affected set.

This is helpful when the frontend wants to highlight one full board set directly.

#### `tileIds`

An optional list of affected tile identifiers.

This is useful when:

- the same set contains many tiles but only some are problematic
- the frontend wants to highlight specific tiles
- debugging tile conservation issues

### Example instance

```kotlin
RuleViolation(
    code = "RUN_COLOR_MISMATCH",
    message = "Run tiles must have the same color",
    setIndex = 1,
    tileIds = listOf("tile-17", "tile-22")
)
```

### Recommended usage

Use `RuleViolation` when:

- validating a final submitted draft
- validating multiple sets on the board
- validating first-move rules
- aggregating multiple rule failures into one result

Do **not** use `RuleViolation` for hard-stop use-case errors that should abort immediately.
Those are better represented as exceptions.

### Good pairing with `ValidationResult`

```kotlin
data class ValidationResult(
    val violations: List<RuleViolation> = emptyList()
) {
    val isValid: Boolean get() = violations.isEmpty()
}
```

This lets validators contribute violations independently while keeping the final result simple.

---

# 5. Recommended Validation Service Hierarchy

A strong separation would look like this:

```text
RummikubRuleService
├── TileConservationService
├── BoardValidationService
│   └── SetValidationService
│       ├── GroupValidationService
│       └── RunValidationService
└── FirstMoveValidationService
```

And separately:

```text
TurnDraftService
├── ownership / lifecycle validation
├── draft persistence
└── calls TileConservationService on draft updates
```

This keeps:

- draft mutation separate from rule evaluation
- group/run detail separate from top-level set resolution
- small validators small
- orchestration explicit

---

# 6. Practical Validation Scenarios

## 6.1 Valid draft update, temporarily invalid board

Player temporarily breaks a valid run while still rearranging tiles.
The backend should accept and persist the draft without running full board validation.

---

## 6.2 Invalid draft update because a tile was duplicated

If a submitted draft contains the same tile twice, the backend should reject it as a tile-conservation failure and keep the previous draft unchanged.

---

## 6.3 Invalid end-turn because a set cannot be resolved as a legal group or run

If a final submitted set is neither a legal group nor a legal run, the backend should reject end-turn and return a set-resolution or set-validation error.

---

## 6.4 Invalid end-turn because final board contains illegal run

If the final board contains an illegal run, such as duplicate numbers or mismatched colors, the backend should reject end-turn and keep the draft uncommitted.

---

## 6.5 Invalid first move because initial meld score is too low

If a player's first submitted meld does not reach the required minimum, the backend should reject end-turn with a first-move-specific violation.

---

# 7. Recommended Implementation Order

If implementing incrementally, a good order is:

1. `ValidationResult` and `RuleViolation`
2. `TileConservationService`
3. `GroupValidationService`
4. `RunValidationService`
5. `SetValidationService`
6. `BoardValidationService`
7. `FirstMoveValidationService`
8. `RummikubRuleService`
9. integrate full validation into end-turn flow

This order works well because:

- draft update safety depends heavily on tile conservation
- end-turn now depends on trying both group and run validation inside set validation
- first-move validation depends on board validity already existing

---

# 8. Final Takeaway

The backend should treat draft updates and turn submission as two different
operations.

- `PUT /draft` protects ownership, lifecycle, and tile integrity
- `POST /end-turn` is the single strict rule-validation gate
- final set type is resolved by the backend during submitted-turn validation

That keeps editing permissive during the turn while keeping the confirmed game
state strict and fully validated before it changes.
