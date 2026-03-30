# Untitled

# SE2 Group Project – UNO Card Game

> Monorepo for our SE2 group project: an UNO-inspired multiplayer card game with an **Android frontend** and a **Spring Boot backend**, both written in **Kotlin**.

## Project Status

![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot-6DB33F)
![Language](https://img.shields.io/badge/language-Kotlin-7F52FF)
![Build](https://img.shields.io/badge/build-Gradle-02303A)
![Project Type](https://img.shields.io/badge/project-SE2-blue)

## Overview

The goal of this project is to build a digital card game inspired by UNO.

The application consists of:

- an **Android app** for players
- a **Spring Boot backend** for lobbies, game state, and scoring
- a shared repository structure for team collaboration and future scaling

This repository is organized as a **monorepo** so frontend, backend, and shared setup can evolve together in one place.

---

## Tech Stack

### Frontend

- Kotlin
- Android
- Jetpack Compose

### Backend

- Kotlin
- Spring Boot

### Tooling

- Gradle Kotlin DSL
- GitHub
- GitHub Actions
- Notion for project management

---

## Repository Structure

```
.
├── apps/
│   ├── android/              # Android application
│   └── backend/              # Spring Boot backend
├── docs/                     # Architecture, setup, API, contribution docs
├── infra/                    # Infrastructure / deployment related files
├── .github/
│   ├── workflows/            # CI workflows
│   └── ISSUE_TEMPLATE/       # GitHub issue templates
├── gradle/
│   └── wrapper/              # Gradle wrapper files
├── build.gradle.kts          # Root Gradle configuration
├── settings.gradle.kts       # Included modules
├── gradle.properties         # Shared Gradle defaults
├── gradlew
├── gradlew.bat
└── README.md
```

## Project Management

We use:

- **GitHub** for source code, issues, and pull requests
- **Notion** for planning, user stories, sprint tracking, and team organization

### Project Links

- **GitHub Repository:** `<add-repo-link-here>`
- **GitHub Organization:** `<add-org-link-here>`
- **Notion Workspace / Hub:** `<add-notion-link-here>`

---

## Requirements

Before starting, make sure you have:

- **JDK 17**
- **Android Studio**
- **Git**
- optionally **IntelliJ IDEA** for backend or general Kotlin work

---

## Getting Started

### 1. Clone the repository

```
git clone <your-repository-url>
cd codebase
```

### 2. Verify Gradle works

On macOS / Linux:

```
./gradlew tasks
```

On Windows:

```
gradlew.bat tasks
```

---

## Running the Backend

From the repository root:

```
./gradlew :apps:backend:bootRun
```

Expected local endpoint:

```
GET /api/health
```

---

## Running the Android App

1. Open the repository in **Android Studio**
2. Wait for **Gradle sync**
3. Select the Android app configuration
4. Start an emulator or connect a physical device
5. Run the app

---

## Workflow

### Branch Naming

Use clear and short branch names:

- `feature/homescreen`
- `feature/create-lobby`
- `feature/play-card`
- `fix/join-lobby-validation`
- `chore/add-ci`
- `docs/update-readme`

### Commit Messages

Use **Conventional Commits** where possible.

Examples:

```
feat(android): add homescreen UI
feat(backend): add health endpoint
feat(game): implement deal cards use case
fix(android): validate username input
chore(repo): add root gradle configuration
docs(readme): add setup instructions
ci(github): add build workflow
```

### Pull Requests

Before opening a pull request:

- make sure the project builds locally
- keep the PR focused and reasonably small
- link the related issue
- update documentation if needed
- add screenshots for UI changes if applicable

---

## Issue Management

We split work into **small technical issues** that can be implemented independently.

Typical categories:

- **repo/setup**
- **android**
- **backend**
- **game logic**
- **docs**
- **ci**
- **testing**

Each issue should ideally include:

- a clear title
- a short description
- acceptance criteria
- labels
- assignee / reviewer if known

---

## Definition of Done

A task is considered done when:

- acceptance criteria are fulfilled
- code builds locally
- relevant tests are added or updated
- code follows project conventions
- no obvious debug or placeholder code remains
- documentation is updated if required
- the PR has been reviewed

---

## Planned Features

Current project scope includes features such as:

- homescreen
- username input
- leaderboard
- create lobby
- browse lobbies
- join lobby by ID
- lobby game options
- start game
- initial game state
- first dealt card
- deal cards
- play a card
- take a card
- timer handling
- win game
- game results and score updates

---

## Suggested Sprint 0 / Setup Scope

Before feature implementation, the repository should provide:

- monorepo folder structure
- root Gradle configuration
- Gradle wrapper
- `.gitignore`
- `.editorconfig`
- Android bootstrap project
- backend bootstrap project
- README and docs skeleton
- issue / PR templates
- CI workflow

---

## Documentation

The `docs/` directory should contain technical project documentation such as:

- `architecture.md`
- `setup.md`
- `api.md`
- `contributing.md`

---

## Team Conventions

- Prefer **small issues** over large vague tasks
- Keep pull requests focused
- Discuss bigger architecture changes before implementation
- Do not commit secrets or local environment files
- Use consistent Kotlin formatting and naming conventions
- Keep frontend and backend contracts explicit and documented

---

## Contributing

1. Pick or create an issue
2. Create a branch
3. Implement the change
4. Test locally
5. Open a pull request
6. Request review
7. Merge after approval

---
