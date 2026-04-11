# CLAUDE.md — Binding Instructions for Claude Code

These instructions govern ALL work in this repository. They OVERRIDE default behavior. Follow them exactly.

## FIRST STEP — EVERY Session

**ALWAYS** read the `memory-claude/` folder (`DEVELOPMENT_PLAN.md`, `achievements.md`, `architecture.md`, `app-design.md`) BEFORE writing any code or making any decisions. No exceptions.

## Environment & Tech Stack

- **OS:** Windows 11 (bash shell via Claude Code; PowerShell also available)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material3)
- **Build System:** Gradle (Kotlin DSL) — primary command: `./gradlew assembleDebug`
- **Target Platform:** Android
- **Persistence:** Room (2.6.1) with KSP annotation processing
- **Key libraries:** OpenCV (target/arrow detection), kizitonwose Calendar 2.6.2, Vico charts 1.13.1, kotlinx.serialization 1.6.3
- **Planned (later phases):** Nordic Android BLE (Phase 3 — M5Stick integration), ML Kit Pose Detection (Phase 4 — form analysis)

## Architecture Principles

- **Favor modular code over monolithic.** Separate concerns into distinct modules/packages. Use interfaces, dependency injection, and clean-architecture patterns where appropriate within Android constraints.
- **Single Responsibility.** Each class/file should have one clear purpose.
- **Testability.** Design components to be independently testable.

## Development Rules

1. **ALWAYS follow the plan.** Refer to `memory-claude/DEVELOPMENT_PLAN.md` before writing ANY code. The plan defines implementation order and specifications. NEVER deviate without approval.

2. **NEVER modify `DEVELOPMENT_PLAN.md` without explicit user permission.** Even with auto-accept enabled, you MUST ask first. Never silently change the plan.

3. **ALWAYS sync agreed discussions into the plan.** When the user agrees to changes that deviate from or add to `DEVELOPMENT_PLAN.md`, you MUST immediately update the plan file. Agreed-upon changes must NEVER remain only in conversation.

4. **Modularity is mandatory.** NEVER write monolith code. Split functionality into small, focused modules. Each file = single responsibility.

## Documentation Updates

After ANY code changes, update relevant documentation files (`achievements.md`, `architecture.md`, `DEVELOPMENT_PLAN.md`) before ending the session. Do NOT wait to be prompted.

## Progress Tracking — CRITICAL, NON-NEGOTIABLE

- **After EVERY substep** → IMMEDIATELY update `achievements.md` with what was accomplished. Do NOT wait until a full subproject is complete.
- **Before EVERY new piece of work** → Re-read `DEVELOPMENT_PLAN.md` AND `achievements.md` to stay aligned with the plan and aware of current progress.
- **One subproject at a time.** Complete and validate one subproject, then STOP. Wait for user confirmation before starting the next. NEVER batch multiple steps without explicit permission.
- **Validate before proceeding.** After implementing a subproject, run `./gradlew assembleDebug` and show the FULL build output (task count, time, success/failure) to the user. If tests exist, run them and show output. NEVER proceed until validation passes.
- **Document everything.** Include build results, file changes (created/modified), any deviations from the plan, and any issues encountered.
- **Minor vs Major steps** (as defined in `DEVELOPMENT_PLAN.md`): Minor = emulator/build validation only. Major = user must additionally test on a real phone before granting approval.

## Environment Assumptions

Use the user's existing Android/Gradle setup. Do NOT install new toolchains, SDK versions, or change Java/Kotlin/Gradle versions unless asked. Ask which setup to use if unclear.

## Read-Only Mode

When asked to 'diagnose', 'audit', 'review', or 'report', do NOT edit any code files. Only read files and produce a written report/document unless explicitly told to implement fixes.

## Permissions

- **Bash commands:** Run ALL freely — `./gradlew` builds, tests, git, adb, etc.
- **File edits:** Proceed freely for implementation work.
- **NEVER install, uninstall, or modify ANY dependencies** (Gradle deps, version-catalog entries, plugins, KSP processors) without EXPLICITLY asking the user first. If a missing library blocks a build, report it and ask the user how to proceed.
- **NEVER delete files or folders** without asking the user first.

## Key Files

- `memory-claude/app-design.md` — Feature specifications and requirements
- `memory-claude/DEVELOPMENT_PLAN.md` — Detailed, sequential implementation steps with per-step tests
- `memory-claude/achievements.md` — Progress log: completed work, session notes, test results, historical issues
- `memory-claude/architecture.md` — Static reference: project structure, architectural decisions, known issues to revisit
