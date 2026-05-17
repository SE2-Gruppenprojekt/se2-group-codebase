# Backend Match / Game Architecture & Business Logic - Backend Issues Split

## Shared monorepo model and transport layer

These issues should be implemented once in `apps/shared` and then consumed from both `apps/backend` and the frontend app. This replaces the old backend-only game domain/DTO work and the duplicated frontend model/DTO work for shared game structures.

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

18. feat(backend)(game): add confirmed game persistence entity
19. feat(backend)(game): add turn draft persistence entity
20. feat(backend)(game): add confirmed game repository
21. feat(backend)(game): add turn draft repository
22. feat(backend)(game): add json state mapper helper
23. feat(backend)(game): add confirmed game entity-domain mapper
24. feat(backend)(game): add turn draft entity-domain mapper

---

## 4. Tile pool and game initialization

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

34. feat(backend)(game): add game service base structure
35. feat(backend)(game): add turn draft service base structure
36. feat(backend)(game): add score service base structure
37. feat(backend)(game): add game load service method
38. feat(backend)(game): add turn draft load service method
39. feat(backend)(game): add confirmed game save service method
40. feat(backend)(game): add turn draft save service method

---

## 6. Turn draft logic

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

56. feat(backend)(game): add draw tile service method
57. feat(backend)(game): add draw pile empty-state handling
58. feat(backend)(game): add player hand update after draw
59. feat(backend)(game): add confirmed game update after draw
60. feat(backend)(game): add reset draft endpoint service logic
61. feat(backend)(game): add reset draft from confirmed state logic

---

## 9. Rule architecture foundation

62. feat(backend)(rules): add validation result model
63. feat(backend)(rules): add rule violation model
64. feat(backend)(rules): add base rule helper methods
65. feat(backend)(rules): add top-level rule service skeleton

---

## 10. Group validation

66. feat(backend)(rules): add group validation service
67. feat(backend)(rules): validate group minimum size
68. feat(backend)(rules): validate group maximum size
69. feat(backend)(rules): validate equal numbers in group
70. feat(backend)(rules): validate unique colors in group
71. feat(backend)(rules): add joker handling in group validation
72. feat(backend)(rules): reject all-joker invalid group case

---

## 11. Run validation

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

89. feat(backend)(rules): add tile conservation validation service
90. feat(backend)(rules): compare confirmed board and draft board tile ids
91. feat(backend)(rules): compare active hand and draft hand tile ids
92. feat(backend)(rules): detect missing tiles during turn
93. feat(backend)(rules): detect duplicated tiles during turn
94. feat(backend)(rules): detect illegally inserted tiles during turn

---

## 14. First move rules

95. feat(backend)(rules): add first move validation service
96. feat(backend)(rules): skip first move validation for players who already completed initial meld
97. feat(backend)(rules): add initial meld minimum points validation
98. feat(backend)(rules): add first move hand-only restriction if needed

---

## 15. Top-level rule orchestration

99. feat(backend)(rules): add submitted draft validation flow
100. feat(backend)(rules): call tile conservation validation from top-level rule service
101. feat(backend)(rules): call board validation from top-level rule service
102. feat(backend)(rules): call first move validation from top-level rule service
103. feat(backend)(rules): fail fast on invalid submitted draft

---

## 16. Submit / end-turn commit flow

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

112. feat(backend)(game): add winner detection logic
113. feat(backend)(game): detect empty-hand win condition
114. feat(backend)(game): add game status transition to finished
115. feat(backend)(game): add score calculation service
116. feat(backend)(game): calculate remaining hand penalty points
117. feat(backend)(game): persist finished game result
118. feat(backend)(game): expose winner information in confirmed game state

---

## 18. WebSocket infrastructure

119. feat(backend)(game)(websocket): add websocket dependency
120. feat(backend)(game)(websocket): add websocket configuration
121. feat(backend)(game)(websocket): add websocket endpoint /ws
122. feat(backend)(game)(websocket): add simple broker configuration
123. feat(backend)(game)(websocket): add game topic naming convention

---

## 19. WebSocket event payloads

124. feat(backend)(game)(websocket): add game draft updated event dto
125. feat(backend)(game)(websocket): add game updated event dto
126. feat(backend)(game)(websocket): add turn changed event dto
127. feat(backend)(game)(websocket): add turn timed out event dto
128. feat(backend)(game)(websocket): add game ended event dto

---

## 20. Game broadcast service

