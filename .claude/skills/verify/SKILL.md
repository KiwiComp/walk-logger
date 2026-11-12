---
name: verify
description: Format the code, build the app and run the style check, lint, the unit tests and, when needed, the instrumented tests, then report the results. Use before reporting a code change as done, before a pull request, or when the developer asks to verify the project.
---

# Verify

Run the checks below from the project root, in this order. Stop at the first failure and report
it. Never weaken, ignore or skip a test or a style rule to make a check pass.

## Checks

1. `./gradlew spotlessApply`: formats the code. This is the only fix verify makes on its own;
   list the files it changed in the report.
2. `./gradlew spotlessCheck`: ktlint and the Compose rules. Violations that formatting can't fix
   are failures.
3. `./gradlew assembleDebug`
4. `./gradlew testDebugUnitTest`
5. `./gradlew lintDebug`
6. `./gradlew connectedAndroidTest`, only when androidTest code or the code it covers changed:
    - Find the changed files with `git diff --name-only main...HEAD` and `git diff --name-only HEAD`,
      plus untracked files from `git status --porcelain`.
    - Instrumented tests are needed when a file under `app/src/androidTest/` changed, or when a
      changed main file is in `core/database`, `core/data`, `core/ui/component`, `navigation/`,
      or a feature's `navigation/` or `component/` package.
    - Run `adb devices`. If no device or emulator is listed, do not run the task and say so
      explicitly. Never claim that instrumented tests pass when they were not run.
    - The instrumented tests find nodes by English text, so the device must use English as its
      system language. If it does not, say so instead of reporting the text failures as bugs.

## CI

- `.github/workflows/pull-request-checks.yml` runs checks 2–5 on every pull request to `main`.
- `.github/workflows/instrumented-tests.yml` runs check 6 on an emulator, only when started by hand
  with `confirm` set to `run`. Never start it (`gh workflow run`, or any other way) unless the
  developer explicitly asks for it in the current message: each run costs Actions minutes.

## When a check fails

- Show the part of the output that explains the failure: the compiler error, the failing test
  with its assertion message, or the ktlint or lint errors. Not the whole log.
- Do not start fixing anything unless the developer asked for it.

## Report

Answer the developer with:

- each task that was run and its result (passed, failed, number of tests if Gradle prints it),
- each task that was not run, and why,
- lint warnings only if there are new ones in files changed on the branch.
