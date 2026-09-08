# nCleaner Production Refactor Audit

## What currently works
- Compose Navigation (`CleanerNavigation.kt`) is set up and functional.
- The UI properly uses Material 3 / NothingCleanerTheme.
- Permissions are being requested appropriately for scoped storage (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, etc).
- `StorageScanner` fetches real basic `MediaStore` data correctly (`_ID`, `SIZE`, `MIME_TYPE`).
- `FilePreviewScreen` has a basic integration with ExoPlayer for videos and Coil for images.
- Basic list rendering with thumbnails is implemented using `LazyColumn` and Coil.

## What is incomplete
- **No Duplicate Detection**: The "Duplicates" category is entirely un-implemented.
- **Storage Accounting**: `OverviewScreen` tries to fetch stats with `StorageUtil.getStorageStats()`, but there's no cohesive `StorageRepository` to properly parse this. The sizes shown in categories don't accurately reflect what Android sees.
- **No Error Handling**: Permission denials, empty states, and MediaStore exceptions are just swallowed or ignored.
- **No Scan State**: `_scanPhase` is just a string updated with arbitrary delays. There's no real step-by-step progress tracking that measures actual work.
- **No Immutable State Flow**: ViewModels expose `List<ScannerItem>` and `Set<String>` separately, rather than a single `StorageUiState` class.

## What is fake or simulated
- **Analysis Screen**: `delay(500)` in `CleanerViewModel.startScan()` creates a fake delay to mimic "Checking storage...".
- **Category Grouping**: Categories are hardcoded fallbacks that run synchronously after all media is fetched, rather than discrete focused scanners running concurrently.

## What is unsafe
- **Deletion Logic**: `CleanerService.kt` tries to use raw `contentResolver.delete()`, which will fail on scoped storage for files not owned by the app, and catches `RecoverableSecurityException` but does nothing to prompt the user. It completely bypasses Android's `MediaStore.createDeleteRequest` user-confirmation flow, which means deletion of media created by other apps will silently fail on modern Android.
- **Memory/Threading**: Fetching all media items via `scanAllMedia` in one giant list and holding it in `MutableStateFlow` is inefficient and can bloat memory for users with huge galleries.
- **ExoPlayer lifecycle**: Needs strict verification to ensure players don't leak or play in the background when popping the backstack.

## What is architecturally wrong
- **Monolithic Scanner**: `StorageScanner.kt` tries to do everything (images, videos, downloads) in one method instead of having a `MediaScanner`, `DownloadsScanner`, `LargeFilesScanner`, etc.
- **Missing `core/` architecture**: Repositories are absent. Data fetching and categorization happen inside the ViewModel.
- **Fragmented UI State**: The `CleanerViewModel` has 4 independent StateFlows instead of one unified `StorageUiState`.
- **Button System**: Uses arbitrary Compose `Button` wrappers scattered everywhere rather than a unified `components/` design system.

## What should be preserved
- **UI Styling**: The monochrome Nothing-inspired colors and typography should be kept.
- **Coil/ExoPlayer Dependencies**: The `AppApplication.kt` Coil VideoFrameDecoder and ExoPlayer setups are valid.
- **AndroidManifest setup**: Clean and minimal.
- **GitHub Actions pipeline**: Functional and validated.

## What should be deleted
- `CleanerService.kt` -> Replace with `DeleteRequestManager` utilizing `MediaStore.createDeleteRequest`.
- `ScannerItem.kt` -> Replace with canonical `StorageItem.kt` inside `core/model/`.
- `CleanerViewModel.kt` -> Burn down and replace with `StorageViewModel.kt` owning `StorageUiState`.
- Existing `StorageScanner.kt` -> Replace with modular `scanner/` package.
