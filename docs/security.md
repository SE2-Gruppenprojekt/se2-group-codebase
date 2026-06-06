# Automated API Security Testing

## Goal

This document explains how the backend satisfies the requirement for automated API security testing:

```text
Implementierung eines automatisierten Penetration/Security Testing API endpoints (z.B. OWASP ZAP)
```

The concrete implementation is:

- the deployed backend is scanned automatically in GitHub Actions
- the scan is performed with **OWASP ZAP**
- the result is visible in CI
- the reports are stored as workflow artifacts for later review
- the repository now uses both:
    - a simple **baseline scan**
    - a plan-driven **Automation Framework scan**

This is intentionally simple. The goal is not to build a full security platform, but to show a correct, automated, repeatable API security check that runs as part of the project workflow.

---

## What Is Implemented

The project currently uses these workflows:

```text
.github/workflows/backend-security.yml
.github/workflows/backend-security-af.yml
```

The baseline workflow:

1. waits until the deployed backend is reachable
2. runs an **OWASP ZAP baseline scan** against the deployed API
3. publishes the scan output into the GitHub Actions run summary
4. uploads the full reports as CI artifacts

The Automation Framework workflow:

1. waits until the deployed backend is reachable
2. creates a dedicated scan fixture through an internal backend-only endpoint
3. substitutes the returned ids into a generated Automation Framework plan
4. runs a committed **Automation Framework plan** from the repository
5. generates dedicated markdown, HTML, and JSON AF reports
6. generates a separate endpoint-inventory markdown artifact
7. publishes the AF summary into the GitHub Actions run summary
8. uploads the AF reports and endpoint inventory as separate artifacts

The current plan file lives at:

```text
.github/zap/backend-automation-plan.yaml
```

This is enough to demonstrate that backend API security testing is:

- automated
- repeatable
- visible in CI
- integrated into the repository
- aligned with the real REST API contract exposed through `/v3/api-docs`
- able to acquire a real positive-state `gameId` automatically before the AF run

---

## Scan Target

The workflow scans the deployed backend on Render:

```text
https://se2-group-codebase.onrender.com
```

The health endpoint used for readiness is:

```text
https://se2-group-codebase.onrender.com/actuator/health
```

That endpoint is checked before the scan starts so ZAP does not run against a sleeping or unavailable deployment.

Example:

```bash
curl -f https://se2-group-codebase.onrender.com/actuator/health
```

Expected response:

```json
{
    "status": "UP"
}
```

### What is actually being scanned right now

The two ZAP workflows now cover different layers:

1. the baseline scan still provides passive public-surface and transport-layer checks
2. the Automation Framework scan imports the generated OpenAPI contract and then
   sends explicit request traffic across public, positive lobby-state, positive
   game-state, and negative-path endpoint groups

The AF workflow now exercises the real REST API shape, including positive-state
coverage for:

- `GET /api/games/{gameId}`
- `PUT /api/games/{gameId}/draft`
- `POST /api/games/{gameId}/draw`
- `POST /api/games/{gameId}/end-turn`

That positive game coverage is possible because the workflow no longer relies on
hard-coded public fixture ids. Instead, it creates fresh scan-safe state through
an internal fixture endpoint before each AF run.

---

## Why This Matters

The Android app is not the only caller of the backend. Anyone can send requests directly to the API with tools like:

```text
curl
Postman
Insomnia
custom scripts
```

That means backend endpoints must be checked independently from the frontend.

An automated security scan helps catch common API and HTTP-layer weaknesses such as:

```text
missing security headers
information leaks in responses
unsafe default behavior
unexpected reachable endpoints
cache-related header problems
content-type problems
```

This does not replace backend validation or authorization logic. It is an additional automated safety net.

---

## Why OWASP ZAP

OWASP ZAP is a widely used web and API security testing tool. For this project it is a good fit because:

- it is standard and recognizable
- it runs well inside GitHub Actions
- it can scan a deployed HTTP target automatically
- it produces human-readable and machine-readable reports

For this project, the **baseline scan** is the right starting choice.

That matters because the baseline scan is:

- safe to run against a deployed environment
- passive by design
- focused on detecting common web/API security weaknesses without attacking the target aggressively

This is a much better fit for a student project deployment than a full active attack scan against production.

The **Automation Framework scan** builds on that baseline setup. It is useful because:

