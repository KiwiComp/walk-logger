# WalkLogger architecture

Follows Google's [recommended app architecture](https://developer.android.com/topic/architecture):
UI layer → (optional) domain layer → data layer, with unidirectional data flow and
Kotlin `Flow` end to end. Packages are organised **by feature**, with shared code under `core/`.

```
com.example.walklogger
├── WalkLoggerApplication.kt   @HiltAndroidApp – root of the DI graph
├── MainActivity.kt            single activity, hosts WalkLoggerApp()
│
├── navigation/                app-level NavHost, bottom bar, top-level destinations
│
├── feature/                   one package per tab/screen (UI only so far – no ViewModels yet)
│   ├── log/                   LogScreen composed from one file per piece (WeekStrip, WalkTimeline,
│   │                          WalkCard, AddWalkPlaceholder); SampleData.kt holds static sample content
│   ├── calendar/              placeholder
│   ├── patterns/              placeholder
│   ├── dog/                   the "Sixten" tab, placeholder
│   └── */navigation/          type-safe @Serializable route + NavGraphBuilder extension
│
├── domain/                    optional use cases (pure Kotlin, injectable)
│
├── core/
│   ├── model/                 domain models (Walk) – no Android deps
│   ├── data/                  WalkRepository interface + Room-backed implementation, mappers
│   ├── database/              Room database, DAO, entities
│   ├── common/di/             injectable coroutine dispatchers
│   └── ui/                    theme (fixed palette, square shapes, AppSpacing/AppRadius tokens)
│                              + shared composables (WalkLoggerHeader, DogBadge, SectionLabel, EmptyTabContent)
```

## Rules of thumb

- **State down, events up.** Composables read a `UiState` and call ViewModel functions; they never
  hold business state themselves.
- **`*Route` vs `*Screen`.** `Route` composables obtain the ViewModel and collect state; `Screen`
  composables are stateless so they can be previewed and UI-tested without Hilt.
- **Repository is the single source of truth.** ViewModels never touch Room or DataStore directly.
- **Depend on interfaces.** `WalkRepository` has a fake in `src/test` so ViewModels are
  unit-testable on the JVM.
- **Each feature owns its navigation.** Add a `@Serializable` route + `NavGraphBuilder.xxxScreen()`
  in `feature/<name>/navigation`, then register it in `WalkLoggerNavHost`.
- **One file per composable piece, flat in the feature package.** A feature's building blocks
  (`WalkCard.kt`, `WeekStrip.kt`, …) sit beside its `Screen`, each with its own `@Preview`.
  The piece other files call is `internal`; helpers and size constants that only that file
  needs are `private`. Only the `Screen`/`Route` and `core/ui` components are public.
- **Dimensions come from tokens.** Padding and gaps use `AppSpacing`, corners use `AppRadius`.
  Component-specific sizes (a dot, a cell height) are named `private val`s in the component's
  file, not spacing tokens.

## Adding a feature

1. `feature/<name>/` → `UiState`, `ViewModel` (`@HiltViewModel`), `Route`/`Screen`, `navigation/`.
2. If it needs new data, add methods to a repository interface in `core/data` and implement them.
3. Register the destination in `WalkLoggerNavHost` (and `TopLevelDestination` if it gets a tab).
4. Write a ViewModel test using the fakes in `src/test/.../testing`.

## Next steps (not yet implemented)

- Add ViewModels per feature and replace the static sample content in `feature/log/SampleData.kt`.
- Bundle a condensed display font (see `Type.kt`).
- Add `DataStore` for user preferences (units, update interval).
- Split `core/*` and `feature/*` into Gradle modules once the app grows.
