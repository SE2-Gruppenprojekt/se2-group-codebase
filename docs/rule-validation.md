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
    - classifies each final set as **group** or **run** on the backend
    - checks whether the final submitted draft can legally become the next confirmed game state

In short:

- `PUT /api/games/{gameId}/draft` stores work in progress
- `POST /api/games/{gameId}/end-turn` validates and commits the final move

The most important design consequence in this document is:

> **The backend, not the frontend, decides whether a final set is a group or a run.**

That means the validation system must include an explicit **set classification** step before full set validation can happen.

---

# 1. Why Rule Validation Must Be Layered

Rummikub turns are not simple single-step actions.
A player may:

- take tiles out of an existing set
- temporarily break a valid run or group
- move those tiles elsewhere
- create new sets
- only make the board fully valid again at the very end

That means the backend **cannot require the draft to always be valid during editing**.
If it did, many legal moves would become impossible to perform.

So the backend must treat the game state as two different things:

## 1.1 Confirmed game state

The confirmed game state is the last valid committed state of the match.

It contains things like:

- official board
- official hands
- active player
- draw pile
- game status
- score state
- winner if the game is finished

This state must always be internally valid.

## 1.2 Live turn draft

The live turn draft is the temporary in-progress state of the active player's current move.

It contains things like:

- temporary board arrangement
- temporary rack state
- draft version
- draft status
- ownership metadata

This state may be temporarily invalid while the player is editing.

That is why validation must be split into:

- **draft safety validation** during updates
- **full rule validation** during turn submission

---

# 2. Validation Goals

The backend rule validation system should guarantee the following:

## 2.1 Integrity goals

The backend must ensure that:

- no tile disappears
- no tile is duplicated
- no tile is invented
- only the active player can change the active draft
- only one active draft exists for the current game
- stale draft updates do not overwrite newer state

## 2.2 Game-rule goals

When a player ends the turn, the backend must ensure that:

- the final board consists only of legal sets
- each set can be classified as either a legal group or a legal run
- the move respects first-move rules if applicable
- the tile movement is legal relative to the confirmed state and active player's rack
- the game can transition safely to the next turn or the finished state

## 2.3 Architectural goals

The validation system should be:

- layered
- testable
- composable
- explicit in responsibility boundaries
- usable by services without controller-level logic duplication
- robust even when the frontend does **not** send set type information

---

# 3. Validation Boundaries: When to Validate What

## 3.1 On draft update

For `PUT /api/games/{gameId}/draft`, the backend should validate only:

- request structure
- game exists
- active draft exists
- caller is the active player
- caller owns the draft
- draft status allows update
- tile conservation / integrity
- optional version match / optimistic locking
- optional lightweight structural sanity checks

The backend should **not** fully validate:

- whether all sets are valid groups/runs
- whether the board is fully legal already
- whether initial meld conditions are already satisfied
- whether every temporary set can already be classified cleanly

That is intentional.

## 3.2 On turn submission

For `POST /api/games/{gameId}/end-turn`, the backend should validate:

- all ownership/integrity checks again
- full board structure validity
- set classification
- set-level legality
- first move rules
- final move legality relative to confirmed state
- game end conditions if the player's rack is empty

This is where full Rummikub validation belongs.

---

# 4. Core Design Change: Backend-Inferred Set Type

The frontend does **not** send `BoardSetType.GROUP` or `BoardSetType.RUN`.
Therefore the backend must decide that itself.

This changes the validation architecture significantly.

## 4.1 What this means in practice

A final submitted set is not validated by simply routing on `set.type`.
Instead, the backend must:

1. inspect the tiles in the set
2. determine whether the set is intended to be a **group candidate** or a **run candidate**
3. reject the set if it can be neither
4. validate it with the correct validator

That means the validation flow becomes:

- classify set
- validate set according to classified type

instead of:

- read declared type
- validate against declared type

## 4.2 Why this is harder

Without an explicit type from the client, the backend must reason about:

- numbered tiles vs jokers
- partial information when jokers are present
- ambiguous or impossible sets
- all-joker edge cases

