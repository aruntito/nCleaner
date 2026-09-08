package com.nothingcleaner.cleaner

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import com.nothingcleaner.core.model.StorageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteRequestManager(private val context: Context) {
    
    suspend fun createDeleteRequest(items: List<StorageItem>): IntentSender? = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = items.map { it.uri }
            try {
                MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
            } catch (e: Exception) {
                null
            }
        } else {
            // For API 29 and below, attempt to delete and catch RecoverableSecurityException
            // A more complex implementation is needed to handle batch deletions for API 29.
            // Returning null signals to fallback to direct deletion.
            null
        }
    }

    suspend fun executeDirectDeletion(items: List<StorageItem>): DeletionResult = withContext(Dispatchers.IO) {
        var deletedCount = 0
        var failedCount = 0
        var recoveredBytes = 0L

        for (item in items) {
            try {
                val deleted = context.contentResolver.delete(item.uri, null, null)
                if (deleted > 0) {
                    deletedCount++
                    recoveredBytes += item.sizeBytes
                } else {
                    failedCount++
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverable = e as? RecoverableSecurityException
                    if (recoverable != null) {
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
        DeletionResult(deletedCount, failedCount, recoveredBytes)
    }
}
