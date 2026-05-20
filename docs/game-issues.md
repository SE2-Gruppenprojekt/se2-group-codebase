# Backend Match / Game Architecture & Business Logic - Backend Issues Split

## Shared monorepo model and transport layer

A shared implementation in `apps/shared` should serve both `apps/backend` and the frontend app. This replaces the old backend-only game domain/DTO work and the duplicated frontend model/DTO work for shared game structures.
It is also the contract surface that both sides will keep depending on as the feature grows. If this layer stays disciplined, the rest of the game stack becomes much easier to evolve without accidental mismatch.

1. feat(shared)(game): add shared game enums for tile color, board set type, game status, and turn draft status
2. feat(shared)(game): add shared tile and board set domain models
3. feat(shared)(game): add shared game player, confirmed game, and turn draft domain models
4. feat(shared)(game): add shared response DTOs for tile, board set, game player, confirmed game, and turn draft
5. feat(shared)(game): add shared command/request DTOs for draft update, end turn, and draw tile flows
6. feat(shared)(game): add shared websocket event DTOs for draft, game, turn change, timeout, and game end events
7. feat(shared)(game): add shared API error response DTO if game endpoints should use it directly
8. feat(shared)(game): switch backend match code to consume shared game models and DTO classes
9. feat(shared)(game): switch frontend match code to consume shared game models and DTO classes

---

## 1. Core game domain model

Moved to the shared monorepo model and transport layer above.

The backend should consume these classes from `apps/shared` instead of redefining them locally.

---

## 2. DTO model for game and draft

Moved to the shared monorepo model and transport layer above.

The backend should consume these DTO classes from `apps/shared` instead of keeping backend-only copies.

---

## 3. Persistence model

The durable storage layer for confirmed games and live turn drafts lives here. It establishes the entities, repositories, and mapper boundaries the rest of the backend feature depends on.
It effectively defines how the backend remembers a match over time, so it has an outsized impact on recovery, reconnect, and later validation work. The more explicit these boundaries are, the less service code will need to guess about persisted state.

18. feat(backend)(game): add confirmed game persistence entity
19. feat(backend)(game): add turn draft persistence entity
20. feat(backend)(game): add confirmed game repository
21. feat(backend)(game): add turn draft repository
22. feat(backend)(game): add json state mapper helper
23. feat(backend)(game): add confirmed game entity-domain mapper
24. feat(backend)(game): add turn draft entity-domain mapper

---

## 4. Tile pool and game initialization

Initial match state creation from a validated lobby belongs in this group. It covers tile generation, shuffling, hand distribution, draw-pile creation, first-player selection, and the first persisted draft.
This phase is where lobby state turns into authoritative game state with stable identities and initial ownership. A consistent initialization flow also makes later bugs much easier to diagnose because every match begins from a predictable baseline.

25. feat(backend)(game): add tile pool generation service
26. feat(backend)(game): add joker tile generation logic
27. feat(backend)(game): add tile shuffling service
28. feat(backend)(game): add initial hand distribution logic
29. feat(backend)(game): add draw pile creation logic
30. feat(backend)(game): add first turn player selection logic
31. feat(backend)(game): add game initialization service
32. feat(backend)(game): add initial confirmed game state creation
33. feat(backend)(game): add initial turn draft creation from confirmed game state

---

## 5. Match service foundation

The basic backend service surface for confirmed games and live drafts is defined here. The goal is to establish the service boundaries and core load/save responsibilities before more complex gameplay logic is added.

34. feat(backend)(game): add game service base structure
35. feat(backend)(game): add turn draft service base structure
36. feat(backend)(game): add score service base structure
37. feat(backend)(game): add game load service method
38. feat(backend)(game): add turn draft load service method
39. feat(backend)(game): add confirmed game save service method
40. feat(backend)(game): add turn draft save service method

---

## 6. Turn draft logic

The temporary in-progress turn state that players edit during a turn is the focus here. It covers draft ownership, updates, status, recreation, and timer-related draft lifecycle behavior.

41. feat(backend)(game): add current turn ownership validation for draft updates
42. feat(backend)(game): add draft update service method
43. feat(backend)(game): add draft version increment logic
44. feat(backend)(game): add draft reset service method
45. feat(backend)(game): add draft cancellation logic
46. feat(backend)(game): add draft status update logic
47. feat(backend)(game): add draft recreation after turn change
48. feat(backend)(game): add turn timer state model
49. feat(backend)(game): add turn timer start logic
50. feat(backend)(game): add turn timeout handling logic
51. feat(backend)(game): add draft reset on turn timeout
52. feat(backend)(game): add automatic turn advance after timeout
53. feat(backend)(game): expose turn timer information in confirmed game state

---

## 7. Turn progression logic

The non-rule mechanics of moving from one active player to the next are handled here. This includes next-player calculation, turn setup, and the state transitions needed after a completed move.

54. feat(backend)(game): add next player calculation logic
55. feat(backend)(game): add previous player helper logic if needed
56. feat(backend)(game): add turn start setup logic
57. feat(backend)(game): add turn end service method skeleton
58. feat(backend)(game): add active player validation for end turn
59. feat(backend)(game): add turn progression after successful move
60. feat(backend)(game): add draft recreation for next turn
61. feat(backend)(game): add turn state persistence after turn change

---

## 8. Draw and reset actions

Auxiliary turn actions outside normal tile placement belong in this group. It defines how drawing from the pile and resetting a draft affect confirmed game state and live draft state.

56. feat(backend)(game): add draw tile service method
57. feat(backend)(game): add draw pile empty-state handling
58. feat(backend)(game): add player hand update after draw
59. feat(backend)(game): add confirmed game update after draw
60. feat(backend)(game): add reset draft endpoint service logic
61. feat(backend)(game): add reset draft from confirmed state logic

---

## 9. Rule architecture foundation

This is the base layer for the backend rule engine. It introduces the shared validation result model, reusable helpers, and the top-level rule-service boundary that later validators compose behind.

62. feat(backend)(rules): add validation result model
63. feat(backend)(rules): add rule violation model
64. feat(backend)(rules): add base rule helper methods
65. feat(backend)(rules): add top-level rule service skeleton

---

## 10. Group validation

Validation for group sets in Rummikub is implemented here. The focus is on number equality, color uniqueness, size limits, and the first joker-specific edge cases.

66. feat(backend)(rules): add group validation service
67. feat(backend)(rules): validate group minimum size
68. feat(backend)(rules): validate group maximum size
69. feat(backend)(rules): validate equal numbers in group
70. feat(backend)(rules): validate unique colors in group
71. feat(backend)(rules): add joker handling in group validation
72. feat(backend)(rules): reject all-joker invalid group case

---

## 11. Run validation

Validation for run sets lives here. It covers ordering, color consistency, duplicate and gap detection, and joker handling for incomplete sequences.

73. feat(backend)(rules): add run validation service
74. feat(backend)(rules): validate run minimum size
75. feat(backend)(rules): validate equal color in run
76. feat(backend)(rules): validate duplicate numbers in run
77. feat(backend)(rules): validate ascending order in run
78. feat(backend)(rules): add gap detection in run validation
79. feat(backend)(rules): add joker gap filling in run validation
80. feat(backend)(rules): reject all-joker invalid run case

---

## 12. Set and board validation

