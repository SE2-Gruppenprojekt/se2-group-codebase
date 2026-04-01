# Architecture

## Overview

This project is a multiplayer card game inspired by UNO. It is developed as a monorepo containing:

- an Android frontend written in Kotlin
- a Spring Boot backend written in Kotlin
- shared documentation and project configuration

## High-Level Structure

```text
apps/
  android/   # Android application
  backend/   # Spring Boot backend
docs/        # Project documentation
infra/       # Infrastructure and deployment related files
```

Frontend Responsibilities

The Android app is responsible for:
• rendering the user interface
• handling user input
• managing screen navigation
• displaying lobby and game state
• communicating with the backend API

Backend Responsibilities

The backend is responsible for:
• lobby management
• game state management
• player actions and validation
• score handling
• exposing API endpoints for the frontend

Planned Architecture Principles
• clear separation between frontend and backend responsibilities
• feature-oriented frontend structure where practical
• layered backend structure with controller, service, and DTO/domain separation
• shared team conventions for naming, formatting, and Git workflow

Current Scope

At the current stage, the project is in bootstrap/setup phase.
The architecture will evolve as the first gameplay and lobby features are implemented.

Future Considerations

Possible future additions:
• shared Kotlin module for common DTOs or models
• persistent storage for scores, users, or match history
• real-time communication for live game updates
• deployment and hosting configuration
