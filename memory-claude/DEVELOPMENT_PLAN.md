# Implementation Plan

This document contains detailed, sequential implementation steps for the Archery App.
Each step includes a validation test to confirm correct implementation.

**Status Legend:** `[ ]` Pending | `[~]` In Progress | `[x]` Complete

**Testing Level:**
- **Minor** = Test on emulator/virtual device only
- **Major** = Test on emulator AND real phone (user must verify)

**Workflow:**
1. Claude completes all edits for a single step without asking permission
2. Claude runs tests and shows results (visual evidence where applicable)
3. Claude informs user of completion and test results
4. User grants permission to proceed to next step

---

# Phase 1: Core Scoring & Persistence

## 1.1 Database Foundation

### Step 1.1.1: Add Room Dependencies [Minor]
**Task:** Add Room database dependencies to build.gradle
**Files:** `app/build.gradle.kts`
**Implementation:**
- Add Room runtime, ktx, and compiler dependencies
- Add kapt plugin for annotation processing
- Sync gradle

**Test:**
- [ ] Project builds successfully after adding dependencies
- [ ] Run `./gradlew build` - no dependency resolution errors

---

### Step 1.1.2: Create Session Entity [Minor]
**Task:** Define the SessionEntity data class for Room
**Files:** `app/src/main/java/com/example/archeryapp/data/local/entity/SessionEntity.kt`
**Implementation:**
```kotlin
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,  // timestamp
    val distance: String?,  // e.g., "18m", "70m"
    val bowType: String?,  // e.g., "recurve", "compound"
    val location: String?,
    val notes: String?
)
```

**Test:**
- [ ] File compiles without errors
- [ ] Entity has correct annotations (@Entity, @PrimaryKey)

---

### Step 1.1.3: Create End Entity [Minor]
**Task:** Define the EndEntity data class linked to Session
**Files:** `app/src/main/java/com/example/archeryapp/data/local/entity/EndEntity.kt`
**Implementation:**
```kotlin
@Entity(
    tableName = "ends",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class EndEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val endNumber: Int,
    val timestamp: Long,
    val notes: String?
)
```

**Test:**
- [ ] File compiles without errors
- [ ] Foreign key relationship defined correctly

---

### Step 1.1.4: Create ArrowScore Entity [Minor]
**Task:** Define the ArrowScoreEntity data class linked to End
**Files:** `app/src/main/java/com/example/archeryapp/data/local/entity/ArrowScoreEntity.kt`
**Implementation:**
```kotlin
@Entity(
    tableName = "arrow_scores",
    foreignKeys = [ForeignKey(
        entity = EndEntity::class,
        parentColumns = ["id"],
        childColumns = ["endId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ArrowScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val endId: Long,
    val arrowNumber: Int,
    val score: Int,  // 0-10
    val isX: Boolean,  // true if X (inner 10)
    val xPosition: Float,  // relative position on target (0-1)
    val yPosition: Float
)
```

**Test:**
- [ ] File compiles without errors
- [ ] Position fields allow reconstruction of arrow placement

---

### Step 1.1.5: Create Session DAO [Minor]
**Task:** Define data access object for Session operations
**Files:** `app/src/main/java/com/example/archeryapp/data/local/dao/SessionDao.kt`
**Implementation:**
- Insert session (returns id)
- Get all sessions (Flow)
- Get session by id
- Get sessions by date range
- Delete session
- Update session

**Test:**
- [ ] File compiles without errors
- [ ] All CRUD operations defined

---

### Step 1.1.6: Create End DAO [Minor]
**Task:** Define data access object for End operations
**Files:** `app/src/main/java/com/example/archeryapp/data/local/dao/EndDao.kt`
**Implementation:**
- Insert end (returns id)
- Get ends by session id (Flow)
- Get end by id
- Delete end
- Get end count for session

**Test:**
- [ ] File compiles without errors
- [ ] Query for ends by session works

---

### Step 1.1.7: Create ArrowScore DAO [Minor]
**Task:** Define data access object for ArrowScore operations
**Files:** `app/src/main/java/com/example/archeryapp/data/local/dao/ArrowScoreDao.kt`
**Implementation:**
- Insert arrow scores (batch)
- Get scores by end id (Flow)
- Delete scores by end id
- Get total score for end
- Get X count for end

**Test:**
- [ ] File compiles without errors
- [ ] Aggregation queries (total, X count) work

---

### Step 1.1.8: Create AppDatabase [Minor]
**Task:** Create Room database class with all entities and DAOs
**Files:** `app/src/main/java/com/example/archeryapp/data/local/database/AppDatabase.kt`
**Implementation:**
- @Database annotation with entities list
- Abstract DAO getters
- Singleton pattern with Room.databaseBuilder

**Test:**
- [ ] File compiles without errors
- [ ] Database version set to 1

---

### Step 1.1.9: Create Database Instance in Application [Major]
**Task:** Initialize database in ArcheryApplication
**Files:** `app/src/main/java/com/example/archeryapp/ArcheryApplication.kt`
**Implementation:**
- Lazy database initialization
- Expose database instance

**Test:**
- [ ] App launches without crash
- [ ] Database instance accessible from Application class

---

## 1.2 Repository Layer

### Step 1.2.1: Create Session Repository Interface [Minor]
**Task:** Define repository interface for session operations
**Files:** `app/src/main/java/com/example/archeryapp/domain/repository/SessionRepository.kt`
**Implementation:**
- Define suspend functions matching DAO operations
- Use domain models (not entities) in interface

**Test:**
- [ ] Interface compiles
- [ ] Uses domain models, not Room entities

---

### Step 1.2.2: Create Session Repository Implementation [Minor]
**Task:** Implement SessionRepository with Room DAO
**Files:** `app/src/main/java/com/example/archeryapp/data/repository/SessionRepositoryImpl.kt`
**Implementation:**
- Inject SessionDao
- Map between Entity and Domain models
- Implement all interface methods

**Test:**
- [ ] Implementation compiles
- [ ] Entity-to-Domain mapping works correctly

---

