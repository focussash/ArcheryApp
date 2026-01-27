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
3. **Update `memory-claude/implementation-plan.md`** when adding new features or steps not originally planned.
4. **Follow `memory-claude/implementation-plan.md`** for step-by-step implementation guidance.
5. **Show test results after each step**: Provide visual evidence where possible (screenshots, logs, etc.). For tests that only show success/failure, a simple confirmation is sufficient.
6. **Step-by-step approval required**:
   - Inform user after completing each step
   - Wait for user permission before proceeding to next step
   - During a single step, proceed freely with all necessary edits without asking permission

## Key Files
- `memory-claude/app-design.md` - Feature specifications and requirements
- `memory-claude/implementation-plan.md` - Detailed implementation steps with tests
- `memory-claude/architecture.md` - Progress documentation and completed work
