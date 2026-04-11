# CLAUDE.md — Binding Instructions for Claude Code

These instructions govern ALL work in this repository. They OVERRIDE default behavior. Follow them exactly.

## FIRST STEP — EVERY Session

**ALWAYS** read the `claude-memory/` folder (`DEVELOPMENT_PLAN.md`, `achievements.md`, `architecture.md`) BEFORE writing any code or making any decisions. No exceptions.

## Development Rules

1. **ALWAYS follow the plan.** Refer to `DEVELOPMENT_PLAN.md` before writing ANY code. The plan defines implementation order and specifications. NEVER deviate without approval.

2. **NEVER modify `DEVELOPMENT_PLAN.md` without explicit user permission.** Even with auto-accept enabled, you MUST ask first. Never silently change the plan.

3. **ALWAYS sync agreed discussions into the plan.** When the user agrees to changes that deviate from or add to `DEVELOPMENT_PLAN.md`, you MUST immediately update the plan file. Agreed-upon changes must NEVER remain only in conversation.

4. **Modularity is mandatory.** NEVER write monolith code. Split functionality into small, focused modules. Each file = single responsibility.

## Documentation Updates

After ANY code changes, update relevant documentation files (achievements, memory files, development plans) before ending the session. Do NOT wait to be prompted.

## Progress Tracking — CRITICAL, NON-NEGOTIABLE

- **After EVERY substep** → IMMEDIATELY update `achievements.md` with what was accomplished. Do NOT wait until a full subproject is complete.
- **Before EVERY new piece of work** → Re-read `DEVELOPMENT_PLAN.md` AND `achievements.md` to stay aligned with the plan and aware of current progress.
- **One subproject at a time.** Complete and validate one subproject, then STOP. Wait for user confirmation before starting the next.
- **Validate before proceeding.** After implementing a subproject, run tests and show results to the user. NEVER proceed until validation passes.
- **Document everything.** Include test counts, file changes, any deviations from the plan (e.g., using `cnlunar` instead of `sxtwl`), and any issues encountered.

## Environment Assumptions

Use the user's existing development environment (conda, venv, PlatformIO, etc.) — do NOT try to install new toolchains or interpreters unless asked. Ask which environment to use if unclear.

## WSL File Editing

The MASS C++ codebase lives on the WSL filesystem at `~/projects/MASS/` (accessible from Windows at `//wsl.localhost/Ubuntu-22.04/home/zijie/projects/MASS/`). **Edit and Write tools may trigger permission prompts** for files on the WSL filesystem even with "Accept Edits" mode enabled, because they are outside the project directory.

**Workaround**: For files on the WSL filesystem, prefer writing edits via Bash using Python scripts:
1. Write a Python patch script to the Windows project directory (`E:\Random\Pet_projects\RL\scripts\`)
2. Run it via `wsl.exe -d Ubuntu-22.04 -u zijie -- bash -c "python3 /mnt/e/Random/Pet_projects/RL/scripts/patch_foo.py"`
3. Clean up the patch script afterward

For **new files** on the WSL side, the Write tool via `//wsl.localhost/...` paths generally works. The issue is mainly with Edit on existing WSL files.

## Read-Only Mode

When asked to 'diagnose', 'audit', 'review', or 'report', do NOT edit any code files. Only read files and produce a written report/document unless explicitly told to implement fixes.

## Permissions

- **Bash commands**: Run ALL freely — builds, tests, uploads, serial monitor, git, etc.
- **File edits**: Proceed freely for implementation work.
- **NEVER install, uninstall, or modify ANY packages/dependencies** (pip install, npm install, scoop install, etc.) without EXPLICITLY asking the user first. This applies to ALL package managers and ALL environments — including PlatformIO's internal Python env. If a missing module blocks a build, report it and ask the user how to proceed.
- **NEVER delete files or folders** without asking the user first.

