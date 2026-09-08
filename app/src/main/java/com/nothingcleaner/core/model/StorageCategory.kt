package com.nothingcleaner.core.model

enum class StorageCategory(val displayName: String, val description: String, val priority: Int) {
    SCREEN_RECORDINGS("SCREEN RECORDINGS", "Screen recordings saved on device", 1),
    LARGE_FILES("LARGE FILES", "Files over 100 MB", 2),
    DOWNLOADS("DOWNLOADS", "Files in Downloads folder", 3),
    SCREENSHOTS("SCREENSHOTS", "Screenshots saved on device", 4),
    DUPLICATES("DUPLICATE FILES", "Identical file content detected", 5),
    OTHER_MEDIA("OTHER MEDIA", "Other videos and images", 6)
}