### Step 1.2.3: Create Score Repository Interface [Minor]
**Task:** Define repository interface for end and arrow score operations
**Files:** `app/src/main/java/com/example/archeryapp/domain/repository/ScoreRepository.kt`
**Implementation:**
- Save end with arrow scores
- Get ends for session
- Get scores for end
- Calculate session totals

**Test:**
- [ ] Interface compiles
- [ ] Covers all scoring operations needed

---

### Step 1.2.4: Create Score Repository Implementation [Minor]
**Task:** Implement ScoreRepository with Room DAOs
**Files:** `app/src/main/java/com/example/archeryapp/data/repository/ScoreRepositoryImpl.kt`
**Implementation:**
- Inject EndDao and ArrowScoreDao
- Transaction for saving end with scores
- Map between entities and domain models

**Test:**
- [ ] Implementation compiles
- [ ] Transaction ensures atomic save of end + scores

---

## 1.3 Domain Models

### Step 1.3.1: Create Session Domain Model [Minor]
**Task:** Define Session data class for domain/UI layer
**Files:** `app/src/main/java/com/example/archeryapp/domain/model/Session.kt`
**Implementation:**
```kotlin
data class Session(
    val id: Long = 0,
    val date: LocalDateTime,
    val distance: String? = null,
    val bowType: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val ends: List<End> = emptyList()
)
```

**Test:**
- [ ] Model compiles
- [ ] Uses Kotlin date/time types

---

### Step 1.3.2: Create End Domain Model [Minor]
**Task:** Define End data class for domain/UI layer
**Files:** `app/src/main/java/com/example/archeryapp/domain/model/End.kt`
**Implementation:**
```kotlin
data class End(
    val id: Long = 0,
    val endNumber: Int,
    val timestamp: LocalDateTime,
    val arrows: List<Arrow> = emptyList(),
    val notes: String? = null
) {
    val totalScore: Int get() = arrows.sumOf { it.score }
    val xCount: Int get() = arrows.count { it.isX }
}
```

**Test:**
- [ ] Model compiles
- [ ] Computed properties work correctly

---

### Step 1.3.3: Create Arrow Domain Model [Minor]
**Task:** Define Arrow data class for domain/UI layer
**Files:** `app/src/main/java/com/example/archeryapp/domain/model/Arrow.kt`
**Implementation:**
```kotlin
data class Arrow(
    val id: Long = 0,
    val arrowNumber: Int,
    val score: Int,
    val isX: Boolean,
    val position: Offset  // relative position (0-1, 0-1)
)
```

**Test:**
- [ ] Model compiles
- [ ] Position uses Compose Offset type

---

## 1.4 Save Score Flow

### Step 1.4.1: Create SaveScoreUseCase [Minor]
**Task:** Create use case for saving an end's scores
**Files:** `app/src/main/java/com/example/archeryapp/domain/usecase/SaveScoreUseCase.kt`
**Implementation:**
- Take session ID (or create new session), end number, list of arrows
- Coordinate between SessionRepository and ScoreRepository
- Return saved End with ID

**Test:**
- [ ] Use case compiles
- [ ] Handles both new session and existing session cases

---

### Step 1.4.2: Update ResultsScreen to Save Scores [Major]
**Task:** Add "Save" button to ResultsScreen that persists data
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/results/ResultsScreen.kt`
**Implementation:**
- Add SaveScoreUseCase dependency
- Add "Save End" button
- Show confirmation on successful save
- Option to continue session or start new

**Test:**
- [ ] Save button visible on ResultsScreen
- [ ] Tapping save stores data in database
- [ ] Verify with Database Inspector in Android Studio

---

### Step 1.4.3: Add Session Selection Before Scoring [Major]
**Task:** Allow user to select existing session or create new before scoring
**Files:**
- `app/src/main/java/com/example/archeryapp/ui/screens/home/HomeScreen.kt`
- `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectScreen.kt` (new)
**Implementation:**
- HomeScreen shows active session (if any) or "Start Session" button
- SessionSelectScreen: list recent sessions, create new button
- Pass selected session ID through navigation

**Test:**
- [ ] Can create new session from home
- [ ] Can continue existing session
- [ ] Session ID flows through to ResultsScreen

---

### Step 1.4.4: Add Session Delete Functionality [Minor]
**Task:** Allow users to delete sessions from SessionSelectScreen
**Files:**
- `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectScreen.kt`
- `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectViewModel.kt`
**Implementation:**
- Swipe-to-delete gesture on session cards (swipe left)
- Delete icon with red background during swipe
- Refresh session list after deletion
- Cascade delete removes associated ends and arrow scores

**Test:**
- [x] Swipe left on session card shows delete indicator
- [x] Completing swipe deletes session
- [x] Session list refreshes after deletion

---

## 1.5 History List

### Step 1.5.1: Create HistoryScreen [Major]
**Task:** Create basic history screen showing list of sessions
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/history/HistoryScreen.kt`
**Implementation:**
- LazyColumn of session cards
- Each card shows: date, end count, total score, average
- Tap to expand and see ends
- Uses SessionRepository to load data

**Test:**
- [x] Screen displays saved sessions
- [x] Sessions ordered by date (newest first)
- [x] End details visible on expand

---

### Step 1.5.2: Add History to Navigation [Minor]
**Task:** Add HistoryScreen to app navigation
**Files:** `app/src/main/java/com/example/archeryapp/ui/navigation/AppNavigation.kt`
**Implementation:**
- Add history route
- Add navigation from HomeScreen to HistoryScreen

**Test:**
- [x] Can navigate to History from Home
- [x] Back navigation works correctly

---

### Step 1.5.3: Create Bottom Navigation Bar [Major]
**Task:** Add bottom nav for main app sections
**Files:**
- `app/src/main/java/com/example/archeryapp/ui/components/BottomNavBar.kt` (new)
- `app/src/main/java/com/example/archeryapp/ui/navigation/AppNavigation.kt`
**Implementation:**
- Bottom nav with: Score, History, Settings
- Integrate with NavController
- Show on main screens only

**Test:**
- [x] Bottom nav visible on Home and History
- [x] Tapping icons switches screens
- [x] Current screen highlighted

---

## 1.6 Detection Improvements

