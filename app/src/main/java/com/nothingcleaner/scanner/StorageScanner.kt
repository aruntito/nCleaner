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

    private fun extractItem(cursor: android.database.Cursor, collection: Uri): ScannerItem {
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

        // Category is assigned later to avoid double-counting
        return ScannerItem(id.toString(), name, uri, size, dateAdded, FileCategory.LARGE_FILES, data, mimeType)
    }

    suspend fun scanAllMedia(): List<ScannerItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<ScannerItem>()
        
        // Scan Videos
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
            projection, null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) items.add(extractItem(cursor, MediaStore.Video.Media.EXTERNAL_CONTENT_URI))
        }

        // Scan Images
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
            projection, null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) items.add(extractItem(cursor, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }

        // Scan Downloads
        val downloadsUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val dlSelection = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.DATA + " LIKE '%/Download/%'"
        } else null

        context.contentResolver.query(
            downloadsUri, 
            projection, dlSelection, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) items.add(extractItem(cursor, downloadsUri))
        }
        
        items
    }
}
