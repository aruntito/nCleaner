package com.nothingcleaner.viewmodel

import com.nothingcleaner.core.model.StorageCategory
import com.nothingcleaner.core.model.StorageItem

sealed interface ScanState {
    object Idle : ScanState
    data class Scanning(val progress: Float, val currentStep: ScanStep) : ScanState
    data class Complete(val analysis: StorageAnalysis) : ScanState
    data class Error(val message: String) : ScanState
}

enum class ScanStep(val description: String) {
    CHECKING_STORAGE("Checking available storage"),
    SCANNING_MEDIA("Scanning media files"),
    SCANNING_DOWNLOADS("Analyzing downloads"),
    FINDING_LARGE_FILES("Detecting large files"),
    FINDING_DUPLICATES("Checking duplicates"),
    ORGANIZING_RESULTS("Organizing results")
}

data class StorageAnalysis(
    val categories: Map<StorageCategory, List<StorageItem>>,
    val duplicateGroups: List<List<StorageItem>> = emptyList(),
    val scannedAt: Long = System.currentTimeMillis()
)
