# Achievements & Progress Log

This document tracks implementation progress and architectural decisions.
Updated after each completed step/subproject.

---

## Current Status

**Phase:** 2.4 - Target Type Support (IN PROGRESS, inserted ahead of Phase 3 completion)
**Current Step:** 2.4.7 CODE-COMPLETE — HomeScreen expanded scope: target-type badge on active card, "End Session" button, "Create New Session" button with target picker, "Take Picture" gated to MC only, and reactive Flow-based active-session observation (fixes stale-after-delete bug). **AWAITING real-device verification** (Major step).
**Last Updated:** 2026-04-11

**Phase 2 status:** Original Phase 2 code complete, awaiting real-device verification (deferred by user).
**Phase 2.4 context:** User requested target-type tracking (MC / BF / MINI_MC / TRIPLE) before returning to Phase 3. Each session is locked to one target type; legacy sessions backfilled as MINI_MC. Plan added to DEVELOPMENT_PLAN.md with 11 sub-steps and a HARD STOP after 2.4.2 (DB migration) for user to verify data survival on real device.
**Phase 3 status:** 3.1.1 and 3.1.2 complete; paused at the 3.1.3 decision point pending user direction. Will resume after 2.4.11.

### Known Issues (To Revisit)
- HoughCircles still takes ~57 seconds even with 2 sweeps on 1200px image
- May need to try: single sweep, lower resolution, or alternative detection approach
- Detection accuracy also needs improvement - defer to later iteration

---
## Completed Work

### Session: 2026-01-27 - Detection & UX Improvements
**Summary of changes:**
1. Fixed bottom navigation padding (nested Scaffold + NavigationBar windowInsets)
2. Implemented progress indicator for image analysis (shows current step)
3. Added detailed timing logs throughout detection pipeline
4. Added image downscaling for target detection (1200px max)
5. Added image cropping for arrow detection (target region only at full res)
6. Reduced HoughCircles from 5 sweeps to 2
7. Made color validation work with partial/zoomed targets

**Outcome:** Detection still slow (~57s for HoughCircles) - paused for later optimization.
User can now see progress updates during analysis instead of indefinite "Analyzing..."

---
## Implementation Progress

### Phase 1: Core Scoring & Persistence

#### 1.1 Database Foundation
| Step | Status | Notes |
|------|--------|-------|
| 1.1.1 Room Dependencies | Complete | Added Room 2.6.1 with KSP 2.0.21-1.0.27 for annotation processing |
| 1.1.2 Session Entity | Complete | Created with @Entity, @PrimaryKey annotations |
| 1.1.3 End Entity | Complete | Created with ForeignKey to Session, indexed |
| 1.1.4 ArrowScore Entity | Complete | Created with ForeignKey to End, indexed |
| 1.1.5 Session DAO | Complete | CRUD + date range queries + Flow support |
| 1.1.6 End DAO | Complete | CRUD + session queries + end count |
| 1.1.7 ArrowScore DAO | Complete | Batch insert + aggregation queries |
| 1.1.8 AppDatabase | Complete | Singleton pattern, version 1 |
| 1.1.9 Database Init | Complete | Lazy init in ArcheryApplication |

#### 1.2 Repository Layer
| Step | Status | Notes |
|------|--------|-------|
| 1.2.1 Session Repo Interface | Complete | CRUD + date range + Flow support |
| 1.2.2 Session Repo Impl | Complete | Entity-Domain mapping with LocalDateTime |
| 1.2.3 Score Repo Interface | Complete | End/Arrow operations + aggregations |
| 1.2.4 Score Repo Impl | Complete | Atomic save with transaction-like behavior |

#### 1.3 Domain Models
| Step | Status | Notes |
|------|--------|-------|
| 1.3.1 Session Model | Complete | With computed properties (totalScore, etc.) |
| 1.3.2 End Model | Complete | With computed totalScore, xCount |
| 1.3.3 Arrow Model | Complete | Uses custom Position class (not Compose Offset) |

#### 1.4 Save Score Flow
| Step | Status | Notes |
|------|--------|-------|
| 1.4.1 SaveScoreUseCase | Complete | Creates/continues session, converts ScoringResult to domain models |
| 1.4.2 ResultsScreen Save | Complete (Device Tested) | Save button, ViewModel, Session Ends table, scrollable layout |
| 1.4.3 Session Selection | Complete | HomeViewModel, SessionSelectScreen, navigation integration |
| 1.4.4 Session Delete | Complete | Swipe-to-delete gesture for sessions |

#### 1.5 History List
| Step | Status | Notes |
|------|--------|-------|
| 1.5.1 HistoryScreen | Complete | Expandable session cards with end details |
| 1.5.2 History Navigation | Complete | Integrated with bottom nav |
| 1.5.3 Bottom Navigation | Complete | Score, History, Settings tabs |

#### 1.6 Detection Improvements (PAUSED - revisit later)
| Step | Status | Notes |
|------|--------|-------|
| 1.6.1 Analysis | Complete | Documented issues with target/arrow detection |
| 1.6.2 Target Detection | Complete | CLAHE, 2 sweeps (reduced from 5), partial target support |
| 1.6.3 Arrow Detection | Complete | Dynamic scaling, 3 Canny thresholds, cropped region |
| 1.6.4 Confidence UI | Skipped | Deferred - focus on speed first |
| Performance | BLOCKED | HoughCircles still ~57s on 1200px - needs different approach |

### Phase 2: Progress Tracking (Complete)

#### 2.0 Auto-Detection Toggle
| Step | Status | Notes |
|------|--------|-------|
| 2.0.1 Auto-detect state | Complete | Added `autoDetectEnabled` state to HomeViewModel |
| 2.0.2 Toggle UI | Complete | Added toggle switch on HomeScreen |
| 2.0.3 Navigation routing | Complete | Routes to ArrowEdit or Review based on preference |

#### 2.1 Calendar View
| Step | Status | Notes |
|------|--------|-------|
| 2.1.1 CalendarScreen | Complete | Monthly calendar view with session indicators |
| 2.1.2 CalendarViewModel | Complete | Loads sessions grouped by date |
| 2.1.3 DayDetailScreen | Complete | Shows all sessions for selected day |
| 2.1.4 DayDetailViewModel | Complete | Loads sessions for specific date |
| 2.1.5 History integration | Complete | Added calendar icon to HistoryScreen |
| 2.1.6 Navigation | Complete | Added Calendar and DayDetail routes |

**Dependency:** kizitonwose calendar 2.6.2 (originally added as 2.5.0 on 2026-01-27; bumped 2026-04-10 to fix `SnapPositionInLayout` ClassNotFoundException — see session log)

#### 2.2 Statistics Screen
| Step | Status | Notes |
|------|--------|-------|
| 2.2.1 StatisticsScreen | Complete | Charts and statistics display |
| 2.2.2 StatisticsViewModel | Complete | Aggregates data for charts |
| 2.2.3 StatisticsRepository | Complete | Statistics queries and calculations |
| 2.2.4 DAO updates | Complete | Added statistics queries to DAOs |
| 2.2.5 Bottom nav | Complete | Added 4th tab for Statistics |

**Dependency:** Vico charts 1.13.1

#### 2.3 Data Export/Import
| Step | Status | Notes |
|------|--------|-------|
| 2.3.1 DataExportService | Complete | Exports sessions to JSON |
| 2.3.2 DataImportService | Complete | Imports sessions from JSON |
| 2.3.3 SettingsViewModel | Complete | Handles export/import actions |
| 2.3.4 SettingsScreen | Complete | Complete rewrite with export/import UI |

**Dependency:** kotlinx.serialization 1.6.3

### Phase 3: M5Stick Integration (Not Started)

### Phase 4: Form Analysis (Not Started)

---
## Test Results

### Unit Tests
*(Add test results as tests are written)*

### Integration Tests
*(Add test results as tests are written)*

### Manual Testing
*(Add manual test results)*

---
## Issues & Blockers

### 2026-01-26 - Step 1.1.1 Validation
**Issue:** Could not run `./gradlew build` due to JAVA_HOME not configured in terminal
**Resolution:** Retried in new session - Gradle found Java and build succeeded

### 2026-01-26 - Step 1.4.2 Save Button Not Visible
**Issue:** User reported no Save button visible on ResultsScreen after confirming arrows
**Cause:** Screen content exceeded viewport; buttons were below visible area
**Resolution:** Made Column scrollable with `verticalScroll(rememberScrollState())`, replaced `Spacer(weight(1f))` with fixed height spacer

### 2026-01-26 - Saved Ends Not Displayed
**Issue:** User tapped Save multiple times, saw "X ends saved" count increase, but couldn't see the actual end scores
**Cause:** ResultsScreen only showed the current scoring result, not previously saved ends in the session
**Resolution:** Added `SavedEndSummary` data class, `savedEnds` list to UI state, and "Session Ends" table showing all ends with their scores, arrow counts, and X counts

---
## Session Notes

### 2026-01-26
- Created app-design.md with all feature specifications
- Created implementation-plan.md with detailed steps
- Created architecture.md (this file) for progress tracking
- Updated CLAUDE.md with workflow rules

### 2026-01-26 - Step 1.1.1 Room Dependencies [COMPLETE]
**What was implemented:**
- Added KSP version 2.0.21-1.0.27 to version catalog
- Added Room 2.6.1 libraries (runtime, ktx, compiler) to version catalog
- Added KSP plugin to version catalog and app/build.gradle.kts
- Added Room dependencies to app/build.gradle.kts

**Files modified:**
- gradle/libs.versions.toml
- app/build.gradle.kts

**Notes:**
- Used KSP instead of kapt (recommended for Kotlin 2.0+)

**Validation Test Results:**
- `./gradlew build` completed successfully (5m 32s, 108 tasks)
- No dependency resolution errors
- Room dependencies resolved correctly
- KSP annotation processor working

