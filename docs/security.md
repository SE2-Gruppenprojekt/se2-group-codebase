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

The workflows are implemented in:

```text
.github/workflows/backend-security.yml
```

Recommended structure:

```yaml
name: Backend Security Scan

on:
    workflow_dispatch:
    pull_request:
        paths:
            - "apps/backend/**"
            - "docs/security.md"
            - ".github/workflows/backend-security.yml"
    push:
        branches: [main]
        paths:
            - "apps/backend/**"
            - "docs/security.md"
            - ".github/workflows/backend-security.yml"
    schedule:
        - cron: "0 6 * * 1"

concurrency:
    group: backend-security-${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
    cancel-in-progress: true

permissions:
    contents: read
    issues: write

env:
    BACKEND_BASE_URL: https://se2-group-codebase.onrender.com
    HEALTH_URL: https://se2-group-codebase.onrender.com/actuator/health

jobs:
    zap-baseline-scan:
        name: OWASP ZAP Baseline Scan
        runs-on: ubuntu-latest
        timeout-minutes: 20

        steps:
            - name: Checkout repository
              uses: actions/checkout@v4

            - name: Wait for deployed backend
              run: |
                  for i in {1..30}; do
                    if curl -fsS "$HEALTH_URL"; then
                      echo "Backend is reachable"
                      exit 0
                    fi

                    echo "Waiting for backend to wake up..."
                    sleep 10
                  done

                  echo "Backend was not reachable"
                  exit 1

            - name: Run OWASP ZAP baseline scan
              uses: zaproxy/action-baseline@v0.14.0
              with:
                  target: ${{ env.BACKEND_BASE_URL }}
                  cmd_options: "-a -I"

            - name: Publish ZAP summary
              if: always()
              run: |
                  {
                    echo "## OWASP ZAP Baseline Scan"
                    echo
                    if [ -f report_md.md ]; then
                      cat report_md.md
                    else
                      echo "No markdown report was generated."
                    fi
                  } >> "$GITHUB_STEP_SUMMARY"

            - name: Upload ZAP report
              if: always()
              uses: actions/upload-artifact@v4
              with:
                  name: zap-security-report
                  retention-days: 14
                  path: |
                      report_html.html
                      report_md.md
                      report_json.json
```

---

## Why The Workflow Is Structured This Way

### `pull_request`

```yaml
pull_request:
```

This makes the scan visible in normal CI whenever backend-relevant code changes.

That is the most important trigger for the professor’s requirement, because it proves the scan is part of the development workflow and not only a one-off manual action.

### `push` on `main`

```yaml
push:
    branches: [main]
```

This verifies the merged mainline state as well. It is useful because a green pull request alone does not prove the deployed branch remains clean after merge.

### `workflow_dispatch`

```yaml
workflow_dispatch:
```

This allows manual reruns before demos, reviews, or submission.

### `schedule`

```yaml
schedule:
    - cron: "0 6 * * 1"
```

This keeps the check alive even when no pull request is open.

### `concurrency`

```yaml
concurrency:
```

This prevents multiple overlapping ZAP scans for the same branch or pull request. Without that, CI can waste time scanning the same target several times in parallel.

### `permissions`

```yaml
permissions:
    contents: read
    issues: write
```

The workflow needs repository read access to check out the project and run the scan configuration. It also grants issue write access so OWASP ZAP can create or update GitHub issues for detected security findings, making important scan results visible directly in GitHub. This is intended.

---

## Wake-Up Step

Render deployments may sleep when idle, so the scan must not start immediately.

The workflow first polls the health endpoint:

```yaml
- name: Wait for deployed backend
  run: |
      for i in {1..30}; do
        if curl -fsS "$HEALTH_URL"; then
          echo "Backend is reachable"
          exit 0
        fi

        echo "Waiting for backend to wake up..."
        sleep 10
      done

      echo "Backend was not reachable"
      exit 1
```

This gives the deployment up to:

```text
30 attempts x 10 seconds = 300 seconds
```

That makes the workflow much more reliable than starting the scan immediately.

---

## OWASP ZAP Baseline Scan

The main scan step is:

```yaml
- name: Run OWASP ZAP baseline scan
  uses: zaproxy/action-baseline@v0.14.0
  with:
      target: ${{ env.BACKEND_BASE_URL }}
      cmd_options: "-a -I"
```

Important options:

### `-a`

```text
-a
```

Enables additional passive scan rules.

This makes the scan more useful without turning it into an aggressive active attack.

### `-I`

```text
-I
```

Prevents the workflow from failing only because ZAP produced warnings.

That is a good default for this project because:

- the scan should remain visible in CI
- findings should be reviewable
- low-level warnings should not automatically break every pull request

If the team later wants stricter enforcement, this can be tightened with custom ZAP rules or by removing `-I`.

---

## Making The Result Visible In CI

The professor’s requirement is not only about running the scan, but about making the result visible.

The workflow now does that in two ways:

### 1. GitHub Actions step summary

```yaml
- name: Publish ZAP summary
```

This writes the Markdown report into the Actions job summary, so reviewers can see the result directly inside CI without downloading artifacts first.