### Step 1.6.1: Analyze Current Detection Issues [Major]
**Task:** Debug and document current auto-detection problems
**Files:** Analysis only (document in architecture.md)
**Implementation:**
- Test with multiple target images
- Log detection parameters and results
- Identify failure patterns

**Test:**
- [ ] Documented at least 3 failure cases
- [ ] Root causes identified

---

### Step 1.6.2: Improve Target Detection Parameters [Major]
**Task:** Tune OpenCV parameters for better target detection
**Files:** `app/src/main/java/com/example/archeryapp/detection/opencv/OpenCvTargetDetector.kt`
**Implementation:**
- Adjust Hough Circle parameters based on analysis
- Improve color validation thresholds
- Add preprocessing steps if needed

**Test:**
- [ ] Detection rate improved on test images
- [ ] Document before/after success rates

---

### Step 1.6.3: Improve Arrow Detection Parameters [Major]
**Task:** Tune OpenCV parameters for better arrow detection
**Files:** `app/src/main/java/com/example/archeryapp/detection/opencv/OpenCvArrowDetector.kt`
**Implementation:**
- Adjust Hough Line parameters
- Improve arrow grouping logic
- Better handling of arrow nocks vs tips

**Test:**
- [ ] Arrow detection rate improved
- [ ] Fewer false positives (detecting non-arrows)

---

### Step 1.6.4: Add Detection Confidence Feedback [Major]
**Task:** Show user confidence level of detection
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/review/ReviewScreen.kt`
**Implementation:**
- Display overall confidence score
- Color-code arrows by confidence (green/yellow/red)
- Suggest manual edit if confidence low

**Test:**
- [ ] Confidence score visible on ReviewScreen
- [ ] Low-confidence arrows visually distinct

---

---

# Phase 2: Progress Tracking & Statistics

## 2.1 Calendar View

### Step 2.1.1: Add Calendar Library Dependency [Minor]
**Task:** Add kizitonwose calendar library
**Files:** `app/build.gradle.kts`
**Implementation:**
- Add calendar compose dependency
- Sync gradle

**Test:**
- [ ] Project builds with calendar dependency

---

### Step 2.1.2: Create CalendarScreen [Major]
**Task:** Create monthly calendar view showing shooting days
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/history/CalendarScreen.kt`
**Implementation:**
- Monthly calendar grid
- Highlight days with sessions
- Color intensity based on performance
- Tap day to show sessions

**Test:**
- [ ] Calendar renders current month
- [ ] Days with sessions are highlighted
- [ ] Can navigate between months

---

### Step 2.1.3: Create Day Detail View [Major]
**Task:** Show sessions for selected day
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/history/DayDetailScreen.kt`
**Implementation:**
- List sessions for selected date
- Session cards with summary stats
- Tap to view full session details

**Test:**
- [ ] Selecting day shows its sessions
- [ ] Empty state for days without sessions

---

### Step 2.1.4: Integrate Calendar into History [Major]
**Task:** Add toggle between list and calendar view
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/history/HistoryScreen.kt`
**Implementation:**
- Tab or toggle for List/Calendar view
- Share session data between views

**Test:**
- [ ] Can switch between list and calendar
- [ ] Both views show same data

---

## 2.2 Statistics

### Step 2.2.1: Add Charts Library Dependency [Minor]
**Task:** Add Vico charts library
**Files:** `app/build.gradle.kts`
**Implementation:**
- Add vico compose and m3 dependencies
- Sync gradle

**Test:**
- [ ] Project builds with charts dependency

---

### Step 2.2.2: Create StatisticsRepository [Minor]
**Task:** Repository for aggregated statistics queries
**Files:** `app/src/main/java/com/example/archeryapp/data/repository/StatisticsRepository.kt`
**Implementation:**
- Total arrows shot
- Average score per arrow/end
- Best session/end scores
- X-count percentage
- Scores by date range

**Test:**
- [ ] Repository compiles
- [ ] Queries return correct aggregations

---

### Step 2.2.3: Create StatisticsScreen [Major]
**Task:** Screen showing overall statistics
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/statistics/StatisticsScreen.kt`
**Implementation:**
- Summary cards (total arrows, average, best, X%)
- Trend chart (average score over time)
- Filter controls (date range, bow type)

**Test:**
- [ ] Statistics display correctly
- [ ] Chart renders with data points
- [ ] Filters update displayed data

---

### Step 2.2.4: Add Statistics to Navigation [Minor]
**Task:** Integrate statistics into bottom nav
**Files:** `app/src/main/java/com/example/archeryapp/ui/navigation/AppNavigation.kt`
**Implementation:**
- Add statistics route
- Update bottom nav

**Test:**
- [ ] Can navigate to Statistics
- [ ] Data loads correctly

---

## 2.3 Data Export/Import

### Step 2.3.1: Create Export Service [Major]
**Task:** Service to export data to JSON/CSV
**Files:** `app/src/main/java/com/example/archeryapp/data/export/DataExportService.kt`
**Implementation:**
- Export all sessions with ends and scores
- JSON format for full fidelity
- CSV format for spreadsheet compatibility
- Use Android's document picker for save location

**Test:**
- [ ] Export produces valid JSON file
- [ ] CSV can be opened in spreadsheet app
- [ ] All data included in export

---

### Step 2.3.2: Create Import Service [Major]
**Task:** Service to import data from JSON
**Files:** `app/src/main/java/com/example/archeryapp/data/export/DataImportService.kt`
**Implementation:**
- Parse JSON export format
- Validate data structure
- Handle duplicates (skip or merge)
- Transaction for atomic import

**Test:**
- [ ] Can import previously exported JSON
- [ ] Invalid files show error message
- [ ] Duplicate handling works correctly

---

### Step 2.3.3: Add Export/Import to Settings [Major]
**Task:** UI for data export and import
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/settings/SettingsScreen.kt`
**Implementation:**
- Export button with format selection
- Import button with file picker
- Progress indicator during operation
- Success/error feedback

**Test:**
- [ ] Export flow works end-to-end
- [ ] Import flow works end-to-end
- [ ] Settings screen accessible from bottom nav

---

---

# Phase 2.4: Target Type Support