### 2026-01-26 - Steps 1.1.2 through 1.1.9 Database Foundation [COMPLETE]
**What was implemented:**
- SessionEntity with id, date, distance, bowType, location, notes
- EndEntity with ForeignKey to Session, endNumber, timestamp, notes
- ArrowScoreEntity with ForeignKey to End, arrowNumber, score, isX, xPosition, yPosition
- SessionDao with CRUD, date range queries, Flow support
- EndDao with CRUD, session queries, end count, max end number
- ArrowScoreDao with batch insert, aggregation queries (total score, X count)
- AppDatabase singleton with all three DAOs
- Lazy database initialization in ArcheryApplication

**Files created:**
- data/local/entity/SessionEntity.kt
- data/local/entity/EndEntity.kt
- data/local/entity/ArrowScoreEntity.kt
- data/local/dao/SessionDao.kt
- data/local/dao/EndDao.kt
- data/local/dao/ArrowScoreDao.kt
- data/local/database/AppDatabase.kt

**Files modified:**
- ArcheryApplication.kt (added lazy database property)

**Validation Test Results:**
- All files compile successfully
- `./gradlew assembleDebug` builds APK successfully
- Ready for device testing (Step 1.1.9 is Major)

### 2026-01-26 - Steps 1.2, 1.3, 1.4 Complete
**What was implemented:**

**Section 1.3 - Domain Models:**
- Session.kt with computed properties (totalScore, totalArrows, totalXCount, averagePerArrow)
- End.kt with computed totalScore and xCount
- Arrow.kt with custom Position class (keeps domain layer independent of Compose)

**Section 1.2 - Repository Layer:**
- SessionRepository interface with CRUD + date range queries
- SessionRepositoryImpl with Entity-Domain mapping using LocalDateTime
- ScoreRepository interface for End/Arrow operations + aggregations
- ScoreRepositoryImpl with atomic save operations

**Section 1.4 - Save Score Flow:**
- SaveScoreUseCase that creates/continues sessions, converts ScoringResult to domain models
- ResultsViewModel with save state management
- ResultsScreen updated with Save button, success/error feedback

**Files created:**
- domain/model/Session.kt, End.kt, Arrow.kt
- domain/repository/SessionRepository.kt, ScoreRepository.kt
- data/repository/SessionRepositoryImpl.kt, ScoreRepositoryImpl.kt
- domain/usecase/SaveScoreUseCase.kt
- ui/screens/results/ResultsViewModel.kt

**Files modified:**
- ui/screens/results/ResultsScreen.kt (added Save button, ViewModel integration)

### 2026-01-26 - UX Improvements to ResultsScreen
**What was implemented:**
- Made ResultsScreen scrollable to handle different screen sizes
- Added "Session Ends" table showing all saved ends in current session
- Shows End #, Arrows, Score, X count for each saved end
- Shows session totals at bottom of table
- Success message now shows "End #X saved!" with the end number
- Session info shows "Session #X - Y ends saved"

**Files modified:**
- ui/screens/results/ResultsScreen.kt (scrollable, ends table)
- ui/screens/results/ResultsViewModel.kt (added SavedEndSummary, savedEnds list)