### 2. Uploaded artifacts

```yaml
- name: Upload ZAP report
```

This preserves the full reports for later inspection.

Together, that gives:

- fast visibility in CI
- persistent full reports for manual review

---

## Generated Reports

The workflow uploads:

```text
report_html.html
report_md.md
report_json.json
```

### HTML

```text
report_html.html
```

Best for manual review in a browser.

### Markdown

```text
report_md.md
```

Best for CI summaries, PR discussion, and issue creation.

### JSON

```text
report_json.json
```

Best for machine processing or future automation.

---

## How To Use This In Practice

### Manual run

1. Open the repository in GitHub.
2. Go to **Actions**.
3. Select **Backend Security Scan**.
4. Click **Run workflow**.
5. Wait for the job to finish.
6. Read the CI summary.
7. Download the `zap-security-report` artifact if deeper inspection is needed.

### Pull request flow

1. Change backend code.
2. Open or update the pull request.
3. Let **Backend Security Scan** run automatically.
4. Review the summary and artifact.
5. Decide whether findings should be fixed now or tracked explicitly.

---

## How To Interpret Findings

Typical ZAP findings are grouped into levels like:

```text
Informational
Low
Medium
High
```

A finding does not automatically mean the backend is broken or exploitable.

The team should review each finding and decide whether it is:

```text
real and should be fixed
acceptable in this project context
a false positive
future hardening work
```

Examples of findings that can appear:

```text
Missing Anti-clickjacking Header
Missing Content Security Policy
Server Leaks Version Information
X-Content-Type-Options Header Missing
```

Some of these are more relevant for browser-facing applications than for a pure Android client, but they are still valid security observations and worth documenting.

---

## What This Setup Proves

This implementation proves that:

- backend API security testing is automated
- the scan is integrated into GitHub Actions
- the scan result is visible inside CI
- the full reports are retained as artifacts
- the check can run on pull requests, on demand, and on a schedule

That is a defensible and concrete implementation of the professor’s requirement.

---

## Limitations

This setup is useful, but deliberately limited.

It does **not** replace:

```text
authentication
authorization
input validation
DTO validation
membership and turn checks
secret handling
manual security review
secure deployment configuration
```

It also does **not** perform an aggressive active attack against the backend. That is intentional. Running active security attacks against a small deployed student backend would be harder to control and easier to misuse.

The current setup is therefore best described as:

```text
automated passive API security scanning in CI
```

That is still a valid and solid answer to the requirement.

An equally important limitation is scope:

```text
the current ZAP workflows do not yet cover the full stateful game API.
```

They are currently strongest at:

- public endpoint checks
- response-header checks
- cache-policy checks
- transport-level observations

They are not yet a substitute for a future deeper scan against:

- game command endpoints
- draft mutation endpoints
- end-turn submission flows
- stateful or authenticated API interactions

---

## Relation To Backend Security In The Codebase

The ZAP scan should be understood as one layer only.

Actual backend security still depends on the application code rejecting invalid or unauthorized requests.

### Current Request Identity Model

Most backend game and lobby endpoints currently receive the acting user through:

```kotlin
@RequestHeader("X-User-Id") userId: String
```

That header is then used for application-level security and ownership checks in
the service layer.

Examples of the kinds of checks this enables:

- only the active player may draw, reset a draft, or end a turn
- only the owning player may mutate their current `TurnDraft`
- only users who belong to a game may access or mutate that game
- only users who belong to a lobby may interact with lobby-backed game state
- only the lobby host may start the lobby
- per-turn limits such as one draw per turn can be enforced against the acting
  user identity

Typical service-level checks look like this:

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

This is a valid lightweight authorization model for the current backend, but it
has an important limit:

```text
X-User-Id is only trustworthy if the caller environment is trusted.
```

In other words, this is not full authentication by itself. If an arbitrary
external client can choose any header value freely, the client can impersonate
another user unless an upstream trusted system constrains that header.

So the current model should be understood as:

- suitable for controlled development, demos, tests, and trusted integration
  scenarios
- useful for enforcing game membership, turn ownership, and host-only actions
- not equivalent to real end-user authentication

That distinction matters when discussing backend security. The CI security scan
checks the deployed API surface, but the correctness of user-level access
control still depends on the service layer enforcing these `X-User-Id`-based
authorization checks consistently.

Examples:

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
```

And for REST behavior:

```kotlin
@ExceptionHandler(SecurityException::class)
fun handleSecurity(ex: SecurityException): ResponseEntity<ApiErrorResponse> {
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(
            ApiErrorResponse(
                errorCode = "FORBIDDEN",
                errorMessage = ex.message ?: "Access denied"
            )
        )
}
```

The security workflow complements these protections. It does not replace them.

---

## Minimal Submission Statement

If this needs to be explained very briefly in a submission or demo:

```text
The backend API is automatically scanned with OWASP ZAP through GitHub Actions.
The workflow waits for the deployed Render backend, runs a passive baseline
security scan, publishes the result in the CI summary, and uploads HTML,
Markdown, and JSON reports as artifacts. This provides automated API security
testing that is visible and repeatable in CI.
```
