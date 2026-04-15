# Frontend Smoke Test Checklist (Android)

## Goal
Ensure that UI, ViewModels and WebSocket updates work correctly in the Android client.

---

## 1. App Start

- [x] App starts without crash
- [x] Navigation works (Home → Lobby → Waiting Room)

---

## 2. Lobby Overview (BrowseLobbiesScreen)

- [x] Lobbies are loaded
- [x] Loading state is displayed (if active)
- [x] Empty state appears correctly
- [x] Join button works

---

## 3. Create Lobby (CreateLobbyScreen / NewLobbyScreen)

- [x] Lobby can be created
- [x] Loading button works (isLoading state)
- [x] Errors are displayed (if API fails)
- [x] After create → navigation works

---

## 4. Waiting Room UI

- [x] Lobby data is displayed
- [x] Room code is displayed correctly
- [x] Player list is rendered
- [ ] Placeholder slots work

---

## 5. WebSocket Integration

- [x] connectWebSocket is triggered when opening the lobby
- [x] No crash on connection
- [x] No "Unknown event" logs

---

## 6. LIVE UPDATES (Core Feature)

### Player Join
- [x] New player appears immediately in Waiting Room
- [x] No manual refresh needed

### Player Leave
- [x] Player disappears immediately from list

### Lobby Update
- [x] Changes (maxPlayers, state) are updated

---

## 7. Lobby Started Event

- [ ] matchId is received
- [ ] UI reacts (Navigation / State Update)

---

## 8. Settings Screen

- [x] Dark mode toggle works
- [x] State is preserved
- [x] UI reacts immediately

---

## 9. Error Handling

- [x] API errors show error state
- [x] WebSocket disconnect does not break UI
- [x] Loading states disappear correctly

---

## 10. Performance Basics

- [x] No UI freeze on join/leave
- [x] LazyColumn scrolls smoothly
- [x] No multiple WebSocket connects

---

## Definition of Done

✔ UI works consistently
✔ WebSocket updates visible live
✔ No crashes or stuck loading states