### 2026-01-26 - Step 1.4.3 Session Selection [COMPLETE]
**What was implemented:**
- HomeViewModel to track active session (auto-loads today's most recent session)
- SessionSelectScreen with list of recent sessions and "New Session" FAB
- SessionSelectViewModel with session summary data (end count, total score, arrows)
- Updated HomeScreen with Active Session card (shows session info or "No Active Session")
- Session ID passed through navigation to ResultsScreen
- ResultsScreen loads existing ends when continuing a session

**Files created:**
- ui/screens/home/HomeViewModel.kt
- ui/screens/session/SessionSelectScreen.kt
- ui/screens/session/SessionSelectViewModel.kt

**Files modified:**
- ui/screens/home/HomeScreen.kt (added Active Session card, onManageSessions callback)
- ui/navigation/AppNavigation.kt (added SessionSelect route, pass session ID to Results)
- ui/screens/results/ResultsScreen.kt (accept initialSessionId parameter)
- ui/screens/results/ResultsViewModel.kt (added setInitialSessionId method)
- domain/repository/SessionRepository.kt (added sync methods)
- data/repository/SessionRepositoryImpl.kt (implemented sync methods)
- data/local/dao/SessionDao.kt (added sync query methods)

**Validation:**
- Build successful (38 tasks, 51s)
- Awaiting device test

### 2026-01-26 - Session Delete Feature [COMPLETE]
**What was implemented:**
- Swipe-to-delete gesture for sessions in SessionSelectScreen
- Added deleteSession method to SessionSelectViewModel
- Added deleteSessionById to SessionRepository and DAO
- Red background with delete icon appears when swiping left
- Session list refreshes after deletion

**Files modified:**
- ui/screens/session/SessionSelectScreen.kt (added SwipeToDismissBox, delete icon)
- ui/screens/session/SessionSelectViewModel.kt (added deleteSession method)
- domain/repository/SessionRepository.kt (added deleteSessionById)
- data/repository/SessionRepositoryImpl.kt (implemented deleteSessionById)
- data/local/dao/SessionDao.kt (added deleteById query)

**Validation:**
- Build successful (38 tasks, 35s)

### 2026-01-26 - Steps 1.5.1, 1.5.2, 1.5.3 History & Navigation [COMPLETE]
**What was implemented:**
- HistoryScreen with expandable session cards showing all ends
- HistoryViewModel for loading sessions with end data
- BottomNavBar component with Score, History, Settings tabs
- Settings placeholder screen
- Navigation wrapped in Scaffold with bottom bar
- Bottom bar only shows on main screens (Home, History, Settings)
- Swipe-to-delete also works on History screen

**Files created:**
- ui/screens/history/HistoryScreen.kt
- ui/screens/history/HistoryViewModel.kt
- ui/components/BottomNavBar.kt
- ui/screens/settings/SettingsScreen.kt

**Files modified:**
- ui/navigation/AppNavigation.kt (added Scaffold with bottom bar, History/Settings routes)

**Validation:**
- Build successful (38 tasks, 45s)

### 2026-01-26 - Bottom Navigation Padding Fix (Multiple Iterations)
**Issue:** Extra gap between bottom nav bar and Android system navigation bar

**Iteration 1 - Removed Nested Scaffolds:**
- Removed Scaffold wrapper from HistoryScreen.kt (used Column with TopAppBar instead)
- Removed Scaffold wrapper from SettingsScreen.kt (used Column with TopAppBar instead)
- Removed unused Scaffold imports from both files
- Result: Gap still present

**Iteration 2 - Disabled NavigationBar Internal Insets:**
- Root cause: Material3 `NavigationBar` applies bottom window insets internally via `NavigationBarDefaults.windowInsets`
- Since AppNavigation's Scaffold already positions the bar correctly, this created double inset padding
- Fix: Set `windowInsets = WindowInsets(0, 0, 0, 0)` on NavigationBar

**Files modified:**
- ui/screens/history/HistoryScreen.kt (removed Scaffold, use Column)
- ui/screens/settings/SettingsScreen.kt (removed Scaffold, use Column)
- ui/components/BottomNavBar.kt (added WindowInsets import, disabled internal insets)

### 2026-01-26 - Step 1.6.1 Detection Analysis [IN PROGRESS]

#### Target Detection Issues (OpenCvTargetDetector.kt)

**1. Rigid Circle Detection Parameters**
- `minRadius = gray.rows() / 8` and `maxRadius = gray.rows() / 2` are image-dependent
- Fails for targets that appear smaller/larger in frame
- `minDist = gray.rows() / 4.0` may be too restrictive

**2. Lighting Sensitivity**
- No brightness/contrast normalization before HSV conversion
- HSV color thresholds assume ideal lighting conditions
- Real-world shadows, overexposure cause detection failures

**3. Color Threshold Brittleness**
- Fixed HSV ranges for gold, red, blue, white
- Ratio thresholds (0.3, 0.2, 0.2, 0.15) are arbitrary
- Different target types (e.g., FITA, field) have different colors

**4. Single Target Assumption**
- Always returns "best" circle, no multi-target support
- Cannot handle partially visible targets

#### Arrow Detection Issues (OpenCvArrowDetector.kt)

**1. Line Length Constraints**
- `MIN_LINE_LENGTH = 50` too long for distant arrows, too short for close ones
- No dynamic scaling based on target radius

**2. High False Positive Rate**
- `MIN_GROUP_SIZE = 1` allows single line segments as arrows
- Target ring lines, scoring markers detected as arrows
- High Canny thresholds (80, 200) may miss low-contrast arrows

**3. Radial Orientation Assumption**
- Assumes arrows point toward center (within 45°)
- Arrows at angles, deflected arrows fail detection
- Tip detection (closer end = tip) fails for angled arrows

**4. Grouping Issues**
- `ARROW_GROUP_DISTANCE = 60` may group close arrows together
- May also split a single arrow detected as multiple segments

**5. No Color-Based Detection**
- Only uses edge/line detection
- Could use arrow shaft color (carbon = black, aluminum = silver) for validation

#### Recommended Improvements (for Step 1.6.2/1.6.3)

**Target Detection:**
1. Add adaptive brightness/contrast normalization
2. Use multiple parameter sweeps for HoughCircles
3. Add concentric circle validation (look for ring pattern)
4. Consider template matching as fallback

**Arrow Detection:**
1. Scale MIN_LINE_LENGTH relative to target radius
2. Increase MIN_GROUP_SIZE to 2-3 to reduce false positives
3. Add color validation for arrow shafts
4. Lower Canny thresholds with additional filtering
5. Weight arrows by radial alignment strength

### 2026-01-26 - Step 1.6.2 Target Detection Improvements [COMPLETE]
**What was implemented:**
- Added CLAHE (Contrast Limited Adaptive Histogram Equalization) for lighting normalization
- Multiple HoughCircles parameter sweeps (5 different configurations)
- Parameter sets cover: original, lenient, strict, small targets, large targets
- Circle deduplication to merge similar detections
- Graduated color scoring instead of binary pass/fail
- Expanded HSV color ranges with lower saturation minimums
- Added black ring detection for scoring lines
- Reduced minDist to allow concentric circle detection

**Files modified:**
- detection/opencv/OpenCvTargetDetector.kt

### 2026-01-26 - Step 1.6.3 Arrow Detection Improvements [COMPLETE]
**What was implemented:**
- Dynamic line length scaling based on target radius (MIN_LINE_LENGTH_FACTOR = 0.15)
- Dynamic group distance scaling (ARROW_GROUP_DISTANCE_FACTOR = 0.12)
- Added CLAHE normalization (consistent with target detector)
- Multiple Canny threshold passes (50/150, 80/200, 30/100)
- Increased MIN_GROUP_SIZE from 1 to 2 to reduce false positives
- Lowered HoughLinesP threshold from 70 to 50 for better sensitivity
- Added detailed logging for group sizes before filtering

**Files modified:**
- detection/opencv/OpenCvArrowDetector.kt

**Build Status:** Successful

### 2026-01-27 - Progress Indicator and Timing Diagnostics [COMPLETE]
**Problem:** Image analysis was taking 6+ minutes with no progress feedback - user couldn't tell if it was stuck or working.

**What was implemented:**
- Added `AnalysisStep` enum to track current phase (PREPARING, DETECTING_TARGET, DETECTING_ARROWS, CALCULATING_SCORES, etc.)
- Added detailed progress messages that update in real-time
- Added timing display after completion showing: Target detection time, Arrow detection time, Total time
- Added `[TIMING]` logs throughout detectors for debugging:
  - OpenCvTargetDetector: logs each of 5 HoughCircles parameter sweeps with duration
  - OpenCvArrowDetector: logs each of 3 Canny+HoughLinesP sweeps with duration
  - Both log preprocessing steps (bitmapToMat, grayscale, CLAHE, GaussianBlur)

**UI Changes:**
- Progress screen now shows current step: "Detecting target...", "Detecting arrows...", etc.
- Sub-detail text shows additional info like image dimensions
- Results screen shows timing info for debugging

**Files modified:**
- ui/screens/review/ReviewScreen.kt (progress states, timing, UI updates)
- detection/opencv/OpenCvTargetDetector.kt (timing logs for each sweep)
- detection/opencv/OpenCvArrowDetector.kt (timing logs for each sweep)

**Note:** The 6-minute detection time is likely caused by the 5+3 parameter sweeps added in 1.6.2/1.6.3. Timing logs will help identify which sweeps are slow so we can optimize later.

**Build Status:** Successful

### 2026-01-27 - Image Downscaling/Cropping Optimization [COMPLETE]
**Problem:** Detection was taking 6+ minutes on high-resolution images (e.g., 4000x3000 from phone cameras).

**Solution - Hybrid approach:**
1. **Target detection**: Downscale image to max 1200px on longest side
   - Circles are large features, easy to detect at lower resolution
   - Scale factor calculated, coordinates scaled back to original after detection
2. **Arrow detection**: Crop to target region at FULL resolution
   - Uses 1.3x target radius as margin for crop
   - Preserves full detail for small arrow tips
   - Much fewer pixels to process (target area vs whole image)

**Implementation:**
- `TARGET_DETECTION_MAX_SIZE = 1200` - max dimension for target detection
- `ARROW_CROP_MARGIN = 1.3f` - crop region = target radius * 1.3
- Downscaled bitmap created with `Bitmap.createScaledBitmap()`
- Cropped bitmap created with `Bitmap.createBitmap(src, x, y, w, h)`
- Coordinates adjusted when scaling back to original image space

**Expected speedup:**
- 4000x3000 → 1200x900 for target = ~11x fewer pixels
- Arrow detection on ~2000x2000 crop instead of 4000x3000 = ~2-3x fewer pixels

**Files modified:**
- ui/screens/review/ReviewScreen.kt

**Build Status:** Successful

### 2026-01-27 - HoughCircles Optimization + Partial Target Support [COMPLETE]
**Problem:**
1. HoughCircles with 5 sweeps was still slow even on 1200px image
2. Color validation required all rings (gold/red/blue/black/white) - failed when user zoomed in on center

**Changes:**
1. **Reduced HoughCircles from 5 sweeps to 2:**
   - Sweep 1: Balanced parameters (p1=100, p2=50)
   - Sweep 2: Lenient parameters (p1=70, p2=35)
   - Should reduce HoughCircles time by ~60%

2. **Updated color validation for partial targets:**
   - Gold (center): Highest weight (up to 2.0 score) - most likely visible when zoomed
   - Red (inner): High weight (up to 1.5 score)
   - Blue (middle): Medium weight (up to 1.0 score) - may be cropped
   - Black lines: Bonus only (0.5) - helpful but not required
   - White (outer): Bonus only (0.5) - often cropped out
   - Extra bonus (0.5) if any target colors detected
   - Added logging of color ratios for debugging

**Files modified:**
- detection/opencv/OpenCvTargetDetector.kt
- ui/screens/review/ReviewScreen.kt (progress message update)

**Build Status:** Successful

### 2026-01-27 - Phase 2 Step 0: Auto-Detection Toggle [COMPLETE]
**What was implemented:**
- Added `autoDetectEnabled` state to HomeViewModel with persistence
- Added toggle switch UI on HomeScreen for enabling/disabling auto-detection
- Updated navigation to route to ArrowEditScreen or ReviewScreen based on toggle state

**Files modified:**
- ui/screens/home/HomeViewModel.kt (added autoDetectEnabled state)
- ui/screens/home/HomeScreen.kt (added toggle UI)
- ui/navigation/AppNavigation.kt (conditional navigation routing)

### 2026-01-27 - Phase 2 Step 1: Calendar View [COMPLETE]
**What was implemented:**
- CalendarScreen with monthly view showing dots on days with sessions
- CalendarViewModel for loading sessions and grouping by date
- DayDetailScreen showing all sessions for a selected day
- DayDetailViewModel for loading sessions for a specific date
- Added calendar icon button to HistoryScreen header
- Navigation routes for Calendar and DayDetail screens

**Files created:**
- ui/screens/calendar/CalendarScreen.kt
- ui/screens/calendar/CalendarViewModel.kt
- ui/screens/calendar/DayDetailScreen.kt
- ui/screens/calendar/DayDetailViewModel.kt

**Files modified:**
- ui/screens/history/HistoryScreen.kt (added calendar icon)
- ui/navigation/AppNavigation.kt (added Calendar, DayDetail routes)
- gradle/libs.versions.toml (added kizitonwose calendar 2.5.0)
- app/build.gradle.kts (added calendar dependency)

### 2026-01-27 - Phase 2 Step 2: Statistics Screen [COMPLETE]
**What was implemented:**
- StatisticsScreen with charts showing score trends, averages, and distributions
- StatisticsViewModel for aggregating statistics data
- StatisticsRepository for statistics queries and calculations
- Added statistics queries to SessionDao, EndDao, ArrowScoreDao
- Added Statistics as 4th tab in BottomNavBar

**Files created:**
- ui/screens/statistics/StatisticsScreen.kt
- ui/screens/statistics/StatisticsViewModel.kt
- data/repository/StatisticsRepository.kt

**Files modified:**
- ui/components/BottomNavBar.kt (added Statistics tab)
- ui/navigation/AppNavigation.kt (added Statistics route)
- data/local/dao/SessionDao.kt (added statistics queries)
- data/local/dao/EndDao.kt (added statistics queries)
- data/local/dao/ArrowScoreDao.kt (added statistics queries)
- gradle/libs.versions.toml (added Vico charts 1.13.1)
- app/build.gradle.kts (added Vico dependency)

### 2026-01-27 - Phase 2 Step 3: Data Export/Import [COMPLETE]
**What was implemented:**
- DataExportService for exporting all sessions to JSON format
- DataImportService for importing sessions from JSON with conflict handling
- SettingsViewModel for managing export/import operations
- Complete rewrite of SettingsScreen with export/import UI, file pickers, and feedback

**Files created:**
- data/export/DataExportService.kt
- data/export/DataImportService.kt
- ui/screens/settings/SettingsViewModel.kt

**Files modified:**
- ui/screens/settings/SettingsScreen.kt (complete rewrite)
- gradle/libs.versions.toml (added kotlinx.serialization 1.6.3)
- app/build.gradle.kts (added serialization plugin and dependency)

**Phase 2 Complete:** All progress tracking features implemented.

### 2026-01-28 - Phase 2 Bug Fixes and UI Improvements [COMPLETE]

#### Step 3: Fix Calendar Crash [COMPLETE]
**Problem:** CalendarViewModel.loadSessions() called getEndsForSessionSync() in a loop, causing ANR on large datasets.

**Solution:**
- Replaced N+1 query pattern with optimized batch queries
- Added `getArrowCountForSession()` to ArrowScoreDao
- Use `Dispatchers.IO` for database operations
- Removed dependency on ScoreRepository in CalendarViewModel

**Files modified:**
- `ui/screens/calendar/CalendarViewModel.kt` - Use optimized queries
- `data/local/dao/ArrowScoreDao.kt` - Added `getArrowCountForSession()`, `deleteAll()`

#### Step 4: Fix Statistics Score Trend Bug [COMPLETE]
**Problem:** getSessionTrends() grouped sessions by date, so multiple same-day sessions = 1 data point.

**Solution:**
- Changed to return individual sessions instead of grouping by date
- Added `sessionId` and `timestamp` fields to `SessionTrendData`
- Sort by timestamp for correct chronological order

**Files modified:**
- `data/repository/StatisticsRepository.kt` - Rewrote `getSessionTrends()` to not group by date

#### Step 5: Add Wipe All Data Feature [COMPLETE]
**What was implemented:**
- Added `deleteAll()` methods to SessionDao, EndDao, ArrowScoreDao
- Added `wipeAllData()` function to SettingsViewModel
- Added "Danger Zone" section in SettingsScreen with confirmation dialog
- Delete order respects foreign key constraints (arrows → ends → sessions)

**Files modified:**
- `data/local/dao/SessionDao.kt` - Added `deleteAll()`
- `data/local/dao/EndDao.kt` - Added `deleteAll()`
- `data/local/dao/ArrowScoreDao.kt` - Added `deleteAll()`
- `ui/screens/settings/SettingsViewModel.kt` - Added `wipeAllData()`, `isWiping`/`wipeSuccess` state
- `ui/screens/settings/SettingsScreen.kt` - Added wipe data card and confirmation dialog

#### Step 1: HomeScreen UI Redesign [COMPLETE]
**Problem:** Auto-detect toggle was confusing. Users wanted explicit "take picture" vs "manual input" options.

**Solution:**
- Removed `AutoDetectToggle` composable
- Removed `autoDetectEnabled` state from HomeViewModel
- Replaced with two side-by-side buttons: "Take Picture" and "Input Score"
- Removed "Upload Image" button (moved to calendar)

**Files modified:**
- `ui/screens/home/HomeScreen.kt` - New button layout, removed toggle
- `ui/screens/home/HomeViewModel.kt` - Removed `autoDetectEnabled` state
- `ui/navigation/AppNavigation.kt` - Updated callbacks

#### Step 2: Create Score Input Screens [COMPLETE]
**What was implemented:**
- `ScoreInputMethodScreen` - Selection screen with "Place on Target" and "Enter Numbers" options
- `NumericScoreInputScreen` - Grid of score buttons (X, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, M)
  - Arrow chips display with tap-to-remove
  - Color-coded buttons matching target ring colors
  - Creates ScoringResult for Results screen

**Files created:**
- `ui/screens/scoreinput/ScoreInputMethodScreen.kt`
- `ui/screens/scoreinput/NumericScoreInputScreen.kt`

**Files modified:**
- `ui/navigation/AppNavigation.kt` - Added `ScoreInputMethod`, `NumericScoreInput` routes

#### Step 6: Move Upload Image to Calendar [COMPLETE]
**What was implemented:**
- Added FAB with "+" icon to DayDetailScreen
- FAB navigates to ScoreInputMethodScreen
- Users can add scores from calendar day view

**Files modified:**
- `ui/screens/calendar/DayDetailScreen.kt` - Added FAB, `onAddScore` callback
- `ui/navigation/AppNavigation.kt` - Connected DayDetail FAB to ScoreInputMethod

**Build Status:** Successful (`./gradlew assembleDebug` - 38 tasks)

**Phase 2 Bug Fixes Complete:** All bug fixes and UI improvements implemented.

### 2026-01-28 - Session Number Display on HomeScreen [COMPLETE]
**Problem:** When user has multiple sessions on the same day, there was no indication of which session number they're on.

**Solution:**
- Added `sessionNumberToday` and `totalSessionsToday` to `HomeUiState`
- HomeViewModel calculates session position among today's sessions
- HomeScreen displays "Session X of Y today" when there are multiple sessions

**Files modified:**
- `ui/screens/home/HomeViewModel.kt` - Added session counting logic in `loadTodaySession()` and `setActiveSession()`
- `ui/screens/home/HomeScreen.kt` - Added conditional text display in `ActiveSessionCard`

### 2026-01-28 - ResultsScreen Button State Changes [COMPLETE]
**Problem:** After saving an end, the buttons still showed "Save End" and "Discard End" which was confusing.

**Solution:**
- Added `endSaved: Boolean` and `savedEndId: Long?` to `ResultsUiState`
- After successful save, set `endSaved = true` and capture `savedEndId`
- Button text/icons change based on `endSaved` state:
  - Before save: "Save End" (save icon) / "Discard End"
  - After save: "Edit End" (edit icon) / "Back to Home" (home icon)

**Files modified:**
- `ui/screens/results/ResultsViewModel.kt` - Added `endSaved`, `savedEndId` state tracking
- `ui/screens/results/ResultsScreen.kt` - Dynamic button text/icons, added Edit and Home icon imports

### 2026-01-28 - Fix "Edit End" Navigation [COMPLETE]
**Problem:** "Edit End" button silently deleted and re-saved the same data instead of navigating back to ScoreInputMethod screen for re-entry.

**Solution:**
- Changed `editEnd()` to `deleteEndForEdit()` which deletes the saved end and triggers navigation
- Added `navigateToEdit` flag to UI state for navigation coordination
- Added `onEditEnd` callback to ResultsScreen for navigation handling

**Flow after fix:**
1. User saves an end → buttons become "Edit End" / "Back to Home"
2. User taps "Edit End" → ViewModel deletes the saved end from DB → navigates to ScoreInputMethod
3. User picks input method → enters new scores → arrives at fresh ResultsScreen
4. Fresh ResultsScreen shows "Save End" / "Discard End" as normal

**Files modified:**
- `ui/screens/results/ResultsViewModel.kt`
  - Added `navigateToEdit: Boolean = false` to `ResultsUiState`
  - Replaced `editEnd(scoringResult)` with `deleteEndForEdit()` - deletes end, sets `navigateToEdit = true`
  - Added `clearNavigateToEdit()` to reset the navigation flag
- `ui/screens/results/ResultsScreen.kt`
  - Added `onEditEnd: () -> Unit` parameter
  - Added `LaunchedEffect(uiState.navigateToEdit)` to trigger navigation callback
  - Changed "Edit End" button onClick from `viewModel.editEnd(scoringResult)` to `viewModel.deleteEndForEdit()`
- `ui/navigation/AppNavigation.kt`
  - Added `onEditEnd` callback to ResultsScreen that clears shared state and navigates to ScoreInputMethod

**Build Status:** Successful (`./gradlew assembleDebug` - 38 tasks)

### 2026-04-10 - Phase 3 Step 3.1.1: Add BLE Dependencies [COMPLETE]
**What was implemented:**
- Added Nordic Android BLE library to version catalog (`ble` + `ble-ktx` 2.7.5) and BLE scanner library (`scanner` 1.6.0)
- Wired all three libraries into `app/build.gradle.kts` dependencies block
- Added BLE permissions to `AndroidManifest.xml`:
  - Android 12+ (API 31+): `BLUETOOTH_SCAN` (with `neverForLocation` flag to avoid runtime location permission), `BLUETOOTH_CONNECT`
  - Legacy (Android 11 and below, maxSdkVersion=30): `BLUETOOTH`, `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION`
- Added `<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />`

**Rationale for permission design:**
- `neverForLocation` flag tells Android we don't derive device location from BLE scans, so runtime FINE_LOCATION permission is NOT required on Android 12+. This significantly simplifies the runtime permission flow for most modern devices.
- Legacy permissions gated by `maxSdkVersion=30` so they only apply to pre-Android-12 devices, preventing duplicate permission requests on new devices.

**Files modified:**
- `gradle/libs.versions.toml` (added `nordicBle`, `nordicScanner` versions; `nordic-ble`, `nordic-ble-ktx`, `nordic-scanner` library entries)
- `app/build.gradle.kts` (added three `implementation(libs.nordic.*)` lines)
- `app/src/main/AndroidManifest.xml` (added feature declaration + 5 permission entries)

**Validation Test Results:**
- `./gradlew assembleDebug` — BUILD SUCCESSFUL in 5m 2s, 38 tasks executed, all 38 executed (no cache hits for the new dependency resolution pass)
- Nordic BLE artifacts resolved from Maven Central without issue
- Two pre-existing CameraX deprecation warnings in `CameraScreen.kt` (unrelated to this step; untouched)
- No compile errors, no new warnings from the Nordic libs

**Awaiting real-device testing:** Deferred per user — BLE permissions cannot be meaningfully verified without a device, and will be validated together with later Phase 3 steps.

### 2026-04-11 - Phase 3 Step 3.1.2: BLE Permission Handler [COMPLETE]
**What was implemented:**
- New file `data/bluetooth/BlePermissionHandler.kt` containing a pure-logic `object` (no Android UI coupling) plus a `BleReadiness` enum
- `requiredRuntimePermissions()` — version-aware list:
  - API 31+ (Android 12+): `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT`
  - API ≤ 30: `ACCESS_FINE_LOCATION` (legacy requirement for BLE scans)
- `isBleSupported()` — `PackageManager.FEATURE_BLUETOOTH_LE` check
- `isBluetoothEnabled()` — adapter presence + enabled state via `BluetoothManager`
- `hasAllPermissions()` / `missingPermissions()` — granted-state checks via `ContextCompat.checkSelfPermission`
- `readiness()` — single-call resolver returning `BleReadiness` enum for UI decision making:
  - `UNSUPPORTED` → no BLE hardware
  - `PERMISSIONS_MISSING` → required runtime permissions not granted
  - `BLUETOOTH_DISABLED` → adapter off
  - `READY` → all checks pass

**Design decisions:**
- Kept as a Kotlin `object` (pure static logic) to stay decoupled from Compose/ViewModel layers — UI integration deferred to Step 3.2.1 (Device Connection Screen)
- No coroutine/Flow API in this layer — readiness is a synchronous one-shot; continuous state observation belongs to the BLE Connection Manager (Step 3.1.4)
- Complements the manifest's `neverForLocation` flag: on Android 12+, users only see two runtime prompts (SCAN, CONNECT), not three

**Files created:**
- `app/src/main/java/com/example/archeryapp/data/bluetooth/BlePermissionHandler.kt`

**Validation Test Results:**
- `./gradlew assembleDebug` — BUILD SUCCESSFUL in 10s, 38 tasks (7 executed, 31 up-to-date)
- Incremental `compileDebugKotlin` succeeded with zero errors/warnings for the new file
- No regressions — prior tasks hit cache as expected

### 2026-04-11 - Phase 2.4 Plan Inserted [COMPLETE]
**What was added:**
- New `Phase 2.4: Target Type Support` section inserted into `DEVELOPMENT_PLAN.md` between Phase 2.3 and Phase 3
- 11 sub-steps planned (2.4.1 through 2.4.11) covering: domain enum + scoring strategies, DB migration v1→v2, session domain propagation, last-used preference, picker dialog, session creation flow, HomeScreen badge/end-session/auto-detect gating, scoring application, per-target statistics, session list display, export/import schema
- HARD STOP defined after 2.4.2 (DB migration) — requires real-device verification of legacy data survival before proceeding
- Soft checkpoint after 2.4.7

**Design decisions locked (user-confirmed):**
- Blue Face scoring: rings 1-5 count as M (miss); valid scores are X, 10, 9, 8, 7, 6, M
- Mini MC and Triple use identical scoring math to MC (MultiColorScoringStrategy)
- Target type locked at session creation — changing requires ending session
- Legacy sessions backfilled as `MINI_MC`
- New session picker defaults to last-used target type (persisted in SharedPreferences)
- Auto-detect ("Take Picture") disabled for non-MC targets — user must use "Input Score"
- Detection improvements for non-MC targets deferred indefinitely (user noted the whole CV pipeline may be removed or rewritten later)

### 2026-04-11 - Phase 2.4 Step 2.4.1: Target Type Domain + Scoring Strategies [COMPLETE]
**What was implemented:**
- `domain/model/TargetType.kt` — enum `{MC, BF, MINI_MC, TRIPLE}` with `code` and `displayName` fields; `fromCode(String?)` companion defaulting to `MINI_MC` on null/unknown for legacy safety
- `domain/scoring/ScoringStrategy.kt` — interface with `normalize(rawRingScore, isX): Int` and `isButtonVisible(ringValue): Boolean`; companion `forTarget(TargetType)` factory
- `domain/scoring/MultiColorScoringStrategy.kt` — `object`, all rings 0–10 valid, pass-through normalization. Used by MC, MINI_MC, TRIPLE.
- `domain/scoring/BlueFaceScoringStrategy.kt` — `object`, demotes rings 1–5 to 0 (miss); only shows buttons for 0 and 6–10
- `app/src/test/java/.../ScoringStrategyTest.kt` — 12 JUnit tests covering both strategies, the factory, and `TargetType.fromCode` edge cases

**Design decisions:**
- Strategies are Kotlin `object` singletons (stateless) — no allocation cost per arrow, trivially testable, no DI plumbing needed
- `normalize` takes `isX` for future symmetry even though neither current strategy needs it — keeping the interface forward-compatible for potential target types that treat X differently
- `isButtonVisible` lives on the strategy (not the TargetType) because it's scoring policy, not target identity
- `BlueFace.isButtonVisible(0) = true` — explicit miss remains pickable in the numeric input

**Files created:**
- `app/src/main/java/com/example/archeryapp/domain/model/TargetType.kt`
- `app/src/main/java/com/example/archeryapp/domain/scoring/ScoringStrategy.kt`
- `app/src/main/java/com/example/archeryapp/domain/scoring/MultiColorScoringStrategy.kt`
- `app/src/main/java/com/example/archeryapp/domain/scoring/BlueFaceScoringStrategy.kt`
- `app/src/test/java/com/example/archeryapp/domain/scoring/ScoringStrategyTest.kt`

**Validation Test Results:**
- `./gradlew assembleDebug testDebugUnitTest` — BUILD SUCCESSFUL in 18s, 45 tasks (12 executed, 33 up-to-date)
- `ScoringStrategyTest`: **12 tests, 0 failures, 0 errors, 0 skipped** (total runtime ~7 ms)
- All 12 cases pass: multicolor pass-through, multicolor button visibility, BF demotes 1-5, BF keeps 6-X, BF miss preservation, BF hides 1-5, BF shows 0 and 6-10, factory MC/MINI_MC/TRIPLE → MultiColor, factory BF → BlueFace, fromCode null/unknown → MINI_MC, fromCode known → matching enum

### 2026-04-10 - Phase 2.4 Step 2.4.2: Database Migration v1→v2 [CODE-COMPLETE, AWAITING DEVICE VERIFICATION]
**What was implemented:**
- `SessionEntity` gained a 7th field: `val targetType: String? = null` — nullable TEXT column storing `TargetType.code`; default value in the Kotlin constructor only (not as a SQL DEFAULT)
- `data/local/database/Migrations.kt` — new file containing `MIGRATION_1_2` which runs `ALTER TABLE sessions ADD COLUMN targetType TEXT` followed by `UPDATE sessions SET targetType = 'MINI_MC' WHERE targetType IS NULL`
- `AppDatabase` version bumped 1→2, `exportSchema` flipped to `true`, `.addMigrations(MIGRATION_1_2)` added to the builder
- `app/build.gradle.kts` — added `ksp { arg("room.schemaLocation", "$projectDir/schemas") }` so schema JSONs are version-controlled

**Database-safety design decisions (per user: "make sure the old database doesn't die. Triple check this"):**
- **NO `fallbackToDestructiveMigration`** — if migration fails the app crashes loudly rather than wiping data
- **Bare `ADD COLUMN TEXT` with no `DEFAULT` clause** — Room's post-migration schema comparison uses the entity's `@ColumnInfo` to build the "expected" schema, which has no SQL default (the Kotlin `= null` is a constructor default, not a column default). If the SQL had `DEFAULT NULL` the identity hash would mismatch and Room would throw. Verified this matches the generated schema v2 JSON.
- **Two-statement migration**: ALTER creates the column NULL-filled, UPDATE backfills existing rows to `'MINI_MC'`. New rows from fresh installs will also go through the same UPDATE path (which is a no-op on empty tables).
- **Legacy backfill value = `'MINI_MC'`** — matches user's stated truth ("legacy sessions are all mini MC") and aligns with `TargetType.fromCode(null) == MINI_MC` so in-memory reads of pre-migration rows would also behave correctly even without the UPDATE.

**Files changed/created:**
- `app/src/main/java/com/example/archeryapp/data/local/entity/SessionEntity.kt` (added `targetType` field)
- `app/src/main/java/com/example/archeryapp/data/local/database/Migrations.kt` (NEW)
- `app/src/main/java/com/example/archeryapp/data/local/database/AppDatabase.kt` (version, exportSchema, addMigrations)
- `app/build.gradle.kts` (ksp schema arg)
- `app/schemas/com.example.archeryapp.data.local.database.AppDatabase/2.json` (GENERATED by KSP)

**Validation Test Results:**
- `./gradlew assembleDebug` — **BUILD SUCCESSFUL in 1m 20s** (38 tasks, 8 executed, 30 up-to-date). KSP + kotlinc + dex + packageDebug all clean.
- Schema v2 JSON generated at `app/schemas/.../AppDatabase/2.json`:
  - `identityHash`: `e7d37a3856b634d9dab25ec0b49473c2`
  - `sessions.createSql`: `CREATE TABLE ... (\`id\` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \`date\` INTEGER NOT NULL, \`distance\` TEXT, \`bowType\` TEXT, \`location\` TEXT, \`notes\` TEXT, \`targetType\` TEXT)`
  - `targetType` field: `affinity: TEXT`, `notNull: false` — matches migration SQL exactly, so Room's hash check will pass after MIGRATION_1_2 runs
- No v1 schema file exists (expected — exportSchema was false during v1 development). This is fine because migrations are validated by Room at runtime against the actual SQLite `room_master_table` identity hash, not against an archived JSON.

**HARD STOP — Real-device verification required before 2.4.3:**
User must install the debug APK on a device that already has v1 data and confirm all three:
1. App opens without crash (Room migration runs successfully on startup)
2. All prior sessions are still visible in History and Calendar screens
3. Existing sessions show `targetType = 'MINI_MC'` (verify via Android Studio Database Inspector, or implicitly via 2.4.9 statistics once that's built)

Do NOT proceed to 2.4.3 Session Domain Propagation until the user confirms data survival.

### 2026-04-10 - Step 2.4.2 Device Verification Results + kizitonwose Calendar Hotfix [COMPLETE]
**Device verification of 2.4.2 migration:**
- User installed debug APK on real device with existing v1 data
- History screen: all prior sessions present ✓
- Statistics screen: all prior data present ✓
- Calendar screen: **CRASHED** (but not due to migration — see below)
- Conclusion: **MIGRATION_1_2 succeeded**. Room's `exportSchema=true` + `addMigrations(MIGRATION_1_2)` path ran cleanly on a populated v1 database. `targetType` column was added and backfilled. HARD STOP cleared on the DB-safety front.

**Separate latent bug discovered: Calendar crash (unrelated to 2.4.2):**
- Stack trace root cause: `java.lang.ClassNotFoundException: androidx.compose.foundation.gestures.snapping.SnapPositionInLayout`
- Not caused by Phase 2.4 work — pre-existing dependency mismatch that only surfaced once user tested the Calendar screen on-device (emulator builds had not exercised this path recently)
- **Root cause:** kizitonwose Calendar 2.5.0 was compiled against Compose Foundation 1.5.x where `SnapPositionInLayout` was a public class. In Compose Foundation 1.6+ it was replaced by the `SnapPosition` interface and the old class was eventually removed. The project's Compose BOM `2024.09.00` resolves Foundation to 1.7.x (class gone), so at runtime kizitonwose 2.5.0's bytecode couldn't link.
- **Why it didn't show up earlier:** `./gradlew assembleDebug` succeeds because Android dex'ing is lazy about linking — the missing class only matters when a code path that touches it actually runs. Calendar screen was the first code path doing that on-device.

**Fix applied (user pre-authorized dependency changes for this issue):**
- `gradle/libs.versions.toml`: `kizitonwoseCalendar = "2.5.0"` → `"2.6.2"` with inline comment explaining the Compose Foundation 1.7.x compatibility requirement
- Compose BOM left at `2024.09.00` (no changes — deliberately minimizing blast radius)
- All calendar API usages in `CalendarScreen.kt` (`HorizontalCalendar`, `rememberCalendarState`, `CalendarDay`, `DayPosition`, `daysOfWeek`, `firstDayOfWeekFromLocale`) are source-compatible between 2.5.0 and 2.6.2 — no code changes required
- Verified by `./gradlew assembleDebug` → **BUILD SUCCESSFUL in 2m 16s** (38 tasks, 15 executed, 23 up-to-date). No new compiler errors or warnings from the calendar library. Only warnings in output are pre-existing CameraX `setTargetAspectRatio` deprecations unrelated to this fix.

**Files changed for the hotfix:**
- `gradle/libs.versions.toml` (version bump + inline explanatory comment)
- `CLAUDE.md` (updated "Key libraries" line: 2.5.0 → 2.6.2)
- `memory-claude/app-design.md` (updated illustrative dependency snippet)
- `memory-claude/architecture.md` (added new row to Decision Log documenting the upgrade and its rationale)
- `memory-claude/achievements.md` (this entry; also noted the upgrade next to the original 2.1 Calendar Complete row so future readers see the current version at a glance)

**Files intentionally NOT changed:**
- `app/src/main/java/com/example/archeryapp/ui/screens/calendar/CalendarScreen.kt` — all kizitonwose APIs used are source-compatible across the upgrade
- `memory-claude/DEVELOPMENT_PLAN.md` — Step 2.1.1 ("Add kizitonwose calendar library") doesn't mention a version and the plan is historically accurate as written
- `app/build.gradle.kts` — only references the catalog entry (`libs.kizitonwose.calendar.compose`), so the version bump propagates automatically

**User action still required:** Reinstall the updated debug APK on the device and re-verify:
1. Calendar screen now opens without crash
2. Previously-saved sessions still appear in Calendar (confirms 2.4.2 migration persistence across this second install)
3. Tap a day with sessions; verify DayDetailScreen still works
Once confirmed, we can proceed to Step 2.4.3 (Session Domain Propagation).

### 2026-04-10 - Calendar Month Chevron Wiring Bugfix [COMPLETE]
**Problem:** After the kizitonwose hotfix, user confirmed the Calendar screen opens without crash, but tapping the left/right chevron buttons in the month header had no effect — they could not navigate between months.

**Root cause:** Pre-existing bug in `CalendarScreen.kt`, not caused by any recent change. Both chevron `IconButton` onClick handlers were stub implementations: they computed `newMonth` but contained only a `// Navigation handled by scroll` placeholder comment and no actual scroll call. The buttons had never actually done anything since they were written. Month navigation previously worked only via horizontal swipe gesture on the calendar body.

**Fix applied:**
- Added `rememberCoroutineScope()` import and local instance in `CalendarScreen`
- Wired both chevron onClick handlers to call `coroutineScope.launch { calendarState.animateScrollToMonth(newMonth) }` (guarded by the existing `startMonth`/`endMonth` bounds check)
- Added `kotlinx.coroutines.launch` import

**Files changed:**
- `app/src/main/java/com/example/archeryapp/ui/screens/calendar/CalendarScreen.kt` (two chevron onClick bodies + 2 imports)

**Validation:**
- `./gradlew assembleDebug` → **BUILD SUCCESSFUL in 21s** (38 tasks, 10 executed, 28 up-to-date)

**Why this was never caught:** No unit or instrumentation test exercises the calendar month navigation path. Manual testing historically focused on swipe gesture, which does work independently of the chevron buttons. Note for future: if horizontal swipe also broke on the user's device after the 2.6.2 upgrade, that would be a separate issue to investigate — the chevron fix addresses the explicit tap path regardless.

### 2026-04-11 - Phase 2.4 Step 2.4.3: Session Domain Propagation [COMPLETE]
**What was implemented:**
- `domain/model/Session.kt` — added `val targetType: TargetType = TargetType.MINI_MC` as a new field positioned between `notes` and `ends`. Default is legacy-safe.
- `data/repository/SessionMappers.kt` (NEW) — extracted `Session.toEntity()` and `SessionEntity.toDomain()` as top-level `internal` functions in the same package as `SessionRepositoryImpl`. Mappers now handle targetType via `TargetType.fromCode(String?)` inbound and `targetType.code` outbound.
- `data/repository/SessionRepositoryImpl.kt` — removed the two private extension mappers; calls the top-level versions via package-level resolution. Also pruned unused imports (`SessionEntity`, `Instant`).
- `domain/usecase/SaveScoreUseCase.kt` — added `sessionTargetType: TargetType = TargetType.MINI_MC` parameter to `execute()`. When creating a new session (sessionId == null), the parameter propagates into the new `Session`. When continuing an existing session, the parameter is ignored — the existing row's stored targetType is never overwritten, matching the spec's "locked at creation" rule.
- `app/src/test/java/.../data/repository/SessionMappersTest.kt` (NEW) — 7 JUnit tests: domain→entity encodes BF code, encodes code for every TargetType, entity BF maps back to enum, null targetType backfills to MINI_MC (legacy path), round-trip BF preserves, round-trip preserves every TargetType, round-trip preserves all non-target fields alongside targetType.

**Design decisions:**
- **Extracted mappers to top-level file** rather than keeping them as private extensions inside `SessionRepositoryImpl`. Rationale: pure functions (no DAO, no Room, no coroutines) are trivially unit-testable and align with CLAUDE.md's modularity mandate. The `internal` visibility keeps them out of other modules while allowing the test source set full access.
- **No change to `SessionRepository` interface** despite the plan text saying "Add targetType parameter to session-creation methods". Since `Session` is a data class and already carries every field, pushing targetType through the existing `createSession(session: Session)` signature is cleaner than adding a redundant parameter. The field itself is the plumbing. This deviation is deliberate and documented here.
- **Field position in Session**: placed `targetType` between `notes` and `ends` rather than at the end. All three existing construction sites (`SaveScoreUseCase`, `SessionRepositoryImpl.toDomain`, `SessionSelectViewModel.createNewSession`) use named arguments, so positional ordering is safe. Placing it before `ends` keeps `ends` (the "body" of the session) as the last field.
- **`SaveScoreUseCase.sessionTargetType` default = `MINI_MC`**: existing callers (e.g., `ResultsViewModel` which doesn't know about target type yet) continue to work without modification. Once 2.4.6 wires the picker into the create-session flow, the default becomes irrelevant for new sessions.

**Files changed/created:**
- Modified: `app/src/main/java/com/example/archeryapp/domain/model/Session.kt`
- Created: `app/src/main/java/com/example/archeryapp/data/repository/SessionMappers.kt`
- Modified: `app/src/main/java/com/example/archeryapp/data/repository/SessionRepositoryImpl.kt`
- Modified: `app/src/main/java/com/example/archeryapp/domain/usecase/SaveScoreUseCase.kt`
- Created: `app/src/test/java/com/example/archeryapp/data/repository/SessionMappersTest.kt`

**Files NOT changed:**
- `app/src/main/java/com/example/archeryapp/domain/repository/SessionRepository.kt` — interface unchanged, see design decision above
- `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectViewModel.kt` — still creates sessions without specifying targetType, which falls back to `MINI_MC` via default. Will be properly wired in Step 2.4.6.
- `app/src/main/java/com/example/archeryapp/ui/screens/results/ResultsViewModel.kt` — same reasoning; existing `SaveScoreUseCase.execute()` callers get the default.
- `app/src/main/java/com/example/archeryapp/data/export/DataExportService.kt` — has its own `ExportSession` type, not the domain Session. Will be updated in Step 2.4.11.
- `app/src/main/java/com/example/archeryapp/data/export/DataImportService.kt` — does not construct domain `Session` directly.

**Validation Test Results:**
- `./gradlew assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL in 22s** (45 tasks, 18 executed, 27 up-to-date)
- `SessionMappersTest`: **7 tests, 0 failures, 0 errors, 0 skipped** (total runtime ~21 ms)
- `ScoringStrategyTest`: **12 tests, 0 failures** (regression check — confirms 2.4.1 work still passes)
- `ExampleUnitTest`: 1 test, 0 failures (stock Android test)
- **Total: 20 tests, 0 failures**

**Plan checklist:**
- [x] Build succeeds
- [x] Round-trip: create session with BF → read back → assert targetType preserved (covered by `round-trip preserves BF` and `round-trip preserves every target type`)
- [x] Legacy sessions (created pre-migration) deserialize as MINI_MC (covered by `entity with null targetType is legacy-backfilled to MINI_MC`)

Minor step — emulator/build validation only. Device re-test not required for this step. Ready for Step 2.4.4 (UserPreferences for last-used target type) on user confirmation.

### 2026-04-11 - Phase 2.4 Step 2.4.4: Last-Used Target Type Preference [COMPLETE]
**What was implemented:**
- `data/preferences/UserPreferences.kt` (NEW) — small class with `getLastTargetType()` and `setLastTargetType(type)`. Reads/writes a single key in a dedicated SharedPreferences file (`archery_user_preferences`, key `last_target_type`). Unset reads fall through `TargetType.fromCode(null)` → `MINI_MC`.
- `UserPreferences` takes a tiny `internal interface TargetTypeStore { read(): String?; write(code: String) }` in its primary constructor; a public secondary constructor takes `Context` and wraps it in the production `SharedPrefsTargetTypeStore` implementation. This allows JVM unit tests to substitute a fake store without needing Robolectric or Android instrumentation.
- `SharedPrefsTargetTypeStore` (private class in the same file) calls `context.applicationContext.getSharedPreferences(...)` to avoid leaking an Activity reference and writes with `.apply()` (async, fire-and-forget — correct semantics for a one-enum preference that doesn't need to be durable across process death within the same tick).
- `app/src/test/java/.../data/preferences/UserPreferencesTest.kt` (NEW) — 5 JUnit tests covering: unset → MINI_MC, set BF then get BF, round-trip every TargetType, later write overwrites earlier write, corrupted/unknown stored code falls back to MINI_MC via `TargetType.fromCode`.

**Design decisions:**
- **Tiny internal store abstraction over raw SharedPreferences.** The plan said "Keep the class small and single-purpose; DataStore is overkill for one enum." A single-method interface with 2 functions is still single-purpose and doesn't approach DataStore's complexity. It buys full JVM testability without adding Robolectric as a dependency. The alternative (using raw `SharedPreferences` in the constructor) would force either (a) skipping unit tests entirely or (b) adding Robolectric, both worse.
- **Separate SharedPreferences file** (`archery_user_preferences`) rather than the app's default prefs. Keeps user-facing preferences namespaced away from anything else the app might persist later, and makes it trivial to clear/export just user prefs during debugging.
- **`applicationContext` capture** inside the store class — prevents accidentally holding a reference to a destroyed Activity/Fragment if `UserPreferences(activityContext)` is ever called with a shorter-lived context.
- **No caching of the enum in memory.** Every call hits SharedPreferences. SharedPreferences itself caches in memory after first read, so this is effectively free, and avoids any staleness bugs if something else ever writes to the same key.
- **`internal` visibility on `TargetTypeStore` and the `UserPreferences` primary constructor** — keeps the abstraction out of other modules' API surface while remaining accessible from the test source set within the same module.

**Files changed/created:**
- Created: `app/src/main/java/com/example/archeryapp/data/preferences/UserPreferences.kt`
- Created: `app/src/test/java/com/example/archeryapp/data/preferences/UserPreferencesTest.kt`

**Validation Test Results:**
- `./gradlew assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL in 11s** (45 tasks, 13 executed, 32 up-to-date)
- `UserPreferencesTest`: **5 tests, 0 failures, 0 errors, 0 skipped** (total runtime ~2 ms)
- Regression check: SessionMappersTest (7), ScoringStrategyTest (12), ExampleUnitTest (1) all still pass
- **Total across suite: 25 tests, 0 failures**

**Plan checklist:**
- [x] Build succeeds
- [x] Set then get round-trips (covered by `set BF then get returns BF` + `round-trip preserves every target type`)

Minor step — no device verification required. Ready for Step 2.4.5 (TargetTypePickerDialog) on user confirmation.

### 2026-04-11 - Phase 2.4 Step 2.4.5: TargetTypePickerDialog [COMPLETE]
**What was implemented:**
- `ui/components/TargetTypePickerDialog.kt` (NEW) — Material3 `AlertDialog` containing a column of selectable rows, one per `TargetType.entries`. Each row uses `Modifier.selectable(role = Role.RadioButton)` around a `RadioButton` and a `Text(type.displayName)`, which is the canonical pattern for grouped radio selection in Compose (accessible to TalkBack, full-row tap target).
- API: `@Composable fun TargetTypePickerDialog(current: TargetType, onSelect: (TargetType) -> Unit, onDismiss: () -> Unit)` — matches the spec signature exactly.
- Internal state: `var selected by remember(current) { mutableStateOf(current) }`. The `remember(current)` keys the state to the incoming `current` value so if the dialog is reused with a different starting type, it resets correctly.
- `Confirm` button invokes `onSelect(selected)` with the local radio selection; `Cancel` button invokes `onDismiss` unchanged. Tapping outside the dialog also routes to `onDismiss` via `AlertDialog.onDismissRequest`.
- `@Preview` function provided for Android Studio tooling, calling the dialog with `current = TargetType.MC` and no-op lambdas.

**Design decisions:**
- **`remember(current)` key instead of bare `remember`** — allows the composable to be reused/recomposed with a new starting value without staleness, which is important because the callers (2.4.6 onward) will want to pass `UserPreferences.getLastTargetType()` each time the dialog is shown.
- **`onClick = null` on the `RadioButton`, full row `selectable`** — avoids double tap handlers and ensures the whole row is the accessibility target. This is the pattern in the official Material3 samples for radio-group dialogs.
- **`TargetType.entries` (not `.values()`)** — uses the modern Kotlin 1.9+ API that returns a reusable `EnumEntries` collection, consistent with usage elsewhere in this project (`ScoringStrategyTest`, `UserPreferencesTest`, `SessionMappersTest`).
- **No viewmodel dependency** — the dialog is a pure stateless presentation component that owns only its transient radio selection. All persistence (reading last-used, writing new choice) happens in the caller (2.4.6). This keeps the component reusable anywhere a target-type pick is needed.
- **Title "Select target type"** (sentence case) — matches Material3 text style conventions; Android's guidelines recommend sentence case for dialog titles.

**Files changed/created:**
- Created: `app/src/main/java/com/example/archeryapp/ui/components/TargetTypePickerDialog.kt`

**Validation Test Results:**
- `./gradlew assembleDebug` → **BUILD SUCCESSFUL in 7s** (38 tasks, 7 executed, 31 up-to-date)
- No new unit tests (plan didn't require any — presentation-only component)
- Regression check: full unit test suite still runs green (25 tests, 0 failures, unchanged from 2.4.4)

**Plan checklist:**
- [x] Build succeeds
- [x] Preview function exists (rendering to be verified in Android Studio Preview pane — or implicitly in 2.4.6 when the dialog gets wired into `SessionSelectScreen`)

Minor step — no device verification required. Ready for Step 2.4.6 (New Session Creation Flow — Major) on user confirmation.

### 2026-04-11 - Phase 2.4 Step 2.4.6: New Session Creation Flow [CODE-COMPLETE, AWAITING DEVICE VERIFICATION]
**What was implemented:**
- `SessionSelectViewModel.kt` — now owns a private `UserPreferences(application)` instance. Exposes `fun getLastTargetType(): TargetType` for the screen to read the current default. `createNewSession` gained a required `targetType: TargetType` parameter (now the first, non-defaulted parameter). On successful session creation, the ViewModel immediately persists the chosen type as the new last-used before invoking `onCreated(sessionId)`. On failure, the last-used preference is not touched.
- `SessionSelectScreen.kt` — introduced `var pickerInitialType by remember { mutableStateOf<TargetType?>(null) }` as the "picker visible" state. The FAB onClick no longer creates a session directly: it reads `viewModel.getLastTargetType()` and assigns it to `pickerInitialType`, which both shows the dialog and captures the initial radio selection in a single atomic step. The FAB is also now guarded against retapping while `uiState.isCreating` is true.
- The `TargetTypePickerDialog` is rendered as a sibling of the `Scaffold` (inside the top-level function body), inside a `pickerInitialType?.let { initial -> ... }` block so it only composes when non-null. `onSelect` sets `pickerInitialType = null` to close the dialog, then calls `viewModel.createNewSession(targetType = ...)`. `onDismiss` (Cancel button or tap-outside) simply nulls the state — no session is created.

**End-to-end flow on "+" tap:**
1. User taps FAB (SessionSelectScreen)
2. FAB reads `getLastTargetType()` from ViewModel → returns a `TargetType` (defaults to `MINI_MC` if never set)
3. `pickerInitialType = <that type>` → dialog enters composition with that type pre-selected in the radio group
4. User taps a different radio → picker's internal `selected` state updates
5. User taps "Confirm" → `onSelect(selected)` fires → `pickerInitialType = null` (dialog leaves composition) → `createNewSession(targetType = selected)` runs on the ViewModel
6. ViewModel builds `Session(..., targetType = selected)`, inserts via `SessionRepository` (which runs the domain→entity mapping from 2.4.3 and persists `targetType.code` into the `targetType` column from 2.4.2)
7. `userPreferences.setLastTargetType(selected)` writes the new default
8. `onCreated(sessionId)` fires → navigation proceeds to the score-input/results flow

**Design decisions:**
- **`pickerInitialType: TargetType?` nullable, both "show" and "initial" in one state.** The classic alternative would be two separate pieces of state (`var showPicker: Boolean` and `var pickerCurrent: TargetType`), but that introduces a subtle bug: if the user taps the FAB twice in quick succession, the `pickerCurrent` state could be read before `showPicker` flips, leading to a picker showing a stale "current". A nullable single-field state makes the show/hide and initial-value assignment atomic — one write does both.
- **`getLastTargetType()` called at FAB-click time, not inside the dialog composable.** This was the explicit reason I captured it into `pickerInitialType` rather than calling it inline as `TargetTypePickerDialog(current = viewModel.getLastTargetType(), ...)`: the latter would read SharedPreferences on every recomposition of SessionSelectScreen while the dialog is visible. SharedPreferences is cheap (cached in memory after first hit), so it's not a perf issue, but reading state on recomposition is a code smell. The captured snapshot is clearer.
- **`targetType` first parameter of `createNewSession`, non-defaulted.** Callers are now forced to pick a target type — there's no silent fallback to `MINI_MC` at the ViewModel boundary. The default-value path exists only one layer deeper (in `SaveScoreUseCase` from 2.4.3), where it's a legitimate backwards-compat cushion. Putting `targetType` first (before the optional `distance`/`bowType`/`location`) makes the signature read naturally and enforces the ordering rule "required args before optional".
- **Write last-used AFTER successful DB insert, not before.** If the DB insert throws, the user's previous "last-used" preference shouldn't be silently clobbered. Writing the preference inside the `try` block after `createSession` succeeds guarantees the two stay consistent: the preference always reflects the type of the most recently created session.
- **FAB `isCreating` guard.** While a session is being created, a second FAB tap is a no-op. Prevents accidentally opening a second picker and (potentially) double-creating sessions if the user is tap-happy. Previously the FAB had no guard at all — this is a small pre-existing-bug cleanup while we're in this file.
- **Dialog rendered outside the Scaffold block** (as a sibling of it, inside the top-level composable). This is the standard Compose idiom for dialogs — they shouldn't be nested inside Scaffold's content slot because the Scaffold manages layout constraints for the primary content only. Keeping the dialog separate means its `AlertDialog` takes Compose's full Window-level positioning.

**Files changed/created:**
- Modified: `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectViewModel.kt`
- Modified: `app/src/main/java/com/example/archeryapp/ui/screens/session/SessionSelectScreen.kt`
- No new files. No new tests (UI flow; will be manually verified on device per plan's "Major" classification).

**Files NOT changed:**
- `ResultsViewModel.kt` / `ResultsScreen.kt` — these flow through `SaveScoreUseCase` which still defaults to `MINI_MC` when called without a target type. In the current post-2.4.6 flow, `SessionSelectViewModel.createNewSession` creates the session with the correct target type via `SessionRepository.createSession` directly, so `SaveScoreUseCase` only ever sees existing-session paths (where targetType is already stored on the row and never overridden).
- `HomeScreen.kt` — badge display and "End Session" button are Step 2.4.7 work.
- Any other screens that list sessions (History, Calendar, SessionSelect list view) — target-type display is Step 2.4.10.

**Validation Test Results:**
- `./gradlew assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL in 15s** (45 tasks, 10 executed, 35 up-to-date)
- Full unit test suite still passes: SessionMappersTest (7), ScoringStrategyTest (12), UserPreferencesTest (5), ExampleUnitTest (1) — **25 tests, 0 failures**

**Plan checklist:**
- [x] Build succeeds
- [ ] **Manual (device):** tap "+ New Session" → picker appears with last-used default → select BF → new session created with BF type

**HARD REQUIREMENT — Device testing required before 2.4.7:**
Major step per DEVELOPMENT_PLAN.md — user must verify on a real device:
1. Tap "+ New Session" FAB on SessionSelectScreen → picker dialog should appear
2. Default radio should be `Mini Multicolor` (no prior preference written) or whatever was last-used if you've gone through this flow before
3. Select "Standard Blue Face" (BF) → tap Confirm → session should be created and you should navigate into it
4. Go back to SessionSelect, tap "+" again → picker should now default to "Standard Blue Face" (proves `setLastTargetType` round-trips via SharedPreferences)
5. Tap Cancel on the picker → dialog dismisses with no session created (verify session count didn't change)
6. Tap outside the dialog → same as Cancel
7. Optional: use Database Inspector to confirm the new sessions have `targetType = 'BF'` (or whatever you picked) in the `sessions` table

If any of 1-6 fails, do not authorize 2.4.7.

---

### 2026-04-11 - Phase 2.4 Step 2.4.7: HomeScreen Badge + End Session + Create Session + Auto-Detect Gating + Active Session Bugfix [CODE-COMPLETE, AWAITING DEVICE VERIFICATION]

**Scope:** Original plan items (badge, End Session button, Take Picture gating) plus two user-requested additions:
1. "Create New Session" button on HomeScreen (duplicate of SessionSelectScreen's FAB functionality, for convenience)
2. Reactive Flow-based active-session observation (bugfix — previously, deleting the active session elsewhere left HomeScreen showing stale state)

The DEVELOPMENT_PLAN.md entry for 2.4.7 was updated to reflect the expanded scope before implementation, per CLAUDE.md rule.

**Files modified:**
- `app/src/main/java/com/example/archeryapp/ui/screens/home/HomeViewModel.kt` — replaced `loadTodaysSession()` with reactive `observeSessions()`, added `createSession`, `getLastTargetType`, `endActiveSession`. Introduced `_explicitlyCleared` suppression flag so End Session doesn't immediately auto-re-promote another today's session.
- `app/src/main/java/com/example/archeryapp/ui/screens/home/HomeScreen.kt` — rewrote `ActiveSessionCard` to show target-type badge + End Session text button; added a "Create New Session" OutlinedButton that opens `TargetTypePickerDialog` (same dialog as SessionSelect); disabled "Take Picture" button when `activeSession.targetType != MC` with helper text; disabled "Input Score" when there is no active session.
- `app/src/main/java/com/example/archeryapp/ui/navigation/AppNavigation.kt` — removed the now-obsolete `homeViewModel.refresh()` call (reactive observation replaces manual refresh).
- `memory-claude/DEVELOPMENT_PLAN.md` — expanded 2.4.7 scope documented with new bullets and new test checklist items.

**Key design decisions:**
- **`combine(getAllSessions(), _activeSessionId, _explicitlyCleared)`**: three-way reactive combine in `observeSessions()`. Any DB change, explicit selection, or End Session triggers `recomputeUiState`. This is what fixes the stale-after-delete bug — the flow self-heals.
- **Self-heal fallback**: if `selectedId` no longer resolves to any row (was deleted), we fall back to the most recent remaining today's session instead of going null. This matches the user's plan-text wording: "delete session 2 → session 1 becomes the active card".
- **`_explicitlyCleared` flag**: without this, End Session would auto-re-promote another today's session. The flag suppresses auto-pick until the user explicitly creates or selects a new session.
- **`refresh()` removed**: reactive observation makes it redundant. Removed both the method and its one caller in AppNavigation (ResultsScreen `onNewScan`).
- **Badge on active card**: small `Surface` with `RoundedCornerShape(8.dp)` in `secondaryContainer` color. Placed inline with the "Active Session" label row. Uses `TargetType.displayName`.
- **Take Picture gating**: `takePictureEnabled = hasActiveSession && targetType == MC`. Button `enabled` flag controls Material3 visual disabled state. Helper text appears below the Create Session button whenever an active session exists but its type isn't MC.

**Deviations from plan:**
- Plan said "gate Take Picture to MC only". Kept as plan written — this means MINI_MC, TRIPLE, and BF are all disabled for auto-detect. (Detection infrastructure currently only handles the standard multicolor target.)
- Plan said `endActiveSession()` should exist. Added it as a thin wrapper over `clearActiveSession()` — both set the explicitly-cleared flag.
- Input Score is also disabled when there is no active session (small extra guard, not in plan — but prevents a no-op navigation that would land on an empty state).

**Validation Test Results:**
- `./gradlew assembleDebug` → **BUILD SUCCESSFUL in 17s** (38 tasks, 10 executed, 28 up-to-date)
- `./gradlew testDebugUnitTest` → **BUILD SUCCESSFUL in 5s** (27 tasks, 5 executed, 22 up-to-date). Full suite still green (SessionMappersTest 7, ScoringStrategyTest 12, UserPreferencesTest 5, ExampleUnitTest 1 = 25 tests).

**Plan checklist:**
- [x] Build succeeds
- [ ] **Manual (device):** Active session card shows badge for all four target types
- [ ] **Manual (device):** Tapping "End Session" hides the active card → shows "No Active Session"
- [ ] **Manual (device):** "Take Picture" is disabled for BF, MINI_MC, TRIPLE; enabled for MC
- [ ] **Manual (device):** "Input Score" is enabled for all types (when active session present)
- [ ] **Manual (device):** Tapping "Create New Session" opens picker → confirming creates session that appears as active
- [ ] **Manual (device):** Create session, delete from SessionSelect → HomeScreen auto-clears (no stale state)
- [ ] **Manual (device):** Create two sessions, delete the active one → the other today's session becomes active

**HARD REQUIREMENT — Device testing required before 2.4.8:**
Major step per DEVELOPMENT_PLAN.md — user must verify on a real device:
1. **Badge display:** Create sessions with each of the 4 target types (BF, MC, MINI_MC, TRIPLE). For each, confirm the active card shows the correct badge text (Standard Blue Face / Multicolor / Mini Multicolor / Triple Spot).
2. **End Session button:** Active card shows an "End Session" text button. Tapping it immediately hides the active card (becomes "No Active Session"). The session row in the database is NOT deleted — confirm via Database Inspector or by navigating to SessionSelect.
3. **Take Picture gating:** With a BF session active, Take Picture button is visually disabled. Helper text appears above the button row: "Auto-detect only supports standard multicolor targets. Use 'Input Score' instead." Switch to an MC session (via SessionSelect) and confirm Take Picture becomes enabled.
4. **Create New Session button:** Tap the new "Create New Session" button on HomeScreen. Picker dialog appears (same dialog used in SessionSelect). Select a type, confirm → new session appears as active on HomeScreen with the correct badge.
5. **Last-used target type persistence:** Create session with BF via HomeScreen button, then reopen the HomeScreen dialog → confirm default is "Standard Blue Face".
6. **Stale-session bugfix — the original user-reported bug:**
   a. Start app fresh, no active session.
   b. Create a session from HomeScreen (e.g., MC). HomeScreen shows it as active.
   c. Navigate to SessionSelect, swipe to delete that session.
   d. Return to HomeScreen → should show "No Active Session" (NOT the deleted session). This is the bug that was fixed.
7. **Self-heal fallback:**
   a. Create two sessions today via HomeScreen (A first, then B). HomeScreen shows B as active.
   b. Navigate to SessionSelect, delete session B.
   c. Return to HomeScreen → should now show session A as the active card.
8. **Settings Data Wipe still works:** Settings → Clear All Data → HomeScreen should show "No Active Session" after wipe.

If any of 1-8 fails, do not authorize 2.4.8.
