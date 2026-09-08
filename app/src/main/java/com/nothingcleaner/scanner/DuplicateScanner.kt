package com.nothingcleaner.scanner

import android.content.Context
import com.nothingcleaner.core.model.StorageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

class DuplicateScanner(private val context: Context) {
    suspend fun findDuplicates(items: List<StorageItem>): List<List<StorageItem>> = withContext(Dispatchers.IO) {
        // Step 1: Group by size, ignore unique sizes
        val sizeGroups = items.groupBy { it.sizeBytes }.filter { it.value.size > 1 }
        
        val duplicateGroups = mutableListOf<List<StorageItem>>()
        
        // Step 2 & 3: Hash same-size candidates
        for ((_, candidates) in sizeGroups) {
            if (!coroutineContext.isActive) break
            
            val hashGroups = mutableMapOf<String, MutableList<StorageItem>>()
            for (item in candidates) {
                if (!coroutineContext.isActive) break
                val hash = calculateHash(item) ?: continue
                hashGroups.getOrPut(hash) { mutableListOf() }.add(item)
            }
            
            for ((_, identicalItems) in hashGroups) {
                if (identicalItems.size > 1) {
                    duplicateGroups.add(identicalItems.sortedBy { it.dateAdded ?: 0L }) // Oldest first
                }
            }
        }
        
        duplicateGroups
    }

    private fun calculateHash(item: StorageItem): String? {
        return try {
            context.contentResolver.openInputStream(item.uri)?.use { stream ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            null
        }
    }
}