Adds target-type awareness to sessions. Four types: standard multicolor (`MC`), standard blue face (`BF`), mini multicolor (`MINI_MC`), and triple (`TRIPLE`). Each session is locked to one target type at creation; changing type requires ending the current session and starting a new one. Statistics, history, and logging all segregate data by target type. Legacy sessions (created before this feature) are backfilled as `MINI_MC` per user decision.

**Scoring rules:**
- `MC`, `MINI_MC`, `TRIPLE`: standard USA Archery scoring — X, 10, 9, …, 1, M (all rings valid)
- `BF`: only X, 10, 9, 8, 7, 6 are scorable; any hit in rings 1–5 counts as M (miss)

**Auto-detection:** Only MC targets support the existing OpenCV auto-detect pipeline. For BF / MINI_MC / TRIPLE sessions the "Take Picture" button is disabled with a helper message; users must use "Input Score" instead. (Detection improvements for other target types are a separate, later concern.)

---

## 2.4.1 Target Type Domain + Scoring Strategies [Minor]
**Task:** Create `TargetType` enum and `ScoringStrategy` interface with two implementations
**Files:**
- `app/src/main/java/com/example/archeryapp/domain/model/TargetType.kt` (new)
- `app/src/main/java/com/example/archeryapp/domain/scoring/ScoringStrategy.kt` (new)
- `app/src/main/java/com/example/archeryapp/domain/scoring/MultiColorScoringStrategy.kt` (new)
- `app/src/main/java/com/example/archeryapp/domain/scoring/BlueFaceScoringStrategy.kt` (new)
- `app/src/test/java/com/example/archeryapp/domain/scoring/ScoringStrategyTest.kt` (new)

**Implementation:**
- `TargetType` enum with 4 values: `MC`, `BF`, `MINI_MC`, `TRIPLE`, each carrying a `code: String` (persistence identifier) and `displayName: String` (UI label).
- Companion `TargetType.fromCode(code: String?): TargetType` returns `MINI_MC` when the code is null or unknown (legacy backfill safety).
- `ScoringStrategy` interface with:
  - `fun normalize(rawRingScore: Int, isX: Boolean): Int` — returns the scored value (0 for miss).
  - `fun isButtonVisible(ringValue: Int): Boolean` — for the numeric input UI to decide which buttons to show.
- `MultiColorScoringStrategy`: all rings 1–10 valid, returns raw score unchanged. Used by `MC`, `MINI_MC`, `TRIPLE`.
- `BlueFaceScoringStrategy`: rings 1–5 → 0 (miss). Rings 6–10 and X pass through unchanged.
- `ScoringStrategy.forTarget(type: TargetType): ScoringStrategy` factory function.

**Test:**
- [ ] File compiles; `./gradlew assembleDebug` succeeds
- [ ] Unit test: MC strategy returns 8 for raw 8, 0 for raw 0
- [ ] Unit test: BF strategy returns 8 for raw 8, 0 for raw 5 (demoted), 0 for raw 0
- [ ] Unit test: `TargetType.fromCode(null)` returns `MINI_MC`
- [ ] `./gradlew test` passes all new tests

---

## 2.4.2 Database Migration v1 → v2 [Major — CRITICAL]
**Task:** Add `targetType` column to `sessions` table with a safe, data-preserving migration
**Files:**
- `app/src/main/java/com/example/archeryapp/data/local/entity/SessionEntity.kt` (modify)
- `app/src/main/java/com/example/archeryapp/data/local/database/Migrations.kt` (new)
- `app/src/main/java/com/example/archeryapp/data/local/database/AppDatabase.kt` (modify: version bump + register migration)

**Implementation:**
- Add `val targetType: String? = null` to `SessionEntity` (nullable for migration compatibility).
- New `Migrations.kt` file defining:
  ```kotlin
  val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE sessions ADD COLUMN targetType TEXT DEFAULT NULL")
          db.execSQL("UPDATE sessions SET targetType = 'MINI_MC' WHERE targetType IS NULL")
      }
  }
  ```
- `AppDatabase`: bump `version = 2`, add `.addMigrations(MIGRATION_1_2)` to the `databaseBuilder` call.
- **DO NOT use `fallbackToDestructiveMigration()`** — legacy data MUST survive.
- Enable Room schema export (`exportSchema = true` in `@Database`, `room.schemaLocation` in build.gradle) if not already enabled, so the schema JSON is version-controlled going forward.

**Test:**
- [ ] `./gradlew assembleDebug` builds
- [ ] Room schema v2 JSON generated under `app/schemas/` (or equivalent if schema export is enabled)
- [ ] **⛔ MANDATORY user verification on a REAL device:** install the debug APK on a device that already has the v1 DB with existing sessions; confirm (a) app opens without crash, (b) all prior sessions still appear in History/Calendar, (c) each prior session is labeled `MINI_MC` in the UI (after 2.4.10) or via Database Inspector (before 2.4.10).

**⛔ HARD STOP:** Claude MUST NOT proceed to 2.4.3 until user has installed the build and explicitly confirmed legacy data survived the migration. Migration bugs cannot be recovered from.

---

## 2.4.3 Session Domain Propagation [Minor]
**Task:** Plumb `targetType` through the `Session` domain model, `SessionRepository`, and `SaveScoreUseCase`
**Files:**
- `app/src/main/java/com/example/archeryapp/domain/model/Session.kt` (modify)
- `app/src/main/java/com/example/archeryapp/domain/repository/SessionRepository.kt` (modify)
- `app/src/main/java/com/example/archeryapp/data/repository/SessionRepositoryImpl.kt` (modify)
- `app/src/main/java/com/example/archeryapp/domain/usecase/SaveScoreUseCase.kt` (modify)

**Implementation:**
- Add `val targetType: TargetType = TargetType.MINI_MC` to `Session` domain model (default = legacy-safe).
- Entity↔Domain mapping in `SessionRepositoryImpl`: map `SessionEntity.targetType` (String?) ↔ `Session.targetType` (enum) using `TargetType.fromCode` and `type.code`.
- Add `targetType: TargetType` parameter to session-creation methods on `SessionRepository` / `SessionRepositoryImpl`.
- `SaveScoreUseCase`: when creating a new session, accept and propagate a `targetType`; when continuing an existing session, use that session's stored target type (do NOT allow override).

