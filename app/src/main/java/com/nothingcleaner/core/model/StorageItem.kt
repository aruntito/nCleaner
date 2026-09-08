package com.nothingcleaner.core.model

import android.net.Uri

data class StorageItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val dateAdded: Long?,
    val categories: Set<StorageCategory>,
    val path: String?,
    val previewType: PreviewType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StorageItem) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
