package com.nothingcleaner.scanner

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.nothingcleaner.core.model.PreviewType
import com.nothingcleaner.core.model.StorageCategory
import com.nothingcleaner.core.model.StorageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class DownloadsScanner(private val context: Context) {
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
        
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        
        val selection = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.DATA + " LIKE '%/Download/%'"
        } else null

        context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

            while (cursor.moveToNext() && coroutineContext.isActive) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(dataColumn) ?: ""
                val dateAdded = cursor.getLong(dateAddedColumn)
                val mimeType = cursor.getString(mimeTypeColumn) ?: "application/octet-stream"
                val itemUri = Uri.withAppendedPath(uri, id.toString())

                val categories = mutableSetOf(StorageCategory.DOWNLOADS)
                if (size > 100 * 1024 * 1024) categories.add(StorageCategory.LARGE_FILES)
                
                val previewType = when {
                    mimeType.startsWith("image/") -> PreviewType.IMAGE
                    mimeType.startsWith("video/") -> PreviewType.VIDEO
                    else -> PreviewType.DOCUMENT
                }
                
                items.add(StorageItem(id, itemUri, name, mimeType, size, dateAdded, categories, path, previewType))
            }
        }
        items
    }
}
