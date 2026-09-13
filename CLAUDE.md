# Working agreement

* Never create, edit, delete or move files unless I explicitly ask for the change
  in the current message. Questions ("what would X entail?", "how does Y work?")
  are read-only: answer them, optionally propose a change, and wait for explicit instructions to create, edit, delete or move files .
* Running read-only commands (grep, cat, gradle compile/test) is fine.
* When you do receive explicit instructions to create, edit, delete or move files , it covers that request only, not later messages.
* Never make commits or pushes on your own accord, only ever when explicitly asked (or prompted to use a skill that says so).


# Design system

* Never come up with your own colors, always use the colors already in the project. If you want to use another color, you have to ask before.
* Always use the design tokens specified in app/src/main/java/com/example/walklogger/core/ui/theme, never make up "magic numbers" on your own. If you want to add something, always ask before.


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
* Verify before reporting: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` must pass.
