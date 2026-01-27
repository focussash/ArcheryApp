# Architecture & Progress Documentation

This document tracks implementation progress and architectural decisions.
Updated after each completed step/subproject.

---

## Current Status

**Phase:** 1 - Core Scoring & Persistence
**Current Step:** 1.6.3 Arrow Detection Complete - Ready for Device Testing
**Last Updated:** 2026-01-26

---

## Completed Work

### Session: [Date]
*(No entries yet - add entries as work is completed)*

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

#### 1.6 Detection Improvements
| Step | Status | Notes |
|------|--------|-------|
| 1.6.1 Analysis | Complete | Documented issues with target/arrow detection |
| 1.6.2 Target Detection | Complete | CLAHE normalization, multi-param sweeps, graduated color scoring |
| 1.6.3 Arrow Detection | Complete | Dynamic scaling, multi-Canny thresholds, MIN_GROUP_SIZE=2 |
| 1.6.4 Confidence UI | Pending | |

### Phase 2: Progress Tracking (Not Started)

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