**Test:**
- [ ] Build succeeds
- [ ] Round-trip: create session with `BF` → read back → assert targetType preserved
- [ ] Legacy sessions (created pre-migration) deserialize as `MINI_MC`

---

## 2.4.4 Last-Used Target Type Preference [Minor]
**Task:** Persist and retrieve the last-used target type so the session picker defaults sensibly
**Files:**
- `app/src/main/java/com/example/archeryapp/data/preferences/UserPreferences.kt` (new)

**Implementation:**
- SharedPreferences-backed singleton (injected or constructed with Context).
- `fun getLastTargetType(): TargetType` — returns `TargetType.MINI_MC` when unset.
- `fun setLastTargetType(type: TargetType)` — writes the enum's `code`.
- Keep the class small and single-purpose; DataStore is overkill for one enum.

**Test:**
- [ ] Build succeeds
- [ ] Set then get round-trips (unit test or manual inspection)

---

## 2.4.5 Target Type Picker Dialog [Minor]
**Task:** Reusable Compose dialog for selecting a target type
**Files:**
- `app/src/main/java/com/example/archeryapp/ui/components/TargetTypePickerDialog.kt` (new)

**Implementation:**
- `@Composable fun TargetTypePickerDialog(current: TargetType, onSelect: (TargetType) -> Unit, onDismiss: () -> Unit)`
- Material3 `AlertDialog` with a radio-button list of all `TargetType` values (shown by `displayName`).
- Confirm button applies selection; Cancel dismisses without change.
- Preview function for Android Studio tooling.

**Test:**
- [ ] Build succeeds
- [ ] Preview renders

---

## 2.4.6 New Session Creation Flow [Major]
**Task:** Integrate target-type picker into "+ New Session" on `SessionSelectScreen`
**Files:**
- `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectScreen.kt` (modify)
- `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectViewModel.kt` (modify)

**Implementation:**
- "+ New Session" FAB now opens `TargetTypePickerDialog`, initial selection = `UserPreferences.getLastTargetType()`.
- On confirm: `SessionSelectViewModel.createNewSession(targetType)` → repository creates the session with the given type → `UserPreferences.setLastTargetType(type)` → navigate to the score-input/results flow with the new session id.
- On cancel: dialog dismisses, no session created.

**Test:**
- [ ] Build succeeds
- [ ] Manual: tap "+ New Session" → picker appears with last-used default → select `BF` → new session created with `BF` type

---

## 2.4.7 HomeScreen: Badge + End Session + Auto-Detect Gating + Create Session + Active Session Bugfix [Major]
**Task:** Surface target type on the active session card, add "End Session" button, gate "Take Picture" to MC only, add a "Create Session" button on HomeScreen, and fix the stale-active-session bug when a session is deleted from elsewhere.
**Files:**
- `app/src/main/java/com/example/archeryapp/ui/screens/home/HomeScreen.kt` (modify)
- `app/src/main/java/com/example/archeryapp/ui/screens/home/HomeViewModel.kt` (modify)

**Implementation:**
- Active session card shows a small target-type chip/badge (uses `displayName`).
- Add "End Session" button — on click: clears the active session in UI state (does NOT delete the session row in the database; it just unsets the "currently active" flag so the next session start triggers a fresh target picker).
- "Take Picture" button: disabled when `activeSession.targetType != MC`, with helper text "Auto-detect only supports standard multicolor targets. Use 'Input Score' instead."
- "Input Score" button: unchanged, enabled for all target types.
- `HomeViewModel.endActiveSession()` — clears `activeSessionId` and related state.
- **[added]** `HomeViewModel.createSession(targetType, onCreated)` — creates a new `Session` with the given target type, persists last-used type via `UserPreferences`, and sets the new session as active. Uses the same strategy as `SessionSelectViewModel.createNewSession`.
- **[added]** `HomeViewModel.getLastTargetType()` — delegates to `UserPreferences` for the picker dialog's initial selection.
- **[added]** HomeScreen gains a "Create New Session" button (above the action-button row) that opens `TargetTypePickerDialog` and calls `HomeViewModel.createSession`. On success, the reactive state flow auto-promotes the new session to active.
- **[added — bugfix]** `HomeViewModel.loadTodaysSession()` is replaced by reactive observation: `combine(sessionRepository.getAllSessions(), _activeSessionId)` runs for the ViewModel's lifetime. When a session is deleted from anywhere (SessionSelect, etc.), the flow re-emits and HomeScreen's active card self-heals — either falling back to the most recent remaining session for today or clearing entirely if none exist.
  - Self-healing rule: if an explicit `_activeSessionId` no longer resolves to any row, clear it and re-pick most-recent-today (or null).
  - `setActiveSession(id)` and `clearActiveSession()` become trivial: they just update `_activeSessionId`, and the combined flow recomputes everything.

**Test:**
- [ ] Build succeeds
- [ ] Active session card shows badge for all four target types
- [ ] Tapping "End Session" hides the active session card → shows "No Active Session"
- [ ] "Take Picture" is disabled when target is `BF`, `MINI_MC`, or `TRIPLE`
- [ ] "Take Picture" is enabled when target is `MC`
- [ ] "Input Score" is enabled for all types
- [ ] Tapping "Create New Session" on HomeScreen opens target picker → confirming creates a session that appears as active
- [ ] Creating a session from HomeScreen, then deleting it from SessionSelect → HomeScreen auto-clears the active card (no stale state)
- [ ] Creating two sessions from HomeScreen → session 2 becomes active → deleting session 2 → session 1 becomes the active card

---

## 2.4.8 Scoring Application (Results + Numeric Input) [Major]
**Task:** Apply the per-target scoring strategy in Results and Numeric Input
**Files:**
- `app/src/main/java/com/example/archeryapp/ui/screens/results/ResultsViewModel.kt` (modify)
- `app/src/main/java/com/example/archeryapp/ui/screens/results/ResultsScreen.kt` (modify if needed)
- `app/src/main/java/com/example/archeryapp/ui/screens/scoreinput/NumericScoreInputScreen.kt` (modify)