This makes **set classification** a first-class part of the rule engine.

## 4.3 Best architectural decision

The cleanest design is:

- do **not** classify sets on every draft update
- classify sets only during **final submitted turn validation**
- keep draft update permissive
- keep end-turn validation strict

That preserves the important editing freedom during the turn.

---

# 5. Recommended Layered Validation Architecture

A clean validation design has multiple levels.
Each level answers a different question.

## Level 0: DTO / transport validation

Question:

> Is the incoming request well-formed?

Examples:

- missing required fields
- malformed JSON
- null lists where not allowed
- malformed tile payloads
- invalid numbers

This belongs in:

- controller request parsing
- DTO annotations / validation
- request mapper layer

### Example

```kotlin
fun validateUpdateDraftRequest(request: UpdateDraftRequest) {
    require(request.boardSets != null) { "boardSets must be present" }
    require(request.rackTiles != null) { "rackTiles must be present" }
}
```

This is not game logic yet. It is only transport validation.

---

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

## Level 3: Set classification

Question:

> If this set is part of a final submitted board, what kind of set is it supposed to be?

This is the key new layer.

Because the frontend does not send a `BoardSetType`, the backend must infer one.

## 5.3.1 Why classification deserves its own service

Classification is not the same thing as full validation.

The service should answer:

- is this set a **group candidate**?
- is this set a **run candidate**?
- is it impossible to classify?
- is it ambiguous?

That is separate from fully validating all the rules of a group or run.

## 5.3.2 Recommended service: `SetClassificationService`

Responsibilities:

- inspect one set
- infer whether it is a `GROUP` or `RUN`
- reject ambiguous/impossible cases
- return a `SetClassificationResult`

### Suggested model

```kotlin
enum class InferredSetType {
    GROUP,
    RUN
}

data class SetClassificationResult(
    val isClassifiable: Boolean,
    val inferredType: InferredSetType? = null,
    val violations: List<RuleViolation> = emptyList()
)
```

## 5.3.3 Suggested classification strategy

For **non-joker tiles** in the set:

- if all have the same number and colors are unique -> group candidate
- if all have the same color and numbers are unique -> run candidate
- if neither -> invalid
- if there are no non-jokers -> invalid or unsupported, depending on your rules

### Example pseudo-code

```kotlin
fun classify(set: BoardSet): SetClassificationResult {
    if (set.tiles.size < 3) {
        return invalidClassification("Set must contain at least 3 tiles")
    }

    val nonJokers = set.tiles.filterNot { it.isJoker }

    if (nonJokers.isEmpty()) {
        return invalidClassification("Cannot classify an all-joker set")
    }

    val sameNumber = nonJokers.map { it.number }.distinct().size == 1
    val sameColor = nonJokers.map { it.color }.distinct().size == 1

    return when {
        sameNumber && !sameColor -> classifiedAs(InferredSetType.GROUP)
        sameColor && !sameNumber -> classifiedAs(InferredSetType.RUN)
        sameNumber && sameColor -> invalidClassification("Set classification is ambiguous")
        else -> invalidClassification("Set is neither a group nor a run")
    }
}
```

## 5.3.4 Important note about jokers

Jokers make classification more complicated.

A good practical rule is:

- classify based on the non-joker subset first
- let the later group/run validator decide whether joker use is legal

That keeps classification simpler and more stable.

---

## Level 4: Set-level validation

Question:

> Given the inferred set type, is this individual set a legal group or run?

At this level, each set is validated independently.

### Recommended split

- `GroupValidationService`
- `RunValidationService`
- optional `SetValidationService` as a router

---

### 5.4.1 `GroupValidationService`

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

### 5.4.2 `RunValidationService`

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

### 5.4.3 `SetValidationService`

This service now routes **after classification**.
It should not read a pre-existing `BoardSetType` from the client.

### Example