128. feat(backend)(game)(websocket): add game broadcast service
129. feat(backend)(game)(websocket): add draft updated broadcast method
130. feat(backend)(game)(websocket): add game updated broadcast method
131. feat(backend)(game)(websocket): add turn changed broadcast method
132. feat(backend)(game)(websocket): add turn timed out broadcast method
133. feat(backend)(game)(websocket): add game ended broadcast method

---

## 21. WebSocket emissions from service flow

133. feat(backend)(game)(websocket): emit game.draft.updated after draft update
134. feat(backend)(game)(websocket): emit game.updated after valid turn commit
135. feat(backend)(game)(websocket): emit turn.changed after advancing turn
136. feat(backend)(game)(websocket): emit game.draft.updated for newly created next draft
137. feat(backend)(game)(websocket): emit turn.timed_out after automatic timeout handling
138. feat(backend)(game)(websocket): emit game.ended after game finish
139. feat(backend)(game)(websocket): emit game.updated after draw tile if state changes

---

## 22. REST API foundation

139. feat(backend)(game)(rest-api): add game controller
140. feat(backend)(game)(rest-api): add get confirmed game endpoint
141. feat(backend)(game)(rest-api): add update draft endpoint
142. feat(backend)(game)(rest-api): add end turn endpoint
143. feat(backend)(game)(rest-api): add draw tile endpoint
144. feat(backend)(game)(rest-api): add reset draft endpoint

---

## 23. REST request / response mapping

145. feat(backend)(game)(rest-api): add game response mapper
146. feat(backend)(game)(rest-api): add turn draft response mapper
147. feat(backend)(game)(rest-api): map update draft request to domain model
148. feat(backend)(game)(rest-api): map end turn request to service input
149. feat(backend)(game)(rest-api): return confirmed game response from get game endpoint
150. feat(backend)(game)(rest-api): return turn draft response after draft update
151. feat(backend)(game)(rest-api): include turn timer information in game response mapping

---

## 24. Error handling

151. feat(backend)(game): add match api error response dto
152. feat(backend)(game): add global exception handler for match api
153. feat(backend)(game): map invalid input exceptions to bad request
154. feat(backend)(game): map missing game exceptions to not found
155. feat(backend)(game): map invalid state exceptions to conflict
156. feat(backend)(game): add generic internal server error fallback handler

---

## 25. Security and ownership checks

157. feat(backend)(game): validate active player ownership for draft update
158. feat(backend)(game): validate active player ownership for end turn
159. feat(backend)(game): validate active player ownership for draw tile
160. feat(backend)(game): validate draft belongs to current turn player
161. feat(backend)(game): reject draft updates from non-active players

---

## 26. Reconnect and recovery support

162. feat(backend)(game): support loading confirmed game for reconnect
163. feat(backend)(game): support loading current draft for reconnect
164. feat(backend)(game): return current turn player in game response
165. feat(backend)(game): expose draft version for reconnect conflict handling
166. feat(backend)(game): keep latest draft persisted for reconnect recovery
167. feat(backend)(game): return turn timer information for reconnect recovery

---

## 27. Testing fixtures and utilities

167. test(backend)(game): add shared tile test fixtures
168. test(backend)(game): add shared board set test fixtures
169. test(backend)(game): add shared game test fixtures
170. test(backend)(game): add shared draft test fixtures
171. test(backend)(game): add shared request dto test fixtures

---

## 28. Rule unit tests

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

187. test(backend)(game)(rest-api): add game controller tests
188. test(backend)(game)(rest-api): add update draft endpoint tests
189. test(backend)(game)(rest-api): add end turn endpoint tests
190. test(backend)(game)(rest-api): add draw tile endpoint tests
191. test(backend)(game)(rest-api): add reset draft endpoint tests
192. test(backend)(game): add global exception handler tests for match api

---

## 31. WebSocket tests

193. test(backend)(game)(websocket): add broadcast service tests
194. test(backend)(game)(websocket): test game.draft.updated topic and payload
195. test(backend)(game)(websocket): test game.updated topic and payload
196. test(backend)(game)(websocket): test turn.changed topic and payload
197. test(backend)(game)(websocket): test turn.timed_out topic and payload
198. test(backend)(game)(websocket): test game.ended topic and payload
199. test(backend)(game)(websocket): test websocket config loads successfully

---

## 32. Smoke / integration basics

199. test(backend): add backend context smoke test for match module
200. test(backend): add simple match module wiring smoke test