- it keeps the scan design inside a committed YAML plan
- it is easier to evolve deliberately over time
- it can grow later into:
    - richer passive scan configuration
    - OpenAPI-driven scans
    - authenticated scans
    - stricter exit policies

The initial AF rollout should still stay conservative and non-blocking.

---

## Security Scan Strategy

The implemented baseline flow is:

```text
GitHub Actions
    -> wake deployed backend
    -> verify /actuator/health
    -> run OWASP ZAP baseline scan
    -> publish CI summary
    -> upload reports as artifacts
```

This approach is deliberate:

- the scan runs against the real deployed backend, not only a local mock
- the scan result is directly visible in the CI run
- the detailed reports remain downloadable afterwards

The Automation Framework flow follows the same readiness and reporting pattern,
but replaces the packaged baseline behavior with a committed scan plan:

```text
GitHub Actions
    -> wake deployed backend
    -> verify /actuator/health
    -> call /internal/security/scan-fixture with X-Scan-Secret
    -> substitute returned ids into a generated AF plan
    -> run ZAP Automation Framework plan
    -> publish CI summary
    -> upload AF reports as artifacts
```

---

## Baseline Scan vs Automation Framework Scan

The repository now uses two ZAP-based scan layers for different purposes.

### Baseline scan

Use the baseline workflow when the goal is:

- simple passive scanning
- fast visibility in CI
- stable recurring checks with minimal configuration

### Automation Framework scan

Use the AF workflow when the goal is:

- versioned scan design in a repository plan file
- more deliberate control over requests, reports, and exit behavior
- a cleaner foundation for future security-scan expansion

The AF workflow does **not** replace the baseline workflow. It extends the
current security-scanning setup with a second, more structured layer.

### Why the Automation Framework needs a plan file

The difference in configuration model is important:

- the **baseline scan** is a packaged scan with a small number of inputs such
  as target URL and command options
- the **Automation Framework scan** is a plan-driven scan where the repository
  explicitly defines what ZAP should do

For the baseline workflow, this is enough:

```yaml
with:
    target: ${{ env.BACKEND_BASE_URL }}
    cmd_options: "-a -I"
```

That works because the baseline action already knows its built-in scan flow.

The Automation Framework is different. It needs a committed YAML plan because
the plan is the scan definition itself. It describes things like:

- which target context to use
- which requests should be executed
- which passive scan jobs should run
- which reports should be generated
- what exit behavior should be applied

In other words:

- baseline scan = run the standard packaged scan
- Automation Framework scan = run the exact scan plan defined in the repository

That is why the Automation Framework workflow uses:

```text
.github/zap/backend-automation-plan.yaml
```

The extra file is not accidental overhead. It is the mechanism that makes the
Automation Framework useful for gradual future expansion, such as:

- authenticated scan flows
- OpenAPI-driven scans
- stricter exit rules
- alert filtering
- controlled active scan jobs

### Dedicated scan-fixture endpoint

The repository now exposes one internal infrastructure endpoint for the
Automation Framework workflow:

```text
POST /internal/security/scan-fixture
```

This endpoint is not part of the public gameplay API. It exists only so the
workflow can create deterministic, scan-safe backend state immediately before
the AF run.

The endpoint is protected by:

- request header:
    - `X-Scan-Secret`
- backend property:
    - `app.scan-fixture.secret`

If the header is missing or wrong, the backend returns `403 FORBIDDEN`.

The endpoint is only registered when:

```text
app.scan-fixture.enabled=true
```

If the feature is not enabled, the endpoint is absent from routing and behaves
like a normal `404`.

### Layer 2: API-wide coverage

Layer 2 is implemented through the generated OpenAPI document:

```text
/v3/api-docs
```

The Automation Framework plan imports that contract and then sends explicit
request traffic so the AF reports contain real HTTP messages instead of only a
schema import.

This is the repository’s:

```text
scan the whole backend API shape
```

layer.

### Layer 3: Positive stateful flows

Layer 3 uses the dedicated fixture endpoint to recreate deterministic state and
return:

- `lobbyId`
- `hostUserId`
- `guestUserId`
- `gameId`
- `draftOwnerUserId`

The workflow substitutes those values into a generated plan before ZAP runs.

The placeholders currently used are:

- `__SCAN_LOBBY_ID__`
- `__SCAN_GAME_ID__`
- `__SCAN_HOST_USER_ID__`
- `__SCAN_GUEST_USER_ID__`

This allows the AF run to execute positive real-state requests for:

- `GET /api/lobbies/{lobbyId}`
- `PATCH /api/lobbies/{lobbyId}/settings`
- `POST /api/lobbies/{lobbyId}/ready`
- `POST /api/lobbies/{lobbyId}/unready`
- `POST /api/lobbies/{lobbyId}/leave`
- `DELETE /api/lobbies/{lobbyId}`
- `GET /api/games/{gameId}`
- `PUT /api/games/{gameId}/draft`
- `POST /api/games/{gameId}/draw`
- `POST /api/games/{gameId}/end-turn`

The fixture guarantees a known pre-draw host rack and first draw tile:

- pre-draw rack:
    - `scan-host-rack-red-5`
    - `scan-host-rack-blue-7`
- first draw tile:
    - `scan-draw-red-1`

That is why the plan can run:

- a valid positive `PUT /draft` request before draw
- a valid positive `POST /draw`
- a valid positive `POST /end-turn` request with the post-draw rack

Negative-path requests are still kept in the plan so missing-id behavior
remains visible alongside the positive-state coverage.

### Authentication scope

This implementation does **not** add real backend authentication.

Gameplay requests still rely on the backend’s current trusted-header identity
model:

```text
X-User-Id
```

Only the fixture setup endpoint uses the additional `X-Scan-Secret` header.
Authenticated scan flows remain follow-up work.

### Endpoint coverage artifact

The workflow now generates an extra artifact:

```text
zap-af-endpoint-coverage.md
```

This file is produced from:

- the generated AF plan used in the run
- the AF JSON report, when present

It surfaces:

- target site metadata
- imported OpenAPI URL count
- explicit endpoint inventory
- grouping by:
    - public endpoints
    - positive lobby-state endpoints
    - positive game-state endpoints
    - negative-path endpoints
- response-profile statistics from the ZAP JSON output

This artifact is more useful than the raw markdown report when the goal is to
see exactly which endpoint inventory the AF run exercised.

---

## Workflow Files

The implemented security-testing files are:

```text
.github/workflows/backend-security.yml
.github/workflows/backend-security-af.yml
.github/zap/backend-automation-plan.yaml
.github/scripts/generate_zap_af_endpoint_coverage.rb
apps/backend/src/main/kotlin/at/se2group/backend/api/SecurityScanFixtureController.kt
apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureService.kt
apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureBootstrap.kt
apps/backend/src/main/resources/application.yml
```

Supporting verification lives in:

```text
apps/backend/src/test/kotlin/at/se2group/backend/api/OpenApiDocsTest.kt
apps/backend/src/test/kotlin/at/se2group/backend/api/SecurityScanFixtureControllerTest.kt
apps/backend/src/test/kotlin/at/se2group/backend/api/SecurityScanFixtureBootstrapTest.kt
apps/backend/src/test/kotlin/at/se2group/backend/api/SecurityScanFixtureServiceTest.kt
```

---

## End-to-End Strategy

The final security-testing design has two scan layers.

### Layer 1: Baseline passive scan

Purpose:

- cheap recurring CI visibility
- passive checks against the deployed backend
- transport and header observations
- low operational risk

Implementation:

- workflow: `.github/workflows/backend-security.yml`
- action: `zaproxy/action-baseline`
- target: deployed Render backend

### Layer 2 and Layer 3: Automation Framework scan

Purpose:

- versioned scan design in the repository
- OpenAPI-driven API-shape coverage
- positive real-state request coverage for game and lobby flows
- explicit endpoint inventory artifact

Implementation:

- workflow: `.github/workflows/backend-security-af.yml`
- plan: `.github/zap/backend-automation-plan.yaml`
- report post-processing: `.github/scripts/generate_zap_af_endpoint_coverage.rb`

The design is deliberate:

- **Layer 2** answers: "Can we scan the whole backend API shape?"
- **Layer 3** answers: "Can we exercise the stateful flows that need valid ids and existing backend state?"

This split keeps the scan understandable and maintainable. OpenAPI import provides breadth. The fixture endpoint provides the minimum amount of controlled state needed for depth.

---

## Baseline Workflow

The baseline workflow exists for stable recurring passive coverage.

Its structure is:

```text
checkout
-> wake deployed backend
-> wait for /actuator/health
-> run ZAP baseline scan
-> publish markdown summary
-> upload HTML / Markdown / JSON artifacts
```

Important characteristics:

- it scans the deployed backend, not a local process
- it is safe to run regularly against Render
- it stays simple on purpose
- it does not try to create or mutate gameplay state

The baseline layer is not intended to understand game flow semantics. It is intended to keep passive HTTP security observations visible in CI.

---

## Automation Framework Workflow

The Automation Framework workflow is the main implementation of the full REST API scan strategy.

Its structure is:

```text
checkout
-> wake deployed backend
-> wait for /actuator/health
-> call /internal/security/scan-fixture with X-Scan-Secret
-> extract lobby/game/user ids
-> generate concrete AF plan from placeholders
-> run ZAP Automation Framework
-> regenerate concrete plan for reporting
-> generate endpoint inventory markdown
-> publish summary
-> upload artifacts
```

The current workflow file is:

```text
.github/workflows/backend-security-af.yml
```

### Why the workflow is structured this way

#### `workflow_dispatch`

Manual runs are necessary while tuning scan design, debugging report output, or validating the deployed scan state.

#### `pull_request`

This is the primary CI integration point. It proves security scanning is part of normal backend development work.

#### `push` on `main`

This re-checks the merged mainline state. It matters because Render deploys from the main branch, not from an isolated pull request workspace.

#### `schedule`

The AF workflow also runs on a schedule so the scan does not depend only on developer activity.

#### `concurrency`

The AF scan mutates real prepared scan state. Overlapping runs on the same branch or pull request would make the artifact stream noisier and could hide failures. The concurrency group prevents that.

#### `permissions`

The workflow currently needs only:

```yaml
permissions:
  contents: read
```

This is the correct least-privilege model for the current implementation. The workflow does not need issue-writing behavior.

---

## Readiness and Deployment Assumptions

Both workflows scan the deployed backend:

```text
https://se2-group-codebase.onrender.com
```

Both wait for:

```text
https://se2-group-codebase.onrender.com/actuator/health
```

The wake-up loop exists because Render deployments may be cold or sleeping. Without a readiness step, scan failures would often be deployment-timing failures rather than security-scan failures.

The health polling loop gives the deployment up to five minutes to wake up:

```text
30 attempts x 10 seconds
```

---

## OpenAPI Generation and Layer 2 Coverage

Layer 2 depends on the backend exposing a generated OpenAPI document at:

```text
/v3/api-docs
```

That endpoint is the backend’s machine-readable API contract.

It is used so ZAP can understand the REST API shape even when the backend does not expose a browsable HTML surface that would naturally reveal routes through links or forms.

### Why this matters

A passive crawler can discover:

- `/`
- `/robots.txt`
- `/sitemap.xml`
- `/actuator/health`

but it cannot infer complex game/lobby routes reliably from that alone.

OpenAPI import fixes that by telling ZAP which routes and methods exist.

### Why OpenAPI import alone is not enough

Importing a contract is necessary, but not sufficient.

The AF report becomes meaningful only when ZAP observes real HTTP traffic. That is why the plan also includes explicit `requestor` jobs after the import. The scan design is therefore:

```text
OpenAPI import
-> explicit traffic over the imported route set
-> passive analysis and reporting
```

This is the repository’s:

```text
scan the whole backend API shape
```

layer.

---

## Dedicated Scan-Fixture Endpoint and Layer 3 Coverage

The stateful gameplay routes cannot be exercised reliably by a generic crawler.

They need:

- a real `lobbyId`
- a real `gameId`
- valid acting user ids
- a draft owned by the correct player
- a known pre-draw rack
- a known post-draw rack for end-turn

That is why the implementation introduces one internal infrastructure endpoint:

```text
POST /internal/security/scan-fixture
```

This endpoint is intentionally not part of the public gameplay API.

### Security model of the fixture endpoint

The endpoint is protected by:

- header: `X-Scan-Secret`
- backend property: `app.scan-fixture.secret`

If:

- the header is missing
- the header is wrong
- the configured secret is blank

then the backend returns:

```text
403 FORBIDDEN
```

The endpoint is only registered when:

```text
app.scan-fixture.enabled=true
```

If that property is false, the endpoint is not present at all.

