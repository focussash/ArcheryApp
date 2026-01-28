# Project Rules

## Environment
- OS: Windows 11 (PowerShell)
- Language: Kotlin
- UI Framework: Jetpack Compose (Material3)

## Architecture Principles
- **Favor modular code over monolithic**: Separate concerns into distinct modules/packages. Use interfaces, dependency injection, and clean architecture patterns where appropriate within Android constraints.
- **Single Responsibility**: Each class/file should have one clear purpose.
- **Testability**: Design components to be independently testable.

## Workflow Rules
1. **Always refer to `memory-claude/app-design.md`** before writing any code to ensure alignment with planned features.
2. **Document ALL work in `memory-claude/architecture.md`** - this is CRITICAL for session continuity:
   - Planned implementation steps from implementation-plan.md
   - Bug fixes and UX improvements requested by user
   - Any modifications, even small ones
   - New features not originally in the plan
   - Include: what was done, files created/modified, validation results

   **⚠️ MANDATORY: Update architecture.md IMMEDIATELY after completing each step, BEFORE moving to the next task.**
   - Do NOT batch documentation updates
   - Do NOT wait until end of session
   - Each step's documentation must be written right after the code is complete
   - This ensures session continuity even if the conversation is interrupted

3. **Update `memory-claude/implementation-plan.md`** when adding new features or steps not originally planned.
4. **Follow `memory-claude/implementation-plan.md`** for step-by-step implementation guidance.
5. **⛔ MANDATORY VALIDATION after each step:**
   - Run `./gradlew assembleDebug` (or appropriate build command)
   - Show the FULL build output (success message with task count and time)
   - If tests exist for the step, run them and show output
   - NEVER proceed without showing validation evidence to user
   - This is NOT optional - skipping validation is a workflow violation

6. **⛔ STOP - User approval required between steps:**
   - After completing a step AND showing validation, STOP and wait
   - Explicitly ask: "Step X complete. Ready for next step?"
   - Do NOT proceed until user responds with approval
   - NEVER batch multiple steps without explicit permission
   - Skipping this checkpoint is a workflow violation

7. **Documentation checkpoint before proceeding:**
   - After each step, verify architecture.md has been updated
   - Only then ask user for permission to continue to next step

8. **No batching without permission:**
   - Each implementation step must be completed, validated, and approved individually
   - Only batch steps if user explicitly says "do steps X through Y" or similar
   - When in doubt, stop and ask

## Key Files
- `memory-claude/app-design.md` - Feature specifications and requirements
- `memory-claude/implementation-plan.md` - Detailed implementation steps with tests
- `memory-claude/architecture.md` - Progress documentation and completed work