```kotlin
class SetValidationService(
    private val setClassificationService: SetClassificationService,
    private val groupValidationService: GroupValidationService,
    private val runValidationService: RunValidationService
) {
    fun validate(set: BoardSet): ValidationResult {
        val classification = setClassificationService.classify(set)
        if (!classification.isClassifiable) {
            return invalid(classification.violations)
        }

        return when (classification.inferredType) {
            InferredSetType.GROUP -> groupValidationService.validate(set)
            InferredSetType.RUN -> runValidationService.validate(set)
            null -> invalid("Set could not be classified")
        }
    }
}
```

This is the core change from the earlier architecture.

---

## Level 5: Board-level validation

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

        boardSets.forEachIndexed { index, set ->
            val result = setValidationService.validate(set)
            violations += result.violations.map {
                it.copy(path = "boardSets[$index]")
            }
        }

        return if (violations.isEmpty()) valid() else invalid(violations)
    }
}
```

This layer is mainly used during **end-turn**, not during every draft update.

---

## Level 6: Turn-level / game-context validation

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

### 5.6.1 `FirstMoveValidationService`

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

## Level 7: Top-level rule orchestration

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

# 6. Recommended Responsibility Split Across Services

A clean backend design should separate the following concerns.

## 6.1 `GameService`

Owns confirmed game state.

Responsibilities:

- load confirmed game
- save confirmed game
- perform confirmed-state transitions
- expose simple game-level queries

Should **not** own live draft mutation logic.

---

## 6.2 `TurnDraftService`

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

## 6.3 `GameBroadcastService`

Owns websocket event emission only.

Responsibilities:

- broadcast draft updates
- broadcast committed game updates
- broadcast turn changes
- broadcast game ended events

Should not contain game rule logic.

---

## 6.4 Rule validation services

Own rule validation only.

Responsibilities:

- set classification
- set legality
- board legality
- tile conservation
- first move checks
- orchestration of submitted draft validation

Should not own persistence or websocket broadcasting.

---

# 7. Full Validation Flow From Start to Finish

This section describes the full validation flow through the backend.

---

## 7.1 Draft update flow (`PUT /api/games/{gameId}/draft`)

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

## 7.2 Turn submission flow (`POST /api/games/{gameId}/end-turn`)

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
    - set classification and set validation inside board validation
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

# 8. Best Options for Classification Timing

Because type is backend-inferred, there are several possible strategies.

## Option A: classify on every draft update

### Idea

Each incoming candidate set is immediately classified as group/run/invalid.

### Pros

- early feedback
- draft may already carry inferred type metadata

### Cons

- too strict for intermediate editing
- many legal editing sequences would be rejected too early
- difficult with temporary incomplete sets

### Recommendation

Not recommended for your main draft update path.

---

## Option B: classify only on end-turn

### Idea

During draft editing, the backend stores temporary sets without requiring final type.
At end-turn, each set is classified and then validated.

### Pros

- preserves editing freedom
- matches real player behavior
- keeps draft update endpoint simpler
- best fit for your architecture

### Cons

- classification errors appear later, at submit time

### Recommendation

This is the **best default option**.

---

## Option C: classify softly on update, strictly on end-turn

### Idea

Draft update attempts a best-effort classification for diagnostics/UI hints, but does not reject temporary ambiguous sets unless integrity is broken.

### Pros

- gives better future UI/debug support
- still keeps end-turn as the strict validation point

### Cons

- more implementation complexity
- may not be worth it early on

### Recommendation

A good later enhancement, but not necessary for the initial backend implementation.

---

# 9. What Should and Should Not Be Validated on Draft Update

## Validate on every draft update

You should validate:

- caller identity
- active player ownership
- draft ownership
- game status allows updates
- draft status allows updates
- tile conservation
- optional version consistency

## Do not fully validate on every draft update

You should usually not validate:

- group legality
- run legality
- final set classification
- full board legality
- first move point threshold
- end-turn scoring
- game end conditions

Reason:
The player must be allowed to pass through temporary invalid states while editing.

---

# 10. Suggested Rule Validation Models

## 10.1 `ValidationResult`

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

## 10.2 `RuleViolation`

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
- optionally point to the affected location in the draft/board
- be stable enough for frontend handling and debugging

### Recommended class shape

```kotlin
data class RuleViolation(
    val code: String,
    val message: String,
    val path: String? = null,
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

#### `path`

An optional logical location string describing where the problem occurred.

Examples:

- `"boardSets[0]"`
- `"boardSets[2].tiles[1]"`
- `"rackTiles"`

This is especially useful when many violations are returned together.

#### `setIndex`

An optional numeric shortcut for the affected set.

This is helpful when the frontend wants to highlight one full board set without parsing the `path` string.

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
    path = "boardSets[1]",
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

## 10.3 `SetClassificationResult`

Because type is inferred, classification should have its own explicit model.

### Example

```kotlin
enum class InferredSetType {
    GROUP,
    RUN
}

data class SetClassificationResult(
    val isClassifiable: Boolean,
    val inferredType: InferredSetType? = null,
    val violations: List<RuleViolation> = emptyList()
)
```

This keeps classification separate from final legality validation.

---

# 11. Recommended Validation Service Hierarchy

A strong separation would look like this:

```text
RummikubRuleService
├── TileConservationService
├── BoardValidationService
│   └── SetValidationService
│       ├── SetClassificationService
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
- classification separate from validation
- small validators small
- orchestration explicit

---

# 12. Practical Examples of Validation Scenarios

## 12.1 Valid draft update, temporarily invalid board

Player takes a tile out of a valid run.
The run becomes invalid for a short time.
The player has not finished rearranging yet.

### Expected backend behavior

- accept draft update
- persist draft
- broadcast draft
- do **not** reject it just because one set is temporarily invalid

This is why full board validation and classification do not belong in the draft update path.

---

## 12.2 Invalid draft update because a tile was duplicated

Player or buggy client submits a draft where one tile appears twice.

### Expected backend behavior

- reject update
- return integrity error
- do not persist candidate draft

This is a `TileConservationService` failure.

---

## 12.3 Invalid end-turn because a set cannot be classified

Player submits a final set whose non-joker tiles are neither same-number nor same-color.

### Expected backend behavior

- reject end-turn
- return classification error
- keep draft uncommitted

This is a `SetClassificationService` failure.

---

## 12.4 Invalid end-turn because final board contains illegal run

Player submits final draft where a run has duplicate numbers or mismatched colors.

### Expected backend behavior

- reject end-turn
- return validation errors
- keep draft uncommitted

This is a `RunValidationService` / `BoardValidationService` failure.

---

## 12.5 Invalid first move because initial meld score is too low

Player completes first turn but only places 24 points.

### Expected backend behavior

- reject end-turn
- keep draft
- return first-move-specific violation

This is a `FirstMoveValidationService` failure.

---

# 13. Recommended PR / Implementation Order for the Rule System

If implementing incrementally, a good order is:

1. `ValidationResult` and `RuleViolation`
2. `SetClassificationResult`
3. `TileConservationService`
4. `SetClassificationService`
5. `GroupValidationService`
6. `RunValidationService`
7. `SetValidationService`
8. `BoardValidationService`
9. `FirstMoveValidationService`
10. `RummikubRuleService`
11. integrate full validation into end-turn flow

This order works well because:

- draft update safety depends heavily on tile conservation
- end-turn now depends on classification before group/run validation
- first-move validation depends on board validity already existing

---

# 14. Final Design Rule

The single most important design rule is:

> **Temporary draft editing must stay permissive, but turn submission must stay strict, and final set type must be inferred by the backend before full set validation runs.**

That means:

- `TurnDraftService` should allow temporary invalid arrangements as long as ownership and tile integrity are preserved
- `SetClassificationService` should decide whether a final set is a group or a run
- `RummikubRuleService` should validate only submitted end-turn states that are about to become confirmed game state

If this boundary stays clear, the architecture remains:

- flexible for frontend editing
- strict where correctness matters
- independent of frontend set-type decisions
- easy to test
- easy to extend later

---

# 15. One-Sentence Summary

The backend should treat draft updates as **safe persistence of temporary player work**, and treat end-turn submission as the **single strict validation point where tile conservation, backend set classification, set legality, board legality, and game-context rules together decide whether the draft may become the next confirmed game state**.
