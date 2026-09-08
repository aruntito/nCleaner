package com.nothingcleaner.core.storage

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import com.nothingcleaner.core.model.StorageSummary
import java.io.File
import java.util.UUID

class StorageRepository(private val context: Context) {
    fun getStorageSummary(): StorageSummary {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            val uuid = StorageManager.UUID_DEFAULT
            val totalBytes = storageStatsManager.getTotalBytes(uuid)
            val freeBytes = storageStatsManager.getFreeBytes(uuid)
            val usedBytes = totalBytes - freeBytes
            StorageSummary(totalBytes, usedBytes, freeBytes)
        } else {
            val path = Environment.getDataDirectory()
            val totalBytes = path.totalSpace
            val freeBytes = path.usableSpace
            val usedBytes = totalBytes - freeBytes
            StorageSummary(totalBytes, usedBytes, freeBytes)
        }
    }
}
