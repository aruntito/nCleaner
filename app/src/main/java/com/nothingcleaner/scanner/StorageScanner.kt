package com.nothingcleaner.scanner

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageScanner(private val context: Context) {
    
    private val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.MIME_TYPE
    )

    private fun extractItem(cursor: android.database.Cursor, collection: Uri, category: FileCategory): ScannerItem {
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
        val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

        val id = cursor.getLong(idColumn)
        val name = cursor.getString(nameColumn) ?: "Unknown"
        val size = cursor.getLong(sizeColumn)
        val data = cursor.getString(dataColumn) ?: ""
        val dateAdded = cursor.getLong(dateAddedColumn)
        val mimeType = cursor.getString(mimeTypeColumn) ?: "application/octet-stream"
        val uri = Uri.withAppendedPath(collection, id.toString())

        return ScannerItem(id.toString(), name, uri, size, dateAdded, category, data, mimeType)
    }

    suspend fun scanDownloads(): List<ScannerItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<ScannerItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val selection = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.DATA + " LIKE '%/Download/%'"
        } else null

        context.contentResolver.query(collection, projection, selection, null, "${MediaStore.MediaColumns.DATE_ADDED} DESC")?.use { cursor ->
            while (cursor.moveToNext()) {
                items.add(extractItem(cursor, collection, FileCategory.DOWNLOADS))
            }
        }
        items
    }
    
    suspend fun scanLargeFiles(thresholdBytes: Long = 100 * 1024 * 1024): List<ScannerItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<ScannerItem>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.MediaColumns.SIZE} >= ?"
        val selectionArgs = arrayOf(thresholdBytes.toString())

        context.contentResolver.query(collection, projection, selection, selectionArgs, "${MediaStore.MediaColumns.SIZE} DESC")?.use { cursor ->
            while (cursor.moveToNext()) {
                items.add(extractItem(cursor, collection, FileCategory.LARGE_FILES))
            }
        }
        items
    }
}
