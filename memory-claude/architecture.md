# Architecture

Static architectural reference for the Archery App.

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
