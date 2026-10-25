# WalkLogger architecture

Follows Google's [recommended app architecture](https://developer.android.com/topic/architecture):
UI layer → (optional) domain layer → data layer, with unidirectional data flow and
Kotlin `Flow` end to end. Packages are organised **by feature**, with shared code under `core/`.

```
com.example.walklogger
├── WalkLoggerApplication.kt   @HiltAndroidApp – root of the DI graph
├── MainActivity.kt            single activity, hosts WalkLoggerApp()
│
├── navigation/                app-level NavHost, bottom bar, top-level destinations;
│                              WalkLoggerAppViewModel reopens the timer when the app starts mid-walk
│
├── feature/                   one package per tab/flow; layout inside a feature:
│   │                            <root>       Screen/Route, ViewModel, UiState of a single-screen feature
│   │                            <screen>/    the same trio per screen when a feature has several,
│   │                                         plus pieces only that screen uses
│   │                            component/   pieces shared by more than one file in the feature
│   │                            preview/     fixed content for @Previews only
│   │                            navigation/  type-safe @Serializable route + NavGraphBuilder extension
│   ├── log/                   LogViewModel + LogUiState; LogRoute/LogScreen composed from
│   │                          component/ (WeekStrip, WalkTimeline, WalkCard)
│   ├── logwalk/               the flow behind AddWalkButton, one sub-package per screen.
│   │                          newwalk/ NewWalkViewModel + NewWalkScreen (handlers, equipment, start / log past walk);
│   │                          activewalk/ ActiveWalkViewModel + ActiveWalkScreen (the timer: elapsed time from the
│   │                          stored start, quick note, end walk; system back is swallowed);
│   │                          finishwalk/ FinishWalkViewModel + FinishWalkScreen (rating, optional ratings, equipment, weather, behaviour,
│   │                          medical, notes for the just-finished walk, with an "Edit times" toggle);
│   │                          pastwalk/ LogPastWalkViewModel + LogPastWalkScreen (date/times, handlers, rating, optional ratings,
│   │                          equipment, weather, behaviour, medical, notes); the same screen edits a logged walk when the route
│   │                          carries a walkId, pushed on top of that walk's detail screen so saving or closing returns there;
│   │                          component/ holds the form pieces the screens share (FormSection, SelectableChip,
│   │                          BehaviourSection, MedicalSection, AddNameDialog and its AddHandlerDialog /
│   │                          AddBehaviourIssueDialog / AddMedicalConcernDialog wrappers, RatingScale, OptionalRatingsSection, WeatherSection, WhenSection, Labels);
│   │                          the chosen day travels as a route argument into both form ViewModels
│   ├── calendar/              CalendarViewModel + CalendarUiState (month, selected day, per-day walk count and
│   │                          rounded rating); CalendarScreen composed from component/ (RatingLegend in the header,
│   │                          MonthGrid with cells tinted by rating and selection ringed in Accent, CalendarWalkList);
│   │                          a walk row opens the walk's detail screen, the dashed button logs a walk on that day
│   ├── walkdetail/            WalkDetailViewModel + WalkDetailScreen: the read-only view of a logged walk (when, handler, dog,
│   │                          equipment, rating, optional ratings, weather, behaviour, medical, notes; empty sections say so),
│   │                          opened from a walk row on Log or Calendar; Edit pushes LogPastWalkScreen, Discard deletes the walk
│   │                          (DiscardWalkDialog confirms first) and back returns to the tab; component/ holds the read-only
│   │                          pieces (DetailField, DetailPlaceholder, RatingSummary, OptionalRatingRow, NotedItem)
│   ├── patterns/              placeholder, same shape as calendar
│   └── dog/                   DogViewModel + DogScreen (profile header, dog list, add-dog CTA) composed from
│                              component/ (DogList);
│                              registerdog/ RegisterDogViewModel + RegisterDogScreen (name, breed, birth date, weight)
│
├── domain/                    optional use cases (pure Kotlin, injectable)
│
├── core/
│   ├── model/                 domain models (Walk, Dog, BehaviourIssue, MedicalConcern) – no Android deps
│   ├── data/                  WalkRepository / DogRepository / HandlerRepository / BehaviourIssueRepository /
│   │                          MedicalConcernRepository interfaces + Room-backed implementations, mappers
│   ├── database/              Room database, DAOs, entities, type converters; walks join behaviour issues and medical
│   │                          concerns through walk_behaviour_issues / walk_medical_concerns; the defaults are
│   │                          seeded on create (SeedCallback) and on the migration that added each table;
│   │                          seeded defaults carry a `key` that Labels.kt maps to a string resource (user-added
│   │                          rows show their name), and `isHidden` soft-deletes without touching walk history
│   ├── datastore/             Preferences DataStore (active dog id)
│   ├── common/di/             injectable coroutine dispatchers and Clock
│   └── ui/                    theme (fixed palette incl. RatingTints/Accent, AppSpacing/AppRadius/AppSize tokens)
│                              + shared composables (WalkLoggerHeader, HeaderIconButton, HeaderTextButton, DogBadge,
│                              SectionLabel, SegmentedToggle, DashedButton, AddWalkButton, DayHeading, EquipmentChip,
│                              AppButton, AppTextField, AppPickerField, AppDateField, EmptyTabContent)
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
  `LogPastWalkViewModel` take their `date` (and the walk screens their `walkId`) through an `@AssistedFactory` rather than `SavedStateHandle.toRoute()`, because
  `SavedStateHandle` needs Android's `Bundle` and would push ViewModel tests off the JVM.
- **Pushed screens hide the bottom bar.** Anything that is not a `TopLevelDestination`
  (new walk, log past walk, walk detail, register dog) is a full-screen task with a leading back chevron
  (or close cross for forms) in the header. The timer and after-walk screens have neither: a
  timed walk is stored the moment it starts, ended by "End walk" and already logged before the
  form opens, so there is nothing to cancel. When a flow finishes, `popToTopLevel()` returns to
  whichever tab (Log or Calendar) it was started from.
- **Each feature owns its navigation.** Add a `@Serializable` route + `NavGraphBuilder.xxxScreen()`
  in `feature/<name>/navigation`, then register it in `WalkLoggerNavHost`.
- **One file per composable piece.** A feature's building blocks (`WalkCard.kt`, `WeekStrip.kt`, …)
  each get their own file with their own `@Preview`. A piece used by more than one file lives in the
  feature's `component/`; a piece used by a single screen sits beside that screen. Preview fixtures
  live in `preview/`. The piece other files call is `internal`; helpers and size constants that only
  that file needs are `private`. Only the `Screen`/`Route` and `core/ui` components are public.
- **Dimensions come from tokens.** Padding and gaps use `AppSpacing`, corners use `AppRadius`,
  sizes shared by several components use `AppSize`. Component-specific sizes (a dot, a cell
  height) are named `private val`s in the component's file, not tokens.

## Adding a feature

1. `feature/<name>/` → `UiState`, `ViewModel` (`@HiltViewModel`), `Route`/`Screen`, `navigation/`;
   shared pieces in `component/`, preview fixtures in `preview/`, one sub-package per screen if there are several.
2. If it needs new data, add methods to a repository interface in `core/data` and implement them.
   A schema change bumps the Room version with an `AutoMigration`, exports a new `app/schemas/*.json`
   and gets a case in `WalkLoggerDatabaseMigrationTest`.
3. Register the destination in `WalkLoggerNavHost` (and `TopLevelDestination` if it gets a tab).
4. Write a ViewModel test using the fakes in `src/test/.../testing`.

## Next steps (not yet implemented)

- Pausing a timed walk; the timer screen only ends it.
- A foreground service so the walk survives the process being killed with a notification to end it
  from (elapsed time is already derived from the stored start, so the timer itself is correct after a restart).
- Bundle a condensed display font (see `Type.kt`).
- Split `core/*` and `feature/*` into Gradle modules once the app grows.
