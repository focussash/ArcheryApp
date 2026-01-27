# Archery App - Feature Design Document

## Overview

A comprehensive Android archery training and scoring application that combines image-based score tracking, IoT device integration for real-time form monitoring, and AI-powered form analysis.

---

## Feature 1: Arrow Detection & Score Logging

### Description
Automatically detect arrows on an archery target from a photo (captured or uploaded) and calculate scores.

### Current State
- Basic implementation exists with OpenCV Hough Circle/Line detection
- Camera capture and gallery upload functional
- Manual arrow editing screen available
- Known issues with auto-detection accuracy

### Requirements

#### Image Input
- [ ] Camera capture with real-time preview
- [ ] Gallery image upload
- [ ] Support for common image formats (JPEG, PNG)
- [ ] Auto-rotation handling based on EXIF data

#### Target Detection
- [ ] Detect circular archery targets in images
- [ ] Primary target: USA Archery multicolor
- [ ] Handle partial target visibility
- [ ] Work in varying lighting conditions

#### Arrow Detection
- [ ] Detect arrow positions on the target
- [ ] Support 1-12 arrows per end (configurable)
- [ ] Provide confidence scores for each detection
- [ ] Handle overlapping/touching arrows

#### Score Calculation
- [ ] Calculate individual arrow scores based on ring position
- [ ] Track X-count (inner 10)
- [ ] Support different scoring systems:
  - 10-ring (X, 10, 9, 8... 1, M)
  - 5-ring (X, 5, 4, 3, 2, 1, M)
- [ ] Total score per end

#### Manual Adjustment
- [ ] Visual arrow position editing
- [ ] Tap to add arrows
- [ ] Drag to reposition arrows
- [ ] Long-press to delete arrows
- [ ] Real-time score recalculation

#### Score Logging
- [ ] Confirm and save scores to local database
- [ ] Associate scores with date/time
- [ ] Associate scores with session/end number
- [ ] Optional notes per end

---

## Feature 2: Progress Tracking & History

### Description
Track archery performance across multiple ends within a session, and across multiple sessions/days.

### Requirements

#### Session Management
- [ ] Create new shooting session
- [ ] Session metadata:
  - Date and time
  - Location (optional)
  - Distance (e.g., 18m, 70m)
  - Target face type
  - Bow type (recurve, compound, barebow, etc.)
  - Weather conditions (optional)
- [ ] Multiple ends per session
- [ ] Session summary with totals and averages

#### End Tracking
- [ ] Sequential end numbering within session
- [ ] Configurable arrows per end (3, 6, or custom)
- [ ] Running total display
- [ ] End-by-end breakdown

#### Calendar View
- [ ] Monthly calendar showing shooting days
- [ ] Visual indicators for:
  - Days with sessions
  - Performance trends (color coding)
- [ ] Quick navigation to specific dates
- [ ] Tap to view day's sessions

#### Statistics & Analytics
- [ ] Overall statistics:
  - Total arrows shot
  - Average score per arrow
  - Average score per end
  - Best session/end
  - X-count percentage
- [ ] Trend graphs over time:
  - Daily averages
  - Weekly averages
  - Monthly progress
- [ ] Filter by:
  - Date range
  - Distance
  - Bow type
  - Target type

#### Data Persistence
- [ ] Local SQLite/Room database
- [ ] Export data (CSV, JSON)
- [ ] Import data

---

## Feature 3: M5Stick Real-Time Integration

### Description
Connect to M5StickC Plus (or similar) devices worn on the archer's wrist/arm to track drawing angles and form metrics in real-time.

### Requirements

#### Device Connection
- [ ] Bluetooth Low Energy (BLE) support
- [ ] WiFi connection support (alternative)
- [ ] Device discovery and pairing
- [ ] Multiple device support (e.g., bow arm + draw arm)
- [ ] Connection status indicators
- [ ] Auto-reconnect on disconnect

#### Data Reception
- [ ] Receive IMU data from M5Stick:
  - Accelerometer readings
  - Gyroscope readings
  - Calculated angles (if processed on device)
- [ ] Configurable data rate (10Hz, 50Hz, 100Hz)
- [ ] Data buffering for smooth display
- [ ] Timestamp synchronization

