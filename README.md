# WalkLogger

An Android app for logging dog walks: who walked, how it went, stool, behaviour, medical notes and the
weather at the time. Walks can be timed live or logged afterwards, and are browsed per day, week and month.

## Features

- **Log**: week strip and a timeline of the active dog's walks.
- **Live walk**: start a timed walk (elapsed time, quick note, stool) and finish it with rating,
  equipment, weather, behaviour, medical concerns and notes.
- **Past walk**: log a walk afterwards with its date and times, on any day.
- **Weather**: fetched automatically from [Open-Meteo](https://open-meteo.com/) for the walk's time and
  place, and editable by hand.
- **Calendar**: month grid tinted by rating, with the walks of the selected day.
- **Walk detail**: read-only view of a logged walk, with edit and discard.
- **Dogs**: register several dogs, pick the active one; deleted dogs keep their history.
- English and Swedish.

## Tech stack

Kotlin, Jetpack Compose (Material 3), Navigation 3, Hilt, Room, DataStore, coroutines and Flow,
Retrofit/OkHttp, Play Services Location. minSdk 29.

Architecture follows Google's recommended app architecture (UI → optional domain → data),
organised by feature. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Build and test

```bash
./gradlew assembleDebug           # build
./gradlew spotlessCheck           # ktlint + Compose rules (spotlessApply to format)
./gradlew testDebugUnitTest       # JVM unit tests
./gradlew lintDebug               # Android lint
./gradlew connectedAndroidTest    # Room and Compose UI tests, needs a device in English
```

## CI

GitHub Actions workflows in [.github/workflows](.github/workflows):

| Workflow | Trigger | What it runs |
|---|---|---|
| [Pull request checks](.github/workflows/pull-request-checks.yml) | Every pull request to `main` | `spotlessCheck`, `assembleDebug`, `testDebugUnitTest`, `lintDebug`; reports uploaded as artifacts |
| [Instrumented tests](.github/workflows/instrumented-tests.yml) | Manually, with `confirm` set to `run` | `connectedAndroidTest` on an API 35 emulator in English; reports uploaded on failure |

Instrumented tests are manual only, since emulator runs cost Actions minutes.

## Working with AI

The app is developed together with Claude Code. The agent follows the rules in
[CLAUDE.md](CLAUDE.md) (architecture, design tokens, strings, comments, git).

1. **Describe**: the developer starts `/new-feature` and describes what they want.
2. **Plan**: the agent explores the code and proposes a plan.
3. **Approve**: the developer reviews the plan and approves it, or asks for changes.
4. **Implement**: the agent implements the plan and verifies it, but does not commit.
5. **Review**: the developer reviews the diff.
6. **Pull request**: when satisfied, the developer asks for a pull request with `/pull-request`.
   The PR is squash-merged.

### Project skills

Custom skills in [.claude/skills](.claude/skills):

| Skill | What it does |
|---|---|
| `/new-feature` | Clarifies a new feature, shares a plan and implements it once approved. |
| `/verify` | Formats, builds and runs style check, lint, unit tests and, when needed, instrumented tests. |
| `/review-changes` | Reviews the branch for bugs and rule breaks that would block a merge. |
| `/pull-request` | Commits, pushes the branch and opens a pull request. |