Board-level validation above group and run validation is handled here. It covers unresolved-set routing, board-wide aggregation, and rejection of illegal final board states.

81. feat(backend)(rules): add set validation router
82. feat(backend)(rules): resolve unresolved set type before validation
83. feat(backend)(rules): route resolved group and run sets to the correct validator
84. feat(backend)(rules): add board validation service
85. feat(backend)(rules): validate all sets on board
86. feat(backend)(rules): aggregate validation errors across board
87. feat(backend)(rules): reject board with invalid group
88. feat(backend)(rules): reject board with invalid run

---

## 13. Tile conservation and move integrity

The core integrity rule of turn editing is protected here: players may rearrange allowed tiles, but must not invent, lose, or duplicate them. It compares confirmed server state against the submitted or updated draft.

89. feat(backend)(rules): add tile conservation validation service
90. feat(backend)(rules): compare confirmed board and draft board tile ids
91. feat(backend)(rules): compare active hand and draft hand tile ids
92. feat(backend)(rules): detect missing tiles during turn
93. feat(backend)(rules): detect duplicated tiles during turn
94. feat(backend)(rules): detect illegally inserted tiles during turn

---

## 14. First move rules

The special rules for a player's first valid meld belong here. They are intentionally separated from generic set validation because they depend on player progression state and first-move-specific score rules.

95. feat(backend)(rules): add first move validation service
96. feat(backend)(rules): skip first move validation for players who already completed initial meld
97. feat(backend)(rules): add initial meld minimum points validation
98. feat(backend)(rules): add first move hand-only restriction if needed

---

## 15. Top-level rule orchestration

Top-level backend-facing validation flow is assembled here by connecting the lower-level validators. The goal is to create one orchestration service that later end-turn logic can call without knowing the internal validator structure.

99. feat(backend)(rules): add submitted draft validation flow
100. feat(backend)(rules): call tile conservation validation from top-level rule service
101. feat(backend)(rules): call board validation from top-level rule service
102. feat(backend)(rules): call first move validation from top-level rule service
103. feat(backend)(rules): fail fast on invalid submitted draft

---

## 16. Submit / end-turn commit flow

The strict submission path that turns a validated draft into the next confirmed game state is implemented here. It covers loading the submitted draft, validation before commit, state persistence, and next-draft creation.

104. feat(backend)(game): add submitted draft load in end-turn flow
105. feat(backend)(game): validate submitted draft before commit
106. feat(backend)(game): update confirmed board from valid draft
107. feat(backend)(game): update active player hand from valid draft
108. feat(backend)(game): persist confirmed game after valid turn
109. feat(backend)(game): delete or close old draft after valid turn
110. feat(backend)(game): create next turn draft after valid turn
111. feat(backend)(game): reject invalid draft submission without committing state

---

## 17. Game end and scoring

Terminal match state and result calculation are covered here. This section defines how winners are detected, how final status is persisted, and how score-related data becomes part of the confirmed game state.

112. feat(backend)(game): add winner detection logic
113. feat(backend)(game): detect empty-hand win condition
114. feat(backend)(game): add game status transition to finished
115. feat(backend)(game): add score calculation service
116. feat(backend)(game): calculate remaining hand penalty points
117. feat(backend)(game): persist finished game result
118. feat(backend)(game): expose winner information in confirmed game state

---

## 18. WebSocket infrastructure

The underlying realtime transport layer for the game feature is established here. It covers dependencies, configuration, topic conventions, and the `/ws` endpoint itself.
They should stay low-level and stable so later event work can plug into them without revisiting the transport basics. The priority here is dependable wiring, not game-specific behavior yet.

119. feat(backend)(game)(websocket): add websocket dependency
120. feat(backend)(game)(websocket): add websocket configuration
121. feat(backend)(game)(websocket): add websocket endpoint /ws
122. feat(backend)(game)(websocket): add simple broker configuration
123. feat(backend)(game)(websocket): add game topic naming convention

---

## 19. WebSocket event payloads

Transport models sent over the game websocket topic are defined here. The goal is to make each game event explicit and stable before wiring them into service flows.
Each event should describe one clear state transition so clients can react without guesswork. That makes frontend integration, testing, and documentation much easier to keep aligned.

124. feat(backend)(game)(websocket): add game draft updated event dto
125. feat(backend)(game)(websocket): add game updated event dto
126. feat(backend)(game)(websocket): add turn changed event dto
127. feat(backend)(game)(websocket): add turn timed out event dto
128. feat(backend)(game)(websocket): add game ended event dto

---

## 20. Game broadcast service

The backend service responsible for emitting websocket events is added here. It should encapsulate topic naming and message payload creation so application services do not build websocket messages directly.

128. feat(backend)(game)(websocket): add game broadcast service
129. feat(backend)(game)(websocket): add draft updated broadcast method
130. feat(backend)(game)(websocket): add game updated broadcast method
131. feat(backend)(game)(websocket): add turn changed broadcast method
132. feat(backend)(game)(websocket): add turn timed out broadcast method
133. feat(backend)(game)(websocket): add game ended broadcast method

---

## 21. WebSocket emissions from service flow

Actual gameplay services are wired to websocket broadcasting here. It documents when draft, game, turn-change, timeout, and end-game events should be emitted after backend state changes.

133. feat(backend)(game)(websocket): emit game.draft.updated after draft update
134. feat(backend)(game)(websocket): emit game.updated after valid turn commit
135. feat(backend)(game)(websocket): emit turn.changed after advancing turn
136. feat(backend)(game)(websocket): emit game.draft.updated for newly created next draft
137. feat(backend)(game)(websocket): emit turn.timed_out after automatic timeout handling
138. feat(backend)(game)(websocket): emit game.ended after game finish
139. feat(backend)(game)(websocket): emit game.updated after draw tile if state changes

---

## 22. REST API foundation

The HTTP controller layer for the match feature is established here. It defines which game endpoints exist and creates the basic REST surface over the internal backend services.
This is where the internal match flow turns into a public contract. The controller layer should stay thin, but it still needs to make ownership, command shape, and response behavior coherent from the outside.

139. feat(backend)(game)(rest-api): add game controller
140. feat(backend)(game)(rest-api): add get confirmed game endpoint
141. feat(backend)(game)(rest-api): add update draft endpoint
142. feat(backend)(game)(rest-api): add end turn endpoint
143. feat(backend)(game)(rest-api): add draw tile endpoint
144. feat(backend)(game)(rest-api): add reset draft endpoint

---

## 23. REST request / response mapping

Transport DTO mapping to domain input and domain state back to API responses happens here. This is the translation layer that keeps HTTP payload shapes separate from internal backend models.

145. feat(backend)(game)(rest-api): add game response mapper
146. feat(backend)(game)(rest-api): add turn draft response mapper
147. feat(backend)(game)(rest-api): map update draft request to domain model
148. feat(backend)(game)(rest-api): map end turn request to service input
149. feat(backend)(game)(rest-api): return confirmed game response from get game endpoint
150. feat(backend)(game)(rest-api): return turn draft response after draft update
151. feat(backend)(game)(rest-api): include turn timer information in game response mapping

---

## 24. Error handling

Consistent and predictable match API failure behavior is defined here. It covers the error payload shape and maps common backend exceptions to stable HTTP status codes.