This keeps the fixture mechanism separate from the public API and prevents accidental exposure in environments where it is not intended to run.

### Why a dedicated fixture endpoint was chosen

A public-API-only setup would have required something like:

```text
POST /api/lobbies
-> POST /api/lobbies/{id}/join
-> POST /api/lobbies/{id}/start
-> somehow discover the resulting gameId
-> somehow guarantee a valid draft state
```

That is much more brittle in CI because:

- the resulting `gameId` is not the central product of the public lobby API
- the scan would be sensitive to business-logic timing and evolving gameplay rules
- the workflow would need to derive state from multiple responses instead of one controlled endpoint

The dedicated fixture endpoint is therefore the pragmatic choice. It creates scan-safe state directly and returns the exact identifiers the plan needs.

---

## Fixture Service Design

The actual fixture creation logic lives in:

```text
apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureService.kt
```

The service does three things:

1. deletes any previous scan fixture state if present
2. recreates one open lobby and one active lobby/game pair
3. creates a live turn draft for the active game

### Why the service recreates state idempotently

The goal is deterministic CI behavior.

Each scan run should start from the same known state instead of depending on:

- leftover previous runs
- unknown demo data
- user-created lobbies and games

The service therefore deletes by fixed ids first, then recreates.

### Fixed fixture identifiers

The current deterministic identifiers are:

- open lobby: `scan-open-lobby`
- active lobby: `scan-active-lobby`
- game: `scan-game-1`
- host user: `scan-host-user`
- guest user: `scan-guest-user`

### Deterministic game state

The service seeds:

- a host rack before draw:
  - `scan-host-rack-red-5`
  - `scan-host-rack-blue-7`
- a known first draw tile:
  - `scan-draw-red-1`

That allows the AF plan to do:

- valid positive `PUT /api/games/{gameId}/draft`
- valid positive `POST /api/games/{gameId}/draw`
- valid positive `POST /api/games/{gameId}/end-turn`

with known payload expectations.

### Returned fixture state

The fixture endpoint returns:

- `lobbyId`
- `hostUserId`
- `guestUserId`
- `gameId`
- `draftOwnerUserId`

The workflow uses those values as substitution inputs for the AF plan.

---

## Bootstrap Behavior

The repository also contains:

```text
apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureBootstrap.kt
```

This bootstrap is intentionally thin.

It does not contain its own seeding logic anymore. It delegates to `SecurityScanFixtureService`.

This matters for maintainability because:

- the internal fixture endpoint
- the bootstrap
- backend tests

all rely on the same fixture-building implementation rather than duplicating seeded state logic in multiple places.

The bootstrap is guarded by:

```text
app.scan-fixture.enabled=true
```

and is disabled in tests by default through:

```text
apps/backend/src/test/resources/application.yml
```

so unrelated test scenarios are not polluted by scan-fixture data.

---

## Placeholder-Based AF Plan Generation

The committed AF plan contains placeholders instead of hard-coded live ids:

- `__SCAN_LOBBY_ID__`
- `__SCAN_GAME_ID__`
- `__SCAN_HOST_USER_ID__`
- `__SCAN_GUEST_USER_ID__`

The workflow:

1. calls `/internal/security/scan-fixture`
2. parses the JSON response with `jq`
3. stores the values in `GITHUB_ENV`
4. runs `sed` substitution against the committed plan
5. writes:

```text
.github/zap/backend-automation-plan.generated.yaml
```

That generated file is the concrete plan executed by ZAP for that run.

The workflow later regenerates the same concrete plan for reporting so the post-processing step can always read the exact substituted endpoint set.

If fixture variables are missing, the workflow falls back to copying the committed template plan so reporting can still degrade gracefully instead of crashing on an absent file.

---

## Automation Framework Plan Structure

The AF plan is committed in:

```text
.github/zap/backend-automation-plan.yaml
```

Its high-level structure is:

```text
env context
-> passiveScan-config
-> openapi import
-> requestor jobs
-> passiveScan-wait
-> report jobs
-> exitStatus
```

### Context definition

The plan defines a `se2-backend` context rooted at the deployed Render base URL.

This keeps the scan scoped to the actual backend host.

### Passive scan configuration

The plan limits passive alert volume and keeps scanning in-scope:

- `maxAlertsPerRule: 10`
- `scanOnlyInScope: true`

