# SE2 Group Project – Rummikub Game

> Monorepo for our SE2 group project: a Rummikub-inspired multiplayer game with an **Android frontend** and a **Spring Boot backend**, both written in **Kotlin**.

## Project Status

![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot-6DB33F)
![Language](https://img.shields.io/badge/language-Kotlin-7F52FF)
![Build](https://img.shields.io/badge/build-Gradle-02303A)
![Project Type](https://img.shields.io/badge/project-SE2-blue)

## Overview

The goal of this project is to design and implement a digital game inspired by Rummikub as part of the SE2 group project. The application is being developed as a shared monorepo so the Android frontend, Spring Boot backend, and all project documentation can evolve together in one repository with consistent structure, tooling, and workflow.

The system is currently planned around two main technical parts:

- an **Android application** that provides the user interface and the player experience
- a **Spring Boot backend** that manages lobbies, game state, validation, and future score-related functionality

In addition to the implementation itself, the repository also contains project documentation, development conventions, and collaboration resources intended to support smooth teamwork throughout the course of the project.

---

## Deployment

Current backend deployment:

- REST API base URL: `https://se2-group-codebase.onrender.com/`
- WebSocket / STOMP endpoint: `wss://se2-group-codebase.onrender.com/ws`
- Health check: `https://se2-group-codebase.onrender.com/actuator/health`

---

## Project Goals

The main objective is to deliver one polished multiplayer card game experience instead of several loosely connected mini-projects. The focus is on building a clear, maintainable, and well-documented application that can be developed collaboratively by the whole team.

The project aims to:

- build a stable Android client in Kotlin
- build a structured backend in Kotlin with Spring Boot
- define clear contracts between frontend and backend
- maintain a clean monorepo structure for the whole team
- document setup, architecture, workflow, and technical decisions inside the repository
- keep project organization transparent through GitHub and Notion

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
- IntelliJ IDEA / Android Studio

---

## Repository Structure

```text
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
├── gradlew                   # Gradle wrapper for macOS / Linux
├── gradlew.bat               # Gradle wrapper for Windows
└── README.md
```

### Repository Notes

- `apps/android` contains the Android client
- `apps/backend` contains the Spring Boot backend
- `docs` contains the main project documentation
- `.github` contains collaboration and workflow templates
- the root Gradle files define shared build configuration for the whole monorepo

---

## Project Management

The team currently uses:

- **GitHub** for source code, issues, pull requests, and code review
- **Notion** for planning, user stories, meeting notes, and project organization

### Project Links

- **GitHub Repository:** `<https://github.com/SE2-Gruppenprojekt/se2-group-codebase>`
- **GitHub Organization:** `<https://github.com/SE2-Gruppenprojekt>`
- **Notion Workspace / Hub:** `<https://kingjulien1.notion.site/SE2-Project-Team-Gamma-Hub-33086e6823a4809eb050f4a8335b695d?source=copy_link>`

---

## Team

The following team members are currently part of the project.

### Erik `@erzeber`

- **Role:** Team Leader
- **Workspace:** Organisational / Frontend / Backend / Reviewer

### Julian Blaschke `@Julian`

- **Role:** Architect
- **Workspace:** Organisational / Backend / Reviewer

### Stefan `@Stefan`

- **Role:** Developer
- **Workspace:** Backend

### Katrin Herold `@Katrin Herold`

- **Role:** Developer
- **Workspace:** Backend

### Vanessa `@Vanessa`

- **Role:** Developer
- **Workspace:** Frontend

### Miriam `@Miri`

- **Role:** Developer
- **Workspace:** Backend

### Sabine

- **Role:** Developer
- **Workspace:** Frontend

---

## Requirements

Before starting, make sure the following tools are installed:

- **JDK 17**
- **Android Studio**
- **Git**
- optionally **IntelliJ IDEA** for backend work or general Kotlin development

It is also recommended that all contributors use the committed Gradle Wrapper instead of a globally installed Gradle version.

---

## Getting Started

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd codebase
```

### 2. Verify Gradle works

On macOS / Linux:

```bash
./gradlew tasks
```

On Windows:

```bat
gradlew.bat tasks
```

If Gradle does not run successfully, verify that the Gradle Wrapper files are present and that the project is using **JDK 17**.

---

## Running the Backend

From the repository root, start the backend with:

```bash
./gradlew :apps:backend:bootRun
```

The backend should start using the local configuration defined in the backend module.

### Notes

- run the backend from the repository root
- use the Gradle Wrapper instead of a system Gradle installation
- update backend-specific documentation when endpoints or configuration change

---

## Running the Android App

To run the Android application:

1. Open the repository in **Android Studio**
2. Wait for **Gradle sync** to complete
3. Select the Android app run configuration
4. Start an emulator or connect a physical device
5. Run the application

### Notes

- make sure the Android SDK is installed correctly
- let Gradle sync fully before running the app
- use the shared repository configuration instead of creating local project variants

### Troubleshooting Gradle Sync or Build Errors

If the Android frontend does not compile, Gradle sync fails, or the build shows an error status, try the following steps.

#### Common Symptoms

- Gradle sync never finishes
- Build fails with an error status
- Android Studio shows missing Gradle tasks
- Cache-related errors such as missing files in `~/.gradle/caches`

#### Fix Steps

##### 1. Close Android Studio completely

Quit Android Studio fully before cleaning any files.

##### 2. Delete the broken Gradle cache

Run these commands in your terminal:

```bash
rm -rf ~/.gradle/caches
rm -rf ~/.gradle/daemon
rm -rf ~/.gradle/native
rm -rf ~/.gradle/wrapper
```

This removes the local Gradle cache so dependencies and wrapper files can be downloaded again cleanly.

##### 3. Delete the project-local Gradle files

From the project root, run:

```bash
rm -rf .gradle
rm -rf build
```

If you also have an app build directory, run:

```bash
rm -rf app/build
```

##### 4. Reopen Android Studio

After reopening the project:

- use **Sync Project with Gradle Files**
- wait for Gradle to redownload dependencies and regenerate project state

##### 5. Force a terminal rebuild if it still fails

From the project root, run:

```bash
./gradlew --stop
./gradlew clean
./gradlew build
```

If the build still fails, run:

```bash
./gradlew tasks --stacktrace
```

This usually gives a more detailed error message than the Android Studio popup.

##### 3. Update Android Studio if the Android Gradle Plugin version is unsupported

If Gradle sync reports that the project is using an unsupported or incompatible Android Gradle Plugin version, your installed Android Studio version may be too old.

In that case:

- update Android Studio to the latest stable version
- reopen the project
- sync Gradle again

This resolved a project issue where the Android Gradle Plugin version was newer than the maximum version supported by the installed Android Studio version.

#### Notes

- Corrupted local Gradle caches are a common cause of sync failures
- If errors continue after clearing caches, check `settings.gradle.kts`, module `build.gradle.kts` files, the Gradle wrapper version, and the JDK version configured in Android Studio
- Make sure you opened the repository root and not only a subfolder

---

## Workflow

### Branch Naming

Use clear and concise branch names.

Examples:

- `feature/homescreen`
- `feature/create-lobby`
- `feature/play-card`
- `fix/join-lobby-validation`
- `chore/add-ci`
- `docs/update-readme`

### Commit Messages

Use **Conventional Commits** for all commit messages where possible.

### Commit Message Format

```text
<type>(<scope>): <short summary>
```

### Format Rules

- use a lowercase commit `type`
- use a short and specific `scope` when applicable
- write the summary in the imperative mood
- keep the summary concise and descriptive
- do not end the summary with a period
- prefer one logical change per commit

### Common Types

- `feat` for a new feature
- `fix` for a bug fix
- `docs` for documentation changes
- `chore` for maintenance, setup, or repository tasks
- `refactor` for code changes that do not add features or fix bugs
- `test` for adding or updating tests
- `ci` for CI or workflow-related changes
- `build` for build system or dependency changes

### Good Examples

```text
feat(android): add homescreen UI
feat(backend): add mock lobby endpoint
feat(game): implement deal cards use case
fix(android): validate username input
chore(repo): add root gradle configuration
docs(readme): add setup instructions
ci(github): add build workflow
build(gradle): add gradle wrapper
```

### References

- [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- [How to Write a Git Commit Message](https://cbea.ms/git-commit/)

### Pull Requests

Before opening a pull request:

- make sure the project builds locally
- keep the PR focused and reasonably small
- link the related issue
- update documentation if needed
- add screenshots for UI changes if applicable

### Pull Request Title Format

Use a title structure similar to the commit message format.

```text
<type>(<scope>): <short summary>
```

### Pull Request Title Rules

- use a lowercase `type`
- use a meaningful `scope` when applicable
- keep the summary short, clear, and review-friendly
- describe the overall purpose of the PR, not every internal detail
- do not end the title with a period
- make sure the title aligns with the linked issue and branch purpose

### Good Examples

```text
feat(android): add lobby screen skeleton
feat(backend): add mock lobby endpoint
docs(project): initialize project documentation
chore(repo): add github templates and ci workflow
build(gradle): configure root monorepo build
fix(android): correct username validation state
```

### Pull Request Description

The pull request description should usually include:

- a short summary of the change
- the motivation or goal of the PR
- the main files or areas affected
- references to related issues
- testing or verification notes
- screenshots for UI changes when relevant

## Definition of Done

A task is considered done when:

- acceptance criteria are fulfilled
- code builds locally
- relevant tests are added or updated where appropriate
- code follows project conventions
- no obvious debug or placeholder code remains unless explicitly intended
- documentation is updated if required
- the pull request has been reviewed

---

## Contributing

Basic contribution flow:

1. Pick or create an issue
2. Create a branch
3. Implement the change
4. Test locally
5. Open a pull request
6. Request review
7. Merge after approval

For more detailed contribution guidance, see the linked project documentation below.

---

## Project Documentation

The `docs/` directory contains the main project documentation. These files should be kept up to date as the project evolves.

### Core Guides

- [Setup Guide](docs/setup.md)
- [Architecture Overview](docs/architecture.md)
- [API Overview](docs/api.md)
- [Testing Guide](docs/testing.md)

### Supporting Documentation

- [Database Notes](docs/database.md)
- [Design System Notes](docs/design-system.md)
- [Meeting Notes Guide](docs/meetings.md)
- [Meeting Notes Template](docs/meeting_notes_template.md)
- [Project Roadmap](docs/roadmap.md)

### Meeting Files

- [Launch Session](docs/meetings/meeting_01_launch_session.md)
- [Pre-Kickoff Meeting](docs/meetings/meeting_02_pre_kickoff.md)
- [Kickoff Meeting](docs/meetings/meeting_03_kickoff.md)

---

## Resources

### Internal Resources

- [GitHub Repository](https://github.com/SE2-Gruppenprojekt/se2-group-codebase)
- [GitHub Organization](https://github.com/SE2-Gruppenprojekt)
- [Notion Workspace / Hub](https://kingjulien1.notion.site/SE2-Project-Team-Gamma-Hub-33086e6823a4809eb050f4a8335b695d?source=copy_link)

### External References

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Developers Documentation](https://developer.android.com/docs)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/documentation.html)
- [Gradle Documentation](https://docs.gradle.org/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [EditorConfig Documentation](https://editorconfig.org/)
- [GitHub Actions Documentation](https://docs.github.com/actions)

---

## Additional Notes

- keep the repository clean and well-structured
- prefer small, focused pull requests over large mixed changes
- discuss larger architectural changes before implementation
- do not commit secrets, local environment files, or generated build output
- keep frontend/backend contracts explicit and documented
- use the repository documentation as the main technical reference whenever possible