151. feat(backend)(game): add match api error response dto
152. feat(backend)(game): add global exception handler for match api
153. feat(backend)(game): map invalid input exceptions to bad request
154. feat(backend)(game): map missing game exceptions to not found
155. feat(backend)(game): map invalid state exceptions to conflict
156. feat(backend)(game): add generic internal server error fallback handler

---

## 25. Security and ownership checks

Authorization and command ownership inside the game feature are covered here. The focus is on ensuring that only the correct active player can mutate the current match or draft state.

157. feat(backend)(game): validate active player ownership for draft update
158. feat(backend)(game): validate active player ownership for end turn
159. feat(backend)(game): validate active player ownership for draw tile
160. feat(backend)(game): validate draft belongs to current turn player
161. feat(backend)(game): reject draft updates from non-active players

---

## 26. Reconnect and recovery support

Backend reconnect safety for temporarily disconnected clients is handled here. It covers loading confirmed game state, recovering the latest draft, and exposing enough metadata for recovery and conflict handling.
This is a correctness concern, not just a convenience feature, because live multiplayer state needs a trustworthy recovery path. The backend should remain the anchor that clients can resync against after any short outage.

162. feat(backend)(game): support loading confirmed game for reconnect
163. feat(backend)(game): support loading current draft for reconnect
164. feat(backend)(game): return current turn player in game response
165. feat(backend)(game): expose draft version for reconnect conflict handling
166. feat(backend)(game): keep latest draft persisted for reconnect recovery
167. feat(backend)(game): return turn timer information for reconnect recovery

---

## 27. Testing fixtures and utilities

Shared test data builders and helpers for the match feature belong here. Good fixtures reduce duplication and make later unit and integration tests much easier to read and maintain.

167. test(backend)(game): add shared tile test fixtures
168. test(backend)(game): add shared board set test fixtures
169. test(backend)(game): add shared game test fixtures
170. test(backend)(game): add shared draft test fixtures
171. test(backend)(game): add shared request dto test fixtures

---

## 28. Rule unit tests

Isolated testing of the backend rule engine belongs here. It should verify group, run, set, board, tile-conservation, and top-level orchestration behavior without involving transport or persistence layers.

172. test(backend)(rules): add group validation tests
173. test(backend)(rules): add run validation tests
174. test(backend)(rules): add set validation router tests
175. test(backend)(rules): add board validation tests
176. test(backend)(rules): add tile conservation tests
177. test(backend)(rules): add first move validation tests
178. test(backend)(rules): add top-level rule service tests
179. test(backend)(rules): add unresolved set type resolution tests

---

## 29. Service unit tests

Backend application-service behavior outside the controller layer is the focus here. It covers initialization, draft updates, draw actions, turn progression, game finishing, and timer-related logic.

179. test(backend)(game): add game initialization service tests
180. test(backend)(game): add turn draft service tests
181. test(backend)(game): add update draft service tests
182. test(backend)(game): add reset draft service tests
183. test(backend)(game): add draw tile service tests
184. test(backend)(game): add end turn service tests
185. test(backend)(game): add turn progression tests
186. test(backend)(game): add game finished detection tests
187. test(backend)(game): add turn timer service tests
188. test(backend)(game): add turn timeout handling tests

---

## 30. Controller and error handling tests

REST-surface and exception-mapping tests belong here. The goal is to verify endpoint behavior, status codes, and error payloads from the outside in.

187. test(backend)(game)(rest-api): add game controller tests
188. test(backend)(game)(rest-api): add update draft endpoint tests
189. test(backend)(game)(rest-api): add end turn endpoint tests
190. test(backend)(game)(rest-api): add draw tile endpoint tests
191. test(backend)(game)(rest-api): add reset draft endpoint tests
192. test(backend)(game): add global exception handler tests for match api

---

## 31. WebSocket tests

Websocket configuration and emitted event payloads are verified here. This should ensure that the transport layer matches the documented topic names and DTO shapes.
These tests are especially useful because websocket regressions are easy to miss in ordinary service tests. Locking down topics and payload contracts early makes Android integration much less brittle.

193. test(backend)(game)(websocket): add broadcast service tests
194. test(backend)(game)(websocket): test game.draft.updated topic and payload
195. test(backend)(game)(websocket): test game.updated topic and payload
196. test(backend)(game)(websocket): test turn.changed topic and payload
197. test(backend)(game)(websocket): test turn.timed_out topic and payload
198. test(backend)(game)(websocket): test game.ended topic and payload
199. test(backend)(game)(websocket): test websocket config loads successfully

---

## 32. Smoke / integration basics

These are lightweight end-to-end wiring checks for the backend match module. They are useful for catching obvious configuration or dependency breakage early, even before deeper integration coverage exists.
They are not meant to replace focused integration tests. Their value is in surfacing broken wiring, missing beans, or configuration drift as early and cheaply as possible.

199. test(backend): add backend context smoke test for match module
200. test(backend): add simple match module wiring smoke test

---

## 33. Documentation issues

Documentation work needed to explain the backend game feature clearly is tracked here. It covers architecture, models, lifecycle flows, validation, realtime communication, and API behavior.

201. docs(backend)(game): add backend game architecture overview
202. docs(backend)(game): document confirmed game state model
203. docs(backend)(game): document turn draft model
204. docs(backend)(rules): document rule validation architecture
205. docs(backend)(game)(websocket): document websocket game events
206. docs(backend)(game)(rest-api): document game command endpoints
207. docs(backend)(game): document turn lifecycle flow
208. docs(backend)(game): document live move sharing flow
209. docs(backend)(game): document board set type resolution flow
210. docs(backend)(game): document backend-managed turn timer and timeout flow

---

## 34. Nice optional extras

Useful cleanup and maintainability improvements that are not critical for the first working version are collected here. They mainly improve readability, reuse, logging, and long-term operability.

209. chore(backend)(game): add game topic helper constant
210. chore(backend)(game): add draft version conflict handling helper
211. chore(backend)(game): add common game exception classes
212. chore(backend)(rules): add reusable invalid result helper methods
213. chore(backend)(game): add logging for draft update flow
214. chore(backend)(game): add logging for end turn validation failures
215. chore(backend)(game)(websocket): add logging for emitted game events

---

## Suggested implementation order plan for the backend parent issue groupings

This section groups the backend issue families into a practical implementation sequence. It is meant to reduce coordination overhead and help the team build dependencies in a stable order.

A good implementation order for the backend parent issue groups is:

### 1. Shared monorepo model and transport layer

This phase establishes the shared source of truth for game models and DTOs before backend or frontend implementation diverges.

Start with:

- Shared monorepo model and transport layer

Why first:

- all later backend and frontend work depends on one shared source of truth for the game domain model and transport payloads

### 2. Persistence and initialization

This phase makes the backend capable of creating and storing a real match. It provides the first durable game state that later services can operate on.

Then implement:

- Persistence model
- Tile pool and game initialization

Why here:

- the backend needs to be able to create, save, and load a real match before turn logic can work

### 3. Service foundations

This phase builds the non-rule service backbone for games and drafts. It creates the operational match flow that later validation and API layers plug into.

Then implement:

- Match service foundation
- Turn draft logic
- Turn progression logic
- Draw and reset actions

Why here:

- this creates the core backend match flow before adding full validation and realtime behavior

### 4. Rule engine

This phase implements the backend validation stack from the bottom up. It should end with one top-level rule service that can validate submitted turns coherently.

