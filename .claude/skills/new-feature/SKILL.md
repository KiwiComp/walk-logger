---
name: new-feature
description: Start work on a new feature. Ask the developer what it is, clarify until it is clear, share a plan in Swedish, and implement it once the developer confirms. Use when the developer wants to begin something new.
disable-model-invocation: true
argument-hint: [short description of the feature]
---

# New feature

There is no roadmap: features are chosen ad hoc. Find out what the developer wants now, do not
assume where the project is heading.

## 1. Ask

- If the developer described the feature when running /new-feature ($ARGUMENTS), start from that.
  Otherwise ask what the feature is about.
- Do not create, edit or move files in this step or the next two.

## 2. Clarify

- Read docs/ARCHITECTURE.md, especially "Adding a feature", before planning.
- Read the code the feature touches before asking, so questions are about real gaps, not about
  things the code already answers.
- The feature is clear when you know:
    - what the user sees and does,
    - what data is stored or changed,
    - what is out of scope.
- If anything is unclear, ask follow-up questions. Ask a few specific ones at a time, and offer
  options when there is an obvious choice. Repeat until it is clear.
- Do not fill gaps with your own assumptions. If something is a matter of taste, ask.

## 3. Plan

Share the plan in Swedish. Keep it short. Use these headings:

- **Mål**: what the feature does for the user, in one or two sentences.
- **Omfattning**: what is included and what is not.
- **Steg**: the changes in order, per layer (data, domain, UI, navigation), naming the packages
  and files that are added or changed. Follow CLAUDE.md and docs/ARCHITECTURE.md.
- **Strängar**: new user-facing strings, in English and Swedish.
- **Tester**: the unit and instrumented tests that are added or changed.
- **Gren**: a branch name following the Git section in CLAUDE.md.
- **Öppna frågor**: anything still undecided. Leave the heading out if there is nothing.

Only the plan is in Swedish. Code, comments, string keys and the rest of the conversation are not.

## 4. Confirm

- Wait for the developer to explicitly confirm the plan. Do not start on anything else.
- If the developer wants changes, update the plan and share it again.
- The confirmation is the developer's instruction to create, edit and move the files in the plan,
  and only those.

## 5. Implement

- Before changing any files:
  - Run `git status`. If there are uncommitted changes, stop and ask what to do with them.
  - `git switch main` and `git pull --ff-only`. If the pull fails, stop and report it.
  - `git switch -c <branch>` with the branch name from the plan.
- Implement the plan step by step.
- If the plan turns out to need a change (a missing file, a different approach), stop, explain it
  and wait for the developer's confirmation before continuing.
- Run the checks in `.claude/skills/verify/SKILL.md`.
- Never commit or push.

## Report

Answer the developer with:

- what was implemented, and any deviation from the plan,
- the verify results,
- what is left for the developer to do, such as manual checks on the emulator.
