package com.nothingcleaner.scanner

import android.net.Uri

data class ScannerItem(
    val id: String,
    val name: String,
    val uri: Uri,
    val sizeBytes: Long,
    val dateAdded: Long,
    val category: FileCategory,
    val path: String,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScannerItem) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