Then implement:

- Rule architecture foundation
- Group validation
- Run validation
- Set and board validation
- Tile conservation and move integrity
- First move rules
- Top-level rule orchestration

Why here:

- once the services exist, the rule system can be integrated into end-turn validation cleanly

### 5. Commit flow and game lifecycle

This phase completes the actual gameplay loop by turning validated drafts into committed game state and handling match completion.

Then implement:

- Submit / end-turn commit flow
- Game end and scoring

Why here:

- this completes the full gameplay loop from editing a draft to committing turns and finishing the match

### 6. Realtime communication

This phase adds live synchronization once the underlying game flow already exists. Doing it here keeps websocket work anchored to real backend behavior.

Then implement:

- WebSocket infrastructure
- WebSocket event payloads
- Game broadcast service
- WebSocket emissions from service flow

Why here:

- after the game flow exists, realtime synchronization can be wired in properly

### 7. REST and API mapping

This phase exposes the internal backend match logic through the external HTTP API. It also finalizes response mapping, error handling, and ownership checks.

Then implement:

- REST API foundation
- REST request / response mapping
- Error handling
- Security and ownership checks
- Reconnect and recovery support

Why here:

- once the internal services are stable, expose them cleanly through the API

### 8. Testing and documentation

This phase adds coverage, support fixtures, and formal documentation around the now-stable architecture. It is easier to complete once the main flow is already defined.

Finish with:

- Testing fixtures and utilities
- Rule unit tests
- Service unit tests
- Controller and error handling tests
- WebSocket tests
- Smoke / integration basics
- Documentation issues
- Nice optional extras

Why last:

- these are easiest to complete once the architecture is already in place and stable

### 9. Backend polish / missing issue groupings

This phase is for robustness work after the main feature exists. It focuses on consistency, edge cases, resilience, observability, and maintainability improvements.

After the main backend parent issue groups are in place, continue with the backend polish issue groups from the polish section:

- State consistency and concurrency
- Recovery and lifecycle
- More rule edge cases
- Match progression and game rules
- Security / authorization / identity
- WebSocket robustness
- Logging and observability
- Persistence improvements
- More backend testing

Why here:

- these issues are best handled after the core backend architecture already works end to end
- they improve robustness, correctness, recovery behavior, and long-term maintainability

# Frontend Match / Game Architecture & Business Logic - Frontend Issues Split

## 1. Core frontend-only model layer

UI-local state that should not live in shared transport or domain models belongs here. It covers drag state, layout state, connection state, and other client-only concerns.

The shared game domain models and transport DTOs now belong in the shared monorepo subsection above. The frontend-only model layer should contain only local UI state and interaction state that should not be shared with the backend.

1. feat(android)(game): add game ui state model
2. feat(android)(game): add connection status model
3. feat(android)(game): add local drag interaction state model
4. feat(android)(game): add local board-set layout state model
5. feat(android)(game): add local hand-tile layout state model
6. feat(android)(game): add turn timer ui state model

---

## 2. Frontend DTOs for backend integration

Moved to the shared monorepo model and transport layer above.

The frontend should consume these DTO and event payload classes from `apps/shared` instead of maintaining a separate frontend-only copy of the transport model.

---

## 3. Mapper layer

The translation layer between shared DTOs, websocket payloads, and frontend-local state models is defined here. The goal is to keep network shapes separate from UI-facing state.

24. feat(android)(game): add tile and board set dto-to-model mappers
25. feat(android)(game): add game player, confirmed game, and turn draft dto-to-model mappers
26. feat(android)(game): add model-to-update draft request mapper
27. feat(android)(game): add websocket event dto-to-model mapper

---

## 4. Network / REST foundation

Android-side HTTP integration for the game feature is added here. It includes the Retrofit-style service surface, base configuration, serialization, and API error parsing.
This layer should stay narrow and boring: a thin client over the backend API, not a second business-logic tier. The less policy lives here, the easier it is to debug request failures and keep behavior aligned with the server.

31. feat(android)(game)(network): add game api service interface
32. feat(android)(game)(network): add get game endpoint call
33. feat(android)(game)(network): add update draft endpoint call
34. feat(android)(game)(network): add end turn endpoint call
35. feat(android)(game)(network): add draw tile endpoint call
36. feat(android)(game)(network): add reset draft endpoint call
37. feat(android)(game)(network): add base url configuration for game api
38. feat(android)(game)(network): add serialization setup for game dto models
39. feat(android)(game)(network): add error response parsing for game api

---

## 5. WebSocket foundation

The Android realtime client for game updates is established here. It covers connection lifecycle, topic subscription, event parsing, and safe handling of unknown payloads.
This layer should be resilient enough to survive ordinary connection problems without pushing complexity into every screen. It is the transport base that all live multiplayer behavior will depend on.

40. feat(android)(game)(websocket): add websocket library dependencies
41. feat(android)(game)(websocket): add websocket service base structure
42. feat(android)(game)(websocket): add websocket connect and disconnect logic
43. feat(android)(game)(websocket): add game topic subscription logic
44. feat(android)(game)(websocket): add websocket lifecycle logging and connection state tracking
45. feat(android)(game)(websocket): add websocket event parsing for draft, game, turn change, timeout, and game end events
46. feat(android)(game)(websocket): add unknown websocket event fallback handling

---

## 6. Current player identity handling

A consistent way for the frontend to know whether the local user owns the active turn is defined here. That state then drives editing permissions and UI affordances.

194. feat(android)(game): add local current user identity source
195. feat(android)(game): compare current user with turn owner
196. feat(android)(game): derive isActivePlayer state in viewmodel
197. feat(android)(game): derive isSpectatingCurrentTurn state in viewmodel

---

## 7. ViewModel foundation

The main orchestration layer between backend data, websocket events, and screen state is defined here. It should produce the stable state model that the UI renders from.

64. feat(android)(game): add game viewmodel base structure
65. feat(android)(game): add confirmed game load logic to viewmodel
66. feat(android)(game): add websocket connect and disconnect logic to viewmodel
67. feat(android)(game): add confirmed game, live draft, turn changed, and game ended state handling in viewmodel
68. feat(android)(game): add loading, error, and connection state handling in viewmodel
69. feat(android)(game): add turn timer and timeout handling in viewmodel
70. feat(android)(game): add automatic draft reset handling after timeout in viewmodel

---

## 8. Shared live draft behavior

Frontend treatment of the live draft as a shared multiplayer state is defined here. It covers observing another player's draft, replacing stale local state, and showing the current draft owner.
It is also where the UI needs to make the difference between confirmed game state and temporary editable draft state feel understandable. That conceptual split is central to the whole match experience.

129. feat(android)(game): render shared live draft for all players
130. feat(android)(game): restrict draft editing to active player only
131. feat(android)(game): allow inactive players to observe live draft changes
132. feat(android)(game): update live draft state from websocket game.draft.updated
133. feat(android)(game): replace outdated local draft with incoming shared draft
134. feat(android)(game): clear live draft when confirmed game update arrives
135. feat(android)(game): show active draft owner in ui

---

## 9. Draft update sending

How the active player sends draft updates back to the backend is covered here. It includes payload composition, send timing, and rules for when updates should or should not be sent.

