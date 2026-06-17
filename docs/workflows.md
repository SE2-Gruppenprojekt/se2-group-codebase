# GitHub Actions Workflows and Helper Scripts

[![Backend CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/backend.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/backend.yml)
[![Frontend CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/android.yml)
[![Shared CI](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/shared.yml/badge.svg?branch=main)](https://github.com/SE2-Gruppenprojekt/se2-group-codebase/actions/workflows/shared.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SE2-Gruppenprojekt_se2-group-codebase&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SE2-Gruppenprojekt_se2-group-codebase)

This document describes the current workflow automation in the repository. It
covers:

- all GitHub Actions workflows under `.github/workflows/`
- all helper scripts under `.github/scripts/`
- the checked-in workflow markdown template used as report reference material

The goal of this document is not just to list files. It is to explain:

- when each workflow runs
- what it actually does
- why the implementation is shaped that way
- which artifacts, secrets, and environment assumptions matter

---

## Workflow Inventory

Current workflow files:

```text
.github/workflows/android-release.yml
.github/workflows/android.yml
.github/workflows/backend-release.yml
.github/workflows/backend-security-af.yml
.github/workflows/backend-security.yml
.github/workflows/backend.yml
.github/workflows/shared.yml
.github/workflows/user-activity-report-exact-range.yml
.github/workflows/user-activity-report-monthly.yml
.github/workflows/user-activity-report-weekly.yml
```

Supporting workflow-adjacent files:

```text
.github/workflows/weekly-report-template.md
.github/scripts/generate_org_user_activity.py
.github/scripts/generate_zap_af_endpoint_coverage.rb
```

At a high level, the workflow set is split into four groups:

1. CI and Sonar validation
2. release automation
3. backend security scanning
4. organization activity reporting

---

## 1. CI Validation Workflows

The repository has three validation workflows:

- backend CI
- Android/frontend CI
- shared-module CI

These are not identical, but they follow the same broad structure.

### Shared design

All three validation workflows:

- run on:
  - `pull_request`
  - `merge_group`
  - `push` to `main`
- use workflow-level concurrency with:

```yaml
cancel-in-progress: true
```

- start with a `changes` job using `dorny/paths-filter@v3`
- skip expensive work if the relevant monorepo slice is unaffected
- end with a deterministic required-status job

That gives the repo:

- path-sensitive CI
- branch-protection-friendly required statuses
- fewer unnecessary Gradle + Sonar runs

### Shared checkout and JDK setup

The main build jobs all use:

```yaml
- uses: actions/checkout@v4
  with:
    fetch-depth: 0

- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: 21
```

`fetch-depth: 0` matters because Sonar analysis is more reliable with full git
history.

### Shared caching strategy

Each main build job caches:

```yaml
- ~/.sonar/cache
- ~/.gradle/caches
```

Example:

```yaml
- name: Cache Gradle packages
  uses: actions/cache@v4
  with:
    path: ~/.gradle/caches
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
    restore-keys: ${{ runner.os }}-gradle
```

### Required-status pattern

Each validation workflow ends with a small `*-required` job that always runs:

```yaml
if: always()
```

That job:

- passes immediately if the area was not affected
- fails if the real build job failed
- passes if the real build job succeeded

This is important because skipped jobs alone are often not a good required
branch-protection surface.

---

## 2. Backend CI

Source file:

```text
.github/workflows/backend.yml
```

Workflow name:

```text
Backend CI
```

### Trigger model

```yaml
on:
  pull_request:
  merge_group:
  push:
    branches: [main]
```

### Concurrency

```yaml
concurrency:
  group: backend-sonar-${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: true
```

This only cancels older backend CI runs for the same PR/ref. It does not
cancel Android or shared runs.

### Change detection scope

Backend CI considers these paths backend-relevant:

```yaml
backend:
  - 'apps/backend/**'
  - 'apps/shared/**'
  - 'gradle/**'
  - 'gradlew'
  - 'gradlew.bat'
  - 'settings.gradle.kts'
  - 'build.gradle.kts'
  - '.github/workflows/**'
```

Important implication:

- shared changes trigger backend CI
- workflow changes trigger backend CI

That makes sense because backend depends on shared DTOs/models, and workflow
changes can alter backend execution behavior.

### Main build job

The main backend job is:

```text
backend-build-sonar
```

It only runs when:

```yaml
if: needs.changes.outputs.backend == 'true'
```

### Test and coverage execution

Backend tests and coverage run in one Gradle command:

```bash
./gradlew :apps:backend:test :apps:backend:jacocoTestReport --info
```

That gives the workflow:

- unit/integration test execution
- JaCoCo coverage report generation

### Sonar execution

Backend Sonar runs through Gradle rather than the standalone Sonar action:

```bash
./gradlew :apps:backend:sonar -Dsonar.qualitygate.wait=true --info
```

This is the most Gradle-native of the validation workflows.

Required secrets:

```text
SONAR_TOKEN
GITHUB_TOKEN
```

### Required-status job

The final branch-protection surface is:

```text
backend-required
```

Its logic is simple:

```yaml
- name: Fail if backend job failed
  if: needs.changes.outputs.backend == 'true' && needs.backend-build-sonar.result != 'success'
  run: exit 1
```

---

## 3. Frontend CI

Source file:

```text
.github/workflows/android.yml
```

Workflow name:

```text
Frontend CI
```

### Trigger model

```yaml
on:
  pull_request:
  merge_group:
  push:
    branches: [main]
```

### Concurrency

```yaml
concurrency:
  group: android-sonar-${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: true
```

### Change detection scope

Frontend CI considers these paths frontend-relevant:

```yaml
frontend:
  - 'apps/android/**'
  - 'gradle/**'
  - 'gradlew'
  - 'gradlew.bat'
  - 'settings.gradle.kts'
  - 'build.gradle.kts'
  - '.github/workflows/**'
```

Unlike backend CI, this does not include `apps/shared/**`. So a shared-only
change will not directly trigger the Android workflow unless another watched
path also changed.

### Main build job

The main frontend job is:

```text
frontend-build
```

### Test and coverage execution

Frontend CI deliberately splits debug and release test execution:

```bash
./gradlew clean :apps:android:app:testDebugUnitTest
./gradlew :apps:android:app:testReleaseUnitTest :apps:android:app:jacocoTestReport
```

That is important because the Android module has historically been more fragile
when debug and release coverage work are over-combined in one command.

### Sonar execution

Frontend Sonar uses the standalone Sonar action:

```yaml
- name: Run frontend Sonar analysis
  uses: SonarSource/sonarqube-scan-action@v6
```

The action receives explicit Sonar arguments:

```text
-Dsonar.projectBaseDir=apps/android/app
-Dsonar.sources=src/main/java
-Dsonar.tests=src/test/java,src/androidTest/java
-Dsonar.java.binaries=build/tmp/kotlin-classes/debug
-Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/jacoco.xml
```

It also sets coverage exclusions:

```text
**/*Screen*.kt
**/components/**/*.kt
**/navigation/**/*.kt
**/theme/**/*.kt
```

Required secrets:

```text
SONAR_TOKEN_ANDROID
GITHUB_TOKEN
```

### Required-status job

The branch-protection surface is:

```text
frontend-required
```

---

## 4. Shared CI

Source file:

```text
.github/workflows/shared.yml
```

Workflow name:

```text
Shared CI
```

### Trigger model

```yaml
on:
  pull_request:
  merge_group:
  push:
    branches: [main]
```

### Concurrency

```yaml
concurrency:
  group: shared-sonar-${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: true
```

### Change detection scope

Shared CI considers these paths shared-relevant:

```yaml
shared:
  - 'apps/shared/**'
  - 'gradle/**'
  - 'gradlew'
  - 'gradlew.bat'
  - 'settings.gradle.kts'
  - 'build.gradle.kts'
  - '.github/workflows/**'
```

### Main build job

The main shared job is:

```text
shared-build
```

### Test execution

Shared tests run with:

```bash
./gradlew :apps:shared:test
```

There is no dedicated JaCoCo step here in the current workflow.

### Sonar execution

Shared Sonar also uses the standalone Sonar action:

```yaml
- name: Run shared Sonar analysis
  uses: SonarSource/sonarqube-scan-action@v6
```

Current explicit arguments are minimal:

```text
-Dsonar.projectKey=se2-gruppenprojekt_se2-group-codebase_backend
-Dsonar.organization=se2-gruppenprojekt
-Dsonar.projectName=shared
-Dsonar.host.url=https://sonarcloud.io
-Dsonar.projectBaseDir=apps/shared
```

Required secrets:

```text
SONAR_TOKEN_SHARED
GITHUB_TOKEN
```

### Required-status job

The branch-protection surface is:

```text
shared-required
```

---

## 5. Validation Workflow Comparison

The three validation workflows share structure, but their build and Sonar
execution strategies differ:

| Workflow | Build/Test Strategy | Sonar Strategy |
| --- | --- | --- |
| Backend | `test` + `jacocoTestReport` | Gradle `:apps:backend:sonar` |
| Frontend | debug unit tests + release coverage task | Sonar scan action with explicit args |
| Shared | shared tests only | Sonar scan action with simpler config |

This is coherent, but not fully unified. The repo prefers consistency in job
shape and required-status behavior, while allowing each module to keep the
execution style that currently works for it.

---

## 6. Android Release Workflow

Source file:

```text
.github/workflows/android-release.yml
```

Workflow name:

```text
Android Release
```

This workflow builds a signed release APK, optionally publishes it to Itch.io,
and optionally publishes a GitHub release asset.

### Trigger model

```yaml
on:
  workflow_dispatch:
  push:
    branches: [main]
    tags:
      - "v*"
```

The workflow can therefore run in three modes:

1. manual release run
2. `main` branch internal build
3. tag-driven product release

### Dispatch inputs

Manual runs accept:

```yaml
release_channel:
  - preview
  - internal
  - production
version_name
version_code
publish_to_itch
```

### Metadata preparation

Release metadata is normalized in the `prepare-release` job.

Core logic:

```bash
if [ "$REF_TYPE" = "tag" ]; then
  RELEASE_CHANNEL="production"
  VERSION_NAME="${REF_NAME#v}"
  VERSION_CODE="$RUN_NUMBER"
  PUBLISH_TO_ITCH="true"
  PUBLISH_GITHUB_RELEASE="true"
elif [ "$EVENT_NAME" = "workflow_dispatch" ]; then
  RELEASE_CHANNEL="$INPUT_RELEASE_CHANNEL"
  VERSION_NAME="$INPUT_VERSION_NAME"
  VERSION_CODE="$INPUT_VERSION_CODE"
else
  RELEASE_CHANNEL="internal"
  VERSION_NAME="0.1.0-main.${RUN_NUMBER}"
  VERSION_CODE="$RUN_NUMBER"
fi
```

That means:

- tags become production releases automatically
- manual dispatch can override release metadata
- plain `main` pushes become internal builds

### APK build and signing

The main build job:

```text
build-release-apk
```

does the following:

1. checks out the repo
2. sets up JDK 21
3. sets up the Android SDK
4. validates Android signing secrets
5. decodes the base64 keystore
6. runs:

```bash
./gradlew :apps:android:app:assembleRelease
```

7. stages the APK as:

```text
dist/rummikub-android-<version>-<channel>.apk
```

8. uploads it as the `android-release-apk` artifact

### Required Android signing secrets

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

### GitHub release publishing

The `publish-github-release` job runs only when:

```yaml
if: needs.prepare-release.outputs.publish_github_release == 'true'
```

That means:

- tagged releases publish a GitHub release asset
- internal `main` builds do not
- manual workflow runs do not by default

The release action used is:

```yaml
uses: softprops/action-gh-release@v2
```

### Itch.io publishing

The `publish-itch` job runs only when:

```yaml
if: needs.prepare-release.outputs.publish_to_itch == 'true'
```

It:

1. validates Itch configuration
2. downloads the built APK artifact
3. installs `butler`
4. pushes the APK to the configured Itch channel

Required secrets/variables:

```text
ITCH_IO_API_KEY        (secret)
ITCH_IO_USER           (repository variable)
ITCH_IO_GAME           (repository variable)
```

Channel mapping:

```bash
preview    -> android-preview
internal   -> android-internal
production -> android-release
```

### Summary job

The workflow ends with `release-summary`, which writes a human-readable release
summary to the Actions UI, including:

- release channel
- version name/code
- APK name
- Itch channel
- GitHub release job result
- Itch publish job result

---

## 7. Backend Release Workflow

Source file:

```text
.github/workflows/backend-release.yml
```

Workflow name:

```text
Backend Release
```

This workflow builds the backend boot jar and optionally triggers a Render
deployment.

### Trigger model

```yaml
on:
  workflow_dispatch:
  push:
    branches: [main]
    tags:
      - "v*"
```

So it can run as:

1. manual backend release
2. `main` branch deployment build
3. tag-driven production release

### Dispatch inputs

Manual dispatch supports:

```yaml
release_channel:
  - internal
  - production
release_version
trigger_render_deploy
```

### Metadata preparation

The `prepare-release` job computes:

- `release_channel`
- `release_version`
- `trigger_render_deploy`
- `deployment_reason`

Core logic:

```bash
if [ "$REF_TYPE" = "tag" ]; then
  RELEASE_CHANNEL="production"
  RELEASE_VERSION="${REF_NAME#v}"
  TRIGGER_RENDER_DEPLOY="true"
elif [ "$EVENT_NAME" = "workflow_dispatch" ]; then
  RELEASE_CHANNEL="$INPUT_RELEASE_CHANNEL"
  RELEASE_VERSION="${INPUT_RELEASE_VERSION:-0.1.0-manual.${RUN_NUMBER}}"
  TRIGGER_RENDER_DEPLOY="$INPUT_TRIGGER_RENDER_DEPLOY"
else
  RELEASE_CHANNEL="internal"
  RELEASE_VERSION="0.1.0-main.${RUN_NUMBER}"
  TRIGGER_RENDER_DEPLOY="true"
fi
```

### Build job

The `build-backend` job:

1. checks out the repo
2. sets up JDK 21
3. caches Gradle
4. runs:

```bash
./gradlew :apps:backend:test
./gradlew :apps:backend:bootJar
```

5. finds the non-plain backend jar
6. stages it as:

```text
dist/se2-backend-<release-version>.jar
```

7. uploads it as the `backend-release-jar` artifact

### Render deployment

The `deploy-render` job runs only when:

```yaml
if: needs.prepare-release.outputs.trigger_render_deploy == 'true'
```

It:

1. verifies tagged releases match `origin/main` before triggering Render
2. POSTs to the Render deploy hook
3. polls the backend health endpoint until the deployment becomes healthy

The tag-vs-main check is important:

```bash
TAG_SHA="$(git rev-parse HEAD)"
MAIN_SHA="$(git rev-parse origin/main)"

if [ "$TAG_SHA" != "$MAIN_SHA" ]; then
  echo "Tagged backend release does not match origin/main."
  exit 1
fi
```

This prevents a tag from claiming to be a deployable release when Render will
actually deploy a different branch state.

### Required secret

```text
RENDER_DEPLOY_HOOK_URL
```

### Health verification

Render health is checked through:

```text
https://se2-group-codebase.onrender.com/actuator/health
```

Polling loop:

```bash
for i in {1..36}; do
  if curl -fsS "$HEALTH_URL"; then
    echo "Backend is healthy"
    exit 0
  fi
  sleep 10
done
```

### Summary job

The workflow ends with `release-summary`, which writes:

- release channel
- release version
- deployment reason
- artifact name
- Render deployment result
- health endpoint

---

## 8. Backend Security Baseline Workflow

Source file:

```text
.github/workflows/backend-security.yml
```

Workflow name:

```text
Backend Security Scan
```

This is the passive OWASP ZAP baseline scan workflow.

### Trigger model

```yaml
on:
  workflow_dispatch:
  pull_request:
    paths:
      - "apps/backend/**"
      - "docs/security.md"
      - ".github/workflows/backend-security.yml"
  push:
    branches: [main]
    paths:
      - "apps/backend/**"
      - "docs/security.md"
      - ".github/workflows/backend-security.yml"
  schedule:
    - cron: "0 6 * * 1"
```

So it supports:

- manual runs
- PR validation on backend/security-doc changes
- main-branch rechecks after merge
- weekly scheduled passive scans

### Permissions

```yaml
permissions:
  contents: read
  issues: write
```

`issues: write` is kept so ZAP-related issue workflows remain possible if
needed.

### Environment

```yaml
env:
  BACKEND_BASE_URL: https://se2-group-codebase.onrender.com
  HEALTH_URL: https://se2-group-codebase.onrender.com/actuator/health
```

### Job flow

The single job is:

```text
zap-baseline-scan
```

It:

1. checks out the repo
2. polls the health endpoint until the backend is reachable
3. runs `zaproxy/action-baseline@v0.14.0`
4. writes the markdown report into `$GITHUB_STEP_SUMMARY`
5. uploads:
   - `report_html.html`
   - `report_md.md`
   - `report_json.json`

### ZAP baseline invocation

```yaml
- name: Run OWASP ZAP baseline scan
  uses: zaproxy/action-baseline@v0.14.0
  with:
    target: ${{ env.BACKEND_BASE_URL }}
    cmd_options: "-a -I"
```

`-a` enables additional passive rules.  
`-I` keeps warnings visible without failing CI only because of non-blocking
issues.

---

## 9. Backend Security Automation Framework Workflow

Source file:

```text
.github/workflows/backend-security-af.yml
```

Workflow name:

```text
Backend Security Scan (Automation Framework)
```

This is the richer stateful ZAP scan workflow.

### Trigger model

```yaml
on:
  workflow_dispatch:
  pull_request:
    paths:
      - "apps/backend/**"
      - "docs/security.md"
      - ".github/scripts/**"
      - ".github/workflows/backend-security-af.yml"
      - ".github/zap/backend-automation-plan.yaml"
  push:
    branches: [main]
    paths:
      - "apps/backend/**"
      - "docs/security.md"
      - ".github/scripts/**"
      - ".github/workflows/backend-security-af.yml"
      - ".github/zap/backend-automation-plan.yaml"
  schedule:
    - cron: "0 8 * * 3"
```

The AF workflow is therefore tied not only to backend code changes, but also to
plan and script changes.

### Permissions

```yaml
permissions:
  contents: read
```

### High-level flow

The single job `zap-automation-framework-scan` does:

1. checkout
2. wait for backend readiness
3. create deterministic scan fixture state
4. generate a concrete AF plan from placeholders
5. run ZAP AF
6. regenerate the concrete plan for reporting
7. generate endpoint coverage markdown
8. publish reports into the GitHub step summary
9. upload artifacts

### Dynamic fixture state step

The dynamic fixture step is the core of the authenticated/stateful scan design:

```bash
FIXTURE_JSON="$(
  curl -fsS \
    -X POST "$BACKEND_BASE_URL/internal/security/scan-fixture" \
    -H "X-Scan-Secret: $ZAP_SCAN_FIXTURE_SECRET"
)"
```

It extracts:

```bash
SCAN_LOBBY_ID
SCAN_GAME_ID
SCAN_HOST_USER_ID
SCAN_GUEST_USER_ID
SCAN_HOST_ACCESS_TOKEN
SCAN_GUEST_ACCESS_TOKEN
```

and constructs:

```bash
SCAN_HOST_AUTH_HEADER="Authorization:Bearer $SCAN_HOST_ACCESS_TOKEN"
SCAN_GUEST_AUTH_HEADER="Authorization:Bearer $SCAN_GUEST_ACCESS_TOKEN"
```

This is important. The AF scan now exercises protected requests with real JWTs,
not with old trusted-header identity.

### Plan generation

The workflow substitutes dynamic values into:

```text
.github/zap/backend-automation-plan.yaml
```

and writes:

```text
.github/zap/backend-automation-plan.generated.yaml
```

The substitution block includes:

```bash
-e "s/__SCAN_LOBBY_ID__/$SCAN_LOBBY_ID/g"
-e "s/__SCAN_GAME_ID__/$SCAN_GAME_ID/g"
-e "s|__SCAN_HOST_AUTH_HEADER__|$SCAN_HOST_AUTH_HEADER|g"
-e "s|__SCAN_GUEST_AUTH_HEADER__|$SCAN_GUEST_AUTH_HEADER|g"
```

### ZAP AF invocation

```yaml
- name: Run ZAP Automation Framework plan
  uses: zaproxy/action-af@v0.3.0
  with:
    plan: ".github/zap/backend-automation-plan.generated.yaml"
```

### Coverage artifact generation

After the AF run, the workflow calls:

```bash
ruby .github/scripts/generate_zap_af_endpoint_coverage.rb .github/zap/backend-automation-plan.generated.yaml zap-af-report.json zap-af-endpoint-coverage.md
```

This produces a reviewer-friendly endpoint inventory markdown artifact.

### Uploaded artifacts

The AF workflow uploads:

```text
.github/zap/backend-automation-plan.generated.yaml
zap-af-report.md
zap-af-report.html
zap-af-report.json
zap-af-endpoint-coverage.md
```

### Required secret

```text
ZAP_SCAN_FIXTURE_SECRET
```

This must match the backend-side scan fixture secret configuration.

---

## 10. Weekly Activity Report Workflow

Source file:

```text
.github/workflows/user-activity-report-weekly.yml
```

Workflow name:

```text
Organization User Activity Report (Weekly)
```

This workflow generates a weekly organization activity report through direct
GitHub API calls made by a custom Python script.

### Trigger model

```yaml
on:
  workflow_dispatch:
    inputs:
      report_since
      report_until
  schedule:
    - cron: "0 7 * * 4"
```

So it supports:

- automatic weekly scheduled generation
- manual regeneration for an exact weekly range

### Reporting window logic

The workflow resolves the reporting window in shell:

```bash
if [ -n "${INPUT_REPORT_SINCE:-}" ] && [ -n "${INPUT_REPORT_UNTIL:-}" ]; then
  REPORT_SINCE="${INPUT_REPORT_SINCE}T00:00:00Z"
  REPORT_UNTIL="${INPUT_REPORT_UNTIL}T00:00:00Z"
else
  REPORT_UNTIL="$(date -u +%Y-%m-%d)T00:00:00Z"
  REPORT_SINCE="$(date -u -d '7 days ago' +%Y-%m-%d)T00:00:00Z"
fi
```

That means:

- manual input can force an exact window
- otherwise the workflow uses a rolling 7-day span

### Generator script

The workflow then runs:

```bash
python3 .github/scripts/generate_org_user_activity.py
```

Required secret:

```text
ORG_ACTIVITY_REPORT_TOKEN
```

### Published artifacts

The weekly workflow currently uploads:

```text
reports/organization_user_activity.csv
reports/organization_user_activity.json
```

### Summary output

The workflow prints:

- organization name
- selected date span
- confirmation that CSV/JSON outputs were generated
- the first 20 lines of the CSV report

Important note: unlike the exact-range workflow, the weekly workflow does not
currently generate a markdown report artifact from the script output.

---

## 11. Monthly Activity Report Workflow

Source file:

```text
.github/workflows/user-activity-report-monthly.yml
```

Workflow name:

```text
Organization User Activity Report (Monthly)
```

This workflow is structurally different from the weekly and exact-range
workflows. It uses a third-party action instead of the local Python generator.

### Trigger model

```yaml
on:
  workflow_dispatch:
  schedule:
    - cron: "0 7 21 * *"
```

That means:

- it runs automatically on the 21st of every month at 07:00 UTC
- it can also be run manually

### Main action

The core step is:

```yaml
- name: Analyze organization user activity
  uses: peter-murray/inactive-users-action@v1
```

Important inputs:

```yaml
token: ${{ secrets.ORG_ACTIVITY_REPORT_TOKEN }}
organization: ${{ env.TARGET_ORGANIZATION }}
activity_days: 30
octokit_max_retries: 15
outputDir: reports
```

So the monthly workflow:

- uses the repository owner as the organization
- reports on the last 30 days
- relies on the third-party action’s own CSV/JSON output format

### Output behavior

The workflow:

- publishes a short summary into `$GITHUB_STEP_SUMMARY`
- uploads the generated CSV and JSON outputs as artifacts

Important architectural difference:

- weekly and exact-range reports use the local `generate_org_user_activity.py`
- monthly currently uses `peter-murray/inactive-users-action@v1`

So the activity-reporting pipeline is not yet fully unified.

---

## 12. Exact-Range Activity Report Workflow

Source file:

```text
.github/workflows/user-activity-report-exact-range.yml
```

Workflow name:

```text
Organization User Activity Report (Exact Range)
```

This workflow is the most presentation-heavy activity-report workflow.

### Trigger model

```yaml
on:
  workflow_dispatch:
    inputs:
      report_since:
        required: true
      report_until:
        required: true
```

So it is purely manual and always requires an explicit date range.

### Environment setup

The job exports:

```yaml
REPORT_SINCE: ${{ inputs.report_since }}T00:00:00Z
REPORT_UNTIL: ${{ inputs.report_until }}T00:00:00Z
```

### Data generation

Like the weekly workflow, it runs:

```bash
python3 .github/scripts/generate_org_user_activity.py
```

### Markdown post-processing

Unlike the weekly workflow, it then runs an inline Python script that reads the
generated JSON and builds a markdown report:

```python
with open("reports/organization_user_activity.json", "r", encoding="utf-8") as fh:
    rows = json.load(fh)
```

It calculates totals such as:

- commits
- issues
- issue comments
- PR comments
- active vs inactive actors
- human vs automated actors

and writes:

```text
reports/report_md.md
```

### Uploaded artifacts

The workflow uploads:

```text
reports/organization_user_activity.csv
reports/organization_user_activity.json
reports/report_md.md
```

### Summary output

The summary step prints the first 80 lines of the generated markdown report
directly into the Actions UI.

---

## 13. Helper Script: `generate_org_user_activity.py`

Source file:

```text
.github/scripts/generate_org_user_activity.py
```

This script is the custom reporting engine used by the weekly and exact-range
activity workflows.

### Inputs

It reads these environment variables:

```python
ORG = os.environ["TARGET_ORGANIZATION"]
SINCE = os.environ["REPORT_SINCE"]
UNTIL = os.environ["REPORT_UNTIL"]
TOKEN = os.environ["GH_TOKEN"]
REPORTS_DIR = os.environ.get("REPORTS_DIR", "reports")
```

So it requires:

- target organization
- exact ISO-like UTC window
- GitHub API token

### API behavior

It performs direct GitHub REST API calls through `urllib.request`, not through
a third-party library.

The request helper:

```python
def gh_get(url: str):
    req = urllib.request.Request(
        url,
        headers={
            "Accept": "application/vnd.github+json",
            "Authorization": f"Bearer {TOKEN}",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "se2-group-codebase-activity-report",
        },
    )
```

### Pagination

The script handles paginated GitHub API responses manually:

```python
def paginate(url: str):
    items = []
    next_url = url
    while next_url:
        data, headers = gh_get(next_url)
        ...
        link = headers.get("Link", "")
```

### Data sources

For each repository in the organization, the script scans:

- commits
- issues
- issue comments
- pull request review comments

Specifically:

```python
commits_url = f"https://api.github.com/repos/{ORG}/{repo_name}/commits?since=...&until=..."
issues_url = f"https://api.github.com/repos/{ORG}/{repo_name}/issues?state=all&since=..."
issue_comments_url = f"https://api.github.com/repos/{ORG}/{repo_name}/issues/comments?since=..."
pr_comments_url = f"https://api.github.com/repos/{ORG}/{repo_name}/pulls/comments?since=..."
```

### Output model

The script writes two files:

```text
reports/organization_user_activity.json
reports/organization_user_activity.csv
```

Current fields per actor are:

```text
login
email
isActive
commits
issues
issueComments
prComments
```

The CSV header is:

```python
fieldnames=["login", "email", "isActive", "commits", "issues", "issueComments", "prComments"]
```

### Important limitation

This script is intentionally simple. It does not currently compute:

- PR approvals
- PR merges
- repository-level summaries
- activity-day distributions

It produces a flat actor-centric report.

---

## 14. Helper Script: `generate_zap_af_endpoint_coverage.rb`

Source file:

```text
.github/scripts/generate_zap_af_endpoint_coverage.rb
```

This script exists because raw ZAP output is good at surfacing alerts but less
good at showing exactly which AF requestors and endpoint groups ran.

### Inputs

The script expects three runtime arguments:

```ruby
plan_path = ARGV.fetch(0)
json_path = ARGV.fetch(1)
output_path = ARGV.fetch(2)
```

So the AF workflow passes:

1. the generated concrete plan
2. the AF JSON report
3. the markdown output file path

### Core categorization logic

The script groups requestors into logical slices:

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

This gives the generated report much more structure than the raw ZAP markdown.

### Request extraction

The script flattens requestor jobs from the plan:

```ruby
requests = plan.fetch("jobs", []).select { |job| job["type"] == "requestor" }.flat_map do |job|
  (job["requests"] || []).map do |request|
    {
      job_name: job.fetch("name"),
      method: request.fetch("method", "GET"),
      url: request.fetch("url"),
      expected_status: request["responseCode"],
      category: request_category(job.fetch("name"), request)
    }
  end
end
```

### Output

The script writes a markdown file with:

- metadata
- coverage summary
- per-category endpoint tables
- response profile
- interpretation notes

That output becomes:

```text
zap-af-endpoint-coverage.md
```

---

## 15. Workflow Template File

Source file:

```text
.github/workflows/weekly-report-template.md
```

This file is not an executable workflow. It is a checked-in markdown reference
document that shows the intended layout and narrative structure for a weekly
organization activity report.

It includes:

- overview section
- report metadata
- executive summary
- activity tables
- human contributor section
- automated/tool activity section
- inactive contributor section

In practice, it acts as a documentation and formatting reference rather than a
runtime workflow input.

---

## 16. Secrets and Variables Used by Workflows

The current workflow set uses these repository secrets and variables.

### Sonar

```text
SONAR_TOKEN
SONAR_TOKEN_ANDROID
SONAR_TOKEN_SHARED
```

### Android signing and release

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

### Itch.io publishing

```text
ITCH_IO_API_KEY        (secret)
ITCH_IO_USER           (repository variable)
ITCH_IO_GAME           (repository variable)
```

### Backend deployment

```text
RENDER_DEPLOY_HOOK_URL
```

### Security scanning

```text
ZAP_SCAN_FIXTURE_SECRET
```

### Activity reporting

```text
ORG_ACTIVITY_REPORT_TOKEN
```

---

## 17. Design Observations

A few structural points are worth calling out.

### Strengths of the current setup

- validation workflows are path-aware and branch-protection-friendly
- release workflows are explicit and artifact-oriented
- security scanning is versioned in the repository
- the AF scan uses real state and real bearer auth
- activity reporting is automated and reproducible

### Current inconsistencies

- backend Sonar runs through Gradle, frontend/shared use the Sonar action
- weekly/exact-range activity reporting use the local Python script
- monthly activity reporting still uses a third-party action
- exact-range report rendering is inline Python inside the workflow rather than
  delegated to a reusable script

These are not necessarily bugs, but they are real design differences.

### Low-risk cleanup opportunities

Good follow-up improvements would be:

- unify monthly reporting onto `generate_org_user_activity.py`
- move exact-range markdown rendering into a reusable script
- standardize Sonar execution style across backend/frontend/shared
- consider documenting release env/secrets in `docs/release-management.md`
  alongside this workflow reference

---

## 18. Conclusion

The repository’s workflow system is no longer just “run tests on PRs.”

It now includes:

- path-sensitive CI validation
- Sonar analysis
- Android release packaging and distribution
- backend release artifact creation and Render deployment
- baseline and plan-driven security scanning
- organization activity reporting

The helper scripts are a meaningful part of that system:

- `generate_org_user_activity.py` powers custom GitHub organization reports
- `generate_zap_af_endpoint_coverage.rb` turns AF scan execution into a
  readable endpoint inventory artifact

Taken together, these workflows form a fairly complete operational layer for
this repository: build, test, analyze, package, deploy, scan, and report.