---

## 33. Documentation issues

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

209. chore(backend)(game): add game topic helper constant
210. chore(backend)(game): add draft version conflict handling helper
211. chore(backend)(game): add common game exception classes
212. chore(backend)(rules): add reusable invalid result helper methods
213. chore(backend)(game): add logging for draft update flow
214. chore(backend)(game): add logging for end turn validation failures
215. chore(backend)(game)(websocket): add logging for emitted game events

---

## Suggested implementation order plan for the backend parent issue groupings

A good implementation order for the backend parent issue groups is:

### 1. Shared monorepo model and transport layer

Start with:

- Shared monorepo model and transport layer

Why first:

- all later backend and frontend work depends on one shared source of truth for the game domain model and transport payloads

### 2. Persistence and initialization

Then implement:

- Persistence model
- Tile pool and game initialization

Why here:

- the backend needs to be able to create, save, and load a real match before turn logic can work

### 3. Service foundations

Then implement:

- Match service foundation
- Turn draft logic
- Turn progression logic
- Draw and reset actions

Why here:

- this creates the core backend match flow before adding full validation and realtime behavior

### 4. Rule engine

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

Then implement:

- Submit / end-turn commit flow
- Game end and scoring

Why here:

- this completes the full gameplay loop from editing a draft to committing turns and finishing the match

### 6. Realtime communication

Then implement:

- WebSocket infrastructure
- WebSocket event payloads
- Game broadcast service
- WebSocket emissions from service flow

Why here:

- after the game flow exists, realtime synchronization can be wired in properly

### 7. REST and API mapping

Then implement:

- REST API foundation
- REST request / response mapping
- Error handling
- Security and ownership checks
- Reconnect and recovery support

Why here:

- once the internal services are stable, expose them cleanly through the API

### 8. Testing and documentation

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

24. feat(android)(game): add tile and board set dto-to-model mappers
25. feat(android)(game): add game player, confirmed game, and turn draft dto-to-model mappers
26. feat(android)(game): add model-to-update draft request mapper
27. feat(android)(game): add websocket event dto-to-model mapper

---

## 4. Network / REST foundation

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

40. feat(android)(game)(websocket): add websocket library dependencies
41. feat(android)(game)(websocket): add websocket service base structure
42. feat(android)(game)(websocket): add websocket connect and disconnect logic
43. feat(android)(game)(websocket): add game topic subscription logic
44. feat(android)(game)(websocket): add websocket lifecycle logging and connection state tracking
45. feat(android)(game)(websocket): add websocket event parsing for draft, game, turn change, timeout, and game end events
46. feat(android)(game)(websocket): add unknown websocket event fallback handling

---

## 6. Current player identity handling

194. feat(android)(game): add local current user identity source
195. feat(android)(game): compare current user with turn owner
196. feat(android)(game): derive isActivePlayer state in viewmodel
197. feat(android)(game): derive isSpectatingCurrentTurn state in viewmodel

---

## 7. ViewModel foundation

64. feat(android)(game): add game viewmodel base structure
65. feat(android)(game): add confirmed game load logic to viewmodel
66. feat(android)(game): add websocket connect and disconnect logic to viewmodel
67. feat(android)(game): add confirmed game, live draft, turn changed, and game ended state handling in viewmodel
68. feat(android)(game): add loading, error, and connection state handling in viewmodel
69. feat(android)(game): add turn timer and timeout handling in viewmodel
70. feat(android)(game): add automatic draft reset handling after timeout in viewmodel

---

## 8. Shared live draft behavior

129. feat(android)(game): render shared live draft for all players
130. feat(android)(game): restrict draft editing to active player only
131. feat(android)(game): allow inactive players to observe live draft changes
132. feat(android)(game): update live draft state from websocket game.draft.updated
133. feat(android)(game): replace outdated local draft with incoming shared draft
134. feat(android)(game): clear live draft when confirmed game update arrives
135. feat(android)(game): show active draft owner in ui

---

## 9. Draft update sending

136. feat(android)(game): add debounce/throttle strategy for draft updates
137. feat(android)(game): send draft update after tile rearrangement
138. feat(android)(game): send full draft board in update request
139. feat(android)(game): send full draft hand in update request
140. feat(android)(game): include draft version in update request
141. feat(android)(game): prevent draft update sending for inactive players
142. feat(android)(game): add draft update failure handling

---

## 10. Turn change handling

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