136. feat(android)(game): add debounce/throttle strategy for draft updates
137. feat(android)(game): send draft update after tile rearrangement
138. feat(android)(game): send full draft board in update request
139. feat(android)(game): send full draft hand in update request
140. feat(android)(game): include draft version in update request
141. feat(android)(game): prevent draft update sending for inactive players
142. feat(android)(game): add draft update failure handling

---

## 10. Turn change handling

The transition from one active player to the next on the Android side is handled here. It includes UI resets, timer resets, and permission changes after a turn switch.
Turn changes affect nearly every visible part of the screen, so this logic needs to be deliberate. Clean turn-switch handling prevents stale editing state and confusing cross-turn leftovers.

159. feat(android)(game): update current turn player from websocket turn.changed
160. feat(android)(game): show turn changed indicator in ui
161. feat(android)(game): reset local interaction state on turn change
162. feat(android)(game): enable editing only when current player matches local user
163. feat(android)(game): disable editing immediately after turn change
164. feat(android)(game): refresh displayed live draft after turn change
165. feat(android)(game): reset turn timer display on turn change
166. feat(android)(game): handle automatic draft reset after timeout

---

## 11. End-turn flow

The submission path from the Android client is implemented here. It covers the button, ViewModel orchestration, network call, and the temporary UI state around submission and rejection.

143. feat(android)(game): add end turn button
144. feat(android)(game): enable end turn only for active player
145. feat(android)(game): disable end turn while submitting
146. feat(android)(game): add end turn action in viewmodel
147. feat(android)(game): call end turn endpoint
148. feat(android)(game): show validation error after failed end turn
149. feat(android)(game): clear temporary submission state after success
150. feat(android)(game): clear temporary submission state after failure

---

## 12. Draw and reset flow

Non-placement turn actions available in the game screen are covered here. This includes draw and reset commands, their UI affordances, and their ViewModel orchestration.

151. feat(android)(game): add draw tile button
152. feat(android)(game): enable draw tile only for active player
153. feat(android)(game): add draw tile action in viewmodel
154. feat(android)(game): call draw tile endpoint
155. feat(android)(game): add reset draft button
156. feat(android)(game): enable reset draft only for active player
157. feat(android)(game): add reset draft action in viewmodel
158. feat(android)(game): call reset draft endpoint

---

## 13. Game ended handling

How the Android client reacts once a match is finished is defined here. It includes winner state, navigation into the result screen, and initial result-screen rendering.
This is the point where the app leaves the live interaction loop and enters outcome presentation. It should feel decisive and easy to understand, especially after a long multiplayer match.

165. feat(android)(game): handle websocket game ended event in viewmodel
166. feat(android)(game): add game result navigation trigger
167. feat(android)(game): add winner display state
168. feat(android)(result): add result screen ui skeleton
169. feat(android)(result): render winner information
170. feat(android)(result): render player scores
171. feat(android)(result): add leave result screen action

---

## 14. Error handling

Backend and websocket failures are turned into usable frontend states here. This should provide both technical recovery behavior and clear player-facing feedback.

187. feat(android)(game): add game error mapper
188. feat(android)(game): map backend validation errors to user-friendly messages
189. feat(android)(game): map websocket errors to connection status
190. feat(android)(game): show invalid move error banner
191. feat(android)(game): show network failure error banner
192. feat(android)(game): show reconnecting state banner
193. feat(android)(game): add generic fallback error state
194. feat(android)(game): show turn timeout feedback state

---

## 15. Connection and reconnect handling

Android resilience to disconnects and reconnects is handled here. It covers connection status UI, reconnect logic, re-subscription, and state reload after reconnect.
This is not just polish for bad networks; it is a basic requirement for a live multiplayer screen. If reconnect behavior is weak, even a correct backend can still feel unreliable to players.

178. feat(android)(game)(websocket): expose websocket connection state and disconnect detection
179. feat(android)(game)(ui): show websocket connection status in game screen
180. feat(android)(game)(websocket): add automatic reconnect, re-subscribe, and duplicate-subscription protection
181. feat(android)(game): reload confirmed game and current draft after reconnect
182. feat(android)(game): resync turn timer and discard stale local timer after reconnect
183. feat(android)(game): clean up websocket connection on game screen exit

---

## 16. Navigation

How the Android app enters and leaves the game flow is defined here. It includes routing, argument handling, and navigation into the result screen.

172. feat(android)(game): add game screen route
173. feat(android)(result): add result screen route
174. feat(android)(game): navigate to game screen with gameId argument
175. feat(android)(game): read gameId argument from nav host
176. feat(android)(game): navigate to result screen on game end
177. feat(android)(game): handle invalid or missing gameId navigation case

---

## 17. Game screen UI foundation

The main static structure of the game screen is built here before detailed interaction logic is added. The focus is on layout, containers, loading states, and the broad visual regions of the screen.
The aim is to create a stable shell that later rendering and interaction work can plug into without repeated layout churn. A strong base screen also makes UI review and iteration much faster.

75. feat(android)(game)(ui): add game screen composable skeleton
76. feat(android)(game)(ui): add game screen header section
77. feat(android)(game)(ui): add current turn indicator
78. feat(android)(game)(ui): add player info row section
79. feat(android)(game)(ui): add game board container
80. feat(android)(game)(ui): add player hand container
81. feat(android)(game)(ui): add game action bar container
82. feat(android)(game)(ui): add loading state for game screen
83. feat(android)(game)(ui): add error state for game screen
84. feat(android)(game)(ui): add empty state fallback for missing game data
85. feat(android)(game)(ui): add turn timer display
86. feat(android)(game)(ui): add timeout feedback message

---

## 18. Shared UI utilities

Reusable UI pieces that multiple game-screen states can share are collected here. This helps keep the larger screen implementation smaller and more consistent.
This becomes more valuable once the screen branches into loading, error, active-turn, and spectating states. Reusable status and action components also make later polish work less repetitive.

198. feat(android)(game)(ui): add reusable turn badge component
199. feat(android)(game)(ui): add reusable connection status indicator
200. feat(android)(game)(ui): add reusable validation error banner
201. feat(android)(game)(ui): add reusable loading overlay
202. feat(android)(game)(ui): add reusable action button row
203. feat(android)(game)(ui): add reusable player summary component
204. feat(android)(game)(ui): add reusable turn timer component

---

## 19. Tile and board set UI components

Reusable composables that represent tiles and sets visually are defined here. They should cover the core visual language needed by both the hand and the board.

85. feat(android)(game)(ui): add reusable tile composable with joker variant
86. feat(android)(game)(ui): add tile selection, dragging, and disabled visual states
87. feat(android)(game)(ui): add tile color and number styling
88. feat(android)(game)(ui): add board set composable
89. feat(android)(game)(ui): add run and group set visual layouts
90. feat(android)(game)(ui): add board set spacing, arrangement, and local ordering behavior
91. feat(android)(game)(ui): add tile and board set previews

---

## 20. Board UI and rendering

Rendering the board in both confirmed and live-draft form is covered here. It also includes scroll behavior, list rendering, and visual treatment of board changes.
The board is the most state-dense area of the screen, so readability matters as much as correctness. The UI should make live draft changes obvious without making the confirmed state hard to parse.

98. feat(android)(game)(ui): add confirmed board rendering
99. feat(android)(game)(ui): add live draft board rendering
100.    feat(android)(game)(ui): add fallback to confirmed board when no draft exists
101.    feat(android)(game)(ui): add board scroll support
102.    feat(android)(game)(ui): add board set list rendering
103.    feat(android)(game)(ui): add board update animation placeholder
104.    feat(android)(game)(ui): add visual highlight for active draft changes

