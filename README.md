# WalkLogger

An Android app for logging dog walks: who walked, how it went, stool, behaviour, medical notes and the
weather at the time. Walks can be timed live or logged afterwards, and are browsed per day, week and month.

## Features

- **Log**: week strip and a timeline of the active dog's walks.
- **Live walk**: start a timed walk (elapsed time, quick note, stool) and finish it with rating,
  equipment, stool, mood and focus ratings, behaviour, health concerns and notes. The handler, times and
  weather can be corrected there, or the walk discarded.
- **Route**: with precise location, a live walk records its route, also with the screen locked, and
  shows it on a map when finishing the walk and in the walk detail. Points outside the walk's times are
  hidden, not deleted. Needs a [Maps API key](#maps-api-key).
- **Past walk**: log a walk afterwards with its date and times, on any day.
- **Weather**: fetched automatically from [Open-Meteo](https://open-meteo.com/) for the walk's time and
  place (the device's precise location, or else a chosen place), and editable by hand.
- **Calendar**: month grid with a rating face per day, with the walks of the selected day.
- **Walk detail**: read-only view of a logged walk with its route map, with edit and discard.
- **Dogs**: register several dogs, pick the active one; deleted dogs keep their history.
- Light and dark theme, following the system or chosen in Settings.
- English and Swedish.

## Appearance

### Light mode
<img width="150" alt="Screenshot_20261117_182731" src="https://github.com/user-attachments/assets/04add677-f8b2-49a9-a6db-15fc52759f2e" />
<img width="150" alt="Screenshot_20261117_182800" src="https://github.com/user-attachments/assets/8ebeea4e-a33d-407f-8baa-0a4f6bbcbf1f" />
<img width="150" alt="Screenshot_20261117_182814" src="https://github.com/user-attachments/assets/126f4781-6219-475f-8330-02f63651c529" />
<img width="150" alt="Screenshot_20261117_182824" src="https://github.com/user-attachments/assets/74f2ddf8-df53-4588-92bc-7abc84db6d51" />
<img width="150" alt="Screenshot_20261117_182846" src="https://github.com/user-attachments/assets/c317558b-3847-41f3-92cf-38fd4c400ab6" />
<img width="150" alt="Screenshot_20261117_182935" src="https://github.com/user-attachments/assets/e2fa3084-5174-4c3f-a35d-6712df29e6a2" />
<img width="150" alt="Screenshot_20261117_183014" src="https://github.com/user-attachments/assets/6264d262-191f-4ddd-b875-61bdeaef376d" />
<img width="150" alt="Screenshot_20261117_183325" src="https://github.com/user-attachments/assets/bc73498c-6714-4d9e-9a28-d20d303562e1" />
<img width="150" alt="Screenshot_20261120_123833" src="https://github.com/user-attachments/assets/0e13ec07-a042-44d9-9d90-57c728eb9e91" />

### Dark mode
<img width="150" alt="Screenshot_20261120_124228" src="https://github.com/user-attachments/assets/4f397b31-4647-4acb-902d-5132f3516cba" />
<img width="150" alt="Screenshot_20261120_124236" src="https://github.com/user-attachments/assets/f9a16a5c-6f0b-4a19-a1ca-51977e7d39a4" />
<img width="150" alt="Screenshot_20261120_124120" src="https://github.com/user-attachments/assets/df993704-a37e-49be-ab54-74d5542d705a" />
<img width="150" alt="Screenshot_20261120_124136" src="https://github.com/user-attachments/assets/ab71aef1-6e2d-4de4-adcd-fe0ee27a2214" />
<img width="150" alt="Screenshot_20261120_124207" src="https://github.com/user-attachments/assets/3edfef73-8611-4ca8-94bc-771a842dcf71" />
<img width="150" alt="Screenshot_20261120_124040" src="https://github.com/user-attachments/assets/53bd6ee2-361e-4ff6-bffd-98abe3d7a897" />

## Tech stack

Kotlin, Jetpack Compose (Material 3), Navigation 3, Hilt, Room, DataStore, coroutines and Flow,
Retrofit/OkHttp, Play Services Location, Maps Compose. minSdk 29.

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

### Maps API key

The route map needs a Google Maps API key. Without one the app still builds and runs, and the map
stays blank.

1. In the [Google Cloud Console](https://console.cloud.google.com/google/maps-apis), enable
   **Maps SDK for Android** and create an API key.
2. Restrict the key to **Android apps**, with the package name `com.example.walklogger` and the
   SHA-1 of every certificate that signs a build you hand out (`./gradlew signingReport` lists the
   debug one), and to the **Maps SDK for Android** API only.
3. Add it to `local.properties`, which is not committed:

   ```properties
   MAPS_API_KEY=your-key
   ```

The key is built into the APK, so anyone with the APK can read it; the restrictions above are what
stop it from being used elsewhere.

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
