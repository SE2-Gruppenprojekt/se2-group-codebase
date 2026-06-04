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
2. runs a committed **Automation Framework plan** from the repository
3. generates dedicated markdown, HTML, and JSON AF reports
4. publishes the AF summary into the GitHub Actions run summary
5. uploads the AF reports as separate artifacts

The current plan file lives at:

```text
.github/zap/backend-automation-plan.yaml
```

This is enough to demonstrate that backend API security testing is:

- automated
- repeatable
- visible in CI
- integrated into the repository

What is important to understand, however, is that the current scan coverage is
still **narrower than the full backend API surface**. The workflows are
implemented correctly, but they do not yet exercise the entire game and lobby
API described in:

```text
docs/game-api.md
```

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

In practice, the current ZAP setup mainly covers:

- `/`
- `/robots.txt`
- `/sitemap.xml`
- `/actuator/health`

This is true for two different reasons:

1. the baseline scan only discovers what it can reach automatically from the
   deployed public surface
2. the current Automation Framework plan explicitly requests only those public
   GET endpoints

That means the current scanning setup is best understood as:

```text
public endpoint and transport-layer security scanning
```

not yet as:

```text
full backend game API security scanning
```

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

### Why the current scans do not yet cover the full game API

The backend game API is much more stateful than the small public endpoint
surface that ZAP is currently hitting.

Many backend endpoints require:

- `X-User-Id`
- path variables such as `gameId` or `lobbyId`
- request bodies
- existing backend state, such as:
    - a real lobby
    - a started game
    - a current draft
    - a valid active player

Because of that, generic passive crawling is not enough to reach or exercise
those endpoints meaningfully.

To scan the real backend API more deeply, a future scan layer needs one of
these layered approaches:

- **Layer 2 — API-wide coverage**
    - generate an OpenAPI spec from the backend
    - import that spec into the existing AF plan through an AF `openapi` job
    - actively explore the imported API context in a bounded way so ZAP
      produces real request/response traffic for the API surface
    - use this as the:
        ```text
        scan the whole backend API shape
        ```
        layer
- **Layer 3 — Stateful flows**
    - add explicit AF `requestor` jobs only for flows that need:
        - `X-User-Id`
        - specific ids such as `gameId`
        - valid request bodies
        - existing backend state

This is the correct split because OpenAPI import can define the broad API
shape cheaply, while explicit `requestor` flows should be reserved for the
smaller set of truly stateful interactions.

One important detail: importing the OpenAPI definition by itself is **not**
enough to produce meaningful passive scan coverage in the reports. The passive
scanner only analyzes real HTTP requests and responses. That means Layer 2 must
combine:

- import of the generated OpenAPI contract
- explicit requests that actually produce request/response traffic over the
  imported REST surface

The repository now implements Layer 2 like this:

1. the backend exposes a generated OpenAPI document at:

   ```text
   /v3/api-docs
   ```

2. the Automation Framework plan imports that contract from:

   ```text
   .github/zap/backend-automation-plan.yaml
   ```

3. the same plan sends explicit `requestor` traffic to the documented REST
   endpoints so the resulting AF reports contain real HTTP messages instead of
   only a contract import

### How the AF workflow now prepares real state before the scan

One practical problem showed up when the AF plan was first expanded to stateful
endpoints: the deployed Render backend did **not** contain the fixed scan IDs
that existed in local fixture code, such as:

- `scan-open-lobby`
- `scan-game-1`

That made a purely static AF plan brittle. Requests like:

- `GET /api/lobbies/scan-open-lobby`
- `PATCH /api/lobbies/scan-open-lobby/settings`
- `GET /api/games/scan-game-1`

failed on the deployed environment because those resources were not actually
present.

The workflow therefore now prepares **dynamic lobby scan state** before ZAP
runs.

The current flow inside:

```text
.github/workflows/backend-security-af.yml
```

is:

1. wait for the deployed backend to become reachable
2. create a real lobby by calling:
   - `POST /api/lobbies`
3. join a real guest player by calling:
   - `POST /api/lobbies/{lobbyId}/join`
