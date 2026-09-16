# Working agreement

* Never create, edit, delete or move files unless I explicitly ask for the change
  in the current message. Questions ("what would X entail?", "how does Y work?")
  are read-only: answer them, optionally propose a change, and wait for explicit instructions to create, edit, delete or move files .
* Running read-only commands (grep, cat, gradle compile/test) is fine.
* When you do receive explicit instructions to create, edit, delete or move files , it covers that request only, not later messages.
* Never make commits or pushes on your own accord, only ever when explicitly asked (or prompted to use a skill that says so).


# Communication

* Be brief and concise in everything: answers, explanations, summaries, commit messages.
  No preamble, no restating the question, no padding. If one sentence answers it, write one sentence.
* Lead with the answer or recommendation. Add reasoning only where it changes the decision.


# Comments

* Comments must earn their place. Write one only when it says something the code cannot:
  why a value is what it is, a non-obvious constraint, a link to a spec or mockup.
* Never comment what the code already says (`// Timeline` above `TimelineGutter`, `/** 4. */` on `val xs = 4.dp`).
* No section-divider comments, no commented-out code, no KDoc on private or self-explanatory declarations.


# Git

* Branch names: `<type>/<short-description>`, e.g. `feat/walk-detail-screen`, `chore/spacing-tokens`,
  `docs/architecture`. Types: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`.
* Commit messages: one short imperative line. Squash-and-merge is used, so the PR title and body
  carry the history, not individual commits.
* PR title: `<type>: <description>`, mirroring the branch name — branch `feat/walk-detail-screen`
  → title `feat: walk detail screen`. This becomes the squash commit message on `main`.
* PR body: brief and concise, using exactly these headers:
  * **What** — one or two sentences on the change.
  * **Issue** — linked issue, or `none`.
  * **Notes** — decisions, trade-offs, follow-ups. Omit if there are none.
  * **Verification** — what was run (`./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`, manual checks).
  * **Screenshots** — only when the UI changed; before/after if applicable.


# Design system

* Never come up with your own colors, always use the colors already in the project. If you want to use another color, you have to ask before.
* Always use the design tokens specified in app/src/main/java/com/example/walklogger/core/ui/theme, never make up "magic numbers" on your own. If you want to add something, always ask before.
* Always use existing components before using Android's build in components.
* When creating new components, always check if there is already a created component for it.


# Strings and locales

* No hard-coded user-facing strings in Kotlin; every string lives in `res/values/strings.xml`.
* The app has two locales: English (`values`, the default) and Swedish (`values-sv`). Every new string
  is added to both files in the same change.


# Architecture

The project follows Google's recommended app architecture: https://developer.android.com/topic/architecture
See ARCHITECTURE.md for the package tree and how to add a feature.

* Layers: UI (Compose + ViewModel) → optional domain (use cases) → data (repository → Room).
  Dependencies only point downwards; the UI layer never touches Room or DAOs directly.
* Unidirectional data flow: ViewModels expose a single immutable `UiState` via `StateFlow`;
  composables read state and call ViewModel functions for events. No business logic in composables.
* Repositories are the single source of truth. Their public API uses domain models from
  `core/model`, never Room entities. Mapping lives in `core/data`.
* Packages are organised by feature (`feature/<name>`), with shared code in `core/`.
  Each feature owns its navigation (`feature/<name>/navigation`), registered in `navigation/WalkLoggerNavHost.kt`.
* Screens are split into a stateful `*Route` (obtains the ViewModel, collects state) and a stateless
  `*Screen` (pure rendering, previewable and UI-testable without Hilt).
* Navigation: Navigation Compose with type-safe `@Serializable` routes. Navigation calls live in the
  UI layer, never in ViewModels. Single activity; `MainActivity` stays thin.
* Dependency injection with Hilt. Depend on interfaces (`WalkRepository`, …) so fakes can be
  swapped in for unit tests; prefer fakes over mocks.
* Async with Kotlin coroutines and `Flow` end to end. Inject dispatchers via the
  `@Dispatcher` qualifier rather than referencing `Dispatchers.IO` directly.
* Local storage: Room for records, DataStore for preferences. No other persistence mechanisms.
* Tests: ViewModel and use-case unit tests on the JVM (`src/test`) using the fakes in
  `src/test/.../testing`; Room and Compose UI tests in `src/androidTest`.
* No deprecated APIs: never use anything marked `@Deprecated`, and fix deprecation warnings you
  introduce or touch rather than suppressing them.
* No APIs that do not work for the project's minSDK: never use anything marked incompatible with the project's minSDK.
* Verify before reporting: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` must pass.
