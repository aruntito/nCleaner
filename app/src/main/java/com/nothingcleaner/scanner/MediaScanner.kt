package com.nothingcleaner.scanner

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.nothingcleaner.core.model.PreviewType
import com.nothingcleaner.core.model.StorageCategory
import com.nothingcleaner.core.model.StorageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class MediaScanner(private val context: Context) {
    suspend fun scan(): List<StorageItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<StorageItem>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.MIME_TYPE
        )
        
        // Videos
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext() && coroutineContext.isActive) {
                items.add(extractItem(cursor, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true))
            }
        }

        // Images
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext() && coroutineContext.isActive) {
                items.add(extractItem(cursor, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false))
            }
        }

        items
    }

    private fun extractItem(cursor: android.database.Cursor, collection: Uri, isVideo: Boolean): StorageItem {
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
        val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

        val id = cursor.getLong(idColumn)
        val name = cursor.getString(nameColumn) ?: "Unknown"
        val size = cursor.getLong(sizeColumn)
        val path = cursor.getString(dataColumn) ?: ""
        val dateAdded = cursor.getLong(dateAddedColumn)
        val mimeType = cursor.getString(mimeTypeColumn) ?: "application/octet-stream"
        val uri = Uri.withAppendedPath(collection, id.toString())

        val categories = mutableSetOf<StorageCategory>()
        val lowerPath = path.lowercase()
        
        if (isVideo) {
            if (lowerPath.contains("screenrecord") || name.lowercase().contains("screen")) {
                categories.add(StorageCategory.SCREEN_RECORDINGS)
            } else {
                categories.add(StorageCategory.OTHER_MEDIA)
            }
        } else {
            if (lowerPath.contains("screenshot") || name.lowercase().contains("screenshot")) {
                categories.add(StorageCategory.SCREENSHOTS)
            } else {
                categories.add(StorageCategory.OTHER_MEDIA)
            }
        }
        
        if (size > 100 * 1024 * 1024) {
            categories.add(StorageCategory.LARGE_FILES)
        }

        val previewType = if (isVideo) PreviewType.VIDEO else PreviewType.IMAGE

        return StorageItem(id, uri, name, mimeType, size, dateAdded, categories, path, previewType)
    }
}
