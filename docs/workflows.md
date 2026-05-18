# CI and Sonar Workflows

[![Backend CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/backend.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/backend.yml)
[![Frontend CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/android.yml)
[![Shared CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/shared.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/shared.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SE2-Gruppenprojekt_se2-group-codebase&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SE2-Gruppenprojekt_se2-group-codebase)

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=se2-gruppenprojekt_se2-group-codebase_backend)

This document explains the three GitHub Actions workflows used in the
repository:

- backend
- android frontend
- shared

It first documents what they have in common, then describes each workflow on
its own with its current implementation details.

---

## 1. Shared Structure

All three workflows follow the same high-level pattern:

1. trigger on the same GitHub events
2. detect whether the relevant part of the monorepo changed
3. skip expensive work when that part is unaffected
4. run tests and Sonar analysis when it is affected
5. end with a small required-status job that always finishes deterministically

This gives the repository a consistent CI model without forcing every workflow
to run on every pull request.

---

## 2. Shared Behavior

### 2.1 Trigger events

All three workflows run on:

- `pull_request`
- `merge_group`
- `push` to `main`

That means:

- contributors get CI on normal PRs
- merge queues are covered through `merge_group`
- direct updates on `main` are also validated

### 2.2 Concurrency model

Each workflow defines its own concurrency group and uses:

```yaml
cancel-in-progress: true
```

Effect:

- if a newer run is started for the same PR or ref, the older one is cancelled
- this prevents outdated Sonar and test runs from continuing unnecessarily

Each workflow has its own prefix:

- backend: `backend-sonar-...`
- frontend: `android-sonar-...`
- shared: `shared-sonar-...`

So they cancel only their own earlier runs, not each other.

### 2.3 Change detection

Each workflow starts with a `changes` job that uses:

- `dorny/paths-filter@v3`

This job decides whether the expensive build-and-analysis job should run.

Shared benefits:

- avoids unnecessary CI work
- reduces Sonar noise on unrelated changes
- keeps PR feedback faster

### 2.4 Git checkout strategy

The main build jobs use:

```yaml
uses: actions/checkout@v4
with:
    fetch-depth: 0
```

This matters especially for Sonar:

- full history is safer for PR analysis
- blame and changed-code analysis are more reliable

The lightweight `changes` jobs use a normal checkout without extra history.

### 2.5 Java setup

All build jobs use:

- `actions/setup-java@v4`
- Temurin JDK 21

So the workflows are aligned on one Java runtime across backend, Android, and
shared.

### 2.6 Caching

All three workflows cache:

- `~/.sonar/cache`
- `~/.gradle/caches`

This reduces:

- dependency download time
- Sonar scanner warm-up cost
- repeated Gradle resolution overhead

### 2.7 Required-status pattern

Each workflow ends with a dedicated `*-required` job:

- `backend-required`
- `frontend-required`
- `shared-required`

These jobs use:

```yaml
if: always()
```

and then decide whether to:

- pass because the area was not affected
- fail because the real build job failed
- pass because the real build job succeeded

This is important for branch protection because skipped jobs alone are often
not a good required-status surface.

---

## 3. Shared Differences

Although the three workflows share the same overall shape, they differ in the
build and Sonar execution details:

- backend uses Gradle’s Sonar task directly
- Android uses the Sonar scan action with explicit `-Dsonar.*` arguments
- shared also uses the Sonar scan action, but with a different and currently
  simpler configuration

So the repo has a consistent CI structure, but not yet a perfectly unified
Sonar implementation strategy.

---

## 4. Backend Workflow

Source file:

- `.github/workflows/backend.yml`

Workflow name:

- `Backend CI`

### 4.1 Change scope

The backend workflow runs when any of these paths change:

- `apps/backend/**`
- `apps/shared/**`
- `gradle/**`
- `gradlew`
- `gradlew.bat`
- `settings.gradle.kts`
- `build.gradle.kts`
- `.github/workflows/**`

Important consequence:

- shared changes also trigger backend CI

That is reasonable because backend depends on shared models and DTOs.

### 4.2 Main job

Main job name:

- `backend-build-sonar`

It runs only if:

```yaml
needs.changes.outputs.backend == 'true'
```

### 4.3 Test and coverage execution

Backend currently runs:

```bash
./gradlew :apps:backend:test :apps:backend:jacocoTestReport --info
```

This means the backend workflow produces:

- test execution
- JaCoCo coverage report

before Sonar runs.

### 4.4 Sonar execution

Backend Sonar currently runs through Gradle:

```bash
./gradlew :apps:backend:sonar -Dsonar.qualitygate.wait=true --info
```

Key characteristics:

- uses the backend Gradle Sonar setup
- waits for the quality gate result before completing
- relies on:
    - `SONAR_TOKEN`
    - `GITHUB_TOKEN`

This is the most Gradle-native of the three workflows.

### 4.5 Required status job

Backend ends with:

- `backend-required`

This job is the branch-protection surface for backend-related changes.

---

## 5. Android Frontend Workflow

Source file:

- `.github/workflows/android.yml`

Workflow name:

- `Frontend CI`

### 5.1 Change scope

The frontend workflow runs when any of these paths change:

- `apps/android/**`
- `gradle/**`
- `gradlew`
- `gradlew.bat`
- `settings.gradle.kts`
- `build.gradle.kts`
- `.github/workflows/**`

Unlike backend, it does not watch `apps/shared/**` directly.

That means frontend CI is primarily scoped to Android-side changes and root
build/workflow changes.

### 5.2 Main job

Main job name:

- `frontend-build`

It runs only if:

```yaml
needs.changes.outputs.frontend == 'true'
```

### 5.3 Test execution

Android currently runs:

```bash
./gradlew :apps:android:app:testDebugUnitTest jacocoTestReport
```

This targets:

- Android unit tests for the debug variant
- JaCoCo coverage generation

### 5.4 Sonar execution

Android Sonar runs through:

- `SonarSource/sonarqube-scan-action@v5`

with explicit Sonar arguments:

- `sonar.projectKey=se2-gruppenprojekt_se2-group-codebase_frontend`
- `sonar.organization=se2-gruppenprojekt`
- `sonar.projectName=frontend`
- `sonar.host.url=https://sonarcloud.io`
- `sonar.projectBaseDir=apps/android/app`
- `sonar.sources=src/main/java`
- `sonar.tests=src/test`
- `sonar.java.binaries=build/tmp/kotlin-classes/debug`
- `sonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/jacoco.xml`

Secrets used:

- `SONAR_TOKEN_ANDROID`
- `GITHUB_TOKEN`

### 5.5 Required status job

Frontend ends with:

- `frontend-required`

This is the always-finishing required gate for Android-related changes.

---

## 6. Shared Workflow

Source file:

- `.github/workflows/shared.yml`

Workflow name:

- `Shared CI`

### 6.1 Change scope

The shared workflow runs when any of these paths change:

- `apps/shared/**`
- `gradle/**`
- `gradlew`
- `gradlew.bat`
- `settings.gradle.kts`
- `build.gradle.kts`
- `.github/workflows/**`

This makes the shared workflow the direct CI surface for changes in the shared
monorepo module itself.

### 6.2 Main job

Main job name:

- `shared-build`

It runs only if:

```yaml
needs.changes.outputs.shared == 'true'
```

### 6.3 Test execution

Shared currently runs:

```bash
./gradlew :apps:shared:test
```

Unlike backend and Android, the current shared workflow does not run a separate
JaCoCo report generation step before Sonar.

### 6.4 Sonar execution

Shared Sonar runs through:

- `SonarSource/sonarqube-scan-action@v6`

with these explicit arguments:

- `sonar.projectKey=se2-gruppenprojekt_se2-group-codebase_backend`
- `sonar.organization=se2-gruppenprojekt`
- `sonar.projectName=shared`
- `sonar.host.url=https://sonarcloud.io`
- `sonar.projectBaseDir=apps/shared`
- `sonar.sources=src/main/kotlin/shared`
- `sonar.tests=src/test`

Secrets used:

- `SONAR_TOKEN_SHARED`
- `GITHUB_TOKEN`

### 6.5 Current Sonar note

As currently configured, the shared workflow uses:

- `projectName=shared`

but:

- `sonar.projectKey=se2-gruppenprojekt_se2-group-codebase_backend`

So the shared workflow documentation should treat that as the current workflow
implementation, not as an idealized target configuration.

### 6.6 Required status job

Shared ends with:

- `shared-required`

This is the required-status surface for shared-module changes.

---

## 7. What All Three Have in Common Operationally

From an operational perspective, all three workflows are built on the same
governing ideas:

- monorepo-aware selective execution
- consistent Java runtime
- Gradle-based test execution
- Sonar analysis as part of the same CI pass
- stable required-status jobs for branch protection

This means contributors can reason about them in one consistent way:

1. a PR changes files
2. path filtering decides which area is affected
3. only the relevant workflow does the expensive work
4. tests and Sonar run for that area
5. the corresponding required job becomes the merge gate

---

## 8. Practical Merge-Gate Interpretation

In practice, the workflows act as three merge gates:

- backend gate
- Android frontend gate
- shared gate

Typical examples:

- if only backend code changes, only backend CI should become relevant
- if only Android code changes, only frontend CI should become relevant
- if shared code changes, shared CI becomes relevant and backend CI may also
  become relevant because backend depends on shared

This selective model is the main reason the repository uses separate workflows
instead of one large monolithic CI pipeline.

---

## 9. Summary

The repository currently uses three parallel workflow tracks:

- backend
- Android frontend
- shared

They share:

- the same triggers
- the same change-detection pattern
- the same cache strategy
- the same required-status pattern

They differ mainly in:

- which paths trigger them
- which Gradle tasks they run
- how Sonar is invoked
- which Sonar token and project configuration they use

If a future refactor standardizes anything further, the most likely targets are:

- Sonar invocation style
- shared coverage reporting
- shared Sonar project-key configuration