---

## 21. Hand UI and rendering

How the player's hand is shown and updated locally is defined here. It also covers the distinction between active-player hand rendering and limited rendering for other players.
This is where most interaction starts, so it needs to feel responsive and stable even under frequent updates. It should also make ownership and editing permissions visually obvious at a glance.

105. feat(android)(game)(ui): add player hand rendering
106. feat(android)(game)(ui): add active player hand update from live draft
107. feat(android)(game)(ui): add inactive player hand hidden/limited rendering
108. feat(android)(game)(ui): add hand tile spacing layout
109. feat(android)(game)(ui): add hand scroll support
110. feat(android)(game)(ui): add selected tile highlight in hand
111. feat(android)(game)(ui): add local hand-tile arrangement behavior
112. feat(android)(game)(ui): allow per-client hand ordering without changing authoritative state

---

## 22. Drag and drop / interaction basics

The core input mechanics for moving tiles around locally are covered here. This includes selection, drag lifecycle, drop handling, and cleanup after valid or invalid interactions.

111. feat(android)(game): add local tile selection logic
112. feat(android)(game): add drag start handling for hand and board tiles
113. feat(android)(game): add drag target tracking
114. feat(android)(game): add tile drop handling onto board and within existing board sets
115. feat(android)(game): add tile removal from board set back to hand
116. feat(android)(game): add local drag state reset and cleanup after drop or cancel
117. feat(android)(game): add invalid drop visual handling

---

## 23. Optimistic / local UX handling

Local editing behavior before backend confirmation arrives is refined here. The focus is on reconciling local interaction state with incoming authoritative draft updates.
This is where the frontend starts to feel polished instead of merely functional. The challenge is preserving responsiveness without pretending the client owns authoritative match state.

210. feat(android)(game): keep local drag responsiveness before backend confirmation
211. feat(android)(game): reconcile local drag state with incoming shared draft
212. feat(android)(game): reset stale drag state after server draft overwrite
213. feat(android)(game): preserve selection where possible after draft update
214. feat(android)(game): ignore own echoed draft update if identical
215. feat(android)(game): handle out-of-order draft version updates

---

## 24. ViewModel tests

Isolated coverage of the Android ViewModel layer belongs here. It should verify loading, websocket reactions, command handling, and timer-related state changes.

239. test(android)(game): add initial game load viewmodel tests
240. test(android)(game): add live draft update viewmodel tests
241. test(android)(game): add turn changed viewmodel tests
242. test(android)(game): add game ended viewmodel tests
243. test(android)(game): add websocket connection state viewmodel tests
244. test(android)(game): add end turn action viewmodel tests
245. test(android)(game): add draw tile action viewmodel tests
246. test(android)(game): add reset draft action viewmodel tests
247. test(android)(game): add turn timer viewmodel tests
248. test(android)(game): add timeout event handling viewmodel tests

---

## 25. UI tests

Visible behavior of the Android game UI is verified here. The focus is on rendering, button state, and key screen-level interactions rather than business logic internals.
They become especially valuable once the screen has to react to many different match and connection states. These tests protect the player-facing contract even when the internal state flow changes.

247. test(android)(game)(ui): add game screen rendering tests
248. test(android)(game)(ui): add turn indicator ui tests
249. test(android)(game)(ui): add player hand rendering tests
250. test(android)(game)(ui): add board rendering tests
251. test(android)(game)(ui): add end turn button state tests
252. test(android)(game)(ui): add connection status ui tests
253. test(android)(result)(ui): add result screen rendering tests
254. test(android)(game)(ui): add turn timer rendering tests

---

## 26. WebSocket service tests

Direct testing of the Android websocket client layer belongs here. It should verify connection lifecycle, topic subscription, event parsing, and reconnect behavior.

254. test(android)(game)(websocket): add websocket connection lifecycle tests
255. test(android)(game)(websocket): add topic subscription tests
256. test(android)(game)(websocket): add websocket event parsing tests for draft, game, and turn-change events
257. test(android)(game)(websocket): add websocket event parsing tests for timeout and game-end events
258. test(android)(game)(websocket): add reconnect behavior tests

---

## 27. Nice optional extras

These are useful Android cleanup and maintainability tasks that are not critical for the first complete game flow. They mostly improve structure, reuse, and developer ergonomics.
They are still worth tracking because UI code tends to accumulate little inconsistencies quickly once the feature grows. This bucket gives the team a place to capture those improvements without mixing them into core delivery work.

270. chore(android)(game): add game route constants
271. chore(android)(game): add websocket topic helper
272. chore(android)(game): add logging for draft update submissions
273. chore(android)(game): add logging for websocket event handling
274. chore(android)(game): add base game error messages
275. chore(android)(game): add reusable drag interaction utilities
276. chore(android)(game): add local layout helper utilities for board and hand arrangement

---

## Suggested implementation order plan for the frontend parent issue groupings

This section groups the Android issue families into a practical implementation sequence. It is intended to help the team build the feature in layers instead of mixing UI, network, and realtime work too early.

A good implementation order for the frontend parent issue groups is:

### 1. Shared models, frontend-only state, and mappers

This phase establishes the frontend data model boundaries first: shared transport types where possible, local UI state where necessary, and the mapping between them.

Start with:

- Shared monorepo model and transport layer
- Core frontend-only model layer
- Mapper layer

Why first:

- the frontend should first depend on the shared source-of-truth game models and DTOs, then add only its own local UI state and mapping layer on top

### 2. Communication foundations

This phase gives the Android client the ability to talk to the backend through both REST and websocket channels before higher-level screen logic is built on top.

Then implement:

- Network / REST foundation
- WebSocket foundation

Why here:

- the frontend must be able to talk to the backend before state and screen logic can be built properly

### 3. State and orchestration layers

This phase builds the ViewModel and user-identity logic that coordinate data loading, live updates, and command ownership.

Then implement:

- ViewModel foundation
- Current player identity handling

Why here:

- this creates the main state flow between backend data, websocket updates, and UI state

### 4. Base game screen and shared UI

This phase creates the reusable UI skeleton for the game screen before detailed board and hand interactions are layered in.

Then implement:

- Game screen UI foundation
- Shared UI utilities

Why here:

- this gives the team a visible screen structure early and makes it easier to plug in the real data later

### 5. Board, hand, and interaction layer

This phase implements the core visible gameplay surface: tiles, sets, board rendering, hand rendering, and local drag/drop behavior.

Then implement:

- Tile and board set UI components
- Board UI and rendering
- Hand UI and rendering
- Drag and drop / interaction basics

Why here:

- these are the main gameplay interaction pieces needed for editing the live draft

### 6. Shared draft and turn flow

This phase connects the live gameplay loop: draft updates, draw/reset actions, end-turn handling, and turn-change/game-end reactions.

Then implement:

- Shared live draft behavior
- Draft update sending
- End-turn flow
- Draw and reset flow
- Turn change handling
- Game ended handling

Why here:

- once rendering and interactions exist, the full turn flow can be connected to backend updates

### 7. Navigation, reconnect, and resilience

This phase hardens the client for real use by covering navigation, recovery, reconnect handling, and user-facing error behavior.