**Implementation:**
- `ResultsViewModel` loads the session's `targetType` when resolving session id, resolves a `ScoringStrategy` via the factory, and applies `normalize()` to each arrow's raw ring value before display/persistence.
- `NumericScoreInputScreen` reads target type (via nav argument or shared state) and uses `ScoringStrategy.isButtonVisible(ringValue)` to hide (or visually disable) buttons that are not scorable:
  - `BF`: show X, 10, 9, 8, 7, 6, M only
  - `MC` / `MINI_MC` / `TRIPLE`: show all 12 buttons
- Arrow chips already populated always reflect the normalized score.

**Test:**
- [ ] Build succeeds
- [ ] Start `BF` session, open numeric input: buttons 1–5 not shown
- [ ] Start `MC` session, open numeric input: all buttons visible
- [ ] Save end in `BF` session → results screen shows correct total (no 1–5 entries)

---

## 2.4.9 Statistics Per Target Type [Major]
**Task:** Filter statistics by target type with a segmented selector
**Files:**
- `app/src/main/java/com/example/archeryapp/data/repository/StatisticsRepository.kt` (modify)
- `app/src/main/java/com/example/archeryapp/data/local/dao/SessionDao.kt` (modify: add target-filtered queries)
- `app/src/main/java/com/example/archeryapp/data/local/dao/EndDao.kt` (modify if needed)
- `app/src/main/java/com/example/archeryapp/data/local/dao/ArrowScoreDao.kt` (modify if needed)
- `app/src/main/java/com/example/archeryapp/ui/screens/statistics/StatisticsScreen.kt` (modify)
- `app/src/main/java/com/example/archeryapp/ui/screens/statistics/StatisticsViewModel.kt` (modify)

**Implementation:**
- Repository methods gain `targetType: TargetType? = null` parameter; `null` = aggregate across all types.
- DAO queries add a `WHERE targetType = :code` clause (or no clause when null); since Room doesn't support optional predicates natively, either (a) two variant queries, or (b) use `COALESCE` trick.
- `StatisticsScreen` gains a segmented-button row at the top: `All | MC | BF | Mini MC | Triple`.
- Selecting a segment triggers `StatisticsViewModel.setTargetFilter(type)` → re-queries and updates state.
- All charts, cards, and aggregates reflect the filter.

**Test:**
- [ ] Build succeeds
- [ ] Default view (`All`) matches current aggregate numbers for existing data
- [ ] Selecting `MC` filters to MC sessions only
- [ ] Selecting `Mini MC` shows legacy sessions (post-migration they are all `MINI_MC`)
- [ ] Trend chart updates appropriately

---

## 2.4.10 Target Type Display in Session Lists [Minor]
**Task:** Show target type chip on session cards in `SessionSelectScreen`, `HistoryScreen`, and `DayDetailScreen`
**Files:**
- `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectScreen.kt` (modify)
- `app/src/main/java/com/example/archeryapp/ui/screens/history/HistoryScreen.kt` (modify)
- `app/src/main/java/com/example/archeryapp/ui/screens/calendar/DayDetailScreen.kt` (modify)

**Implementation:**
- Small compact chip on each session row/card, next to the session number/date.
- Optional: subtle color coding (gold for MC, blue for BF, etc.) — but keep the label visible regardless of color, for accessibility.
- Single composable `TargetTypeChip(type: TargetType)` placed in `ui/components/` so all three screens share the same rendering.

**Test:**
- [ ] Build succeeds
- [ ] All three screens show the chip on every session card

---

## 2.4.11 Export/Import Schema Update [Minor]
**Task:** Include `targetType` in JSON export; on import, treat missing field as `MINI_MC`
**Files:**
- `app/src/main/java/com/example/archeryapp/data/export/DataExportService.kt` (modify)
- `app/src/main/java/com/example/archeryapp/data/export/DataImportService.kt` (modify)

**Implementation:**
- Extend the serialized session DTO with a nullable `targetType: String?` field.
- Export: write the session's `targetType.code`.
- Import: read field; on null/missing, fall back to `TargetType.MINI_MC` (consistent with the legacy backfill rule in 2.4.2).
- Backwards compatible: legacy JSON files (without `targetType`) import cleanly as `MINI_MC`.

**Test:**
- [ ] Build succeeds
- [ ] Export → JSON contains `targetType` for every session
- [ ] Import a hand-crafted legacy JSON (no `targetType` field) → sessions land as `MINI_MC`

---

## Phase 2.4 Execution Order & Stop Points

Steps MUST be executed in numeric order. Dependencies: every step from 2.4.3 onward depends on 2.4.2's schema change being present.

- **After 2.4.2:** ⛔ HARD STOP. Database migration cannot be validated by `./gradlew assembleDebug` alone. User MUST install the debug APK on a real device, verify all pre-existing sessions still appear, and explicitly confirm data survived before Claude proceeds to 2.4.3. Migration bugs on shipped schemas are unrecoverable.

- **After 2.4.7:** Soft checkpoint. End-to-end manual sanity check recommended (create a `BF` session, confirm numeric input restrictions, verify home screen gating). Proceeding blind is acceptable if user authorizes.

- **After 2.4.11:** Full feature complete. Regression testing of existing Phase 1 / Phase 2 features recommended before closing out Phase 2.4 and resuming Phase 3.

---

---

# Phase 3: M5Stick Integration

## 3.1 Bluetooth Foundation

### Step 3.1.1: Add BLE Dependencies [Minor]
**Task:** Add Nordic BLE library
**Files:** `app/build.gradle.kts`
**Implementation:**
- Add Nordic Android BLE library
- Add necessary permissions to manifest

**Test:**
- [ ] Project builds with BLE dependency
- [ ] Permissions declared in manifest

---

### Step 3.1.2: Create BLE Permission Handler [Major]
**Task:** Handle Bluetooth and location permissions
**Files:** `app/src/main/java/com/example/archeryapp/data/bluetooth/BlePermissionHandler.kt`
**Implementation:**
- Check BLE availability
- Request BLUETOOTH_SCAN, BLUETOOTH_CONNECT permissions
- Handle Android 12+ permission changes
- Location permission for BLE scanning

