package com.nothingcleaner.scanner

enum class FileCategory(val displayName: String) {
    LARGE_FILES("LARGE FILES"),
    DOWNLOADS("DOWNLOADS"),
    SCREENSHOTS("SCREENSHOTS"),
    SCREEN_RECORDINGS("SCREEN RECORDINGS"),
    VIDEOS("VIDEOS"),
    IMAGES("IMAGES"),
    DUPLICATE_CANDIDATES("DUPLICATES")
}
