# OWASP ZAP AF Workflow Guide

![Scope](https://img.shields.io/badge/Scope-Backend%20Security-475569)
![Workflow](https://img.shields.io/badge/Workflow-ZAP%20Automation%20Framework-64748b)
![Auth](https://img.shields.io/badge/Auth-JWT%20Bearer-6b7280)
![Fixtures](https://img.shields.io/badge/Fixtures-Deterministic%20Scan%20State-78716c)
![Runtime](https://img.shields.io/badge/Runtime-Render%20Deployed%20Backend-52525b)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-4b5563)

## Purpose

This document explains, from start to finish, how the OWASP ZAP Automation
Framework workflow works in this repository.

It covers:

- what the workflow is scanning
- how the backend scan fixtures are created
- how JWT authentication fits into the scan
- which GitHub and Render settings are required
- how the generated AF plan is built
- how to debug failures when the workflow breaks

This guide is specifically about:

- [.github/workflows/backend-security-af.yml](../.github/workflows/backend-security-af.yml)
- [.github/zap/backend-automation-plan.yaml](../.github/zap/backend-automation-plan.yaml)
- [SecurityScanFixtureController.kt](../apps/backend/src/main/kotlin/at/se2group/backend/api/SecurityScanFixtureController.kt)
- [SecurityScanFixtureService.kt](../apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureService.kt)

---

## High-level overview

The ZAP AF workflow does **not** scan the backend blindly.

Instead, it uses a prepared backend fixture so that it can make meaningful,
repeatable requests against:

- a real open lobby
- a real active game
- a real draft
- real users with valid authentication

The flow looks like this:

1. GitHub Actions waits until the deployed backend is reachable on Render.
2. The workflow calls an internal backend endpoint:
   - `POST /internal/security/scan-fixture`
3. The backend recreates deterministic scan data:
   - prepared lobby
   - prepared active game
   - prepared host user
   - prepared guest user
4. The backend returns:
   - fixture ids
   - JWT access tokens for the prepared users
5. The workflow injects those values into the generated ZAP AF plan.
6. ZAP executes the plan against the deployed backend.
7. Reports and endpoint coverage artifacts are generated and uploaded.

That means the scan is not just "pinging endpoints". It is exercising real API
flows using the same JWT auth model that real clients use.

---

## Which workflow runs this

The workflow file is:

- [.github/workflows/backend-security-af.yml](../.github/workflows/backend-security-af.yml)

It runs on:

- manual `workflow_dispatch`
- backend-related pull requests
- pushes to `main`
- a weekly schedule

Its target is the deployed Render backend:

```yaml
env:
  BACKEND_BASE_URL: https://se2-group-codebase.onrender.com
  HEALTH_URL: https://se2-group-codebase.onrender.com/actuator/health
```

So the scan always tests the deployed backend, not a temporary local server
inside GitHub Actions.

---

## Why scan fixtures exist

Many protected endpoints cannot be scanned meaningfully without state.

Examples:

- `GET /api/lobbies/{id}`
- `PATCH /api/lobbies/{id}/settings`
- `POST /api/games/{id}/draw`
- `POST /api/games/{id}/end-turn`

Those need:

- a real lobby id
- a real game id
- a real player who belongs to that lobby/game
- valid authorization

Without fixtures, the AF plan would mostly hit:

- `401 Unauthorized`
- `404 Not Found`
- or invalid-state conflicts

That would not prove that the positive paths still work.

So the backend creates deterministic scan fixtures before ZAP runs.

---

## The internal fixture endpoint

The workflow bootstraps everything through:

```http
POST /internal/security/scan-fixture
X-Scan-Secret: <secret>
```

This endpoint is implemented in:

- [SecurityScanFixtureController.kt](../apps/backend/src/main/kotlin/at/se2group/backend/api/SecurityScanFixtureController.kt)

It is intentionally:

- hidden from OpenAPI
- protected by a dedicated scan secret
- enabled only when scan fixtures are explicitly turned on

Core behavior:

```kotlin
@PostMapping("/scan-fixture")
fun createScanFixture(
    @RequestHeader("X-Scan-Secret", required = false) providedSecret: String?
): SecurityScanFixtureResponse
```

If the secret is missing or wrong, the endpoint rejects the request.

If the secret is valid, it calls:

```kotlin
val state = securityScanFixtureService.recreateFixture()
```

and returns a response containing both ids and JWTs.

---

## What the fixture response looks like

The workflow expects a JSON response with this shape:

```json
{
  "lobbyId": "scan-open-lobby",
  "hostUserId": "scan-host-user",
  "guestUserId": "scan-guest-user",
  "gameId": "scan-game-1",
  "draftOwnerUserId": "scan-host-user",
  "hostAccessToken": "<jwt>",
  "guestAccessToken": "<jwt>"
}
```

Meaning of each field:

- `lobbyId`
  - the prepared open lobby used for positive lobby-state requests
- `hostUserId`
  - the prepared host player identity
- `guestUserId`
  - the prepared guest player identity
- `gameId`
  - the prepared active game used for positive game-state requests
- `draftOwnerUserId`
  - the user who currently owns the prepared draft
- `hostAccessToken`
  - valid backend-issued JWT for `hostUserId`
- `guestAccessToken`
  - valid backend-issued JWT for `guestUserId`

These token fields are critical after the JWT auth refactor. The AF workflow is
now JWT-only and expects those fields to exist.

---

## How the backend creates those JWTs

The fixture controller does not invent tokens manually. It uses the same
backend JWT service that the rest of the application uses.

Relevant code pattern:

```kotlin
hostAccessToken = jwtService.issueAccessToken(state.hostUserId)
guestAccessToken = jwtService.issueAccessToken(state.guestUserId)
```

This matters because the scan should authenticate exactly like normal backend
clients do:

- same JWT issuer
- same signature
- same expiry logic
- same subject claim (`sub`)

So the ZAP scan is not using fake auth. It is using real backend-issued bearer
tokens.

---

## Where the fixture state itself comes from

The fixture ids are created by:

- [SecurityScanFixtureService.kt](../apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureService.kt)

That service recreates deterministic backend state for the scan.

Important point:

- the workflow does **not** create the fixture itself
- the backend service creates it
- the workflow only requests it and consumes the returned ids/tokens

This is why names like `scan-open-lobby` and `scan-game-1` show up in reports.

The service seeds known state so the AF plan can rely on it repeatedly.

---

## Why Render must run the updated backend code

The AF workflow runs against:

```text
https://se2-group-codebase.onrender.com
```

So even if your branch contains the correct code locally, the workflow still
depends on what Render is currently serving.

This is the most important operational point in the whole setup:

> The deployed backend on Render must already include the updated
> `SecurityScanFixtureController` implementation that returns
> `hostAccessToken` and `guestAccessToken`.

If Render is still on an older backend version, the fixture endpoint may return
only ids and no JWT token fields.

Then this workflow step will fail:

```bash
SCAN_HOST_ACCESS_TOKEN="$(echo "$FIXTURE_JSON" | jq -r '.hostAccessToken')"
SCAN_GUEST_ACCESS_TOKEN="$(echo "$FIXTURE_JSON" | jq -r '.guestAccessToken')"
```

and the guard will stop the scan:

```bash
if [ -z "$value" ] || [ "$value" = "null" ]; then
  echo "Scan fixture endpoint returned incomplete data"
  exit 1
fi
```

So when the AF workflow says:

```text
Scan fixture endpoint returned incomplete data
```

the first thing to check is whether Render is running the updated backend code.

---

## Required configuration in GitHub and Render

## GitHub Actions

In the repository settings:

`Settings -> Secrets and variables -> Actions`

you need the repository secret:

```text
ZAP_SCAN_FIXTURE_SECRET
```

This is the secret that GitHub sends in the request header:

```http
X-Scan-Secret: <secret>
```

## Render backend

In the backend service environment variables, you need:

```text
APP_SCAN_FIXTURE_ENABLED=true
APP_SCAN_FIXTURE_SECRET=<same value as ZAP_SCAN_FIXTURE_SECRET>
```

These do two things:

- enable the internal fixture endpoint
- make sure the endpoint only responds when the correct secret is provided

The values must match between:

- GitHub secret: `ZAP_SCAN_FIXTURE_SECRET`
- Render env var: `APP_SCAN_FIXTURE_SECRET`

If they do not match, the fixture request will fail with `403 Forbidden`.

---

## How the workflow uses the fixture response

After the fixture response comes back, the workflow extracts values with `jq`:

```bash
SCAN_LOBBY_ID="$(echo "$FIXTURE_JSON" | jq -r '.lobbyId')"
SCAN_GAME_ID="$(echo "$FIXTURE_JSON" | jq -r '.gameId')"
SCAN_HOST_USER_ID="$(echo "$FIXTURE_JSON" | jq -r '.hostUserId')"
SCAN_GUEST_USER_ID="$(echo "$FIXTURE_JSON" | jq -r '.guestUserId')"
SCAN_HOST_ACCESS_TOKEN="$(echo "$FIXTURE_JSON" | jq -r '.hostAccessToken')"
SCAN_GUEST_ACCESS_TOKEN="$(echo "$FIXTURE_JSON" | jq -r '.guestAccessToken')"
```

Then it constructs reusable auth headers:

```bash
SCAN_HOST_AUTH_HEADER="Authorization:Bearer $SCAN_HOST_ACCESS_TOKEN"
SCAN_GUEST_AUTH_HEADER="Authorization:Bearer $SCAN_GUEST_ACCESS_TOKEN"
```

Then it substitutes placeholders in the AF plan:

```bash
sed \
  -e "s/__SCAN_LOBBY_ID__/$SCAN_LOBBY_ID/g" \
  -e "s/__SCAN_GAME_ID__/$SCAN_GAME_ID/g" \
  -e "s|__SCAN_HOST_AUTH_HEADER__|$SCAN_HOST_AUTH_HEADER|g" \
  -e "s|__SCAN_GUEST_AUTH_HEADER__|$SCAN_GUEST_AUTH_HEADER|g" \
  .github/zap/backend-automation-plan.yaml \
  > .github/zap/backend-automation-plan.generated.yaml
```

So ZAP never runs against the placeholder template directly. It runs against the
generated concrete plan.

---

## The AF plan template

The template is:

- [.github/zap/backend-automation-plan.yaml](../.github/zap/backend-automation-plan.yaml)

This file contains:

- public requests
- positive lobby-state requests
- positive game-state requests
- negative-path requests
- report generation steps

Examples of protected requests now using JWT placeholders:

```yaml
- url: https://se2-group-codebase.onrender.com/api/lobbies/__SCAN_LOBBY_ID__
  method: GET
  headers:
    - "__SCAN_HOST_AUTH_HEADER__"
  responseCode: 200
```

```yaml
- url: https://se2-group-codebase.onrender.com/api/games/__SCAN_GAME_ID__/draw
  method: POST
  headers:
    - "__SCAN_HOST_AUTH_HEADER__"
  responseCode: 200
```

This is the JWT-specific alignment. Before the auth refactor, these protected
requests used `X-User-Id`. Now they use backend-issued bearer tokens.

---

## Public vs protected requests

Not every request in the AF plan should carry a JWT.

### Public requests stay public

Examples:

- `GET /`
- `GET /robots.txt`
- `GET /sitemap.xml`
- `GET /actuator/health`
- `GET /api/leaderboard`
- `GET /api/lobbies`
- invalid `POST /api/lobbies`
- missing `POST /api/lobbies/{id}/join`

These do not need bearer auth.

### Protected requests must use JWT

Examples:

- `GET /api/lobbies/{id}`
- `PATCH /api/lobbies/{id}/settings`
- `POST /api/lobbies/{id}/ready`
- `POST /api/lobbies/{id}/leave`
- `DELETE /api/lobbies/{id}`
- `GET /api/games/{id}`
- `PUT /api/games/{id}/draft`
- `POST /api/games/{id}/draw`
- `POST /api/games/{id}/end-turn`

These must use valid bearer tokens or they should fail with `401`.

---

## Why some negative-path requests still expect `404`

Some protected requests intentionally target missing resources, for example:

- missing lobby id
- missing game id

These still send a valid JWT.

Why:

- without auth, they would fail too early with `401`
- with auth, they correctly reach the controller/service layer
- then the backend can return the intended `404`

So the scan is checking both:

- authentication boundary
- correct missing-resource behavior behind that boundary

---

## Reports and artifacts

After ZAP runs, the workflow generates:

- `zap-af-report.md`
- `zap-af-report.html`
- `zap-af-report.json`
- `zap-af-endpoint-coverage.md`
- `.github/zap/backend-automation-plan.generated.yaml`

These artifacts are the fastest way to confirm whether the scan executed the
expected JWT-authenticated request path after a deployment.

The most useful files for debugging are:

## 1. `backend-automation-plan.generated.yaml`

This tells you what ZAP actually ran after placeholder substitution.

Use this to answer:

- did the workflow inject the real lobby/game ids?
- did it inject `Authorization: Bearer ...` headers?
- did the join request body match the current DTO shape?

## 2. `zap-af-endpoint-coverage.md`

This summarizes:

- which endpoints were exercised
- expected status codes
- response profile

## 3. `zap-af-report.md`

This is the human-readable ZAP alert report.

## 4. `zap-af-report.json`

This is the machine-readable version for deeper inspection.

---

## How to verify that JWT mode is really being used

A clean AF report with zero alerts does **not** by itself prove that the scan
used JWT auth correctly.

The definitive check is the generated plan artifact:

look at:

```text
.github/zap/backend-automation-plan.generated.yaml
```

If protected requests contain:

```yaml
Authorization:Bearer <token>
```

then the scan is truly running in JWT mode.

That is the file you should inspect whenever you want to confirm that the scan
is aligned with the current auth contract.

---

## Typical failure modes

## 1. `Scan fixture endpoint returned incomplete data`

Most likely causes:

- Render is running an older backend version
- the deployed fixture controller does not yet return token fields
- the backend code on Render is behind your branch

Fix:

- deploy the updated backend to Render
- rerun the workflow

## 2. `403 Forbidden` on `/internal/security/scan-fixture`

Most likely causes:

- `ZAP_SCAN_FIXTURE_SECRET` missing in GitHub
- `APP_SCAN_FIXTURE_SECRET` missing in Render
- values do not match

Fix:

- set both secrets
- ensure they are identical

## 3. `404` or `401` where a positive protected endpoint should return `200`

Most likely causes:

- fixture ids are wrong
- generated plan did not substitute properly
- fixture tokens are missing or invalid
- Render backend and local workflow contract are out of sync

Fix:

- inspect `backend-automation-plan.generated.yaml`
- inspect fixture endpoint response shape

## 4. Health check never becomes reachable

Most likely causes:

- Render service asleep or unhealthy
- backend deployment broken

Fix:

- check Render deploy logs
- verify `/actuator/health` manually

---

## Manual verification with curl

If you want to verify the fixture endpoint manually, call it directly:

```bash
curl -fsS \
  -X POST "https://se2-group-codebase.onrender.com/internal/security/scan-fixture" \
  -H "X-Scan-Secret: <your-secret>"
```

Expected result:

```json
{
  "lobbyId": "scan-open-lobby",
  "hostUserId": "scan-host-user",
  "guestUserId": "scan-guest-user",
  "gameId": "scan-game-1",
  "draftOwnerUserId": "scan-host-user",
  "hostAccessToken": "<jwt>",
  "guestAccessToken": "<jwt>"
}
```

If the token fields are missing, the deployed backend is not on the required
version yet.

---

## Final mental model

The AF workflow is best understood like this:

- GitHub Actions does not create scan state itself
- the backend creates scan state
- the backend also mints JWTs for the scan users
- the workflow injects those values into a generated AF plan
- ZAP runs that concrete plan against the deployed Render backend

So the scan is only correct when all three pieces match:

1. backend fixture code
2. workflow extraction/substitution logic
3. AF request plan

If those stay aligned, the ZAP AF scan continues to test the real backend
security model even after major authentication changes.
