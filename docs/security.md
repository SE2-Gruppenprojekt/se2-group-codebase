# Security and Automated API Security Testing

![Scope](https://img.shields.io/badge/Scope-Backend%20%26%20Frontend-475569) ![Domain](https://img.shields.io/badge/Domain-API%20Security%20%26%20Authentication-64748b) ![Transport](https://img.shields.io/badge/Transport-JWT%20Bearer%20Auth-6b7280)
![Scanning](https://img.shields.io/badge/Scanning-OWASP%20ZAP-52525b) ![Runtime](https://img.shields.io/badge/Runtime-Spring%20Security%20%2B%20Android%20Client-78716c) ![Status](https://img.shields.io/badge/Docs-Current%20Implementation-4b5563)

## Goal

This document explains the project’s current security model and how the
repository satisfies the requirement for automated API security testing:

```text
Implementierung eines automatisierten Penetration/Security Testing API endpoints (z.B. OWASP ZAP)
```

The current implementation has two main parts:

1. a JWT bearer authentication system for protected backend and Android client
   flows
2. automated OWASP ZAP scanning in GitHub Actions against the deployed backend

This is intentionally focused. The goal is not to build a full production
identity platform or a large security operations system. The goal is to have a
clear, correct, repository-owned security setup that covers:

- authenticated backend request handling
- authenticated Android client transport behavior
- automated API security scanning in CI

---

## Security Overview

At a high level, the current security model is:

```text
Android client
    -> stores backend-issued JWT
    -> sends Authorization: Bearer <jwt>
    -> receives 401/403 if token is missing/invalid

Backend
    -> verifies JWT
    -> derives acting user from JWT sub claim
    -> runs normal service-level authorization checks

GitHub Actions
    -> wakes deployed backend
    -> creates deterministic scan fixture state
    -> receives fixture JWTs
    -> runs OWASP ZAP against the real deployed API
```

That means the project now has:

- real token-based authentication for protected REST routes
- centralized backend identity verification
- centralized frontend bearer transport behavior
- automated CI security scanning aligned with the same auth model

---

## Why the JWT Migration Was Needed

The older request model trusted a caller-provided user id header:

```http
X-User-Id: player-1
```

That was identification, not authentication. Any caller could change the value
and impersonate another player.

Typical old-style controller code looked like this:

```kotlin
@PostMapping("/{gameId}/draw")
fun drawTile(
    @PathVariable gameId: String,
    @RequestHeader("X-User-Id") userId: String
): GameResponse {
    return drawTileService.drawTile(gameId, userId).toResponse()
}
```

The service-level checks were not the issue. The issue was that the `userId`
being checked came directly from an untrusted request header.

The project now uses:

```http
Authorization: Bearer <jwt>
```

The backend verifies that token first and only then derives the acting user id
from the JWT `sub` claim.

Current-style controller code looks like this:

```kotlin
@PostMapping("/{gameId}/draw")
fun drawTile(
    @PathVariable gameId: String,
    authentication: Authentication
): GameResponse {
    return drawTileService.drawTile(gameId, authentication.name).toResponse()
}
```

This is the core security improvement:

```text
before: user id was caller-chosen request data
now: user id is backend-verified token data
```

---

## Backend JWT Authentication

The backend JWT implementation is centered around:

```text
apps/backend/src/main/kotlin/at/se2group/backend/security/JwtService.kt
apps/backend/src/main/kotlin/at/se2group/backend/security/JwtAuthenticationFilter.kt
apps/backend/src/main/kotlin/at/se2group/backend/security/SecurityConfig.kt
apps/backend/src/main/kotlin/at/se2group/backend/service/LobbyAuthenticationService.kt
```

### JWT configuration

The backend reads JWT configuration from:

```yaml
app:
  auth:
    jwt:
      issuer: ${APP_AUTH_JWT_ISSUER:se2-group-backend}
      secret: ${APP_AUTH_JWT_SECRET:change-me-in-production-change-me-in-production}
      access-token-ttl-seconds: ${APP_AUTH_JWT_ACCESS_TOKEN_TTL_SECONDS:86400}
```

Current source:

- [application.yml](../apps/backend/src/main/resources/application.yml)

Operationally, the important environment variables are:

```text
APP_AUTH_JWT_ISSUER
APP_AUTH_JWT_SECRET
APP_AUTH_JWT_ACCESS_TOKEN_TTL_SECONDS
```

The issuer and TTL can be given safe defaults. The secret should be set
explicitly in deployed environments.

### Token issuance

The backend signs JWTs in [JwtService.kt](../apps/backend/src/main/kotlin/at/se2group/backend/security/JwtService.kt):

```kotlin
fun issueAccessToken(userId: String): String {
    val now = Instant.now()
    val expiresAt = now.plusSeconds(jwtProperties.accessTokenTtlSeconds)
    return JWT.create()
        .withIssuer(jwtProperties.issuer)
        .withSubject(userId)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(expiresAt))
        .sign(algorithm)
}
```

Important details:

- the authenticated user id is stored in `sub`
- the token is issuer-bound
- the token is time-limited
- the backend, not the client, decides the authoritative identity

### Token verification

Verification is centralized in the same service:

```kotlin
fun extractUserId(token: String): String {
    return try {
        verifier.verify(token).subject
            ?: throw InvalidBearerTokenAuthenticationException()
    } catch (_: Exception) {
        throw InvalidBearerTokenAuthenticationException()
    }
}
```

That means the backend stops trusting identity transport data and instead trusts
its own signature verification boundary.

### Request authentication filter

The JWT request boundary is implemented in
[JwtAuthenticationFilter.kt](../apps/backend/src/main/kotlin/at/se2group/backend/security/JwtAuthenticationFilter.kt).

The filter:

1. reads the `Authorization` header
2. parses scheme and token
3. performs a case-insensitive `Bearer` check
4. verifies the token through `JwtService`
5. stores a Spring Security authentication in the security context

Important fragment:

```kotlin
val authorization = request.getHeader("Authorization")

if (authorization.isNullOrBlank()) {
    filterChain.doFilter(request, response)
    return
}

val parts = authorization.trim().split(Regex("\\s+"), limit = 2)
val scheme = parts.firstOrNull()
val token = parts.getOrNull(1)?.trim()

if (!scheme.equals("Bearer", ignoreCase = true) || token.isNullOrBlank()) {
    authenticationEntryPoint.commence(
        request,
        response,
        InvalidBearerTokenAuthenticationException()
    )
    return
}
```

On success:

```kotlin
val userId = jwtService.extractUserId(token)
val authentication = UsernamePasswordAuthenticationToken.authenticated(
    userId,
    null,
    emptyList()
)
SecurityContextHolder.getContext().authentication = authentication
```

So the acting user available to controllers is:

```kotlin
authentication.name
```

### Route protection model

The REST security boundary is defined in
[SecurityConfig.kt](../apps/backend/src/main/kotlin/at/se2group/backend/security/SecurityConfig.kt).

Current public routes:

- `/`
- `/robots.txt`
- `/sitemap.xml`
- `/actuator/health`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/internal/security/**`
- `/ws/**`
- `GET /api/leaderboard`
- `GET /api/lobbies`
- `POST /api/lobbies`
- `POST /api/lobbies/*/join`

Everything else is authenticated.

The current configuration is:

```kotlin
.authorizeHttpRequests {
    it.requestMatchers("/", "/robots.txt", "/sitemap.xml").permitAll()
    it.requestMatchers("/actuator/health").permitAll()
    it.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
    it.requestMatchers("/internal/security/**").permitAll()
    it.requestMatchers("/ws/**").permitAll()
    it.requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
    it.requestMatchers(HttpMethod.GET, "/api/lobbies").permitAll()
    it.requestMatchers(HttpMethod.POST, "/api/lobbies").permitAll()
    it.requestMatchers(HttpMethod.POST, "/api/lobbies/*/join").permitAll()
    it.anyRequest().authenticated()
}
```

This split is intentional:

- create and join must be able to bootstrap a first session
- all protected lobby/game actions should require a token afterward

### Session bootstrap: create and join

The project does not have a separate username/password login system. The first
authenticated session is created during lobby create and lobby join.

That logic lives in
[LobbyAuthenticationService.kt](../apps/backend/src/main/kotlin/at/se2group/backend/service/LobbyAuthenticationService.kt).

Create flow:

```kotlin
fun createAuthenticatedLobby(
    request: CreateLobbyRequest,
    existingUserId: String?
): AuthenticatedLobbyResponse {
    val userId = existingUserId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
    val lobby = lobbyService.createLobby(userId, request)
    return AuthenticatedLobbyResponse(
        accessToken = jwtService.issueAccessToken(userId),
        lobby = lobby.toResponse()
    )
}
```

Join flow:

```kotlin
fun joinAuthenticatedLobby(
    lobbyId: String,
    request: JoinLobbyRequest,
    existingUserId: String?
): AuthenticatedLobbyResponse {
    val userId = existingUserId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
    val lobby = lobbyService.joinLobby(lobbyId, userId, request)
    return AuthenticatedLobbyResponse(
        accessToken = jwtService.issueAccessToken(userId),
        lobby = lobby.toResponse()
    )
}
```

This has an important continuity property:

- first-time create/join without a JWT creates a new user id
- repeated create/join with an existing JWT can reuse the authenticated user id

So the client does not re-authenticate by sending a raw `userId`. It
re-authenticates by reusing the bearer token it already has.

---

## Android JWT Client

The Android client was migrated from “send `X-User-Id` everywhere” to “store
and transport the backend-issued JWT”.

Key files:

```text
apps/android/app/src/main/proto/user.proto
apps/android/app/src/main/java/at/aau/serg/android/core/datastore/user/UserStore.kt
apps/android/app/src/main/java/at/aau/serg/android/core/network/auth/AuthTokenInterceptor.kt
apps/android/app/src/main/java/at/aau/serg/android/core/network/lobby/LobbyAPI.kt
apps/android/app/src/main/java/at/aau/serg/android/core/network/game/GameAPI.kt
```

### Local session storage

The local user session now includes the access token:

```proto
message User {
  string uid = 1;
  string displayName = 2;
  string gameId = 3;
  string accessToken = 4;
}
```

This gives the Android app one stable local source for:

- authenticated user id
- display name
- current game context
- JWT access token

### Automatic bearer header injection

Protected requests are authenticated centrally through an OkHttp interceptor:

```kotlin
class AuthTokenInterceptor(
    private val tokenProvider: AccessTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider.currentAccessToken()

        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val updated = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(updated)
    }
}
```

This is the main frontend transport change. The app no longer treats a plain
`userId` as request-level proof of identity.

### Frontend auth lifecycle

The Android flow is now:

1. create or join lobby
2. receive `AuthenticatedLobbyResponse`
3. persist `accessToken`
4. use bearer auth for protected lobby/game requests
5. clear or reset session when the backend returns unauthorized

The frontend therefore matches the backend contract:

```text
backend owns identity
frontend stores the backend-issued token
frontend transports the token
backend verifies the token
```

---

## Automated API Security Testing

The repository currently uses these workflows:

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
3. receives deterministic ids and JWTs for the scan actors
4. substitutes those values into a generated Automation Framework plan
5. runs a committed **Automation Framework plan** from the repository
6. generates dedicated markdown, HTML, and JSON AF reports
7. generates a separate endpoint-inventory markdown artifact
8. publishes the AF summary into the GitHub Actions run summary
9. uploads the AF reports and endpoint inventory as separate artifacts

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
- aligned with the current JWT-based request authentication model

---

## Scan Target

The workflows scan the deployed backend on Render:

```text
https://se2-group-codebase.onrender.com
```

The readiness endpoint is:

```text
https://se2-group-codebase.onrender.com/actuator/health
```

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

The readiness step exists because Render may be cold or sleeping. Without it,
many scan failures would really be deployment-timing failures rather than
security-scan failures.

---

## Why OWASP ZAP

OWASP ZAP is a good fit for this project because:

- it is standard and recognizable
- it runs well inside GitHub Actions
- it can scan a deployed HTTP target automatically
- it produces human-readable and machine-readable reports
- it supports both a baseline scan and a more explicit plan-driven model

The repository deliberately uses two scan layers.

### Baseline scan

Use the baseline workflow for:

- simple passive scanning
- fast recurring visibility in CI
- stable transport/header observations
- low operational risk against the deployed backend

### Automation Framework scan

Use the AF workflow for:

- a repository-owned scan definition
- richer API-shape coverage
- stateful positive-path request execution
- explicit endpoint inventory reporting
- authenticated request coverage using real fixture JWTs

The AF scan does not replace the baseline scan. It extends it.

---

## Security Scan Strategy

The baseline scan flow is:

```text
GitHub Actions
    -> wake deployed backend
    -> verify /actuator/health
    -> run OWASP ZAP baseline scan
    -> publish CI summary
    -> upload reports as artifacts
```

The Automation Framework flow is:

```text
GitHub Actions
    -> wake deployed backend
    -> verify /actuator/health
    -> call /internal/security/scan-fixture with X-Scan-Secret
    -> receive ids and JWTs for host/guest scan actors
    -> substitute values into a generated AF plan
    -> run ZAP Automation Framework
    -> generate endpoint coverage markdown
    -> publish CI summary
    -> upload reports as artifacts
```

This is deliberate:

- the scans run against the real deployed backend
- the reports are directly visible in CI
- the detailed reports remain downloadable
- the AF scan now exercises the same bearer-authenticated REST model as the
  real client

---

## Baseline Scan vs Automation Framework Scan

The difference in configuration model is important.

### Baseline scan

This is mostly a packaged scan with a small number of inputs:

```yaml
with:
  target: ${{ env.BACKEND_BASE_URL }}
  cmd_options: "-a -I"
```

That works because the baseline action already knows its built-in scan flow.

### Automation Framework scan

The AF scan is plan-driven. The repository explicitly defines:

- which context exists
- which URLs are in scope
- which requests should be executed
- which report jobs should run
- which exit behavior should be applied

That is why the repository keeps:

```text
.github/zap/backend-automation-plan.yaml
```

The committed plan is the scan definition itself.

---

## Dedicated Scan-Fixture Endpoint

The stateful gameplay routes cannot be exercised reliably by a generic crawler.

They need:

- a real `lobbyId`
- a real `gameId`
- valid acting users
- valid acting JWTs
- a known draft owner
- a known pre-draw rack
- a known first draw tile

That is why the repository exposes one internal infrastructure endpoint:

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

This keeps the fixture mechanism separate from the public API and prevents
accidental exposure in environments where it is not intended to run.

### Fixture response

The endpoint now returns both deterministic ids and real JWTs for the scan
actors:

```kotlin
data class SecurityScanFixtureResponse(
    val lobbyId: String,
    val hostUserId: String,
    val guestUserId: String,
    val gameId: String,
    val draftOwnerUserId: String,
    val hostAccessToken: String,
    val guestAccessToken: String
)
```

That is important because the AF workflow no longer has to simulate identity
with a trusted header. It can scan protected requests using the real bearer
auth model.

---

## Fixture Service Design

The actual fixture-building logic lives in:

```text
apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureService.kt
```

The service does three things:

1. deletes any previous scan fixture state if present
2. recreates one open lobby and one active lobby/game pair
3. creates a live turn draft for the active game

The goal is deterministic CI behavior.

Each run should start from the same known state instead of depending on:

- leftover scan data
- user-created lobbies
- changing demo state

### Fixed fixture identifiers

The deterministic fixture ids are:

- open lobby: `scan-open-lobby`
- active lobby: `scan-active-lobby`
- game: `scan-game-1`
- host user: `scan-host-user`
- guest user: `scan-guest-user`

### Deterministic game state

The service seeds:

- host rack before draw:
  - `scan-host-rack-red-5`
  - `scan-host-rack-blue-7`
- known first draw tile:
  - `scan-draw-red-1`

That is why the AF plan can run valid positive requests for:

- `GET /api/games/{gameId}`
- `PUT /api/games/{gameId}/draft`
- `POST /api/games/{gameId}/draw`
- `POST /api/games/{gameId}/end-turn`

---

## Placeholder-Based AF Plan Generation

The committed AF plan contains placeholders instead of hard-coded live ids:

- `__SCAN_LOBBY_ID__`
- `__SCAN_GAME_ID__`
- `__SCAN_HOST_USER_ID__`
- `__SCAN_GUEST_USER_ID__`
- `__SCAN_HOST_ACCESS_TOKEN__`
- `__SCAN_GUEST_ACCESS_TOKEN__`
- `__SCAN_HOST_AUTH_HEADER__`
- `__SCAN_GUEST_AUTH_HEADER__`

The workflow:

1. calls `/internal/security/scan-fixture`
2. parses the JSON response with `jq`
3. stores the values in `GITHUB_ENV`
4. constructs:

```text
Authorization:Bearer <host-jwt>
Authorization:Bearer <guest-jwt>
```

5. substitutes the placeholders into the committed plan
6. writes:

```text
.github/zap/backend-automation-plan.generated.yaml
```

That generated file is the concrete plan executed by ZAP.

### Example requestor entry

The current AF plan uses the authorization placeholders like this:

```yaml
- type: requestor
  name: "Positive lobby state: update prepared scan lobby settings"
  requests:
    - url: https://se2-group-codebase.onrender.com/api/lobbies/__SCAN_LOBBY_ID__/settings
      method: PATCH
      headers:
        - "__SCAN_HOST_AUTH_HEADER__"
        - "Content-Type:application/json"
      data: |
        {
          "maxPlayers": 4,
          "isPrivate": false,
          "allowGuests": true
        }
      responseCode: 200
```

And protected game requests follow the same pattern:

```yaml
- type: requestor
  name: "Positive game state: get prepared scan game"
  requests:
    - url: https://se2-group-codebase.onrender.com/api/games/__SCAN_GAME_ID__
      method: GET
      headers:
        - "__SCAN_HOST_AUTH_HEADER__"
      responseCode: 200
```

So the AF workflow is now aligned to the same authenticated REST contract as
the real application.

---

## Endpoint Coverage Artifact

The AF workflow generates an additional artifact:

```text
zap-af-endpoint-coverage.md
```

This file is produced by:

```text
.github/scripts/generate_zap_af_endpoint_coverage.rb
```

It combines:

1. the generated concrete AF plan
2. the AF JSON report

and writes a reviewer-friendly markdown artifact that highlights:

- target site metadata
- OpenAPI import counts
- explicit request inventory
- grouping by public, positive, and negative-path coverage
- response-profile statistics

### Core categorization logic

The script groups requests like this:

```ruby
def request_category(job_name, request)
  url = request.fetch("url")
  expected = request["responseCode"]

  return "Public Endpoints" if job_name.start_with?("Public endpoint:")
  return "Positive Lobby-State Endpoints" if url.include?("/api/lobbies/") && [200, 204].include?(expected)
  return "Positive Game-State Endpoints" if url.include?("/api/games/") && expected == 200

  "Negative-Path Endpoints"
end
```

That is why the artifact reads like an implementation summary rather than a raw
machine log.

---

## Workflow Files

The implemented security-related files are:

```text
.github/workflows/backend-security.yml
.github/workflows/backend-security-af.yml
.github/zap/backend-automation-plan.yaml
.github/scripts/generate_zap_af_endpoint_coverage.rb
apps/backend/src/main/kotlin/at/se2group/backend/security/JwtService.kt
apps/backend/src/main/kotlin/at/se2group/backend/security/JwtAuthenticationFilter.kt
apps/backend/src/main/kotlin/at/se2group/backend/security/SecurityConfig.kt
apps/backend/src/main/kotlin/at/se2group/backend/api/SecurityScanFixtureController.kt
apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureService.kt
apps/backend/src/main/kotlin/at/se2group/backend/service/SecurityScanFixtureBootstrap.kt
apps/backend/src/main/kotlin/at/se2group/backend/service/LobbyAuthenticationService.kt
apps/backend/src/main/resources/application.yml
apps/android/app/src/main/proto/user.proto
apps/android/app/src/main/java/at/aau/serg/android/core/network/auth/AuthTokenInterceptor.kt
apps/android/app/src/main/java/at/aau/serg/android/core/datastore/user/UserStore.kt
```

Supporting verification lives in:

```text
apps/backend/src/test/kotlin/at/se2group/backend/api/OpenApiDocsTest.kt
apps/backend/src/test/kotlin/at/se2group/backend/api/SecurityScanFixtureControllerTest.kt
apps/backend/src/test/kotlin/at/se2group/backend/api/SecurityScanFixtureBootstrapTest.kt
apps/backend/src/test/kotlin/at/se2group/backend/api/SecurityScanFixtureServiceTest.kt
apps/backend/src/test/kotlin/at/se2group/backend/api/LobbyControllerTest.kt
apps/backend/src/test/kotlin/at/se2group/backend/lobby/service/LobbyAuthenticationServiceTest.kt
```

---

## Relation to Backend Authorization

The security workflows are one layer. Actual request-level authorization still
depends on backend application logic.

Typical service-level checks still look like this:

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

The important difference is what feeds those checks now:

```text
before: caller-supplied X-User-Id
now: verified JWT subject via authentication.name
```

So the current authorization stack is:

```text
JWT verification
    -> authenticated principal
    -> service-level game/lobby authorization rules
```

This is a meaningful improvement even though the project still intentionally
stops short of a full account-management system.

---

## Limitations and Non-Goals

This implementation is intentionally strong in some areas and intentionally
narrow in others.

### What it does well

- protected REST requests now use real token-based authentication
- the Android client now transports backend-issued bearer tokens
- the backend derives acting user ids from verified JWTs
- CI performs repeatable API security scans against the real deployed backend
- the AF workflow exercises positive stateful flows with real authenticated
  requests

### What it does not try to be

- a full user-account or password-based identity platform
- an external OAuth/OpenID Connect integration
- a complete websocket JWT-authenticated session model
- a full active-attack security testing program against production

### Current websocket boundary

The current JWT rollout finalizes the REST/session model. The websocket
handshake endpoint is permitted in the backend security configuration:

```kotlin
it.requestMatchers("/ws/**").permitAll()
```

That keeps the current realtime transport working, but it is not equivalent to
a separate full STOMP-level JWT authentication design.

---

## Conclusion

The project now has a coherent security implementation across backend,
frontend, and CI scanning:

- the backend issues JWTs
- the Android client stores and sends those JWTs
- protected REST requests authenticate through `Authorization: Bearer`
- backend authorization rules operate on verified identities
- GitHub Actions scans the deployed backend with OWASP ZAP
- the Automation Framework scan uses dedicated fixture JWTs to exercise the
  protected API

That is a substantial improvement over the earlier trusted-header model and a
much stronger foundation for both gameplay security and automated API security
testing.