165. feat(android)(game): handle websocket game ended event in viewmodel
166. feat(android)(game): add game result navigation trigger
167. feat(android)(game): add winner display state
168. feat(android)(result): add result screen ui skeleton
169. feat(android)(result): render winner information
170. feat(android)(result): render player scores
171. feat(android)(result): add leave result screen action

---

## 14. Error handling

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

178. feat(android)(game)(websocket): expose websocket connection state and disconnect detection
179. feat(android)(game)(ui): show websocket connection status in game screen
180. feat(android)(game)(websocket): add automatic reconnect, re-subscribe, and duplicate-subscription protection
181. feat(android)(game): reload confirmed game and current draft after reconnect
182. feat(android)(game): resync turn timer and discard stale local timer after reconnect
183. feat(android)(game): clean up websocket connection on game screen exit

---

## 16. Navigation

172. feat(android)(game): add game screen route
173. feat(android)(result): add result screen route
174. feat(android)(game): navigate to game screen with gameId argument
175. feat(android)(game): read gameId argument from nav host
176. feat(android)(game): navigate to result screen on game end
177. feat(android)(game): handle invalid or missing gameId navigation case

---

## 17. Game screen UI foundation

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

198. feat(android)(game)(ui): add reusable turn badge component
199. feat(android)(game)(ui): add reusable connection status indicator
200. feat(android)(game)(ui): add reusable validation error banner
201. feat(android)(game)(ui): add reusable loading overlay
202. feat(android)(game)(ui): add reusable action button row
203. feat(android)(game)(ui): add reusable player summary component
204. feat(android)(game)(ui): add reusable turn timer component

---

## 19. Tile and board set UI components

85. feat(android)(game)(ui): add reusable tile composable with joker variant
86. feat(android)(game)(ui): add tile selection, dragging, and disabled visual states
87. feat(android)(game)(ui): add tile color and number styling
88. feat(android)(game)(ui): add board set composable
89. feat(android)(game)(ui): add run and group set visual layouts
90. feat(android)(game)(ui): add board set spacing, arrangement, and local ordering behavior
91. feat(android)(game)(ui): add tile and board set previews

---

## 20. Board UI and rendering

98. feat(android)(game)(ui): add confirmed board rendering
99. feat(android)(game)(ui): add live draft board rendering
100.    feat(android)(game)(ui): add fallback to confirmed board when no draft exists
101.    feat(android)(game)(ui): add board scroll support
102.    feat(android)(game)(ui): add board set list rendering
103.    feat(android)(game)(ui): add board update animation placeholder
104.    feat(android)(game)(ui): add visual highlight for active draft changes

---

## 21. Hand UI and rendering

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

111. feat(android)(game): add local tile selection logic
112. feat(android)(game): add drag start handling for hand and board tiles
113. feat(android)(game): add drag target tracking
114. feat(android)(game): add tile drop handling onto board and within existing board sets
115. feat(android)(game): add tile removal from board set back to hand
116. feat(android)(game): add local drag state reset and cleanup after drop or cancel
117. feat(android)(game): add invalid drop visual handling

---

## 23. Optimistic / local UX handling

210. feat(android)(game): keep local drag responsiveness before backend confirmation
211. feat(android)(game): reconcile local drag state with incoming shared draft
212. feat(android)(game): reset stale drag state after server draft overwrite
213. feat(android)(game): preserve selection where possible after draft update
214. feat(android)(game): ignore own echoed draft update if identical
215. feat(android)(game): handle out-of-order draft version updates

---

## 24. ViewModel tests

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

254. test(android)(game)(websocket): add websocket connect tests
255. test(android)(game)(websocket): add websocket disconnect tests
256. test(android)(game)(websocket): add topic subscription tests
257. test(android)(game)(websocket): add game.draft.updated parsing tests
258. test(android)(game)(websocket): add game.updated parsing tests
259. test(android)(game)(websocket): add turn.changed parsing tests
260. test(android)(game)(websocket): add game.ended parsing tests
261. test(android)(game)(websocket): add reconnect behavior tests
262. test(android)(game)(websocket): add turn.timed_out parsing tests

---

## 27. Nice optional extras

270. chore(android)(game): add game route constants
271. chore(android)(game): add websocket topic helper
272. chore(android)(game): add logging for draft update submissions
273. chore(android)(game): add logging for websocket event handling
274. chore(android)(game): add base game error messages
275. chore(android)(game): add reusable drag interaction utilities
276. chore(android)(game): add local layout helper utilities for board and hand arrangement

