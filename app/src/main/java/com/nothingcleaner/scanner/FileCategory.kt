package com.nothingcleaner.scanner

enum class FileCategory(val displayName: String) {
    LARGE_FILES("LARGE FILES"),
    DOWNLOADS("DOWNLOADS"),
    SCREEN_RECORDINGS("SCREEN RECORDINGS"),
    SCREENSHOTS("SCREENSHOTS"),
    DUPLICATE_CANDIDATES("DUPLICATES")
}