### OpenAPI import job

The `openapi` job imports:

```text
https://se2-group-codebase.onrender.com/v3/api-docs
```

against:

```text
https://se2-group-codebase.onrender.com
```

inside the `se2-backend` context.

### Requestor groups

The requestor jobs are organized intentionally into four groups.

#### Public endpoints

- `/`
- `/robots.txt`
- `/sitemap.xml`
- `/actuator/health`
- `/api/leaderboard`
- `/api/lobbies`

#### Positive lobby-state endpoints

- `GET /api/lobbies/{lobbyId}`
- `PATCH /api/lobbies/{lobbyId}/settings`
- `POST /api/lobbies/{lobbyId}/ready`
- `POST /api/lobbies/{lobbyId}/unready`
- `POST /api/lobbies/{lobbyId}/leave`
- `DELETE /api/lobbies/{lobbyId}`

#### Positive game-state endpoints

- `GET /api/games/{gameId}`
- `PUT /api/games/{gameId}/draft`
- `POST /api/games/{gameId}/draw`
- `POST /api/games/{gameId}/end-turn`

#### Negative-path endpoints

- create/join/start/leave/delete cases against missing lobby ids where useful
- missing game get/draft/draw/end-turn cases

The negative-path coverage remains in the plan because a scan should not only show happy-path behavior. It is useful to keep visible how the API behaves when resources do not exist.

### Why the plan includes both positive and negative requests

The point of the AF plan is not just to make ZAP touch endpoints. It is to make the report communicate:

- which routes exist
- which routes succeed with valid state
- which routes reject missing resources correctly

That is why the report inventory is grouped the same way.

### Report jobs

The plan generates:

- Markdown: `traditional-md`
- HTML: `traditional-html-plus`
- JSON: `traditional-json-plus`

The `plus` variants are used for HTML and JSON because they preserve richer request/response information than the traditional minimal variants.

### Exit behavior

The first rollout stays intentionally non-blocking at the warning level:

```text
warnLevel: High
errorLevel: High
```

That means the workflow is still informative rather than aggressively enforcement-driven.

---

## Endpoint Coverage Post-Processing

The raw ZAP Markdown report is useful, but weak as an endpoint inventory.

That is why the repository adds:

```text
.github/scripts/generate_zap_af_endpoint_coverage.rb
```

This script reads:

- the generated concrete AF plan
- the ZAP JSON report

and emits:

```text
zap-af-endpoint-coverage.md
```

### What the generated endpoint artifact contains

- target site metadata
- automation plan path
- report generation timestamps
- endpoint count reported by ZAP
- grouped inventory by:
  - public endpoints
  - positive lobby-state endpoints
  - positive game-state endpoints
  - negative-path endpoints
- expected method and status per request
- response profile statistics from the JSON report

This artifact exists because reviewers often need to know exactly what the scan exercised, not just whether alerts were found.

---

## Artifacts and CI Visibility

### Baseline workflow artifacts

The baseline workflow uploads:

- `report_html.html`
- `report_md.md`
- `report_json.json`

### Automation Framework workflow artifacts

The AF workflow uploads:

- `.github/zap/backend-automation-plan.generated.yaml`
- `zap-af-report.md`
- `zap-af-report.html`
- `zap-af-report.json`
- `zap-af-endpoint-coverage.md`

### Why both summary and artifacts are kept

The GitHub Actions summary is optimized for quick review.

The artifacts are optimized for:

- later inspection
- PR discussion
- formatted documentation
- comparing one scan run with another

The workflow therefore does both instead of choosing only one output path.

---

## External Configuration Required for the AF Workflow

The AF workflow depends on live deployed backend configuration.

### Render environment variables

The deployed backend must have:

```text
APP_SCAN_FIXTURE_ENABLED=true
APP_SCAN_FIXTURE_SECRET=<shared-secret>
```

### GitHub Actions secret

The repository must have:

```text
ZAP_SCAN_FIXTURE_SECRET=<same shared-secret>
```

If these are not configured consistently:

- the fixture endpoint will not exist, or
- the fixture call will be rejected with `403`, or
- the workflow will not receive real ids for plan substitution

That is the main operational dependency for the stateful AF scan.

---

## Verification and Regression Coverage

The repository locks this feature down with targeted backend tests.

### `OpenApiDocsTest`

Verifies:

- `/v3/api-docs` is reachable
- the response is a real OpenAPI document
- key gameplay and lobby paths are present
- the internal fixture endpoint is hidden from the public OpenAPI document
- response security headers still apply to the generated docs endpoint

### `SecurityScanFixtureControllerTest`

Verifies:

- missing `X-Scan-Secret` returns `403`
- wrong secret returns `403`
- valid secret returns the expected ids
- the fixture endpoint actually creates the expected lobby/game/draft state

### `SecurityScanFixtureBootstrapTest`

Verifies:

- bootstrap seeding is enabled only when requested
- the deterministic open lobby, active lobby, game, and draft are present
- the seeded entities match the latest intended fixture contract

### `SecurityScanFixtureServiceTest`

Verifies:

- fixture recreation is idempotent
- the service creates deterministic state
- the positive game flow is valid for:
  - get game
  - update draft
  - draw tile
  - end turn

This test is especially important because it proves that the AF plan’s positive game-state requests are not arbitrary. They are grounded in a real backend state that the service constructs intentionally.

---

## Relation to Backend Authorization

The security workflows are one layer only.

Actual request-level authorization still depends on backend application logic.

### Current request identity model

Most gameplay and lobby endpoints receive the acting user through:

```kotlin
@RequestHeader("X-User-Id") userId: String
```

That enables checks such as:

- player belongs to game
- player belongs to lobby
- acting player owns the draft
- acting player is the current player
- acting player is the lobby host when a host-only action is attempted

Typical service-level checks look like:

```kotlin
fun requirePlayerInGame(game: ConfirmedGame, userId: String) {
    if (game.players.none { it.userId == userId }) {
        throw SecurityException("Player is not part of this game")
    }
}

fun requireCurrentPlayer(game: ConfirmedGame, userId: String) {
    if (game.currentPlayerUserId != userId) {
        throw SecurityException("It is not this player's turn")
    }
}

fun requireDraftOwner(draft: TurnDraft, userId: String) {
    if (draft.playerUserId != userId) {
        throw SecurityException("Draft does not belong to this player")
    }
}
```

This is still not full authentication.

Important limitation:

```text
X-User-Id is only trustworthy if the caller environment is trusted.
```

The AF scan does not change that. It uses the same `X-User-Id` model for gameplay requests and only adds `X-Scan-Secret` for the dedicated internal fixture endpoint.

So the current security model is:

- useful for controlled development and CI scanning
- useful for service-level authorization checks
- not equivalent to production-grade end-user authentication

Authenticated security scanning remains future work.

---

## Limitations and Non-Goals

This implementation is intentionally strong in some areas and intentionally narrow in others.

### What it does well

- automated CI visibility
- deployed-environment scanning
- passive header and transport checks
- OpenAPI-driven API-shape coverage
- positive stateful lobby and game coverage
- explicit endpoint inventory reporting

### What it does not replace

- authentication
- authorization design
- DTO validation
- business-rule validation
- secure secret management
- secure deployment hardening
- manual security review

### What it does not currently cover

- websocket traffic
- full browser-oriented frontend security behavior
- aggressive active attack scanning
- richer authenticated user-session models

This is intentional. The goal is a correct, automated, repeatable API security-scanning layer, not a full security platform.

---

## What This Setup Proves

This implementation proves all of the following:

- backend API security testing is automated
- the scan is integrated into GitHub Actions
- the deployed Render backend is the real scan target
- the repository uses both a baseline and a plan-driven scan layer
- the backend publishes an OpenAPI contract that the scan can import
- the AF workflow can acquire a real `lobbyId` and `gameId` automatically
- stateful game and lobby flows are exercised with deterministic valid state
- CI exposes both high-level summaries and detailed downloadable artifacts

That is a concrete and defensible implementation of automated API security testing for this project.

---

## Minimal Submission Statement

If this needs to be summarized briefly in a submission or demo:

```text
The backend API is automatically scanned with OWASP ZAP through GitHub Actions.
The project uses two scan layers: a passive baseline scan and a plan-driven
Automation Framework scan. The AF workflow imports the generated OpenAPI
document, creates dedicated scan-safe backend state through an internal
secret-gated fixture endpoint, substitutes the resulting lobby and game ids
into a generated plan, runs the scan against the deployed Render backend, and
publishes both human-readable summaries and downloadable scan artifacts.
```