**Test:**
- [ ] Permission requests shown correctly
- [ ] Handles permission denied gracefully

---

### Step 3.1.3: Create BLE Scanner [Major]
**Task:** Scan for M5Stick devices
**Files:** `app/src/main/java/com/example/archeryapp/data/bluetooth/BleScanner.kt`
**Implementation:**
- Start/stop scanning
- Filter by device name or service UUID
- Emit discovered devices as Flow
- Handle scan timeout

**Test:**
- [ ] Scanner finds nearby BLE devices
- [ ] Filtering works correctly
- [ ] Scan stops after timeout

---

### Step 3.1.4: Create BLE Connection Manager [Major]
**Task:** Manage connection to M5Stick device
**Files:** `app/src/main/java/com/example/archeryapp/data/bluetooth/BleConnectionManager.kt`
**Implementation:**
- Connect to device by address
- Handle connection states (connecting, connected, disconnected)
- Auto-reconnect on disconnect
- Expose connection state as StateFlow

**Test:**
- [ ] Can connect to test BLE device
- [ ] Connection state updates correctly
- [ ] Auto-reconnect works after disconnect

---

### Step 3.1.5: Create M5Stick Data Parser [Minor]
**Task:** Parse incoming data from M5Stick
**Files:** `app/src/main/java/com/example/archeryapp/data/bluetooth/M5StickDataParser.kt`
**Implementation:**
- Generic parser interface
- JSON parsing implementation
- Handle malformed data gracefully
- Map to domain model (AngleReading)

**Test:**
- [ ] Parses valid JSON packets
- [ ] Handles invalid data without crash
- [ ] Outputs correct AngleReading objects

---

## 3.2 Real-Time UI

### Step 3.2.1: Create Device Connection Screen [Major]
**Task:** UI for scanning and connecting to devices
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/realtime/DeviceConnectionScreen.kt`
**Implementation:**
- Scan button
- List of discovered devices
- Connection status indicator
- Connect/disconnect buttons
- Support multiple devices

**Test:**
- [ ] Discovered devices listed
- [ ] Can connect to device from list
- [ ] Connection status shown correctly

---

### Step 3.2.2: Create Angle Data Model [Minor]
**Task:** Domain model for angle readings
**Files:** `app/src/main/java/com/example/archeryapp/domain/model/AngleReading.kt`
**Implementation:**
```kotlin
data class AngleReading(
    val deviceId: String,
    val timestamp: Long,
    val pitch: Float,
    val roll: Float,
    val yaw: Float,
    val rawAccel: Vector3? = null,
    val rawGyro: Vector3? = null
)
```

**Test:**
- [ ] Model compiles
- [ ] Covers all expected data fields

---

### Step 3.2.3: Create Live Angle Display Screen [Major]
**Task:** Real-time visualization of angle data
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/realtime/LiveAngleScreen.kt`
**Implementation:**
- Numeric angle display
- Visual arm position indicator
- Ideal range guides
- Recording controls
- Multi-device support (tabs or split view)

**Test:**
- [ ] Angles update in real-time
- [ ] Visual indicator moves with angle changes
- [ ] Ideal range clearly shown

---

### Step 3.2.4: Add Haptic/Audio Feedback [Major]
**Task:** Feedback when angle outside ideal range
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/realtime/LiveAngleScreen.kt`
**Implementation:**
- Configurable ideal angle ranges
- Vibration when outside range
- Optional audio tone
- Settings to enable/disable feedback

**Test:**
- [ ] Vibration occurs when angle exceeds threshold
- [ ] Feedback can be disabled in settings

---

## 3.3 Angle Data Persistence

### Step 3.3.1: Create AngleData Entity [Minor]
**Task:** Room entity for storing angle recordings
**Files:** `app/src/main/java/com/example/archeryapp/data/local/entity/AngleDataEntity.kt`
**Implementation:**
- Link to session (optional) and end (optional)
- Store raw readings with timestamps
- Device identifier

**Test:**
- [ ] Entity compiles
- [ ] Relationships defined correctly

---

### Step 3.3.2: Create AngleData DAO [Minor]
**Task:** Data access for angle recordings
**Files:** `app/src/main/java/com/example/archeryapp/data/local/dao/AngleDataDao.kt`
**Implementation:**
- Insert batch of readings
- Get readings by session/end
- Get readings by time range
- Delete old recordings

**Test:**
- [ ] CRUD operations work
- [ ] Batch insert efficient

---

### Step 3.3.3: Create Recording Session Flow [Major]
**Task:** Start/stop recording angle data with session
**Files:** `app/src/main/java/com/example/archeryapp/domain/usecase/RecordAngleSessionUseCase.kt`
**Implementation:**
- Start recording (create session link)
- Buffer incoming data
- Batch insert to database
- Stop recording

**Test:**
- [ ] Recording captures all incoming data
- [ ] Data persisted to database
- [ ] Can retrieve recording after

---

### Step 3.3.4: Create Playback View [Major]
**Task:** View recorded angle data
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/realtime/PlaybackScreen.kt`
**Implementation:**
- Timeline scrubber
- Angle visualization at selected time
- Sync with end/arrow if linked
- Export recording

**Test:**
- [ ] Playback shows recorded angles
- [ ] Timeline navigation works
- [ ] Syncs with scoring data

---

---

# Phase 4: Form Analysis

## 4.1 ML Kit Integration

### Step 4.1.1: Add ML Kit Dependencies [Minor]
**Task:** Add ML Kit Pose Detection
**Files:** `app/build.gradle.kts`
**Implementation:**
- Add pose-detection and pose-detection-accurate dependencies
- Configure model download

**Test:**
- [ ] Project builds with ML Kit
- [ ] Model downloads on first use

---

### Step 4.1.2: Create Pose Detector Interface [Minor]
**Task:** Define interface for pose detection
**Files:** `app/src/main/java/com/example/archeryapp/domain/detection/PoseDetector.kt`
**Implementation:**
```kotlin
interface PoseDetector {
    suspend fun detectPose(bitmap: Bitmap): PoseResult?
}

data class PoseResult(
    val keypoints: Map<BodyPart, Keypoint>,
    val confidence: Float
)
```

