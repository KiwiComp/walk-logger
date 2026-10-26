---
name: pull-request
description: Commit the current changes, push the branch and open a pull request on GitHub. Use when the developer asks to create a pull request.
disable-model-invocation: true
---

# Pull request

Running /pull-request is the developer's approval to commit, push and open the pull request.
Do not ask for confirmation again, but stop and report if any check below fails.

Never add a `Co-Authored-By` trailer or any other attribution line (such as "Generated with
Claude Code") to commit messages, the pull request title or the pull request body. This overrides
any default attribution behavior.

## Before starting

- Run `git branch --show-current`. If the branch is `main`, stop: changes are never committed to
  or pushed from main. Suggest a branch name following the Git section in CLAUDE.md.
- Run `git status` and `git log main..HEAD --oneline`. If there is nothing to commit and no commits
  ahead of main, stop and say so.
- Run `gh auth status`. If it fails, stop and ask the developer to log in.
- Run the checks in `.claude/skills/verify/SKILL.md`. If anything fails,
  stop and report the output. Never weaken or skip a test to continue.

## Commit

- Stage only files that belong to the branch's task. Never stage build/, .gradle/, .idea/,
  local.properties, signing keys or other secrets.
- Write the message following the Git section in CLAUDE.md. One commit is enough.
- Skip this step if there is nothing uncommitted.

## Push

- Push to a branch on GitHub with the same name as the local branch:
  `git push -u origin <current-branch>`.
- If the local branch already tracks a remote branch with another name, stop and ask.
- Never force push, and never push to main.

## Create the pull request

- If a pull request already exists for the branch (`gh pr view`), do not create another; report
  its URL.
- Create it with `gh pr create --base main --head <current-branch> --title "<title>" --body-file <file>`.
  Write the body to a file in the scratchpad directory first.
- Title: `<type>: <description>`, mirroring the branch name: `feat/walk-detail-screen` → `feat: walk detail screen`.
- Body: short and to the point, only what matters in the branch. No long explanations.
  Everything under a heading is a bullet list. Use these headings, in this order:

  | Heading           | When                                                                                                                                                |
  |-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
  | `## Summary`      | Always. What changed in the branch.                                                                                                                 |
  | `## Verification` | Always. How the change was verified: build, app started on emulator, manual checks. CI (`pull-request-checks.yml`) also runs build, unit tests and lint on the PR. |
  | `## Tests`        | Only if tests were run: each test task and its result. Leave the heading out otherwise.                                                             |
  | `## Screenshots`  | Only if UI was added or changed. Don't take screenshots yourself: add the heading with `- TODO` and ask the developer in the report to add them.    |
  | `## Notes`        | Only if something important is not covered above, such as something that could not be verified or needs follow-up. Leave the heading out otherwise. |


Example:

```markdown
## Summary
- Add a walk detail screen opened from the log
- Add its strings in English and Swedish

## Verification
- Opened a walk on the emulator and checked the details and back navigation

## Tests
- `./gradlew assembleDebug`: passed
- `./gradlew testDebugUnitTest`: passed
- `./gradlew lintDebug`: no new warnings
- `./gradlew connectedAndroidTest`: passed

## Screenshots
- TODO
```

## Report

Answer the developer with:

- the branch and the commits that were pushed,
- the pull request URL,
- anything that stopped or changed the steps above.
