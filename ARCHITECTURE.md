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
├── feature/                   one package per tab/screen
│   ├── log/                   LogViewModel + LogUiState; LogRoute/LogScreen composed from one file per
│   │                          piece (WeekStrip, WalkTimeline, WalkCard, AddWalkButton);
│   │                          LogPreviewData.kt holds fixed content for @Previews only
│   ├── logwalk/               the flow behind AddWalkButton, one package for all its screens.
│   │                          NewWalkViewModel + NewWalkScreen (handlers, equipment, start / log past walk);
│   │                          LogPastWalkViewModel + LogPastWalkScreen (date/times, handlers, rating,
│   │                          equipment, weather, notes) composed from WhenSection, RatingScale, WeatherRow;
│   │                          the same screen edits a logged walk when the route carries a walkId;
│   │                          the chosen day travels as a route argument into both ViewModels
│   ├── calendar/              placeholder; ViewModel only feeds the active dog's name to the header badge
│   ├── patterns/              placeholder, same shape as calendar
│   ├── dog/                   DogViewModel + DogUiState (profile header, dog list, add-dog CTA);
│   │                          RegisterDogViewModel + RegisterDogScreen (name, breed, birth date, weight)
│   └── */navigation/          type-safe @Serializable route + NavGraphBuilder extension
│
├── domain/                    optional use cases (pure Kotlin, injectable)
│
├── core/
│   ├── model/                 domain models (Walk, Dog) – no Android deps
│   ├── data/                  WalkRepository / DogRepository / HandlerRepository interfaces
│   │                          + Room-backed implementations, mappers
│   ├── database/              Room database, DAOs, entities, type converters
│   ├── datastore/             Preferences DataStore (active dog id)
│   ├── common/di/             injectable coroutine dispatchers and Clock
│   └── ui/                    theme (fixed palette, AppSpacing/AppRadius/AppSize tokens)
│                              + shared composables (WalkLoggerHeader, HeaderIconButton, DogBadge, SectionLabel,
│                              SegmentedToggle, DashedButton, AppButton, AppTextField, AppPickerField,
│                              AppDateField, EmptyTabContent)
```

## Rules of thumb

- **State down, events up.** Composables read a `UiState` and call ViewModel functions; they never
  hold business state themselves.
- **`*Route` vs `*Screen`.** `Route` composables obtain the ViewModel and collect state; `Screen`
  composables are stateless so they can be previewed and UI-tested without Hilt.
- **Repository is the single source of truth.** ViewModels never touch Room or DataStore directly.
- **Depend on interfaces.** Every repository has a fake in `src/test` so ViewModels are
  unit-testable on the JVM.
- **Route arguments reach the ViewModel by assisted injection.** `NewWalkViewModel` and
  `LogPastWalkViewModel` take their `date` through an `@AssistedFactory` rather than `SavedStateHandle.toRoute()`, because
  `SavedStateHandle` needs Android's `Bundle` and would push ViewModel tests off the JVM.
- **Pushed screens hide the bottom bar.** Anything that is not a `TopLevelDestination`
  (new walk, log past walk, register dog) is a full-screen task with a leading back chevron
  (or close cross for forms) in the header.
- **Each feature owns its navigation.** Add a `@Serializable` route + `NavGraphBuilder.xxxScreen()`
  in `feature/<name>/navigation`, then register it in `WalkLoggerNavHost`.
- **One file per composable piece, flat in the feature package.** A feature's building blocks
  (`WalkCard.kt`, `WeekStrip.kt`, …) sit beside its `Screen`, each with its own `@Preview`.
  The piece other files call is `internal`; helpers and size constants that only that file
  needs are `private`. Only the `Screen`/`Route` and `core/ui` components are public.
- **Dimensions come from tokens.** Padding and gaps use `AppSpacing`, corners use `AppRadius`,
  sizes shared by several components use `AppSize`. Component-specific sizes (a dot, a cell
  height) are named `private val`s in the component's file, not tokens.

## Adding a feature

1. `feature/<name>/` → `UiState`, `ViewModel` (`@HiltViewModel`), `Route`/`Screen`, `navigation/`.
2. If it needs new data, add methods to a repository interface in `core/data` and implement them.
   A schema change bumps the Room version with an `AutoMigration`, exports a new `app/schemas/*.json`
   and gets a case in `WalkLoggerDatabaseMigrationTest`.
3. Register the destination in `WalkLoggerNavHost` (and `TopLevelDestination` if it gets a tab).
4. Write a ViewModel test using the fakes in `src/test/.../testing`.

## Next steps (not yet implemented)

- `NewWalkScreen`: "Start walk" (timer screen) is still a no-op;
  `WalkRepository.startWalk(dogId, handlers, equipment)` is ready for it.
- Bundle a condensed display font (see `Type.kt`).
- Split `core/*` and `feature/*` into Gradle modules once the app grows.