**Test:**
- [ ] Interface compiles
- [ ] Covers all needed body parts

---

### Step 4.1.3: Create ML Kit Pose Detector Implementation [Major]
**Task:** Implement pose detection with ML Kit
**Files:** `app/src/main/java/com/example/archeryapp/detection/mlkit/MlKitPoseDetector.kt`
**Implementation:**
- Initialize ML Kit pose detector
- Process bitmap and extract landmarks
- Map ML Kit landmarks to domain model
- Handle detection failures

**Test:**
- [ ] Detects pose in test image
- [ ] All key landmarks identified
- [ ] Confidence scores populated

---

## 4.2 Form Capture

### Step 4.2.1: Create Form Capture Screen [Major]
**Task:** Camera screen with pose guidance overlay
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/form/FormCaptureScreen.kt`
**Implementation:**
- Camera preview
- Silhouette overlay showing ideal position
- Left/right hand toggle
- Distance indicator (person should fill frame)
- Capture button

**Test:**
- [ ] Camera preview works
- [ ] Guidance overlay visible
- [ ] Can capture image

---

### Step 4.2.2: Create Pose Overlay Component [Major]
**Task:** Composable to draw detected pose on image
**Files:** `app/src/main/java/com/example/archeryapp/ui/components/pose/PoseOverlay.kt`
**Implementation:**
- Draw skeleton lines between keypoints
- Draw keypoint markers
- Color-code by confidence
- Scale to image dimensions

**Test:**
- [ ] Pose overlay renders correctly
- [ ] Scales properly on different image sizes
- [ ] Colors indicate confidence

---

## 4.3 Form Analysis

### Step 4.3.1: Create Form Metrics Calculator [Minor]
**Task:** Calculate form metrics from pose
**Files:** `app/src/main/java/com/example/archeryapp/domain/analysis/FormMetricsCalculator.kt`
**Implementation:**
- Calculate joint angles (elbow, shoulder)
- Measure alignment (head, spine, hips)
- Compare to ideal values
- Generate per-metric scores

**Test:**
- [ ] Angles calculated correctly from keypoints
- [ ] Comparison to ideal produces sensible scores
- [ ] Unit tests for angle calculations

---

### Step 4.3.2: Create Form Analysis Screen [Major]
**Task:** Display form analysis results
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/form/FormAnalysisScreen.kt`
**Implementation:**
- Image with pose overlay
- Metric cards (arm extension, elbow angle, etc.)
- Color-coded feedback (green/yellow/red)
- Overall form score
- Specific recommendations

**Test:**
- [ ] Analysis results display correctly
- [ ] Recommendations actionable and clear
- [ ] Can retake or save

---

### Step 4.3.3: Create Form Analysis Entity [Minor]
**Task:** Persist form analysis results
**Files:** `app/src/main/java/com/example/archeryapp/data/local/entity/FormAnalysisEntity.kt`
**Implementation:**
- Image path
- Keypoints JSON
- Metrics JSON
- Overall score
- Timestamp
- Optional session link

**Test:**
- [ ] Entity compiles
- [ ] JSON storage for flexible keypoint data

---

### Step 4.3.4: Create Form History View [Major]
**Task:** View past form analyses
**Files:** `app/src/main/java/com/example/archeryapp/ui/screens/form/FormHistoryScreen.kt`
**Implementation:**
- List of past analyses with thumbnails
- Score trend over time
- Compare two analyses side-by-side

**Test:**
- [ ] History loads saved analyses
- [ ] Comparison view works
- [ ] Can delete old analyses

---

### Step 4.3.5: Add Form to Navigation [Major]
**Task:** Integrate form screens into app
**Files:** `app/src/main/java/com/example/archeryapp/ui/navigation/AppNavigation.kt`
**Implementation:**
- Add form routes (capture, analysis, history)
- Update bottom nav
- Navigation flow: capture -> analysis -> history

**Test:**
- [ ] Full form flow navigable
- [ ] Bottom nav shows Form tab
- [ ] Back navigation correct

---

---

# Implementation Notes

## Testing Requirements

**Minor Steps [Minor]:**
- Test on Android emulator/virtual device
- Claude shows build/compile results
- Claude shows test output where applicable
- User informed of completion, permission required to proceed

**Major Steps [Major]:**
- Test on Android emulator first
- User must additionally test on real phone
- Claude shows all available visual evidence (screenshots, logs, etc.)
- User confirms real-device testing complete before proceeding

## Step Execution Workflow - MANDATORY CHECKLIST

For EACH step, complete ALL of the following before proceeding:

□ 1. Complete all code edits for the step
□ 2. Run build: `./gradlew assembleDebug`
□ 3. Show build output to user (task count, time, success/failure)
□ 4. Update architecture.md with session notes
□ 5. Inform user: "Step X complete. [Summary]. Ready for next step?"
□ 6. WAIT for user approval before proceeding

⚠️ **VIOLATION**: Proceeding to next step without completing checklist
⚠️ **VIOLATION**: Batching multiple steps without user approval between each
⚠️ **VIOLATION**: Skipping build validation or not showing output

### Additional Requirements for Major Steps
- User must test on real phone before granting approval
- Wait for user to confirm real-device testing complete

## Testing Strategy
- **Unit Tests**: Domain logic, use cases, calculators
- **Integration Tests**: Repository + Database
- **UI Tests**: Screen navigation, user flows
- **Manual Tests**: Camera, Bluetooth, ML Kit (device-specific)

## Progress Tracking
After completing each step:
1. Mark step as complete in this document `[x]`
2. Document in `architecture.md`:
   - What was implemented
   - Any deviations from plan
   - Issues encountered
   - Test results

## Dependencies Between Steps
- 1.1.x (Database) must complete before 1.2.x (Repository)
- 1.2.x (Repository) must complete before 1.4.x (Save Flow)
- 1.5.x (History) can run parallel to 1.4.x after 1.2.x
- Phase 2 requires Phase 1 completion
- Phase 3 and 4 can run parallel after Phase 1
