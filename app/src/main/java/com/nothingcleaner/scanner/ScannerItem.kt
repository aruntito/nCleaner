package com.nothingcleaner.scanner

import android.net.Uri

data class ScannerItem(
    val id: String,
    val name: String,
    val uri: Uri,
    val sizeBytes: Long,
    val category: FileCategory,
    val path: String
)