Then implement:

- Navigation
- Connection and reconnect handling
- Error handling
- Optimistic / local UX handling

Why here:

- this makes the feature robust enough for real use and helps the app recover from failures and reconnects

### 8. Testing, docs, and optional cleanup

This phase adds verification and cleanup once the main feature already works. It is the right time to invest in tests and smaller ergonomic improvements.

Finish with:

- ViewModel tests
- UI tests
- WebSocket service tests
- Nice optional extras

Why last:

- these are best completed once the main feature flow already exists and is stable enough to verify properly

### 9. Frontend polish / missing issue groupings

This phase covers the larger second-pass polish work: synchronization safety, lifecycle recovery, UX refinement, accessibility, and longer-term cleanup.

After the main frontend parent issue groups are in place, continue with the frontend polish issue groups from the polish section:

- State synchronization and consistency
- Recovery / reconnect / app lifecycle
- UX and interaction polish
- Drag/drop edge cases
- Performance and rendering
- Identity and permissions
- Error handling and resilience
- Accessibility and usability
- More frontend testing
- Cross-cutting / shared polish issues

Why here:

- these issues are best handled after the base frontend feature already works with real backend integration
- they improve synchronization safety, usability, resilience, accessibility, and overall polish

---

# Polish Issues - missing in the main issue stack

## Backend

This section collects backend polish issues that were not part of the main grouped issue stack. These are mostly robustness, edge-case, and long-term maintenance concerns.

### State consistency and concurrency

Keeping the backend authoritative under conflicting or stale draft operations is the focus here. These problems matter most once the basic draft and end-turn flows already work.
This is where the system starts dealing with less ideal but very realistic timing problems. The goal is to define explicit conflict behavior instead of letting race conditions decide outcomes implicitly.

1. feat(backend)(game): add draft version conflict handling
2. feat(backend)(game): reject stale draft updates by version
3. feat(backend)(game): prevent end turn while stale draft version is submitted
4. feat(backend)(game): handle concurrent draft update and end turn requests
5. feat(backend)(game): add last-write / version consistency strategy for draft state

---

### Recovery and lifecycle

Backend recovery behavior around reconnects, cleanup, and restart scenarios is covered here. The aim is to make match state safer over longer-running sessions.
These issues become important as soon as real players can leave and rejoin a match at inconvenient times. The backend should stay predictable even when the surrounding client lifecycle is not.

6. feat(backend)(game): add reconnect-safe game state recovery flow
7. feat(backend)(game): add reconnect-safe draft state recovery flow
8. feat(backend)(game): add stale draft cleanup logic
9. feat(backend)(game): add finished game persistence cleanup strategy
10. feat(backend)(game): add match recovery behavior after backend restart

---

### More rule edge cases

These are additional validation issues beyond the first complete rule engine. They focus especially on joker handling, rearrangement edge cases, and invalid no-op submissions.

11. feat(backend)(rules): add advanced joker validation edge cases
12. feat(backend)(rules): validate joker placement inside runs more strictly
13. feat(backend)(rules): validate joker placement inside groups more strictly
14. feat(backend)(rules): add exact first meld scoring rule implementation
15. feat(backend)(rules): restrict first meld to hand-only tiles if required
16. feat(backend)(rules): validate complex board rearrangement edge cases
17. feat(backend)(rules): validate unchanged board with no effective move
18. feat(backend)(rules): detect illegal no-op turn submissions

---

### Match progression and game rules

Further gameplay-rule details that can be layered on after the first playable loop exists are captured here. They are useful for making the implementation closer to the full rule set.

19. feat(backend)(game): add pass turn logic if needed
20. feat(backend)(game): add draw-and-end-turn flow handling
21. feat(backend)(game): add no-move available handling
22. feat(backend)(game): add end-of-game score calculation details
23. feat(backend)(game): add tie-breaking logic if needed

---

### Security / authorization / identity

Authorization and participant validation around match commands and game event production are tightened here. This hardens the backend against invalid callers and states.

24. feat(backend)(game): validate player belongs to game before processing commands
25. feat(backend)(game): validate websocket events are only produced for valid match participants
26. feat(backend)(game): centralize acting-player authorization checks
27. feat(backend)(game): reject commands for finished matches
28. feat(backend)(game): reject commands for non-active players consistently

---

### WebSocket robustness

Improvements to the quality of the realtime contract itself live here. The focus is on sequencing, timestamps, event versioning, and reconnect-safe websocket semantics.

29. feat(backend)(game)(websocket): add event sequencing metadata
30. feat(backend)(game)(websocket): add server timestamp to websocket events
31. feat(backend)(game)(websocket): add event version to game draft updates
32. feat(backend)(game)(websocket): add event version to confirmed game updates
33. feat(backend)(game)(websocket): add websocket reconnect resync support contract

---

### Logging and observability

Operational and debugging improvements for the backend are collected here. They are mainly about structured logging around gameplay, validation failures, and emitted realtime events.

34. chore(backend)(game): add structured logging for draft updates
35. chore(backend)(game): add structured logging for invalid move submissions
36. chore(backend)(game): add structured logging for turn transitions
37. chore(backend)(game)(websocket): add structured logging for emitted game events

---

### Persistence improvements

Long-term storage concerns that usually matter after the initial schema has stabilized are covered here. This includes migrations, indexing, archival, and schema evolution strategy.

38. chore(backend)(game): add migration strategy for game json state schema
39. chore(backend)(game): add migration strategy for draft json state schema
40. chore(backend)(game): add index strategy for game and draft lookup
41. chore(backend)(game): add archival strategy for finished games

---

### More backend testing

These are the higher-value backend tests that become useful after the main implementation and first polish pass are already in place. They focus on concurrency, reconnects, and integrated behavior.

42. test(backend)(game): add concurrent draft update tests
43. test(backend)(game): add draft version conflict tests
44. test(backend)(rules): add joker edge case tests
45. test(backend)(game): add reconnect recovery tests
46. test(backend)(game): add invalid no-op turn tests
47. test(backend)(game)(integration): add end-turn integration test across persistence and websocket
48. test(backend)(game)(integration): add reconnect integration test for confirmed state and draft state

---

## Frontend

This section collects Android polish issues that sit beyond the main grouped implementation plan. They mostly improve synchronization, resilience, UX quality, and advanced testing depth.

### State synchronization and consistency

Keeping the Android client aligned with authoritative backend state under repeated, stale, or out-of-order websocket updates is the concern here.
Without this layer, the UI can look inconsistent even if transport and backend logic are technically correct. These issues are about making state convergence reliable under noisy realtime conditions.

49. feat(android)(game): ignore stale websocket draft updates by version
50. feat(android)(game): ignore stale websocket confirmed game updates by version
51. feat(android)(game): handle out-of-order websocket events
52. feat(android)(game): deduplicate repeated websocket events
53. feat(android)(game): reconcile local draft with server draft after version conflict

---

### Recovery / reconnect / app lifecycle

Hardening the Android experience around reconnects and app lifecycle interruptions is the focus here. The goal is to restore the game state safely after the app leaves and re-enters the active session.

54. feat(android)(game): restore game screen state after app process recreation
55. feat(android)(game): reload confirmed game after app returns from background
56. feat(android)(game): reload draft after app returns from background
57. feat(android)(game): handle reconnect while active player is editing
58. feat(android)(game): handle reconnect while spectating another player's draft
59. feat(android)(game): add full resync after websocket reconnect

