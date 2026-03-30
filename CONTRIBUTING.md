# Contributing Guide

Thank you for contributing to this project.

This repository contains a monorepo for an Android card game app and a Kotlin Spring Boot backend. The goal of this guide is to keep development consistent, readable, and easy to review for everyone in the team.

---

## Project structure

This repository is organized as a monorepo.

Main parts of the project:

- `app/android/` – Android application
- `backend/` – Spring Boot backend
- root configuration – shared Gradle, GitHub, and repository setup

Please keep changes scoped to the correct module whenever possible.

---

## General workflow

All changes must go through a pull request.

### Branch workflow

- Create a new branch from `main`
- Make focused changes for one issue or task
- Open a pull request into `main`
- Wait for review and passing checks before merging

Do not push directly to `main`.

---

## Branch naming

Use clear and predictable branch names.

Recommended format:

- `feature/<issue-number>-short-description`
- `fix/<issue-number>-short-description`
- `chore/<issue-number>-short-description`
- `docs/<issue-number>-short-description`
- `build/<issue-number>-short-description`
- `ci/<issue-number>-short-description`

Examples:

- `feature/12-login-screen`
- `fix/18-card-draw-bug`
- `chore/4-editorconfig-setup`

Use lowercase letters and hyphens.

---

## Commit message conventions

Write short, clear commit messages.

Recommended format:

- `feat: add user registration endpoint`
- `fix: handle empty deck state correctly`
- `docs: update setup instructions`
- `chore: add editorconfig`
- `build: configure root gradle settings`
- `ci: add GitHub Actions workflow`

### Common prefixes

- `feat` – new feature
- `fix` – bug fix
- `docs` – documentation only
- `chore` – maintenance or cleanup
- `build` – build system or dependency changes
- `ci` – CI/CD related changes
- `refactor` – code restructuring without behavior changes
- `test` – adding or updating tests

If relevant, reference the issue in the commit message or PR.

Example:

- `feat(android): add initial home screen, closes #15`

---

## Pull requests

Each pull request should be focused and easy to review.

### Before opening a pull request

Make sure that:

- your branch is up to date with the latest `main`
- your changes are limited to one topic or task
- the project still builds
- tests pass where applicable
- documentation is updated if needed

### Pull request title

Use a clear title with a suitable prefix.

Examples:

- `feat(android): add game lobby screen`
- `feat(backend): add match creation endpoint`
- `docs: improve local setup instructions`
- `chore: add repository contributing guide`

### Pull request description

A pull request description should usually include:

- what was changed
- why it was changed
- any important implementation notes
- linked issue(s)

Example issue references:

- `Closes #12`
- `Related to #18`

### Review expectations

- At least one approval is required before merging
- Resolve all review comments before merging
- If you push more commits after approval, approval may need to be renewed

---

## Code style

We use shared repository formatting rules.

### Formatting

- Follow the root `.editorconfig`
- Use the formatter built into IntelliJ IDEA / Android Studio
- Do not manually reformat unrelated files in the same PR

### Kotlin style

- Prefer clear and simple code over clever code
- Use meaningful names
- Keep functions focused
- Avoid large classes with too many responsibilities
- Follow standard Kotlin naming conventions:
    - `PascalCase` for classes
    - `camelCase` for functions and variables
    - `UPPER_SNAKE_CASE` for constants

### Comments

- Write comments only when they add useful context
- Prefer self-explanatory code over excessive comments
- Keep TODOs specific and actionable

---

## Testing

Testing expectations depend on the type of change.

### Backend

For backend changes:

- add or update tests where reasonable
- make sure the backend builds successfully
- verify core logic changes with tests if possible

### Android

For Android changes:

- ensure the app builds successfully
- test UI and behavior manually if automated tests are not yet available
- add tests where the project already supports them

Do not merge changes that knowingly break the build.

---

## Documentation

Update documentation when your changes affect:

- project setup
- developer workflow
- commands
- architecture
- API behavior
- important assumptions or conventions

At minimum, update the `README.md` when setup or usage changes.

---

## Issue tracking

Before starting work:

- make sure the task has a GitHub issue
- assign yourself if your team uses assignment
- keep the PR linked to the relevant issue

Try to keep one pull request tied to one main task.

---

## Scope and cleanliness

Please keep pull requests small and focused.

Avoid mixing unrelated changes such as:

- formatting unrelated files
- refactoring unrelated code
- renaming files without need
- adding features outside the issue scope

Smaller PRs are easier to review and safer to merge.

---

## Dependencies and configuration

When adding a new dependency:

- make sure it is necessary
- prefer stable and well-supported libraries
- keep versions consistent with project conventions
- mention the reason in the pull request description

Do not commit secrets, API keys, credentials, or private environment values.

---

## Need help?

If something is unclear:

- ask in the team chat
- leave a note in the issue
- mention the question in the pull request description

It is better to clarify early than to rework large changes later.

---

## Summary

When contributing, please:

- branch from `main`
- use clear branch names
- follow commit conventions
- keep PRs focused
- link issues
- follow formatting rules
- make sure builds and tests pass
- update documentation when needed

Thanks for contributing.
