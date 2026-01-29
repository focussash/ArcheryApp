# Architecture & Progress Documentation

This document tracks implementation progress and architectural decisions.
Updated after each completed step/subproject.

---

## Current Status

**Phase:** 2 - Progress Tracking + Bug Fixes (COMPLETE)
**Current Step:** Phase 2 Bug Fixes fully implemented
**Last Updated:** 2026-01-28

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

## Existing Architecture (Pre-Implementation)

### Project Structure
```
app/src/main/java/com/example/archeryapp/
├── MainActivity.kt
├── ArcheryApplication.kt
├── data/model/
│   ├── ArrowDetection.kt
│   ├── ArrowScore.kt
│   ├── ScoringResult.kt
│   └── TargetDetection.kt
├── detection/opencv/
│   ├── OpenCvTargetDetector.kt
│   └── OpenCvArrowDetector.kt
├── domain/detection/
│   ├── ArrowDetector.kt
│   └── TargetDetector.kt
├── domain/scoring/
│   ├── ScoreCalculator.kt
│   └── UsaArcheryTarget.kt
├── ui/navigation/
│   └── AppNavigation.kt
├── ui/screens/
│   ├── home/
│   ├── camera/
│   ├── review/
│   ├── arrowedit/
│   └── results/
└── ui/theme/
```

### Current Features
- Camera capture with CameraX
- Gallery image upload
- Target detection (OpenCV Hough Circles)
- Arrow detection (OpenCV Hough Lines)
- Manual arrow editing
- Score calculation (USA Archery)
- Results display

### Known Issues
- Auto-detection accuracy problems (per commit history)
- No data persistence (scores not saved)
- No session/history tracking

---

## Architectural Decisions

### Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-01-26 | Use Room for local database | Standard Android persistence, good Kotlin support |
| 2026-01-26 | Repository pattern | Abstracts data sources, enables testing |
| 2026-01-26 | Nordic BLE library for Bluetooth | Well-maintained, handles Android BLE complexity |
| 2026-01-26 | ML Kit for pose detection | On-device, no network required, Google-supported |
| 2026-01-26 | Kizitonwose calendar library | Compose-native, flexible, well-documented |

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

**Dependency:** kizitonwose calendar 2.5.0

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
