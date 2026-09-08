package com.nothingcleaner.util

import android.os.Environment
import android.os.StatFs

object StorageUtil {
    fun getStorageStats(): StorageStats {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        
        val totalBytes = totalBlocks * blockSize
        val availableBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - availableBytes
        
        return StorageStats(totalBytes, usedBytes, availableBytes)
    }
}

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long
)