---

## Suggested implementation order plan for the frontend parent issue groupings

A good implementation order for the frontend parent issue groups is:

### 1. Shared models, frontend-only state, and mappers

Start with:

- Shared monorepo model and transport layer
- Core frontend-only model layer
- Mapper layer

Why first:

- the frontend should first depend on the shared source-of-truth game models and DTOs, then add only its own local UI state and mapping layer on top

### 2. Communication foundations

Then implement:

- Network / REST foundation
- WebSocket foundation

Why here:

- the frontend must be able to talk to the backend before state and screen logic can be built properly

### 3. State and orchestration layers

Then implement:

- ViewModel foundation
- Current player identity handling

Why here:

- this creates the main state flow between backend data, websocket updates, and UI state

### 4. Base game screen and shared UI

Then implement:

- Game screen UI foundation
- Shared UI utilities

Why here:

- this gives the team a visible screen structure early and makes it easier to plug in the real data later

### 5. Board, hand, and interaction layer

Then implement:

- Tile and board set UI components
- Board UI and rendering
- Hand UI and rendering
- Drag and drop / interaction basics

Why here:

- these are the main gameplay interaction pieces needed for editing the live draft

### 6. Shared draft and turn flow

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

Then implement:

- Navigation
- Connection and reconnect handling
- Error handling
- Optimistic / local UX handling

Why here:

- this makes the feature robust enough for real use and helps the app recover from failures and reconnects

### 8. Testing, docs, and optional cleanup

Finish with:

- ViewModel tests
- UI tests
- WebSocket service tests
- Nice optional extras

Why last:

- these are best completed once the main feature flow already exists and is stable enough to verify properly

### 9. Frontend polish / missing issue groupings

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

### State consistency and concurrency

1. feat(backend)(game): add draft version conflict handling
2. feat(backend)(game): reject stale draft updates by version
3. feat(backend)(game): prevent end turn while stale draft version is submitted
4. feat(backend)(game): handle concurrent draft update and end turn requests
5. feat(backend)(game): add last-write / version consistency strategy for draft state

---

### Recovery and lifecycle

6. feat(backend)(game): add reconnect-safe game state recovery flow
7. feat(backend)(game): add reconnect-safe draft state recovery flow
8. feat(backend)(game): add stale draft cleanup logic
9. feat(backend)(game): add finished game persistence cleanup strategy
10. feat(backend)(game): add match recovery behavior after backend restart

---

### More rule edge cases

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

19. feat(backend)(game): add pass turn logic if needed
20. feat(backend)(game): add draw-and-end-turn flow handling
21. feat(backend)(game): add no-move available handling
22. feat(backend)(game): add end-of-game score calculation details
23. feat(backend)(game): add tie-breaking logic if needed

---

### Security / authorization / identity

24. feat(backend)(game): validate player belongs to game before processing commands
25. feat(backend)(game): validate websocket events are only produced for valid match participants
26. feat(backend)(game): centralize acting-player authorization checks
27. feat(backend)(game): reject commands for finished matches
28. feat(backend)(game): reject commands for non-active players consistently

---

### WebSocket robustness

29. feat(backend)(game)(websocket): add event sequencing metadata
30. feat(backend)(game)(websocket): add server timestamp to websocket events
31. feat(backend)(game)(websocket): add event version to game draft updates
32. feat(backend)(game)(websocket): add event version to confirmed game updates
33. feat(backend)(game)(websocket): add websocket reconnect resync support contract

---

### Logging and observability

34. chore(backend)(game): add structured logging for draft updates
35. chore(backend)(game): add structured logging for invalid move submissions
36. chore(backend)(game): add structured logging for turn transitions
37. chore(backend)(game)(websocket): add structured logging for emitted game events

---

### Persistence improvements

38. chore(backend)(game): add migration strategy for game json state schema
39. chore(backend)(game): add migration strategy for draft json state schema
40. chore(backend)(game): add index strategy for game and draft lookup
41. chore(backend)(game): add archival strategy for finished games

---

### More backend testing

42. test(backend)(game): add concurrent draft update tests
43. test(backend)(game): add draft version conflict tests
44. test(backend)(rules): add joker edge case tests
45. test(backend)(game): add reconnect recovery tests
46. test(backend)(game): add invalid no-op turn tests
47. test(backend)(game)(integration): add end-turn integration test across persistence and websocket
48. test(backend)(game)(integration): add reconnect integration test for confirmed state and draft state

