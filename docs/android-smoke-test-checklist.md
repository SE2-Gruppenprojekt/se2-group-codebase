# Frontend Smoke Test Checklist (Android)

## Ziel
Sicherstellen, dass UI, ViewModels und WebSocket-Updates korrekt im Android Client funktionieren.

---

## 1. App Start

- [x] App startet ohne Crash
- [x] Navigation funktioniert (Home → Lobby → Waiting Room)

---

## 2. Lobby Übersicht (BrowseLobbiesScreen)

- [x] Lobbys werden geladen
- [x] Loading State wird angezeigt (falls aktiv)
- [x] Empty State erscheint korrekt
- [x] Join Button funktioniert

---

## 3. Lobby erstellen (CreateLobbyScreen / NewLobbyScreen)

- [x] Lobby kann erstellt werden
- [x] Loading Button funktioniert (isLoading State)
- [x] Fehler werden angezeigt (falls API fail)
- [x] Nach Create → Navigation funktioniert

---

## 4. Waiting Room UI

- [x] Lobby Daten werden angezeigt
- [x] Room Code wird korrekt angezeigt
- [x] Player Liste wird gerendert
- [ ] Placeholder Slots funktionieren

---

## 5. WebSocket Integration

- [x] connectWebSocket wird beim Öffnen der Lobby ausgelöst
- [x] Kein Crash bei Verbindung
- [x] Keine "Unknown event" Logs

---

## 6. LIVE UPDATES (Core Feature)

### Player Join
- [x] Neuer Spieler erscheint sofort im Waiting Room
- [x] Kein manueller Refresh nötig

### Player Leave
- [x] Spieler verschwindet sofort aus Liste

### Lobby Update
- [x] Änderungen (maxPlayers, state) werden aktualisiert

---

## 7. Lobby Started Event

- [ ] matchId wird empfangen
- [ ] UI reagiert (Navigation / State Update)

---

## 8. Settings Screen

- [x] Dark Mode Toggle funktioniert
- [x] State bleibt erhalten
- [x] UI reagiert sofort

---

## 9. Error Handling

- [x] API Fehler zeigen Error State
- [x] WebSocket disconnect bricht UI nicht
- [x] Loading States verschwinden korrekt

---

## 10. Performance Basics

- [x] Kein UI Freeze beim Join/Leave
- [x] LazyColumn scrollt flüssig
- [x] Keine mehrfachen WebSocket connects

---

## Definition of Done

✔ UI funktioniert durchgehend
✔ WebSocket Updates live sichtbar
✔ Keine Crashes oder stuck loading states
