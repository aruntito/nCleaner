# nCleaner# Nothing Cleaner

A lightweight, fast, privacy-first Android storage cleaner inspired by the minimalist visual language of Nothing phones.

## Core Product Principles
- 100% offline
- No analytics, no ads, no cloud sync
- Clean, premium, technical, slightly experimental UI
- Safe deletion via Android-supported APIs

## Android Storage Limitations
- Android Scoped Storage (API 29+) restricts apps from freely accessing and deleting files outside their own directories.
- This app uses `MediaStore` to query accessible files (like Downloads and Large Media files).
- Deletion is requested through `ContentResolver.delete`. On Android 10+, this may prompt the user for individual confirmation via `RecoverableSecurityException`, which the app is structured to handle gracefully.
- The app requests `MANAGE_EXTERNAL_STORAGE` optionally to allow comprehensive duplicate finding and cleaning, but gracefully degrades to MediaStore if not granted.

## How Scanning Works
The scanner uses Kotlin Coroutines on the `Dispatchers.IO` thread to prevent blocking the main UI. It scans:
1. **Downloads:** Queries `MediaStore.Downloads` (API 29+) or filters `MediaStore.Files`.
2. **Large Files:** Queries `MediaStore.Files` with a size threshold.

## Project Architecture
- `ui/`: Jetpack Compose screens (`DashboardScreen`, `ScannerScreen`, `ResultsScreen`) and Navigation.
- `ui/theme/`: Nothing-inspired design system (`Theme`, `Color`, `Type`).
- `scanner/`: Coroutine-based `StorageScanner` and models.
- `storage/`: `CleanerService` for executing file deletion.
- `util/`: `StorageUtil` using `StatFs` to fetch total, used, and free space.

## Build Instructions
Run the following from the root directory:
```bash
./gradlew assembleDebug
```
*Note: A Java Runtime (JDK) is required to build the project.*
