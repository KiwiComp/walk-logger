---
name: review-changes
description: Review the changes on the current branch for important bugs and breaks of the project's rules, and report only what would stop a merge. Use when the developer asks for a review.
disable-model-invocation: true
---

# Review changes

Review the changes on the current branch and report the important problems. Do not edit, commit or
push anything, and do not run Gradle: the build, the tests and lint run in CI on every pull request.

## 1. Read

- Find the changed files with `git diff --name-only main...HEAD` and `git diff --name-only HEAD`,
  plus untracked files from `git status --porcelain`.
- If nothing has changed, say so and stop.
- Read `CLAUDE.md` and `docs/ARCHITECTURE.md`.
- Read the changes, and as much of the surrounding code as you need to understand them.

## 2. Decide what to report

Report a problem only if the answer to both questions is yes:

1. **Can you point to it?** It breaks a specific rule in the documents above, or there is a concrete
   scenario in which the app does the wrong thing. Otherwise it is a matter of taste.
2. **Is it important?** A developer would stop the merge because of it.

Report, for example:

- Bugs: crashes, wrong behavior, lost data or user input, leaked secrets.
- Layers: the UI layer touching Room, a DAO or DataStore directly; a repository API exposing Room
  entities instead of `core/model` types; a dependency that points upwards.
- Data flow: a ViewModel that navigates; a `*Screen` that receives a ViewModel; business logic in a
  composable; a ViewModel exposing mutable state or more than one `UiState`.
- Coroutines: direct use of `Dispatchers.*` instead of the `@Dispatcher` qualifier;
  `catch (e: Exception)` or `runCatching` outside `core/common/RunCatchingCancellable.kt`; a
  ViewModel write not wrapped in `runCatchingCancellable`, or one whose failure does not reset the
  busy flag and show a message through the ViewModel's `UserMessageState`.
- Room: `fallbackToDestructiveMigration`; a schema change without a version bump, an
  `AutoMigration`, a new `app/schemas/*.json` and a case in `WalkLoggerDatabaseMigrationTest`; a
  hard delete where soft delete (`isHidden`) applies.
- Persistence other than Room and DataStore.
- Navigation: a route that is not `@Serializable` or not registered in `WalkLoggerNavHost`; route
  arguments read with `SavedStateHandle.toRoute()` instead of assisted injection.
- Strings: hard-coded user-facing text in Kotlin; a new string missing from `values` or
  `values-sv`.
- Design system: a palette hue instead of an `AppTheme.colors` role, or a new colour; a magic dp
  value instead of `AppSpacing`, `AppRadius` or `AppSize` (named component-specific `private val`s
  are fine); a Material component where a `core/ui` component exists.
- Tests: a new or changed ViewModel or use case without a JVM test using the fakes in
  `src/test/.../testing`; a new repository interface without a fake; a change in `core/database` or
  `core/data` without an instrumented test; tests that cannot fail.

Do not report:

- Style, formatting or wording, including comments.
- An alternative solution that is only equally good.
- Refactorings, small performance improvements or comments.
- Small deviations without practical consequences, for example in a name or a file's placement.
- Problems in code that the changes do not touch.
- Exceptions that `docs/ARCHITECTURE.md` allows, such as the fixed stool swatches.

If you are unsure whether a problem is real, read more of the code. If you are still unsure, leave
it out, unless it could cause a crash or data loss. Then report it and mark it as unverified.

## 3. Report

Write the report in Swedish. Make one pass, and report the most serious problem first. For each
problem, give:

- the file and line,
- the problem,
- the rule it breaks, or the scenario in which it fails,
- a short suggested fix.

If there are no problems to report, answer "Inga viktiga problem hittades". Do not add praise, and
do not add smaller findings to fill the report.

Do not fix anything until the developer chooses what to fix.