---

### UX and interaction polish

Improvements to how the game feels once the base functionality exists are collected here. The focus is on clearer feedback, better turn visibility, and a stronger distinction between confirmed and live state.
They should be treated as gameplay clarity work, not just visual decoration. Better cues here reduce user confusion and make the realtime model easier to understand during play.

60. feat(android)(game)(ui): add clearer invalid set indicators
61. feat(android)(game)(ui): add turn ownership badge
62. feat(android)(game)(ui): add “waiting for other player” state
63. feat(android)(game)(ui): add “syncing move” indicator
64. feat(android)(game)(ui): add “move rejected” feedback
65. feat(android)(game)(ui): add “reconnected and resynced” feedback
66. feat(android)(game)(ui): add visual distinction between confirmed board and live draft
67. feat(android)(game)(ui): add visual highlight for tiles changed in current draft

---

### Drag/drop edge cases

Less common but important interaction failures around local tile movement are handled here. The goal is to keep the draft visually and logically consistent even under interrupted gestures.

68. feat(android)(game): handle drag cancellation safely
69. feat(android)(game): handle invalid drop target safely
70. feat(android)(game): prevent duplicate tile rendering during drag
71. feat(android)(game): prevent tile loss during drag interruption
72. feat(android)(game): preserve board consistency during rapid drag actions
73. feat(android)(game): support moving tile between two existing sets cleanly
74. feat(android)(game): support splitting an existing set into two draft sets
75. feat(android)(game): support merging tiles into an existing set cleanly

---

### Performance and rendering

Recomposition, payload size, and other performance concerns that become more visible once live draft updates are flowing frequently are addressed here.

76. chore(android)(game): optimize board recomposition during websocket draft updates
77. chore(android)(game): optimize hand recomposition during websocket draft updates
78. chore(android)(game): reduce unnecessary UI updates for unchanged draft events
79. chore(android)(game): review payload size impact of full-draft updates
80. chore(android)(game): add throttling strategy for frequent draft updates

---

### Identity and permissions

Consistently reflecting what the local user is actually allowed to do is the purpose of this group. It keeps active-player and inactive-player behavior aligned with backend rules.

81. feat(android)(game): derive active-player permissions consistently in viewmodel
82. feat(android)(game): block drag/edit actions for non-active players
83. feat(android)(game): block end turn action for non-active players consistently
84. feat(android)(game): block draw/reset actions for non-active players consistently

---

### Error handling and resilience

Recovery from command failures, websocket instability, and version conflicts is improved here so the Android client does not leave the player stuck.

85. feat(android)(game): show version conflict error message
86. feat(android)(game): recover gracefully after rejected draft update
87. feat(android)(game): recover gracefully after rejected end turn
88. feat(android)(game): add fallback resync after repeated websocket failures
89. feat(android)(game): add fallback resync after repeated command failures

---

### Accessibility and usability

Readability and accessibility improvements for the game UI belong here. They become especially useful once the main board and hand rendering have stabilized visually.

90. feat(android)(game)(ui): improve tile accessibility labels
91. feat(android)(game)(ui): improve board accessibility labels
92. feat(android)(game)(ui): add content descriptions for action buttons
93. feat(android)(game)(ui): improve readability of tile colors and numbers

---

### More frontend testing

These are the additional Android tests that become valuable once reconnect, synchronization, and reconciliation behavior are implemented and worth verifying in detail.
They target the failure modes that are hardest to trust by inspection alone. Once the base flow works, this is where extra test effort prevents regressions in the most fragile interaction paths.

94. test(android)(game): add stale draft event handling tests
95. test(android)(game): add out-of-order websocket event tests
96. test(android)(game): add reconnect resync tests
97. test(android)(game): add process recreation state restore tests
98. test(android)(game)(ui): add invalid set indicator UI tests
99. test(android)(game)(ui): add active vs inactive player UI permission tests
100.    test(android)(game): add local/server draft reconciliation tests

---

### Cross-cutting / shared polish issues

Documentation and shared clarification work that helps both backend and frontend teams align on behavior after the main implementation is in place is captured here.

101. docs(game): document draft versioning and conflict handling
102. docs(game): document reconnect and resync flow
103. docs(game): document confirmed state vs live draft state clearly
104. docs(game): document active-player permissions and command rules
105. docs(game): document end-turn validation and rejection flow

---

## Best “missing issues” to prioritize first

This subsection highlights the highest-value polish issues if the team does not want to add every missing issue immediately. It is intended as a practical shortlist, not a replacement for the full backlog.

If you do not want to create all of them immediately, the most useful missing ones are:

- add draft version conflict handling
- reject stale draft updates by version
- ignore stale websocket draft updates by version
- handle out-of-order websocket events
- add full resync after websocket reconnect
- add reconnect-safe game state recovery flow
- add reconnect-safe draft state recovery flow
- add advanced joker validation edge cases
- add concurrent draft update tests
- add reconnect resync tests

## Suggested implementation order plan for the polish issue groupings

This final ordering section groups the polish issues into a practical second-pass sequence. The intent is to stabilize consistency and reconnect behavior first, then deepen rules, resilience, UX, and testing.

A good implementation order for the polish issue groups is:

### 1. State consistency and reconnect safety

This phase protects the architecture from stale state and recovery bugs before the team spends time on lower-impact polish work.

Start with:

- backend state consistency and concurrency issues
- frontend state synchronization and consistency issues
- backend recovery and lifecycle issues
- frontend recovery / reconnect / app lifecycle issues

Why first:

- these issues protect the core architecture from stale state, reconnect bugs, and conflicting updates

### 2. Validation and gameplay edge cases

This phase tightens the correctness of both backend rule handling and frontend tile interaction once the primary gameplay loop already works.

Then implement:

- backend rule edge cases
- backend match progression and game-rule edge cases
- frontend drag/drop edge cases

Why here:

- after the base feature works, these issues improve correctness for unusual but important gameplay cases

### 3. Authorization, robustness, and resilience

This phase hardens both sides of the system against invalid users, invalid states, and degraded realtime/network conditions.

Then implement:

- backend security / authorization / identity issues
- backend websocket robustness issues
- frontend identity and permission issues
- frontend error handling and resilience issues

Why here:

- these make the feature safer and more robust in real multiplayer scenarios

### 4. UX and performance polish

This phase improves how the feature feels and performs once correctness is already in a good place. It covers usability, rendering cost, and developer visibility.

Then implement:

- frontend UX and interaction polish issues
- frontend performance and rendering issues
- frontend accessibility and usability issues
- backend logging and observability issues

Why here:

- once correctness is solid, polish the gameplay experience and developer visibility

### 5. Persistence and long-term maintenance

This phase addresses storage, schema, and documentation concerns that matter more once the core design is already stable enough to preserve.

Then implement:

- backend persistence improvements
- cross-cutting / shared polish issues

Why here:

- these issues matter more once the architecture has stabilized and the team knows which long-term concerns are worth keeping

### 6. Advanced testing

This final phase expands the automated safety net around the already-polished feature. It is most useful once the behavior being tested has largely settled.

Finish with:

- backend extra testing issues
- frontend extra testing issues

Why last:

- these tests are most useful after the main feature and polish behavior are already implemented and stable enough to verify thoroughly
