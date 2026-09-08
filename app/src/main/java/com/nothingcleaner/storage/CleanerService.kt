package com.nothingcleaner.storage

import android.app.RecoverableSecurityException
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.nothingcleaner.scanner.ScannerItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CleanerService(private val context: Context) {
    suspend fun deleteItems(items: List<ScannerItem>): DeleteResult = withContext(Dispatchers.IO) {
        var deletedCount = 0
        var failedCount = 0
        var freedBytes = 0L

        for (item in items) {
            try {
                val deleted = context.contentResolver.delete(item.uri, null, null)
                if (deleted > 0) {
                    deletedCount++
                    freedBytes += item.sizeBytes
                } else {
                    failedCount++
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverable = e as? RecoverableSecurityException
                    if (recoverable != null) {
                        // In a real app we might request permission for each, but we'll count as failed for bulk
                        failedCount++
                    } else {
                        failedCount++
                    }
                } else {
                    failedCount++
                }
            } catch (e: Exception) {
                failedCount++
            }
        }
        DeleteResult(deletedCount, failedCount, freedBytes)
    }
}

data class DeleteResult(
    val deletedCount: Int,
    val failedCount: Int,
    val freedBytes: Long
)