#### Real-Time Display
- [ ] Live angle visualization:
  - Bow arm angle
  - Draw arm angle
  - Shoulder alignment
- [ ] Visual guides showing ideal ranges
- [ ] Audio/haptic feedback for out-of-range angles
- [ ] Recording indicator

#### Session Recording
- [ ] Record angle data during shooting session
- [ ] Associate angle data with specific shots/ends
- [ ] Synchronize with score data from Feature 1
- [ ] Playback recorded sessions

#### M5Stick Communication Protocol
*Protocol TBD - designed to be generic and adaptable*

- [ ] Generic data packet format supporting:
  - Device identification
  - Timestamps for synchronization
  - IMU data (accelerometer, gyroscope)
  - Calculated angles (pitch, roll, yaw)
  - Device status (battery, etc.)
- [ ] Example format (adaptable based on firmware):
  ```
  {
    "device_id": "string",
    "timestamp": "long",
    "accel": {"x": float, "y": float, "z": float},
    "gyro": {"x": float, "y": float, "z": float},
    "angles": {"pitch": float, "roll": float, "yaw": float},
    "battery": int,
    "custom": {}  // Extensible for device-specific data
  }
  ```
- [ ] Command messages (start, stop, calibrate)
- [ ] Status messages (battery, connection quality)
- [ ] Abstraction layer to support different device firmware versions

#### Calendar Synchronization
- [ ] Link M5Stick sessions to calendar entries
- [ ] View angle data alongside scores for any date
- [ ] Correlate form metrics with scoring performance
- [ ] Identify trends (e.g., better scores with specific arm angles)

---

## Feature 4: Form Analysis via Pose Estimation

### Description
Analyze archer's form from a side-view photo using body pose estimation to evaluate draw technique.

### Requirements

#### Image Input
- [ ] Camera capture (side view)
- [ ] Gallery image upload
- [ ] Guidance overlay for proper camera positioning
- [ ] Support for both left and right-handed archers

#### Pose Detection
- [ ] Detect human body keypoints:
  - Head/face
  - Shoulders (both)
  - Elbows (both)
  - Wrists (both)
  - Hips (both)
  - Spine/back line
- [ ] Use ML Kit Pose Detection or MediaPipe
- [ ] Handle partial visibility
- [ ] Confidence scores per keypoint

#### Form Analysis Metrics
- [ ] Draw arm analysis:
  - Elbow angle at full draw
  - Elbow height relative to arrow line
  - Back tension indicators
- [ ] Bow arm analysis:
  - Arm extension (locked vs bent)
  - Shoulder alignment
  - Grip position
- [ ] Body alignment:
  - Stance (open, square, closed)
  - Head position and anchor point
  - Spine alignment (lean)
  - Hip rotation

#### Feedback & Scoring
- [ ] Visual overlay showing detected pose
- [ ] Color-coded feedback:
  - Green: Good form
  - Yellow: Minor issues
  - Red: Needs attention
- [ ] Specific recommendations per body part
- [ ] Overall form score (0-100)
- [ ] Comparison to "ideal" form template

#### Reference Poses
- [ ] Built-in reference poses for different styles:
  - Olympic recurve
  - Compound
  - Barebow
  - Traditional
- [ ] Save personal "best form" as reference
- [ ] Side-by-side comparison view

#### History & Progress
- [ ] Save form analysis sessions
- [ ] Track form improvements over time
- [ ] Link to calendar/scoring data
- [ ] Before/after comparisons

---

## Technical Architecture

### Data Layer
```
data/
├── local/
│   ├── database/
│   │   ├── AppDatabase.kt (Room)
│   │   ├── dao/
│   │   │   ├── SessionDao.kt
│   │   │   ├── EndDao.kt
│   │   │   ├── ScoreDao.kt
│   │   │   ├── AngleDataDao.kt
│   │   │   └── FormAnalysisDao.kt
│   │   └── entity/
│   │       ├── SessionEntity.kt
│   │       ├── EndEntity.kt
│   │       ├── ArrowScoreEntity.kt
│   │       ├── AngleDataEntity.kt
│   │       └── FormAnalysisEntity.kt
│   └── preferences/
│       └── UserPreferences.kt
├── remote/
│   └── bluetooth/
│       ├── BleManager.kt
│       └── M5StickProtocol.kt
└── repository/
    ├── SessionRepository.kt
    ├── ScoreRepository.kt
    ├── AngleDataRepository.kt
    └── FormAnalysisRepository.kt
```