---

## Frontend

### State synchronization and consistency

49. feat(android)(game): ignore stale websocket draft updates by version
50. feat(android)(game): ignore stale websocket confirmed game updates by version
51. feat(android)(game): handle out-of-order websocket events
52. feat(android)(game): deduplicate repeated websocket events
53. feat(android)(game): reconcile local draft with server draft after version conflict

---

### Recovery / reconnect / app lifecycle

54. feat(android)(game): restore game screen state after app process recreation
55. feat(android)(game): reload confirmed game after app returns from background
56. feat(android)(game): reload draft after app returns from background
57. feat(android)(game): handle reconnect while active player is editing
58. feat(android)(game): handle reconnect while spectating another player's draft
59. feat(android)(game): add full resync after websocket reconnect

---

### UX and interaction polish

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

76. chore(android)(game): optimize board recomposition during websocket draft updates
77. chore(android)(game): optimize hand recomposition during websocket draft updates
78. chore(android)(game): reduce unnecessary UI updates for unchanged draft events
79. chore(android)(game): review payload size impact of full-draft updates
80. chore(android)(game): add throttling strategy for frequent draft updates

---

### Identity and permissions

81. feat(android)(game): derive active-player permissions consistently in viewmodel
82. feat(android)(game): block drag/edit actions for non-active players
83. feat(android)(game): block end turn action for non-active players consistently
84. feat(android)(game): block draw/reset actions for non-active players consistently

---

### Error handling and resilience

85. feat(android)(game): show version conflict error message
86. feat(android)(game): recover gracefully after rejected draft update
87. feat(android)(game): recover gracefully after rejected end turn
88. feat(android)(game): add fallback resync after repeated websocket failures
89. feat(android)(game): add fallback resync after repeated command failures

---

### Accessibility and usability

90. feat(android)(game)(ui): improve tile accessibility labels
91. feat(android)(game)(ui): improve board accessibility labels
92. feat(android)(game)(ui): add content descriptions for action buttons
93. feat(android)(game)(ui): improve readability of tile colors and numbers

---

### More frontend testing

94. test(android)(game): add stale draft event handling tests
95. test(android)(game): add out-of-order websocket event tests
96. test(android)(game): add reconnect resync tests
97. test(android)(game): add process recreation state restore tests
98. test(android)(game)(ui): add invalid set indicator UI tests
99. test(android)(game)(ui): add active vs inactive player UI permission tests
100.    test(android)(game): add local/server draft reconciliation tests

---

### Cross-cutting / shared polish issues

101. docs(game): document draft versioning and conflict handling
102. docs(game): document reconnect and resync flow
103. docs(game): document confirmed state vs live draft state clearly
104. docs(game): document active-player permissions and command rules
105. docs(game): document end-turn validation and rejection flow

---

## Best “missing issues” to prioritize first

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

A good implementation order for the polish issue groups is:

### 1. State consistency and reconnect safety

Start with:

- backend state consistency and concurrency issues
- frontend state synchronization and consistency issues
- backend recovery and lifecycle issues
- frontend recovery / reconnect / app lifecycle issues

Why first:

- these issues protect the core architecture from stale state, reconnect bugs, and conflicting updates

### 2. Validation and gameplay edge cases

Then implement:

- backend rule edge cases
- backend match progression and game-rule edge cases
- frontend drag/drop edge cases

Why here:

- after the base feature works, these issues improve correctness for unusual but important gameplay cases

### 3. Authorization, robustness, and resilience

Then implement:

- backend security / authorization / identity issues
- backend websocket robustness issues
- frontend identity and permission issues
- frontend error handling and resilience issues

Why here:

- these make the feature safer and more robust in real multiplayer scenarios

### 4. UX and performance polish

Then implement:

- frontend UX and interaction polish issues
- frontend performance and rendering issues
- frontend accessibility and usability issues
- backend logging and observability issues

Why here:

- once correctness is solid, polish the gameplay experience and developer visibility

### 5. Persistence and long-term maintenance

Then implement:

- backend persistence improvements
- cross-cutting / shared polish issues

Why here:

- these issues matter more once the architecture has stabilized and the team knows which long-term concerns are worth keeping

### 6. Advanced testing

Finish with:

- backend extra testing issues
- frontend extra testing issues

Why last:

- these tests are most useful after the main feature and polish behavior are already implemented and stable enough to verify thoroughly