4. extract the returned `lobbyId`
5. generate a temporary plan file where placeholder tokens are replaced with
   that real `lobbyId`
6. run the AF scan against the generated plan

This keeps the stateful lobby coverage real without requiring hard-coded,
pre-seeded persistent IDs on Render.

### What the generated-plan placeholders are used for

The committed plan file contains placeholder values such as:

```text
__SCAN_MUTABLE_LOBBY_ID__
```

Before ZAP starts, the workflow replaces that placeholder with the real lobby
that was just created for the scan run.

That generated plan is then passed to the ZAP AF action.

This allows the AF plan to run positive lobby flows such as:

- `GET /api/lobbies/{lobbyId}`
- `PATCH /api/lobbies/{lobbyId}/settings`
- `POST /api/lobbies/{lobbyId}/ready`
- `POST /api/lobbies/{lobbyId}/unready`
- `POST /api/lobbies/{lobbyId}/leave`
- `DELETE /api/lobbies/{lobbyId}`

against a **real** existing resource rather than a guessed static ID.

### Why the workflow does not create a real game yet

The same technique is not cleanly available for game coverage yet.

The backend can create and mutate real lobby state over REST, but the current
public REST API does not expose a simple reliable way for the workflow to:

- start a lobby,
- obtain the resulting `gameId`,
- and then feed that `gameId` back into the AF plan

without introducing more persistent side effects or much more complex
orchestration.

Because of that, the current AF implementation uses:

- **positive real-state coverage** for the lobby flows that can be created
  safely before the scan
- **negative-path coverage** for game endpoints that still need a reliable game
  identifier strategy

That means the AF workflow already exercises:

- API-wide contract shape through OpenAPI import
- real positive lobby stateful flows
- negative-path game endpoint behavior

but it does **not yet** provide fully positive created-state coverage for the
game and draft endpoints on the deployed backend.

### Current practical coverage model

The current AF workflow should therefore be read as:

- **Layer 2**
  - generated OpenAPI import
  - broad REST endpoint-shape traffic
- **Layer 3 (implemented in part)**
  - real positive lobby-state flows created dynamically in the workflow
  - negative-path game-state requests until a reliable `gameId` strategy exists

This is materially stronger than the earlier public-endpoint-only scan, but it
is still not the final end state for stateful game coverage.

### The remaining follow-up for full positive stateful game coverage

To complete Layer 3 for game endpoints, one of these needs to be added in a
future PR:

- a reliable REST-visible way to discover the created `gameId` after starting a
  lobby
- a dedicated scan-fixture endpoint or environment for controlled game setup
- a more advanced AF/script chaining approach that can extract and reuse
  created game identifiers safely

Until then, the AF workflow should be treated as:

- complete for public endpoint coverage
- complete for OpenAPI-driven REST shape coverage
- complete for dynamic positive lobby-state coverage
- partial for positive game/draft-state coverage

---

## Workflow Files

The workflows are implemented in:

```text
.github/workflows/backend-security.yml
.github/workflows/backend-security-af.yml
```

- `backend-security.yml` contains the **baseline scan** workflow.
- `backend-security-af.yml` contains the **Automation Framework scan** workflow.

The code example below is the **baseline scan** workflow. The AF workflow follows the same overall readiness and reporting pattern, but runs the committed Automation Framework plan instead of the packaged baseline action.

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

## Why The Workflows Are Structured This Way

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

The workflows upload separate report sets.

### Baseline scan reports

```text
report_html.html
report_md.md
report_json.json
```

These are uploaded under the artifact:

```text
zap-security-report
```

### Automation Framework scan reports

```text
zap-af-report.html
zap-af-report.md
zap-af-report.json
```

These are uploaded under the artifact:

```text
zap-security-report-af
```

### HTML

```text
report_html.html / zap-af-report.html
```

Best for manual review in a browser.

### Markdown

```text
report_md.md / zap-af-report.md
```

Best for CI summaries, PR discussion, and issue creation.

### JSON

```text
report_json.json / zap-af-report.json
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
