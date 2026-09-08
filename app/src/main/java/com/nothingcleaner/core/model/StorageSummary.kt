package com.nothingcleaner.core.model

data class StorageSummary(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long
)