### Domain Layer
```
domain/
├── model/
│   ├── Session.kt
│   ├── End.kt
│   ├── Arrow.kt
│   ├── AngleReading.kt
│   └── FormAnalysis.kt
├── detection/
│   ├── TargetDetector.kt (interface)
│   ├── ArrowDetector.kt (interface)
│   └── PoseDetector.kt (interface)
├── scoring/
│   ├── ScoreCalculator.kt
│   └── TargetDefinitions.kt
├── analysis/
│   ├── FormAnalyzer.kt
│   └── FormMetrics.kt
└── usecase/
    ├── CreateSessionUseCase.kt
    ├── LogScoreUseCase.kt
    ├── GetStatisticsUseCase.kt
    └── AnalyzeFormUseCase.kt
```

### UI Layer
```
ui/
├── navigation/
│   └── AppNavigation.kt
├── screens/
│   ├── home/
│   ├── camera/
│   ├── review/
│   ├── arrowedit/
│   ├── results/
│   ├── history/
│   │   ├── HistoryScreen.kt
│   │   └── CalendarScreen.kt
│   ├── statistics/
│   │   └── StatisticsScreen.kt
│   ├── realtime/
│   │   ├── DeviceConnectionScreen.kt
│   │   └── LiveAngleScreen.kt
│   ├── form/
│   │   ├── FormCaptureScreen.kt
│   │   └── FormAnalysisScreen.kt
│   └── settings/
│       └── SettingsScreen.kt
├── components/
│   ├── target/
│   ├── charts/
│   ├── calendar/
│   └── pose/
└── theme/
```

### Dependencies to Add
```gradle
// Database
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// Bluetooth
implementation "no.nordicsemi.android:ble:2.7.0"

// ML Kit Pose Detection
implementation "com.google.mlkit:pose-detection:18.0.0-beta4"
implementation "com.google.mlkit:pose-detection-accurate:18.0.0-beta4"

// Charts
implementation "com.patrykandpatrick.vico:compose:1.13.1"
implementation "com.patrykandpatrick.vico:compose-m3:1.13.1"

// Calendar
implementation "com.kizitonwose.calendar:compose:2.5.0"
```

---

## UI/UX Design Notes

### Navigation Structure
```
Bottom Navigation:
├── Home (Score)     - Main scoring workflow
├── History          - Calendar & past sessions
├── Real-Time        - M5Stick connection & live view
├── Form             - Pose analysis
└── Settings         - App configuration
```

### Color Scheme
- Primary: Archery gold (#FFD700)
- Secondary: Target red (#E31837)
- Accent: Target blue (#00A2E8)
- Background: Dark theme friendly

### Key Interactions
- Swipe between ends in history
- Pinch to zoom on targets/poses
- Pull to refresh on statistics
- Long-press for context menus

---

## Implementation Priority

### Phase 1: Core Scoring (Current Focus)
1. Fix auto-detection accuracy issues
2. Implement local database for score persistence
3. Basic session/end management
4. Simple history list view

### Phase 2: Progress Tracking
1. Calendar view implementation
2. Statistics and charts
3. Data export/import
4. Session metadata

### Phase 3: M5Stick Integration
1. BLE scanning and connection
2. Data protocol implementation
3. Real-time angle display
4. Session recording and sync

### Phase 4: Form Analysis
1. ML Kit pose detection integration
2. Form metrics calculation
3. Visual feedback overlay
4. Reference pose comparison

---

## Scope Decisions

- **Target Types**: Focus on USA Archery multicolor target
- **M5Stick Protocol**: Generic/adaptable design (protocol TBD as firmware is developed)
- **Cloud Sync**: Not in scope for initial release (local storage only)

---

## Future Considerations

The following features are out of scope for the initial release but may be considered for future versions:

- **Multi-user Support**: Multiple archer profiles on a single device
- **Competition Mode**: Match play, elimination rounds, and official competition scoring formats
- **Cloud Backup**: Sync data across devices
- **Additional Target Types**: World Archery, NFAA, 3D targets, etc.